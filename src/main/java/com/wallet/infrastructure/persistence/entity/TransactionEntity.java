package com.wallet.infrastructure.persistence.entity;

import com.wallet.domain.entity.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_from_wallet_id", columnList = "from_wallet_id"),
    @Index(name = "idx_transactions_to_wallet_id", columnList = "to_wallet_id"),
    @Index(name = "idx_transactions_status", columnList = "status"),
    @Index(name = "idx_transactions_created_at", columnList = "created_at"),
    @Index(name = "idx_transactions_amount", columnList = "amount"),
    @Index(name = "idx_transactions_from_status", columnList = "from_wallet_id, status"),
    @Index(name = "idx_transactions_to_status", columnList = "to_wallet_id, status"),
    @Index(name = "idx_transactions_wallets_combined", columnList = "from_wallet_id, to_wallet_id, created_at DESC"),
    @Index(name = "idx_transactions_status_date", columnList = "status, created_at DESC")
})
@Data
@Builder
@AllArgsConstructor
public class TransactionEntity {
    @Id
    private UUID id;

    @Column(name = "from_wallet_id", nullable = false)
    private UUID fromWalletId;

    @Column(name = "to_wallet_id", nullable = false)
    private UUID toWalletId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
