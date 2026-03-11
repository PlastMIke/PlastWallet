package com.wallet.application.service;

import com.wallet.application.port.out.TransactionPort;
import com.wallet.application.port.out.WalletPort;
import com.wallet.domain.entity.Transaction;
import com.wallet.domain.entity.TransactionStatus;
import com.wallet.domain.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletServiceAdapter Unit Tests")
class WalletServiceAdapterTest {

    @Mock
    private WalletPort walletPort;

    @Mock
    private TransactionPort transactionPort;

    @InjectMocks
    private WalletServiceAdapter walletServiceAdapter;

    private Wallet testWallet;
    private UUID walletId;
    private Long userId;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        userId = 1L;

        testWallet = Wallet.builder()
                .id(walletId)
                .userId(userId)
                .currency("USD")
                .balance(BigDecimal.valueOf(100.00))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("createWallet()")
    class CreateWalletTests {

        @Test
        @DisplayName("Should create wallet successfully")
        void shouldCreateWalletSuccessfully() {
            // Given
            given(walletPort.existsByUserId(userId)).willReturn(false);
            given(walletPort.save(any(Wallet.class))).willReturn(testWallet);

            // When
            var result = walletServiceAdapter.createWallet(userId, "USD");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getCurrency()).isEqualTo("USD");

            then(walletPort).should().existsByUserId(userId);
            then(walletPort).should().save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should throw exception when user already has wallet")
        void shouldThrowExceptionWhenUserAlreadyHasWallet() {
            // Given
            given(walletPort.existsByUserId(userId)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.createWallet(userId, "USD"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Wallet already exists for user");

            then(walletPort).should(never()).save(any(Wallet.class));
        }
    }

    @Nested
    @DisplayName("deposit()")
    class DepositTests {

        @Test
        @DisplayName("Should deposit amount successfully")
        void shouldDepositAmountSuccessfully() {
            // Given
            BigDecimal depositAmount = BigDecimal.valueOf(50.00);

            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));
            given(walletPort.save(any(Wallet.class))).willAnswer(invocation -> {
                Wallet wallet = invocation.getArgument(0);
                return Wallet.builder()
                        .id(wallet.getId())
                        .userId(wallet.getUserId())
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .createdAt(wallet.getCreatedAt())
                        .updatedAt(Instant.now())
                        .build();
            });

            // When
            var result = walletServiceAdapter.deposit(walletId, depositAmount, "Test deposit");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualByComparingTo(depositAmount);
            assertThat(result.getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("Should throw exception for negative deposit amount")
        void shouldThrowExceptionForNegativeDepositAmount() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.deposit(walletId, BigDecimal.valueOf(-50.00), "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Deposit amount must be positive");
        }

        @Test
        @DisplayName("Should throw exception for zero deposit amount")
        void shouldThrowExceptionForZeroDepositAmount() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.deposit(walletId, BigDecimal.ZERO, "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Deposit amount must be positive");
        }

        @Test
        @DisplayName("Should throw exception when wallet not found")
        void shouldThrowExceptionWhenWalletNotFoundForDeposit() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.deposit(walletId, BigDecimal.valueOf(50.00), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Wallet not found");
        }
    }

    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {

        @Test
        @DisplayName("Should withdraw amount successfully")
        void shouldWithdrawAmountSuccessfully() {
            // Given
            BigDecimal withdrawAmount = BigDecimal.valueOf(30.00);

            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));
            given(walletPort.save(any(Wallet.class))).willAnswer(invocation -> {
                Wallet wallet = invocation.getArgument(0);
                return Wallet.builder()
                        .id(wallet.getId())
                        .userId(wallet.getUserId())
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .createdAt(wallet.getCreatedAt())
                        .updatedAt(Instant.now())
                        .build();
            });

