package com.wallet.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallets_user_id", columnList = "user_id"),
    @Index(name = "idx_wallets_currency", columnList = "currency"),
    @Index(name = "idx_wallets_balance", columnList = "balance"),
    @Index(name = "idx_wallets_created_at", columnList = "created_at"),
    @Index(name = "idx_wallets_currency_balance", columnList = "currency, balance"),
    @Index(name = "idx_wallets_user_currency", columnList = "user_id, currency")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
