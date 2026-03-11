package com.wallet.interfaces.api.controller;

import com.wallet.application.dto.TransactionDTO;
import com.wallet.application.dto.WalletDTO;
import com.wallet.application.port.in.WalletUseCase;
import com.wallet.interfaces.api.request.CreateWalletRequest;
import com.wallet.interfaces.api.request.DepositRequest;
import com.wallet.interfaces.api.request.TransferRequest;
import com.wallet.interfaces.api.request.WithdrawRequest;
import com.wallet.interfaces.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "APIs for managing digital wallets including creation, balance operations, and transaction history")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WalletController {

    private final WalletUseCase walletUseCase;

    @PostMapping
    @Operation(
        summary = "Create new wallet",
        description = "Creates a new digital wallet for a specified user with the given currency",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet created successfully",
            content = @Content(schema = @Schema(implementation = WalletDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input - user already has a wallet or invalid currency"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<WalletDTO>> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {
        WalletDTO wallet = walletUseCase.createWallet(request.getUserId(), request.getCurrency());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(wallet, "Wallet created successfully"));
    }

    @GetMapping("/{walletId}")
    @Operation(
        summary = "Get wallet by ID",
        description = "Retrieves wallet details by its unique identifier",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet found",
            content = @Content(schema = @Schema(implementation = WalletDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<WalletDTO>> getWallet(
            @Parameter(description = "Unique identifier of the wallet", example = "f87046c4-2b67-4369-b1fc-865d40544a40")
            @PathVariable UUID walletId) {
        WalletDTO wallet = walletUseCase.getWallet(walletId);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get wallet by user ID",
        description = "Retrieves wallet details for a specific user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet found",
            content = @Content(schema = @Schema(implementation = WalletDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found for this user"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<WalletDTO>> getWalletByUserId(
            @Parameter(description = "Unique identifier of the user", example = "1")
            @PathVariable Long userId) {
        WalletDTO wallet = walletUseCase.getWalletByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @GetMapping("/{walletId}/transactions")
    @Operation(
        summary = "Get transaction history",
        description = "Retrieves all transactions for a specific wallet",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
            content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionHistory(
            @Parameter(description = "Unique identifier of the wallet", example = "f87046c4-2b67-4369-b1fc-865d40544a40")
            @PathVariable UUID walletId) {
        List<TransactionDTO> transactions = walletUseCase.getTransactionHistory(walletId);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transaction history retrieved"));
    }

    @PostMapping("/{walletId}/deposit")
    @Operation(
        summary = "Deposit funds",
        description = "Adds funds to the wallet balance",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deposit successful",
            content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid amount (must be positive)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<TransactionDTO>> deposit(
            @Parameter(description = "Unique identifier of the wallet", example = "f87046c4-2b67-4369-b1fc-865d40544a40")
            @PathVariable UUID walletId,
            @Valid @RequestBody DepositRequest request) {
        TransactionDTO transaction = walletUseCase.deposit(walletId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success(transaction, "Deposit completed successfully"));
    }

    @PostMapping("/{walletId}/withdraw")
    @Operation(
        summary = "Withdraw funds",
        description = "Removes funds from the wallet balance",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Withdrawal successful",
            content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<TransactionDTO>> withdraw(
            @Parameter(description = "Unique identifier of the wallet", example = "f87046c4-2b67-4369-b1fc-865d40544a40")
            @PathVariable UUID walletId,
            @Valid @RequestBody WithdrawRequest request) {
        TransactionDTO transaction = walletUseCase.withdraw(walletId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success(transaction, "Withdrawal completed successfully"));
    }

    @PostMapping("/{fromWalletId}/transfer")
    @Operation(
        summary = "Transfer funds",
        description = "Transfers funds from one wallet to another",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transfer successful",
            content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid amount, same wallet, or insufficient balance"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Source or destination wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<TransactionDTO>> transfer(
            @Parameter(description = "Source wallet identifier", example = "f87046c4-2b67-4369-b1fc-865d40544a40")
            @PathVariable UUID fromWalletId,
            @Valid @RequestBody TransferRequest request) {
        TransactionDTO transaction = walletUseCase.transfer(
                fromWalletId,
                request.getToWalletId(),
                request.getAmount(),
                request.getDescription());
        return ResponseEntity.ok(ApiResponse.success(transaction, "Transfer completed successfully"));
    }

    @DeleteMapping("/{walletId}")
    @Operation(
        summary = "Delete wallet",
        description = "Permanently deletes a wallet (only if balance is zero)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete wallet with non-zero balance"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Void>> deleteWallet(
            @Parameter(description = "Unique identifier of the wallet", example = "f87046c4-2b67-4369-b1fc-865d40544a40")
            @PathVariable UUID walletId) {
        // TODO: Implement delete in WalletUseCase
        return ResponseEntity.ok(ApiResponse.success(null, "Wallet deletion not yet implemented"));
    }
}
