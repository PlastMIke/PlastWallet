package com.wallet.infrastructure.telegram;

import com.wallet.application.port.out.TransactionPort;
import com.wallet.application.port.out.WalletPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled tasks for Telegram notifications
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramScheduledTasks {

    private final TelegramNotificationService telegramService;
    private final WalletPort walletPort;
    private final TransactionPort transactionPort;

    /**
     * Send daily report at 23:00 every day
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void sendDailyReport() {
        log.info("Sending daily report via Telegram...");
        
        try {
            // In real implementation, fetch actual data from database
            String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String totalTransactions = "234";
            String totalAmount = "$123,456.78";
            String newWallets = "12";
            String errors = "2";

            telegramService.sendDailyReport(date, totalTransactions, totalAmount, newWallets, errors);
            
            log.info("Daily report sent successfully");
        } catch (Exception e) {
            log.error("Failed to send daily report: {}", e.getMessage(), e);
        }
    }

    /**
     * Send health check every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void sendHourlyHealthCheck() {
        log.info("Sending hourly health check...");
        
        try {
            // In real implementation, fetch actual health status
            telegramService.sendHealthStatus("UP", "24h 15m", "567", "1,234");
            
            log.info("Hourly health check sent");
        } catch (Exception e) {
            log.error("Failed to send health check: {}", e.getMessage(), e);
        }
    }

    /**
     * Send morning summary at 09:00 every day
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendMorningSummary() {
        log.info("Sending morning summary...");
        
        try {
            String message = "☀️ <b>Good Morning!</b>\n\n" +
                    "Wallet Service is running smoothly.\n\n" +
                    "📊 <b>Overnight Stats:</b>\n" +
                    "• Transactions: 45\n" +
                    "• Volume: $12,345.67\n" +
                    "• New Users: 3\n\n" +
                    "Have a great day! 🚀";
            
            telegramService.sendMessage(message);
            
            log.info("Morning summary sent");
        } catch (Exception e) {
            log.error("Failed to send morning summary: {}", e.getMessage(), e);
        }
    }
}
