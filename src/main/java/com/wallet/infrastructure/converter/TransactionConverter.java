package com.wallet.infrastructure.converter;

import com.wallet.domain.entity.Transaction;
import com.wallet.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionConverter {
    public TransactionEntity toEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.getId() != null ? transaction.getId() : java.util.UUID.randomUUID());
        entity.setFromWalletId(transaction.getFromWalletId());
        entity.setToWalletId(transaction.getToWalletId());
        entity.setAmount(transaction.getAmount());
        entity.setStatus(transaction.getStatus());
        entity.setCreatedAt(transaction.getCreatedAt());
        return entity;
    }

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Transaction.builder()
                .id(entity.getId())
                .fromWalletId(entity.getFromWalletId())
                .toWalletId(entity.getToWalletId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
