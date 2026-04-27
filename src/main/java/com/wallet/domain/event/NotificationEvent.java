package com.wallet.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event for sending notifications
 */
@Data
@Builder
@AllArgsConstructor
public class NotificationEvent {
    private UUID id;
    private UUID userId;
    private String type; // TRANSACTION, ALERT, SYSTEM
    private String title;
    private String message;
    private String channel; // EMAIL, SMS, PUSH
    private boolean read;
    private Instant createdAt;
}
