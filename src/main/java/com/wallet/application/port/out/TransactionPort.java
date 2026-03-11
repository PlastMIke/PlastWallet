package com.wallet.application.port.out;

import com.wallet.domain.entity.Transaction;
import com.wallet.domain.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionPort {
    // Standard operations
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    void deleteById(UUID id);

    // Standard queries
    List<Transaction> findByFromWalletId(UUID fromWalletId);
    List<Transaction> findByToWalletId(UUID toWalletId);
    List<Transaction> findByStatus(TransactionStatus status);

    // Custom queries
    List<Transaction> findAllByWalletId(UUID walletId);
    List<Transaction> findAllByWalletIdAndStatus(UUID walletId, TransactionStatus status);
    List<Transaction> findAllByDateRange(Instant startDate, Instant endDate);
    List<Transaction> findAllByWalletIdAndDateRange(UUID walletId, Instant startDate, Instant endDate);
    List<Transaction> findAllByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);
    List<Transaction> findAllByWalletIdAndAmountRange(UUID walletId, BigDecimal minAmount, BigDecimal maxAmount);
    List<Transaction> findAllByStatusAndDateRange(TransactionStatus status, Instant startDate, Instant endDate);
    long countByWalletId(UUID walletId);
    long countByStatus(TransactionStatus status);
    long countByWalletIdAndStatus(UUID walletId, TransactionStatus status);
    BigDecimal getTotalAmountByWalletId(UUID walletId);
    BigDecimal getTotalAmountByStatus(TransactionStatus status);
    BigDecimal getTotalSentAmount(UUID walletId);
    BigDecimal getTotalReceivedAmount(UUID walletId);
    List<Transaction> findTopTransactionsByWalletId(UUID walletId, int limit);
    List<Transaction> findRecentTransactions(int limit);
    List<Transaction> findByWalletIdWithPagination(UUID walletId, int page, int size);
    void deleteByStatusAndCreatedAtBefore(TransactionStatus status, Instant date);
    List<Transaction> findOldTransactionsByStatus(TransactionStatus status, Instant date);
}
