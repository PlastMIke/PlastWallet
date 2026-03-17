package com.wallet.infrastructure.persistence.repository;

import com.wallet.infrastructure.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaWalletRepository extends JpaRepository<WalletEntity, UUID> {
    
    // Standard queries
    Optional<WalletEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    // Custom queries

    /**
     * Find all wallets for a list of user IDs
     */
    @Query("SELECT w FROM WalletEntity w WHERE w.userId IN :userIds")
    List<WalletEntity> findAllByUserIds(@Param("userIds") List<Long> userIds);
    
    /**
     * Find wallets by currency
     */
    List<WalletEntity> findByCurrency(String currency);
    
    /**
     * Find wallets with balance greater than or equal to amount
     */
    @Query("SELECT w FROM WalletEntity w WHERE w.balance >= :amount")
    List<WalletEntity> findAllWithBalanceGreaterThan(@Param("amount") BigDecimal amount);
    
    /**
     * Find wallets with balance in range
     */
    @Query("SELECT w FROM WalletEntity w WHERE w.balance BETWEEN :minBalance AND :maxBalance")
    List<WalletEntity> findAllWithBalanceBetween(
        @Param("minBalance") BigDecimal minBalance,
        @Param("maxBalance") BigDecimal maxBalance
    );
    
    /**
     * Find wallets by currency and minimum balance
     */
    @Query("SELECT w FROM WalletEntity w WHERE w.currency = :currency AND w.balance >= :minBalance")
    List<WalletEntity> findByCurrencyAndMinBalance(
        @Param("currency") String currency,
        @Param("minBalance") BigDecimal minBalance
    );
    
    /**
     * Count total wallets by currency
     */
    @Query("SELECT COUNT(w) FROM WalletEntity w WHERE w.currency = :currency")
    long countByCurrency(@Param("currency") String currency);
    
    /**
     * Calculate total balance across all wallets
     */
    @Query("SELECT COALESCE(SUM(w.balance), 0) FROM WalletEntity w")
    BigDecimal getTotalBalance();
    
    /**
     * Calculate total balance by currency
     */
    @Query("SELECT COALESCE(SUM(w.balance), 0) FROM WalletEntity w WHERE w.currency = :currency")
    BigDecimal getTotalBalanceByCurrency(@Param("currency") String currency);
    
    /**
     * Find top N wallets by balance
     */
    @Query("SELECT w FROM WalletEntity w ORDER BY w.balance DESC")
    List<WalletEntity> findTopWalletsByBalance(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find wallets created after a specific date
     */
    @Query("SELECT w FROM WalletEntity w WHERE w.createdAt >= :date")
    List<WalletEntity> findAllCreatedAfter(@Param("date") java.time.Instant date);
    
    /**
     * Check if user has any wallet with specific currency
     */
    @Query("SELECT COUNT(w) > 0 FROM WalletEntity w WHERE w.userId = :userId AND w.currency = :currency")
    boolean existsByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    /**
     * Find all wallets for a user sorted by creation date
     */
    @Query("SELECT w FROM WalletEntity w WHERE w.userId = :userId ORDER BY w.createdAt DESC")
    List<WalletEntity> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
