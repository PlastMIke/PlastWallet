package com.wallet.infrastructure.persistence.repository;

import com.wallet.domain.entity.TransactionStatus;
import com.wallet.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    // Standard queries
    List<TransactionEntity> findByFromWalletId(UUID fromWalletId);
    List<TransactionEntity> findByToWalletId(UUID toWalletId);
    List<TransactionEntity> findByStatus(TransactionStatus status);
    Optional<TransactionEntity> findById(UUID id);

    // JPQL Custom queries

    /**
     * Find transactions where wallet is either sender or receiver
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId ORDER BY t.createdAt DESC")
    List<TransactionEntity> findAllByWalletId(@Param("walletId") UUID walletId);

    /**
     * Find transactions by wallet and status
     */
    @Query("SELECT t FROM TransactionEntity t WHERE (t.fromWalletId = :walletId OR t.toWalletId = :walletId) AND t.status = :status")
    List<TransactionEntity> findAllByWalletIdAndStatus(@Param("walletId") UUID walletId, @Param("status") TransactionStatus status);

    /**
     * Find transactions in date range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<TransactionEntity> findAllByDateRange(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Find transactions by wallet and date range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE (t.fromWalletId = :walletId OR t.toWalletId = :walletId) AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<TransactionEntity> findAllByWalletIdAndDateRange(
        @Param("walletId") UUID walletId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Find transactions by amount range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.amount DESC")
    List<TransactionEntity> findAllByAmountRange(
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount
    );

    /**
     * Find transactions by wallet and amount range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE (t.fromWalletId = :walletId OR t.toWalletId = :walletId) AND t.amount BETWEEN :minAmount AND :maxAmount")
    List<TransactionEntity> findAllByWalletIdAndAmountRange(
        @Param("walletId") UUID walletId,
        @Param("minAmount") BigDecimal minAmount,
        @Param("maxAmount") BigDecimal maxAmount
    );

    /**
     * Find transactions by status and date range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    List<TransactionEntity> findAllByStatusAndDateRange(
        @Param("status") TransactionStatus status,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Count transactions by wallet
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    long countByWalletId(@Param("walletId") UUID walletId);

    /**
     * Count transactions by status
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);

    /**
     * Count transactions by wallet and status
     */
    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE (t.fromWalletId = :walletId OR t.toWalletId = :walletId) AND t.status = :status")
    long countByWalletIdAndStatus(@Param("walletId") UUID walletId, @Param("status") TransactionStatus status);

    /**
     * Calculate total transaction amount by wallet
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    BigDecimal getTotalAmountByWalletId(@Param("walletId") UUID walletId);

    /**
     * Calculate total transaction amount by status
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") TransactionStatus status);

    /**
     * Calculate total sent amount by wallet
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.fromWalletId = :walletId")
    BigDecimal getTotalSentAmount(@Param("walletId") UUID walletId);

    /**
     * Calculate total received amount by wallet
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.toWalletId = :walletId")
    BigDecimal getTotalReceivedAmount(@Param("walletId") UUID walletId);

    /**
     * Find largest transaction by wallet
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId ORDER BY t.amount DESC")
    List<TransactionEntity> findTopTransactionsByWalletId(@Param("walletId") UUID walletId, org.springframework.data.domain.Pageable pageable);

    /**
     * Find recent transactions
     */
    @Query("SELECT t FROM TransactionEntity t ORDER BY t.createdAt DESC")
    List<TransactionEntity> findRecentTransactions(org.springframework.data.domain.Pageable pageable);

    /**
     * Find transactions by wallet with pagination
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByWalletIdWithPagination(@Param("walletId") UUID walletId, org.springframework.data.domain.Pageable pageable);

    /**
     * Delete transactions by status older than date
     */
    @Query("DELETE FROM TransactionEntity t WHERE t.status = :status AND t.createdAt < :date")
    void deleteByStatusAndCreatedAtBefore(@Param("status") TransactionStatus status, @Param("date") Instant date);

    /**
     * Find pending transactions older than date
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.status = :status AND t.createdAt < :date")
    List<TransactionEntity> findOldTransactionsByStatus(@Param("status") TransactionStatus status, @Param("date") Instant date);
}
