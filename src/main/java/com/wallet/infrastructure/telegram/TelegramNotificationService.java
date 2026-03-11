package com.wallet.infrastructure.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Telegram service for sending notifications
 */
@Component
@Slf4j
public class TelegramNotificationService {

    @Value("${telegram.bot.token:}")
    private String botToken;
    
    @Value("${telegram.chat.id:}")
    private String chatId;
    
    private final java.net.http.HttpClient httpClient;

    public TelegramNotificationService() {
        this.httpClient = java.net.http.HttpClient.newHttpClient();
    }

    /**
     * Send text message to configured chat
     */
    public void sendMessage(String text) {
        sendMessage(chatId, text);
    }

    /**
     * Send text message to specific chat using Telegram Bot API
     */
    public void sendMessage(String chatId, String text) {
        if (chatId == null || chatId.isEmpty() || botToken == null || botToken.isEmpty()) {
            log.warn("Chat ID or Bot token is not configured, skipping message");
            return;
        }

        try {
            // Use Telegram Bot API directly via HTTP
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
            
            String json = String.format(
                "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"HTML\"}",
                chatId,
                text.replace("\"", "\\\"")
            );
            
            var request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                log.info("Telegram message sent to chat {}", chatId);
            } else {
                log.error("Failed to send Telegram message: {} - {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage(), e);
        }
    }

    /**
     * Send alert notification
     */
    public void sendAlert(String alertName, String severity, String description) {
        String emoji = getSeverityEmoji(severity);
        String message = String.format(
                "%s <b>🚨 ALERT</b>\n\n" +
                "<b>Name:</b> %s\n" +
                "<b>Severity:</b> %s\n" +
                "<b>Description:</b> %s\n\n" +
                "<i>Time:</i> %s",
                emoji,
                alertName,
                severity,
                description,
                java.time.Instant.now()
        );
        sendMessage(message);
    }

    /**
     * Send daily report
     */
    public void sendDailyReport(String date, String totalTransactions, String totalAmount, 
                                 String newWallets, String errors) {
        String message = String.format(
                "📊 <b>Daily Report</b>\n\n" +
                "<b>Date:</b> %s\n\n" +
                "<b>💰 Transactions:</b> %s\n" +
                "<b>💵 Total Amount:</b> %s\n" +
                "<b>👛 New Wallets:</b> %s\n" +
                "<b>⚠️ Errors:</b> %s",
                date,
                totalTransactions,
                totalAmount,
                newWallets,
                errors
        );
        sendMessage(message);
    }

    private String getSeverityEmoji(String severity) {
        return switch (severity.toLowerCase()) {
            case "critical" -> "🔴";
            case "error" -> "🔴";
            case "warning" -> "🟡";
            case "info" -> "🔵";
            default -> "⚪";
        };
    }
}
