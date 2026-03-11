package com.wallet.infrastructure.alert;

import com.wallet.infrastructure.telegram.TelegramNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Alert service for monitoring and notifications
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final TelegramNotificationService telegramService;
    private final HealthEndpoint healthEndpoint;

    private boolean lastKnownHealthy = true;

    /**
     * Check health every 5 minutes and send alert if status changed
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void checkHealthAndAlert() {
        HealthComponent health = healthEndpoint.health();
        boolean currentlyHealthy = Status.UP.equals(health.getStatus());

        if (!currentlyHealthy && lastKnownHealthy) {
            // Health changed from UP to DOWN
            sendHealthAlert(health);
        } else if (currentlyHealthy && !lastKnownHealthy) {
            // Health changed from DOWN to UP
            sendHealthRecoveryNotification();
        }

        lastKnownHealthy = currentlyHealthy;
    }

    /**
     * Send alert when health check fails
     */
    private void sendHealthAlert(HealthComponent health) {
        log.error("Health check failed! Sending Telegram alert...");
        
        String alertName = "Service Health Check Failed";
        String severity = "CRITICAL";
        String description = String.format(
                "Wallet Service health status changed to: %s\n\n" +
                "Details: %s",
                health.getStatus(),
                health.getDetails()
        );

        telegramService.sendAlert(alertName, severity, description);
    }

    /**
     * Send notification when service recovers
     */
    private void sendHealthRecoveryNotification() {
        log.info("Service recovered! Sending Telegram notification...");
        
        String message = "✅ <b>Service Recovered!</b>\n\n" +
                "Wallet Service is back online and healthy.\n\n" +
                "<i>Automatic recovery detected</i>";
        
        telegramService.sendMessage(message);
    }

    /**
     * Send manual alert
     */
    public void sendAlert(String alertName, String severity, String description) {
        telegramService.sendAlert(alertName, severity, description);
    }

    /**
     * Send transaction alert
     */
    public void sendTransactionAlert(String type, String amount, String status, String walletId) {
        telegramService.sendTransactionNotification(type, amount, status, walletId);
    }
}
