package com.wallet.interfaces.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Currency is required")
    private String currency;
}
