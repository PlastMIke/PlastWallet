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
public class Wallet {
    private UUID id;
    private Long userId;
    private String currency;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;
}
