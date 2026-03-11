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

/**
 * Kafka consumer for processing notification events with retry and DLQ support
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    /**
     * Listen for transaction events with automatic retry and DLQ
     * - Retries: 3 attempts with 1 second backoff
     * - DLQ: Messages sent to wallet-notifications-dlq after all retries exhausted
     */
    @KafkaListener(
            topics = "${kafka.topics.transactions:wallet-transactions}",
            groupId = "${spring.kafka.consumer.group-id:wallet-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionEvent(
            ConsumerRecord<String, Object> record,
            Acknowledgment ack,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset) {
        
        log.info("Received transaction event: key={}, topic={}, partition={}, offset={}",
                record.key(), topic, partition, offset);

        try {
            // Process the transaction event
            Object value = record.value();
            if (value instanceof TransactionEvent) {
                TransactionEvent event = (TransactionEvent) value;
                processTransactionEvent(event);
            }

            // Acknowledge the message
            ack.acknowledge();
            log.info("Transaction event processed successfully: {}", record.key());

        } catch (Exception e) {
            log.error("Error processing transaction event: key={}, topic={}, partition={}, offset={}, error={}", 
                    record.key(), topic, partition, offset, e.getMessage(), e);
            // Rethrow to trigger retry mechanism
            throw e;
        }
    }

    /**
     * Process transaction event and create notification
     * This method may throw exceptions to trigger retry
     */
    private void processTransactionEvent(TransactionEvent event) {
        log.info("Processing transaction event: {}", event.getTransactionId());

        // Simulate potential failures for demonstration
        // In production, remove this simulation code
        if (event.getAmount() != null && event.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid transaction amount: " + event.getAmount());
        }

        // Create notification based on transaction type
        NotificationEvent notification = createNotificationFromTransaction(event);

        // Send notification (may throw exception)
        sendNotification(notification);
        
        log.info("Transaction event processed successfully: {}", event.getTransactionId());
    }

    /**
     * Create notification from transaction event
     */
    private NotificationEvent createNotificationFromTransaction(TransactionEvent event) {
        String title;
        String message;

        switch (event.getType().toUpperCase()) {
            case "DEPOSIT":
                title = "Deposit Successful";
                message = String.format("Your account has been credited with %s %s",
                        event.getAmount(), event.getType());
                break;
            case "WITHDRAWAL":
                title = "Withdrawal Processed";
                message = String.format("%s %s has been debited from your account",
                        event.getAmount(), event.getType());
                break;
            case "TRANSFER":
                title = "Transfer Completed";
                message = String.format("Transfer of %s %s completed successfully",
                        event.getAmount(), event.getType());
                break;
            default:
                title = "Transaction Update";
                message = String.format("Transaction of %s %s processed",
                        event.getAmount(), event.getType());
        }

        return NotificationEvent.builder()
                .id(java.util.UUID.randomUUID())
                .userId(java.util.UUID.randomUUID()) // Extract from event in real scenario
                .type("TRANSACTION")
                .title(title)
                .message(message)
                .channel("PUSH")
                .read(false)
                .createdAt(java.time.Instant.now())
                .build();
    }

    /**
     * Send notification (placeholder for actual implementation)
     * May throw exception to trigger retry
     */
    private void sendNotification(NotificationEvent notification) {
        log.info("Sending notification: {} - {}", notification.getTitle(), notification.getMessage());
        
        // Simulate failure for demonstration (remove in production)
        // This demonstrates retry mechanism
        if (notification.getTitle() == null) {
            throw new RuntimeException("Notification title is required");
        }
        
        // In a real implementation, this would:
        // - Save to database
        // - Send email via email service
        // - Send push notification via FCM/APNS
        // - Send SMS via Twilio/etc.
    }
}
