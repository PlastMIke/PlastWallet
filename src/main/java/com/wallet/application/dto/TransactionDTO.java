package com.wallet.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class TransactionDTO {
    private UUID id;
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private String status;
    private Instant createdAt;
}
