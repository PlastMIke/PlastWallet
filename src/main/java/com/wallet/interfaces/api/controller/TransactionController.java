package com.wallet.interfaces.api.controller;

import com.wallet.application.dto.TransactionDTO;
import com.wallet.application.port.out.TransactionPort;
import com.wallet.domain.entity.Transaction;
import com.wallet.domain.entity.TransactionStatus;
import com.wallet.interfaces.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for querying and managing transaction history")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    private final TransactionPort transactionPort;

    @GetMapping("/{transactionId}")
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieves a specific transaction by its unique identifier",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction found",
            content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(
            @Parameter(description = "Unique identifier of the transaction", example = "15c5713b-b915-4ba3-a101-a7ee931898ac")
            @PathVariable UUID transactionId) {
        return transactionPort.findById(transactionId)
                .map(transaction -> ResponseEntity.ok(ApiResponse.success(convertToDTO(transaction))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/wallet/{walletId}")
    @Operation(
        summary = "Get transactions by wallet",
        description = "Retrieves all transactions where the wallet is either sender or receiver",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByWallet(
            @Parameter(description = "Wallet identifier", example = "f87046c4-2b67-4369-b1fc-865d40544a40")
            @PathVariable UUID walletId) {
        List<TransactionDTO> transactions = transactionPort.findAllByWalletId(walletId).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/wallet/{walletId}/status/{status}")
    @Operation(
        summary = "Get transactions by wallet and status",
        description = "Retrieves transactions filtered by wallet and status",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByWalletAndStatus(
            @Parameter(description = "Wallet identifier") @PathVariable UUID walletId,
            @Parameter(description = "Transaction status", example = "COMPLETED") @PathVariable String status) {
        TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
        List<TransactionDTO> transactions = transactionPort.findAllByWalletIdAndStatus(walletId, transactionStatus).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/date-range")
    @Operation(
        summary = "Get transactions by date range",
        description = "Retrieves all transactions within a specified date range",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByDateRange(
            @Parameter(description = "Start date (ISO-8601)", example = "2024-01-01T00:00:00Z")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date (ISO-8601)", example = "2024-12-31T23:59:59Z")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        List<TransactionDTO> transactions = transactionPort.findAllByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/wallet/{walletId}/date-range")
    @Operation(
        summary = "Get wallet transactions by date range",
        description = "Retrieves transactions for a specific wallet within a date range",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByWalletAndDateRange(
            @Parameter(description = "Wallet identifier") @PathVariable UUID walletId,
            @Parameter(description = "Start date (ISO-8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date (ISO-8601)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        List<TransactionDTO> transactions = transactionPort.findAllByWalletIdAndDateRange(walletId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/amount-range")
    @Operation(
        summary = "Get transactions by amount range",
        description = "Retrieves transactions filtered by amount range",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid amount range"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByAmountRange(
            @Parameter(description = "Minimum amount", example = "10.00") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum amount", example = "1000.00") @RequestParam BigDecimal maxAmount) {
        List<TransactionDTO> transactions = transactionPort.findAllByAmountRange(minAmount, maxAmount).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get transactions by status",
        description = "Retrieves all transactions with a specific status",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByStatus(
            @Parameter(description = "Transaction status", example = "COMPLETED")
            @PathVariable String status) {
        TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
        List<TransactionDTO> transactions = transactionPort.findByStatus(transactionStatus).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/stats/wallet/{walletId}")
    @Operation(
        summary = "Get wallet transaction statistics",
        description = "Retrieves transaction statistics for a specific wallet including totals and counts",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<TransactionStats>> getWalletTransactionStats(
            @Parameter(description = "Wallet identifier") @PathVariable UUID walletId) {
        BigDecimal totalAmount = transactionPort.getTotalAmountByWalletId(walletId);
        BigDecimal totalSent = transactionPort.getTotalSentAmount(walletId);
        BigDecimal totalReceived = transactionPort.getTotalReceivedAmount(walletId);
        long totalCount = transactionPort.countByWalletId(walletId);

        TransactionStats stats = TransactionStats.builder()
                .walletId(walletId)
                .totalAmount(totalAmount)
                .totalSent(totalSent)
                .totalReceived(totalReceived)
                .transactionCount(totalCount)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getRecentTransactions(
            @Parameter(description = "Number of transactions to retrieve", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<TransactionDTO> transactions = transactionPort.findRecentTransactions(limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/wallet/{walletId}/top")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTopTransactionsByWallet(
            @Parameter(description = "Wallet identifier") @PathVariable UUID walletId,
            @Parameter(description = "Number of transactions", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        List<TransactionDTO> transactions = transactionPort.findTopTransactionsByWalletId(walletId, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/wallet/{walletId}/pagination")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByWalletWithPagination(
            @Parameter(description = "Wallet identifier") @PathVariable UUID walletId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        List<TransactionDTO> transactions = transactionPort.findByWalletIdWithPagination(walletId, page, size).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/count/wallet/{walletId}")
    @Operation(
        summary = "Count transactions by wallet",
        description = "Returns the total number of transactions for a wallet",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<TransactionCount>> countTransactionsByWallet(
            @Parameter(description = "Wallet identifier") @PathVariable UUID walletId) {
        long count = transactionPort.countByWalletId(walletId);
        TransactionCount transactionCount = TransactionCount.builder()
                .walletId(walletId)
                .count(count)
                .build();
        return ResponseEntity.ok(ApiResponse.success(transactionCount));
    }

    @GetMapping("/count/status/{status}")
    @Operation(
        summary = "Count transactions by status",
        description = "Returns the total number of transactions with a specific status",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<StatusCount>> countTransactionsByStatus(
            @Parameter(description = "Transaction status", example = "COMPLETED")
            @PathVariable String status) {
        TransactionStatus transactionStatus = TransactionStatus.valueOf(status.toUpperCase());
        long count = transactionPort.countByStatus(transactionStatus);
        StatusCount statusCount = StatusCount.builder()
                .status(transactionStatus.name())
                .count(count)
                .build();
        return ResponseEntity.ok(ApiResponse.success(statusCount));
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .fromWalletId(transaction.getFromWalletId())
                .toWalletId(transaction.getToWalletId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    @lombok.Builder
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionStats {
        private UUID walletId;
        private BigDecimal totalAmount;
        private BigDecimal totalSent;
        private BigDecimal totalReceived;
        private long transactionCount;
    }

    @lombok.Builder
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransactionCount {
        private UUID walletId;
        private long count;
    }

    @lombok.Builder
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StatusCount {
        private String status;
        private long count;
    }
}
