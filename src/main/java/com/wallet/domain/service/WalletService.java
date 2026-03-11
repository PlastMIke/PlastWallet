package com.wallet.domain.service;

import com.wallet.domain.entity.Wallet;
import com.wallet.application.port.out.WalletPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletPort walletPort;

    @Transactional
    public Wallet createWallet(Long userId, String currency) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return walletPort.save(wallet);
    }

    public Optional<Wallet> getWallet(UUID walletId) {
        return walletPort.findById(walletId);
    }

    public Optional<Wallet> getWalletByUserId(Long userId) {
        return walletPort.findByUserId(userId);
    }

    @Transactional
    public Wallet updateBalance(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletPort.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(Instant.now());
        return walletPort.save(wallet);
    }
}
