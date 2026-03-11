package com.wallet.domain.repository;

import com.wallet.domain.entity.Transaction;
import com.wallet.domain.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    // Standard CRUD operations
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    void deleteById(UUID id);

    // Standard queries
    List<Transaction> findByFromWalletId(UUID fromWalletId);
    List<Transaction> findByToWalletId(UUID toWalletId);
    List<Transaction> findByStatus(TransactionStatus status);

    // Custom queries

    /**
     * Find transactions where wallet is either sender or receiver
     */
    List<Transaction> findAllByWalletId(UUID walletId);

    /**
     * Find transactions by wallet and status
     */
    List<Transaction> findAllByWalletIdAndStatus(UUID walletId, TransactionStatus status);

    /**
     * Find transactions in date range
     */
    List<Transaction> findAllByDateRange(Instant startDate, Instant endDate);

    /**
     * Find transactions by wallet and date range
     */
    List<Transaction> findAllByWalletIdAndDateRange(UUID walletId, Instant startDate, Instant endDate);

    /**
     * Find transactions by amount range
     */
    List<Transaction> findAllByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find transactions by wallet and amount range
     */
    List<Transaction> findAllByWalletIdAndAmountRange(UUID walletId, BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find transactions by status and date range
     */
    List<Transaction> findAllByStatusAndDateRange(TransactionStatus status, Instant startDate, Instant endDate);

    /**
     * Count transactions by wallet
     */
    long countByWalletId(UUID walletId);

    /**
     * Count transactions by status
     */
    long countByStatus(TransactionStatus status);

    /**
     * Count transactions by wallet and status
     */
    long countByWalletIdAndStatus(UUID walletId, TransactionStatus status);

    /**
     * Calculate total transaction amount by wallet
     */
    BigDecimal getTotalAmountByWalletId(UUID walletId);

    /**
     * Calculate total transaction amount by status
     */
    BigDecimal getTotalAmountByStatus(TransactionStatus status);

    /**
     * Calculate total sent amount by wallet
     */
    BigDecimal getTotalSentAmount(UUID walletId);

    /**
     * Calculate total received amount by wallet
     */
    BigDecimal getTotalReceivedAmount(UUID walletId);

    /**
     * Find largest transactions by wallet
     */
    List<Transaction> findTopTransactionsByWalletId(UUID walletId, int limit);

    /**
     * Find recent transactions
     */
    List<Transaction> findRecentTransactions(int limit);

    /**
     * Find transactions by wallet with pagination
     */
    List<Transaction> findByWalletIdWithPagination(UUID walletId, int page, int size);

    /**
     * Delete transactions by status older than date
     */
    void deleteByStatusAndCreatedAtBefore(TransactionStatus status, Instant date);

    /**
     * Find old transactions by status
     */
    List<Transaction> findOldTransactionsByStatus(TransactionStatus status, Instant date);
}
