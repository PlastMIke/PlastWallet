package com.wallet.infrastructure.converter;

import com.wallet.domain.entity.Wallet;
import com.wallet.infrastructure.persistence.entity.WalletEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletConverter {
    public WalletEntity toEntity(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        WalletEntity entity = new WalletEntity();
        entity.setId(wallet.getId() != null ? wallet.getId() : UUID.randomUUID());
        entity.setUserId(wallet.getUserId());
        entity.setCurrency(wallet.getCurrency());
        entity.setBalance(wallet.getBalance());
        entity.setCreatedAt(wallet.getCreatedAt());
        entity.setUpdatedAt(wallet.getUpdatedAt());
        return entity;
    }

    public Wallet toDomain(WalletEntity entity) {
        if (entity == null) {
            return null;
        }
        return Wallet.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .currency(entity.getCurrency())
                .balance(entity.getBalance())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