            // When
            var result = walletServiceAdapter.withdraw(walletId, withdrawAmount, "Test withdrawal");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualByComparingTo(withdrawAmount);
        }

        @Test
        @DisplayName("Should throw exception for insufficient balance")
        void shouldThrowExceptionForInsufficientBalance() {
            // Given
            BigDecimal withdrawAmount = BigDecimal.valueOf(200.00);
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.withdraw(walletId, withdrawAmount, "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient balance");
        }

        @Test
        @DisplayName("Should throw exception for negative withdrawal amount")
        void shouldThrowExceptionForNegativeWithdrawalAmount() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.withdraw(walletId, BigDecimal.valueOf(-50.00), "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Withdrawal amount must be positive");
        }
    }

    @Nested
    @DisplayName("transfer()")
    class TransferTests {

        @Test
        @DisplayName("Should transfer amount successfully")
        void shouldTransferAmountSuccessfully() {
            // Given
            UUID toWalletId = UUID.randomUUID();
            BigDecimal transferAmount = BigDecimal.valueOf(25.00);

            Wallet toWallet = Wallet.builder()
                    .id(toWalletId)
                    .userId(2L)
                    .currency("USD")
                    .balance(BigDecimal.valueOf(50.00))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            Wallet updatedFromWallet = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .currency("USD")
                    .balance(BigDecimal.valueOf(75.00))
                    .createdAt(testWallet.getCreatedAt())
                    .updatedAt(Instant.now())
                    .build();

            Wallet updatedToWallet = Wallet.builder()
                    .id(toWalletId)
                    .userId(2L)
                    .currency("USD")
                    .balance(BigDecimal.valueOf(75.00))
                    .createdAt(toWallet.getCreatedAt())
                    .updatedAt(Instant.now())
                    .build();

            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));
            given(walletPort.findById(toWalletId)).willReturn(Optional.of(toWallet));
            given(walletPort.save(any(Wallet.class))).willAnswer(invocation -> {
                Wallet wallet = invocation.getArgument(0);
                return Wallet.builder()
                        .id(wallet.getId())
                        .userId(wallet.getUserId())
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .createdAt(wallet.getCreatedAt())
                        .updatedAt(Instant.now())
                        .build();
            });

            // When
            var result = walletServiceAdapter.transfer(walletId, toWalletId, transferAmount, "Test transfer");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualByComparingTo(transferAmount);
        }

        @Test
        @DisplayName("Should throw exception when transferring to same wallet")
        void shouldThrowExceptionWhenTransferringToSameWallet() {
            // Given & When & Then
            assertThatThrownBy(() -> walletServiceAdapter.transfer(walletId, walletId, BigDecimal.valueOf(10.00), "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot transfer to the same wallet");
        }

        @Test
        @DisplayName("Should throw exception when source wallet not found")
        void shouldThrowExceptionWhenSourceWalletNotFound() {
            // Given
            UUID toWalletId = UUID.randomUUID();
            given(walletPort.findById(walletId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.transfer(walletId, toWalletId, BigDecimal.valueOf(10.00), "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Source wallet not found");
        }

        @Test
        @DisplayName("Should throw exception when destination wallet not found")
        void shouldThrowExceptionWhenDestinationWalletNotFound() {
            // Given
            UUID toWalletId = UUID.randomUUID();
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));
            given(walletPort.findById(toWalletId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.transfer(walletId, toWalletId, BigDecimal.valueOf(10.00), "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Destination wallet not found");
        }

        @Test
        @DisplayName("Should throw exception for insufficient balance on transfer")
        void shouldThrowExceptionForInsufficientBalanceOnTransfer() {
            // Given
            UUID toWalletId = UUID.randomUUID();
            Wallet toWallet = Wallet.builder()
                    .id(toWalletId)
                    .userId(2L)
                    .currency("USD")
                    .balance(BigDecimal.valueOf(50.00))
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));
            given(walletPort.findById(toWalletId)).willReturn(Optional.of(toWallet));

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.transfer(walletId, toWalletId, BigDecimal.valueOf(200.00), "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient balance for transfer");
        }
    }

    @Nested
    @DisplayName("getTransactionHistory()")
    class GetTransactionHistoryTests {

        @Test
        @DisplayName("Should return transaction history")
        void shouldReturnTransactionHistory() {
            // Given
            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID())
                    .fromWalletId(null)
                    .toWalletId(walletId)
                    .amount(BigDecimal.valueOf(100.00))
                    .status(TransactionStatus.COMPLETED)
                    .createdAt(Instant.now())
                    .build();

            given(transactionPort.findAllByWalletId(walletId)).willReturn(List.of(transaction));

            // When
            var result = walletServiceAdapter.getTransactionHistory(walletId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        }

        @Test
        @DisplayName("Should return empty list when no transactions")
        void shouldReturnEmptyListWhenNoTransactions() {
            // Given
            given(transactionPort.findAllByWalletId(walletId)).willReturn(List.of());

            // When
            var result = walletServiceAdapter.getTransactionHistory(walletId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getWallet()")
    class GetWalletTests {

        @Test
        @DisplayName("Should return wallet by ID")
        void shouldReturnWalletById() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));

            // When
            var result = walletServiceAdapter.getWallet(walletId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(walletId);
            assertThat(result.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should throw exception when wallet not found")
        void shouldThrowExceptionWhenWalletNotFound() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.getWallet(walletId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Wallet not found");
        }
    }

    @Nested
    @DisplayName("getWalletByUserId()")
    class GetWalletByUserIdTests {

        @Test
        @DisplayName("Should return wallet by user ID")
        void shouldReturnWalletByUserId() {
            // Given
            given(walletPort.findByUserId(userId)).willReturn(Optional.of(testWallet));

            // When
            var result = walletServiceAdapter.getWalletByUserId(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should throw exception when user has no wallet")
        void shouldThrowExceptionWhenUserHasNoWallet() {
            // Given
            given(walletPort.findByUserId(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletServiceAdapter.getWalletByUserId(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Wallet not found for user");
        }
    }
}
