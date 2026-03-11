package com.wallet.infrastructure.adapter;

import com.wallet.application.port.out.WalletPort;
import com.wallet.domain.entity.Wallet;
import com.wallet.infrastructure.converter.WalletConverter;
import com.wallet.infrastructure.persistence.entity.WalletEntity;
import com.wallet.infrastructure.persistence.repository.JpaWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalletAdapter implements WalletPort {
    private final JpaWalletRepository jpaWalletRepository;
    private final WalletConverter walletConverter;

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = walletConverter.toEntity(wallet);
        WalletEntity saved = jpaWalletRepository.save(entity);
        return walletConverter.toDomain(saved);
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return jpaWalletRepository.findById(id).map(walletConverter::toDomain);
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return jpaWalletRepository.findByUserId(userId).map(walletConverter::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaWalletRepository.existsByUserId(userId);
    }

    @Override
    public List<Wallet> findAllByUserIds(List<Long> userIds) {
        return jpaWalletRepository.findAllByUserIds(userIds).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wallet> findByCurrency(String currency) {
        return jpaWalletRepository.findByCurrency(currency).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wallet> findAllWithBalanceGreaterThan(BigDecimal amount) {
        return jpaWalletRepository.findAllWithBalanceGreaterThan(amount).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wallet> findAllWithBalanceBetween(BigDecimal minBalance, BigDecimal maxBalance) {
        return jpaWalletRepository.findAllWithBalanceBetween(minBalance, maxBalance).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wallet> findByCurrencyAndMinBalance(String currency, BigDecimal minBalance) {
        return jpaWalletRepository.findByCurrencyAndMinBalance(currency, minBalance).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCurrency(String currency) {
        return jpaWalletRepository.countByCurrency(currency);
    }

    @Override
    public BigDecimal getTotalBalance() {
        return jpaWalletRepository.getTotalBalance();
    }

    @Override
    public BigDecimal getTotalBalanceByCurrency(String currency) {
        return jpaWalletRepository.getTotalBalanceByCurrency(currency);
    }

    @Override
    public List<Wallet> findTopWalletsByBalance(int limit) {
        return jpaWalletRepository.findTopWalletsByBalance(PageRequest.of(0, limit)).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Wallet> findAllCreatedAfter(Instant date) {
        return jpaWalletRepository.findAllCreatedAfter(date).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndCurrency(Long userId, String currency) {
        return jpaWalletRepository.existsByUserIdAndCurrency(userId, currency);
    }

    @Override
    public List<Wallet> findAllByUserIdOrderByCreatedAtDesc(Long userId) {
        return jpaWalletRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(walletConverter::toDomain)
                .collect(Collectors.toList());
    }
}
