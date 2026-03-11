package com.wallet.infrastructure.kafka.producer;

import com.wallet.domain.event.TransactionEvent;
import com.wallet.infrastructure.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for publishing transaction events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send transaction event to Kafka topic
     *
     * @param event Transaction event to publish
     */
    public void sendTransactionEvent(TransactionEvent event) {
        log.info("Sending transaction event: {}", event.getTransactionId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.TRANSACTION_TOPIC,
                event.getTransactionId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Transaction event sent successfully: {} [partition={}, offset={}]",
                        event.getTransactionId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send transaction event: {}", event.getTransactionId(), ex);
            }
        });
    }

    /**
     * Send transaction event to specific partition
     */
    public void sendTransactionEvent(TransactionEvent event, Integer partition) {
        log.info("Sending transaction event to partition {}: {}", partition, event.getTransactionId());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.TRANSACTION_TOPIC,
                partition,
                event.getTransactionId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Transaction event sent to partition {}: {} [offset={}]",
                        partition,
                        event.getTransactionId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send transaction event to partition {}: {}",
                        partition, event.getTransactionId(), ex);
            }
        });
    }

    /**
     * Send transaction event synchronously
     */
    public SendResult<String, Object> sendTransactionEventSync(TransactionEvent event) {
        log.info("Sending transaction event synchronously: {}", event.getTransactionId());

        try {
            SendResult<String, Object> result = kafkaTemplate.send(
                    KafkaConfig.TRANSACTION_TOPIC,
                    event.getTransactionId().toString(),
                    event
            ).get();

            log.info("Transaction event sent successfully: {} [partition={}, offset={}]",
                    event.getTransactionId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return result;
        } catch (Exception e) {
            log.error("Failed to send transaction event synchronously: {}", event.getTransactionId(), e);
            throw new RuntimeException("Failed to send transaction event", e);
        }
    }
}
