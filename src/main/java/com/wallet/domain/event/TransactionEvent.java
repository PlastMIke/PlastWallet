package com.wallet.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event representing a completed transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private UUID id;
    private UUID transactionId;
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    private String status;
    private Instant createdAt;
    private String description;
}
