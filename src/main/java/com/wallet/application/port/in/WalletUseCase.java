package com.wallet.application.port.in;

import com.wallet.application.dto.TransactionDTO;
import com.wallet.application.dto.WalletDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletUseCase {
    WalletDTO createWallet(Long userId, String currency);
    WalletDTO getWallet(UUID walletId);
    WalletDTO getWalletByUserId(Long userId);
    List<TransactionDTO> getTransactionHistory(UUID walletId);
    TransactionDTO deposit(UUID walletId, BigDecimal amount, String description);
    TransactionDTO withdraw(UUID walletId, BigDecimal amount, String description);
    TransactionDTO transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String description);
}
