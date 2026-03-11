package com.wallet.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private UUID id;
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private TransactionStatus status;
    private Instant createdAt;
}
