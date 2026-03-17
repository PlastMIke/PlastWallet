package com.wallet.application.port.out;

import com.wallet.domain.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletPort {
    // Standard operations
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    // Custom queries
    List<Wallet> findAllByUserIds(List<Long> userIds);
    List<Wallet> findByCurrency(String currency);
    List<Wallet> findAllWithBalanceGreaterThan(BigDecimal amount);
    List<Wallet> findAllWithBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance);
    List<Wallet> findByCurrencyAndMinBalance(String currency, BigDecimal minBalance);
    long countByCurrency(String currency);
    BigDecimal getTotalBalance();
    BigDecimal getTotalBalanceByCurrency(String currency);
    List<Wallet> findTopWalletsByBalance(int limit);
    List<Wallet> findAllCreatedAfter(Instant date);
    boolean existsByUserIdAndCurrency(Long userId, String currency);
    List<Wallet> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
