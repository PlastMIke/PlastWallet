package com.wallet.application.service;

import com.wallet.application.dto.TransactionDTO;
import com.wallet.application.dto.WalletDTO;
import com.wallet.application.port.in.WalletUseCase;
import com.wallet.application.port.out.TransactionPort;
import com.wallet.application.port.out.WalletPort;
import com.wallet.domain.entity.Transaction;
import com.wallet.domain.entity.TransactionStatus;
import com.wallet.domain.entity.TransactionType;
import com.wallet.domain.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceAdapter implements WalletUseCase {
    private final WalletPort walletPort;
    private final TransactionPort transactionPort;

    @Override
    @Transactional
    public WalletDTO createWallet(Long userId, String currency) {
        if (walletPort.existsByUserId(userId)) {
            throw new IllegalArgumentException("Wallet already exists for user: " + userId);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Wallet savedWallet = walletPort.save(wallet);
        return mapToDTO(savedWallet);
    }

    @Override
    public WalletDTO getWallet(UUID walletId) {
        Wallet wallet = walletPort.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));
        return mapToDTO(wallet);
    }

    @Override
    public WalletDTO getWalletByUserId(Long userId) {
        Wallet wallet = walletPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));
        return mapToDTO(wallet);
    }

    @Override
    public List<TransactionDTO> getTransactionHistory(UUID walletId) {
        List<Transaction> transactions = transactionPort.findAllByWalletId(walletId);
        return transactions.stream()
                .map(this::mapToTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionDTO deposit(UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = walletPort.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(Instant.now());
        walletPort.save(wallet);

        Transaction transaction = createTransaction(null, walletId, amount, description);
        return mapToTransactionDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionDTO withdraw(UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = walletPort.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(Instant.now());
        walletPort.save(wallet);

        Transaction transaction = createTransaction(walletId, null, amount, description);
        return mapToTransactionDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionDTO transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String description) {
        if (fromWalletId.equals(toWalletId)) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        Wallet fromWallet = walletPort.findById(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found: " + fromWalletId));
        Wallet toWallet = walletPort.findById(toWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found: " + toWalletId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for transfer");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        fromWallet.setUpdatedAt(Instant.now());
        walletPort.save(fromWallet);

        toWallet.setBalance(toWallet.getBalance().add(amount));
        toWallet.setUpdatedAt(Instant.now());
        walletPort.save(toWallet);

        Transaction transaction = createTransaction(fromWalletId, toWalletId, amount, description);
        return mapToTransactionDTO(transaction);
    }

    private Transaction createTransaction(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String description) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .status(TransactionStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();
    }

    private String generateReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private WalletDTO mapToDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .currency(wallet.getCurrency())
                .balance(wallet.getBalance())
                .build();
    }

    private TransactionDTO mapToTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .fromWalletId(transaction.getFromWalletId())
                .toWalletId(transaction.getToWalletId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
