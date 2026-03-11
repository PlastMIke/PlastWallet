package com.wallet.integration;

import com.wallet.application.dto.TransactionDTO;
import com.wallet.application.dto.WalletDTO;
import com.wallet.application.port.in.WalletUseCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Wallet operations using Testcontainers with PostgreSQL.
 * 
 * Requirements:
 * - Docker must be running
 * - PostgreSQL container will be started automatically
 * 
 * Run with: mvnw verify -Pintegration
 */
@Testcontainers
@SpringBootTest
@Transactional
@DisplayName("Wallet Integration Tests (PostgreSQL + Testcontainers)")
class WalletIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("wallet_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @Autowired
    private WalletUseCase walletUseCase;

    private UUID walletId;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        WalletDTO wallet = walletUseCase.createWallet(userId, "USD");
        walletId = wallet.getId();
    }

    @Nested
    @DisplayName("Wallet Creation")
    class WalletCreationTests {

        @Test
        @DisplayName("Should create wallet with zero balance")
        void shouldCreateWalletWithZeroBalance() {
            Long newUserId = 2L;
            String currency = "EUR";

            WalletDTO wallet = walletUseCase.createWallet(newUserId, currency);

            assertThat(wallet).isNotNull();
            assertThat(wallet.getUserId()).isEqualTo(newUserId);
            assertThat(wallet.getCurrency()).isEqualTo(currency);
            assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw exception for duplicate user wallet")
        void shouldThrowExceptionForDuplicateUserWallet() {
            assertThatThrownBy(() -> walletUseCase.createWallet(userId, "USD"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Wallet already exists for user");
        }
    }

    @Nested
    @DisplayName("Wallet Retrieval")
    class WalletRetrievalTests {

        @Test
        @DisplayName("Should retrieve wallet by ID")
        void shouldRetrieveWalletById() {
            WalletDTO wallet = walletUseCase.getWallet(walletId);

            assertThat(wallet).isNotNull();
            assertThat(wallet.getId()).isEqualTo(walletId);
            assertThat(wallet.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should retrieve wallet by user ID")
        void shouldRetrieveWalletByUserId() {
            WalletDTO wallet = walletUseCase.getWalletByUserId(userId);

            assertThat(wallet).isNotNull();
            assertThat(wallet.getUserId()).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("Deposit Operations")
    class DepositTests {

        @Test
        @DisplayName("Should deposit amount successfully")
        void shouldDepositAmountSuccessfully() {
            BigDecimal depositAmount = BigDecimal.valueOf(100.00);

            TransactionDTO transaction = walletUseCase.deposit(walletId, depositAmount, "Initial deposit");

            assertThat(transaction).isNotNull();
            assertThat(transaction.getAmount()).isEqualByComparingTo(depositAmount);
            assertThat(transaction.getStatus()).isEqualTo("COMPLETED");

            WalletDTO updatedWallet = walletUseCase.getWallet(walletId);
            assertThat(updatedWallet.getBalance()).isEqualByComparingTo(depositAmount);
        }

        @Test
        @DisplayName("Should handle multiple deposits")
        void shouldHandleMultipleDeposits() {
            BigDecimal deposit1 = BigDecimal.valueOf(50.00);
            BigDecimal deposit2 = BigDecimal.valueOf(75.00);

            walletUseCase.deposit(walletId, deposit1, "First deposit");
            walletUseCase.deposit(walletId, deposit2, "Second deposit");

            WalletDTO updatedWallet = walletUseCase.getWallet(walletId);
            assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(125.00));
        }
    }

    @Nested
    @DisplayName("Withdrawal Operations")
    class WithdrawalTests {

        @Test
        @DisplayName("Should withdraw amount successfully")
        void shouldWithdrawAmountSuccessfully() {
            walletUseCase.deposit(walletId, BigDecimal.valueOf(100.00), "Deposit");
            BigDecimal withdrawAmount = BigDecimal.valueOf(30.00);

            TransactionDTO transaction = walletUseCase.withdraw(walletId, withdrawAmount, "Withdrawal");

            assertThat(transaction).isNotNull();
            assertThat(transaction.getAmount()).isEqualByComparingTo(withdrawAmount);

            WalletDTO updatedWallet = walletUseCase.getWallet(walletId);
            assertThat(updatedWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(70.00));
        }

        @Test
        @DisplayName("Should throw exception for insufficient balance")
        void shouldThrowExceptionForInsufficientBalance() {
            BigDecimal withdrawAmount = BigDecimal.valueOf(200.00);

            assertThatThrownBy(() -> walletUseCase.withdraw(walletId, withdrawAmount, "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient balance");
        }
    }

    @Nested
    @DisplayName("Transfer Operations")
    class TransferTests {

        @Test
        @DisplayName("Should transfer between wallets successfully")
        void shouldTransferBetweenWalletsSuccessfully() {
            Long toUserId = 2L;
            WalletDTO toWalletDTO = walletUseCase.createWallet(toUserId, "USD");
            UUID toWalletId = toWalletDTO.getId();

            walletUseCase.deposit(walletId, BigDecimal.valueOf(100.00), "Deposit for transfer");
            BigDecimal transferAmount = BigDecimal.valueOf(40.00);

            TransactionDTO transaction = walletUseCase.transfer(walletId, toWalletId, transferAmount, "Transfer");

            assertThat(transaction).isNotNull();
            assertThat(transaction.getAmount()).isEqualByComparingTo(transferAmount);

            WalletDTO fromWallet = walletUseCase.getWallet(walletId);
            WalletDTO toWallet = walletUseCase.getWallet(toWalletId);

            assertThat(fromWallet.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
            assertThat(toWallet.getBalance()).isEqualByComparingTo(transferAmount);
        }

        @Test
        @DisplayName("Should prevent transfer to same wallet")
        void shouldPreventTransferToSameWallet() {
            assertThatThrownBy(() -> walletUseCase.transfer(walletId, walletId, BigDecimal.valueOf(10.00), "Invalid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot transfer to the same wallet");
        }
    }

    @Nested
    @DisplayName("Transaction History")
    class TransactionHistoryTests {

        @Test
        @DisplayName("Should retrieve transaction history")
        void shouldRetrieveTransactionHistory() {
            walletUseCase.deposit(walletId, BigDecimal.valueOf(100.00), "Deposit 1");
            walletUseCase.deposit(walletId, BigDecimal.valueOf(50.00), "Deposit 2");

            List<TransactionDTO> transactions = walletUseCase.getTransactionHistory(walletId);

            assertThat(transactions).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list for wallet with no transactions")
        void shouldReturnEmptyListForNoTransactions() {
            List<TransactionDTO> transactions = walletUseCase.getTransactionHistory(walletId);

            assertThat(transactions).isEmpty();
        }
    }
}
