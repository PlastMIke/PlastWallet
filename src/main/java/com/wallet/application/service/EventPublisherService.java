package com.wallet.application.service;

import com.wallet.application.dto.TransactionDTO;
import com.wallet.domain.event.TransactionEvent;
import com.wallet.infrastructure.kafka.producer.TransactionProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for publishing domain events to Kafka
 */
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final TransactionProducer transactionProducer;

    /**
     * Publish transaction event to Kafka
     */
    public void publishTransactionEvent(TransactionDTO transactionDTO, String description) {
        TransactionEvent event = TransactionEvent.builder()
                .id(java.util.UUID.randomUUID())
                .transactionId(transactionDTO.getId())
                .fromWalletId(transactionDTO.getFromWalletId())
                .toWalletId(transactionDTO.getToWalletId())
                .amount(transactionDTO.getAmount())
                .type(determineTransactionType(transactionDTO))
                .status(transactionDTO.getStatus())
                .createdAt(transactionDTO.getCreatedAt())
                .description(description)
                .build();

        transactionProducer.sendTransactionEvent(event);
    }

    /**
     * Determine transaction type from DTO
     */
    private String determineTransactionType(TransactionDTO dto) {
        if (dto.getFromWalletId() == null) {
            return "DEPOSIT";
        } else if (dto.getToWalletId() == null) {
            return "WITHDRAWAL";
        } else {
            return "TRANSFER";
        }
    }
}
