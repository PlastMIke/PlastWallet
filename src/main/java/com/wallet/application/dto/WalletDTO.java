package com.wallet.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for Wallet entity - excludes internal audit fields (createdAt, updatedAt)
 * Used for API responses and external communication
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private UUID id;
    private Long userId;
    private String currency;
    private BigDecimal balance;
}
