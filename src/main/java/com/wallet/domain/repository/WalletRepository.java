package com.wallet.domain.repository;

import com.wallet.domain.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    // Standard CRUD operations
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    void deleteById(UUID id);
    
    // Custom queries
    
    /**
     * Find all wallets for a list of user IDs
     */
    List<Wallet> findAllByUserIds(List<Long> userIds);
    
    /**
     * Find wallets by currency
     */
    List<Wallet> findByCurrency(String currency);
    
    /**
     * Find wallets with balance greater than or equal to amount
     */
    List<Wallet> findAllWithBalanceGreaterThan(BigDecimal amount);
    
    /**
     * Find wallets with balance in range
     */
    List<Wallet> findAllWithBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);
    
    /**
     * Find wallets by currency and minimum balance
     */
    List<Wallet> findByCurrencyAndMinBalance(String currency, BigDecimal minBalance);
    
    /**
     * Count total wallets by currency
     */
    long countByCurrency(String currency);
    
    /**
     * Calculate total balance across all wallets
     */
    BigDecimal getTotalBalance();
    
    /**
     * Calculate total balance by currency
     */
    BigDecimal getTotalBalanceByCurrency(String currency);
    
    /**
     * Find top N wallets by balance
     */
    List<Wallet> findTopWalletsByBalance(int limit);
    
    /**
     * Find wallets created after a specific date
     */
    List<Wallet> findAllCreatedAfter(Instant date);
    
    /**
     * Check if user has any wallet with specific currency
     */
    boolean existsByUserIdAndCurrency(Long userId, String currency);
    
    /**
     * Find all wallets for a user sorted by creation date
     */
    List<Wallet> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
