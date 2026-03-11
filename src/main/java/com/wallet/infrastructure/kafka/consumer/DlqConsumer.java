package com.wallet.infrastructure.kafka.consumer;

import com.wallet.domain.event.NotificationEvent;
import com.wallet.domain.event.TransactionEvent;
import com.wallet.infrastructure.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Kafka consumer for processing Dead Letter Queue (DLQ) messages
 * Handles messages that failed processing after all retry attempts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DlqConsumer {

    /**
     * Listen for messages in the notification DLQ topic
     * These are messages that failed processing after all retry attempts
     */
    @KafkaListener(
            topics = KafkaConfig.NOTIFICATION_DLQ_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:wallet-service-group}-dlq",
            containerFactory = "dlqKafkaListenerContainerFactory"
    )
    public void consumeDlqMessage(
            ConsumerRecord<String, Object> record,
            Acknowledgment ack,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset) {
        
        log.warn("Received DLQ message: key={}, topic={}, partition={}, offset={}",
                record.key(), topic, partition, offset);
        log.warn("Message value: {}", record.value());

        try {
            // Analyze and process DLQ message
            processDlqMessage(record);
            
            // Acknowledge - message processed (logged/alerted)
            ack.acknowledge();
            log.info("DLQ message processed and acknowledged: {}", record.key());
            
        } catch (Exception e) {
            log.error("Error processing DLQ message: key={}, error={}", record.key(), e.getMessage(), e);
            // Don't rethrow - we don't want to loop DLQ messages
            // Just acknowledge to avoid infinite loop
            ack.acknowledge();
        }
    }

    /**
     * Process DLQ message - analyze failure and take appropriate action
     */
    private void processDlqMessage(ConsumerRecord<String, Object> record) {
        log.info("Processing DLQ message: key={}", record.key());
        
        Object value = record.value();
        
        if (value instanceof NotificationEvent) {
            NotificationEvent notification = (NotificationEvent) value;
            handleFailedNotification(notification);
        } else if (value instanceof TransactionEvent) {
            TransactionEvent transaction = (TransactionEvent) value;
            handleFailedTransaction(transaction);
        } else {
            log.warn("Unknown message type in DLQ: {}", value != null ? value.getClass().getName() : "null");
        }
        
        // In production, you might want to:
        // 1. Save to database for manual review
        // 2. Send alert to operations team
        // 3. Attempt alternative processing
        // 4. Archive for compliance
    }

    /**
     * Handle failed notification event
     */
    private void handleFailedNotification(NotificationEvent notification) {
        log.error("Failed to process notification: id={}, userId={}, type={}, title={}",
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle());
        
        // Save failed notification for manual review
        saveFailedMessage(notification, "NOTIFICATION");
    }

    /**
     * Handle failed transaction event
     */
    private void handleFailedTransaction(TransactionEvent transaction) {
        log.error("Failed to process transaction: id={}, transactionId={}, amount={}, type={}",
                transaction.getId(),
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getType());
        
        // Save failed transaction for manual review
        saveFailedMessage(transaction, "TRANSACTION");
    }

    /**
     * Save failed message to database for manual review
     * (Placeholder for actual implementation)
     */
    private void saveFailedMessage(Object message, String type) {
        log.info("Saving failed {} message to database for manual review: {}", type, message);
        // TODO: Implement database persistence
        // failedMessageRepository.save(FailedMessage.builder()
        //     .messageType(type)
        //     .payload(message)
        //     .failedAt(Instant.now())
        //     .status("FAILED")
        //     .build());
    }
}
