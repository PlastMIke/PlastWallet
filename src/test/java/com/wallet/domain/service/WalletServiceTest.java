package com.wallet.domain.service;

import com.wallet.application.port.out.WalletPort;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Unit Tests")
class WalletServiceTest {

    @Mock
    private WalletPort walletPort;

    @InjectMocks
    private WalletService walletService;

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
            String currency = "USD";
            Wallet savedWallet = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .currency(currency)
                    .balance(BigDecimal.ZERO)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            given(walletPort.save(any(Wallet.class))).willReturn(savedWallet);

            // When
            Wallet result = walletService.createWallet(userId, currency);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getCurrency()).isEqualTo(currency);
            assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

            then(walletPort).should().save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should set initial balance to zero")
        void shouldSetInitialBalanceToZero() {
            // Given
            Wallet savedWallet = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .currency("EUR")
                    .balance(BigDecimal.ZERO)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            given(walletPort.save(any(Wallet.class))).willReturn(savedWallet);

            // When
            Wallet result = walletService.createWallet(userId, "EUR");

            // Then
            assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getWallet()")
    class GetWalletTests {

        @Test
        @DisplayName("Should return wallet when found")
        void shouldReturnWalletWhenFound() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));

            // When
            Optional<Wallet> result = walletService.getWallet(walletId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(walletId);
            assertThat(result.get().getUserId()).isEqualTo(userId);

            then(walletPort).should().findById(walletId);
        }

        @Test
        @DisplayName("Should return empty when wallet not found")
        void shouldReturnEmptyWhenWalletNotFound() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.empty());

            // When
            Optional<Wallet> result = walletService.getWallet(walletId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getWalletByUserId()")
    class GetWalletByUserIdTests {

        @Test
        @DisplayName("Should return wallet for user when found")
        void shouldReturnWalletForUserWhenFound() {
            // Given
            given(walletPort.findByUserId(userId)).willReturn(Optional.of(testWallet));

            // When
            Optional<Wallet> result = walletService.getWalletByUserId(userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(userId);

            then(walletPort).should().findByUserId(userId);
        }

        @Test
        @DisplayName("Should return empty when user has no wallet")
        void shouldReturnEmptyWhenUserHasNoWallet() {
            // Given
            given(walletPort.findByUserId(userId)).willReturn(Optional.empty());

            // When
            Optional<Wallet> result = walletService.getWalletByUserId(userId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateBalance()")
    class UpdateBalanceTests {

        @Test
        @DisplayName("Should update balance successfully")
        void shouldUpdateBalanceSuccessfully() {
            // Given
            BigDecimal amount = BigDecimal.valueOf(50.00);
            BigDecimal expectedBalance = BigDecimal.valueOf(150.00);

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
            Wallet result = walletService.updateBalance(walletId, amount);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getBalance()).isEqualByComparingTo(expectedBalance);
            assertThat(result.getUpdatedAt()).isAfterOrEqualTo(testWallet.getUpdatedAt());

            then(walletPort).should().save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should throw exception when wallet not found")
        void shouldThrowExceptionWhenWalletNotFound() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletService.updateBalance(walletId, BigDecimal.valueOf(50.00)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Wallet not found");

            then(walletPort).should(never()).save(any(Wallet.class));
        }

        @Test
        @DisplayName("Should handle negative amount (withdrawal)")
        void shouldHandleNegativeAmount() {
            // Given
            BigDecimal amount = BigDecimal.valueOf(-30.00);
            BigDecimal expectedBalance = BigDecimal.valueOf(70.00);

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
            Wallet result = walletService.updateBalance(walletId, amount);

            // Then
            assertThat(result.getBalance()).isEqualByComparingTo(expectedBalance);
        }

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            // Given
            given(walletPort.findById(walletId)).willReturn(Optional.of(testWallet));
            given(walletPort.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            Wallet result = walletService.updateBalance(walletId, BigDecimal.ZERO);

            // Then
            assertThat(result.getBalance()).isEqualByComparingTo(testWallet.getBalance());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large balance amount")
        void shouldHandleVeryLargeBalanceAmount() {
            // Given
            BigDecimal largeAmount = new BigDecimal("999999999999.99");
            Wallet largeBalanceWallet = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .currency("USD")
                    .balance(largeAmount)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            given(walletPort.findById(walletId)).willReturn(Optional.of(largeBalanceWallet));
            given(walletPort.save(any(Wallet.class))).willReturn(largeBalanceWallet);

            // When
            Wallet result = walletService.updateBalance(walletId, largeAmount);

            // Then
            assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1999999999999.98"));
        }

        @Test
        @DisplayName("Should handle null wallet ID gracefully")
        void shouldHandleNullWalletId() {
            // Given
            given(walletPort.findById(null)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> walletService.updateBalance(null, BigDecimal.valueOf(100.00)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle different currencies")
        void shouldHandleDifferentCurrencies() {
            // Given
            String[] currencies = {"USD", "EUR", "GBP", "JPY", "CHF"};

            for (String currency : currencies) {
                Wallet currencyWallet = Wallet.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .currency(currency)
                        .balance(BigDecimal.valueOf(100.00))
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                given(walletPort.save(any(Wallet.class))).willReturn(currencyWallet);

                // When
                Wallet result = walletService.createWallet(userId, currency);

                // Then
                assertThat(result.getCurrency()).isEqualTo(currency);
            }
        }
    }
}
