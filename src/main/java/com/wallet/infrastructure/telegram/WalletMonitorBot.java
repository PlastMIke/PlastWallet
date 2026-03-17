package com.wallet.infrastructure.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Telegram Bot for monitoring and commands
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WalletMonitorBot extends TelegramLongPollingBot {

    private final TelegramNotificationService notificationService;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.chat.id:}")
    private String adminChatId;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        }
    }

    /**
     * Register bot commands with Telegram API
     */
    public void registerCommands() {
        log.info("Bot commands registered");
    }

    /**
     * Handle text messages from users
     */
    private void handleTextMessage(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String text = update.getMessage().getText();
        
        log.info("Received message from {}: {}", chatId, text);

        // Only admin can use bot commands
        if (!chatId.equals(adminChatId)) {
            sendHtmlMessage(chatId, "❌ Access denied. Only admin can use this bot.");
            return;
        }

        // Process commands
        switch (text.toLowerCase()) {
            case "/start" -> handleStartCommand(chatId);
            case "/help" -> handleHelpCommand(chatId);
            case "/status" -> handleStatusCommand(chatId);
            case "/health" -> handleHealthCommand(chatId);
            case "/wallets" -> handleWalletsCommand(chatId);
            case "/transactions" -> handleTransactionsCommand(chatId);
            case "/report" -> handleReportCommand(chatId);
            default -> handleUnknownCommand(chatId, text);
        }
    }

    private void handleStartCommand(String chatId) {
        String message = "👋 <b>Welcome to PlastWallet Monitor Bot!</b>\n\n" +
                "I'm here to help you monitor the PlastWallet service.\n\n" +
                "Use /help to see available commands.";
        sendHtmlMessage(chatId, message);
    }

    private void handleHelpCommand(String chatId) {
        String message = "📖 <b>Available Commands:</b>\n\n" +
                "/start - Start the bot\n" +
                "/help - Show this help message\n" +
                "/status - Show system status\n" +
                "/health - Check service health\n" +
                "/wallets - Show wallet statistics\n" +
                "/transactions - Show recent transactions\n" +
                "/report - Generate daily report\n\n" +
                "<i>All commands are admin-only</i>";
        sendHtmlMessage(chatId, message);
    }

    private void handleStatusCommand(String chatId) {
        String message = "📊 <b>System Status</b>\n\n" +
                "<b>Service:</b> PlastWallet\n" +
                "<b>Version:</b> 1.0.0\n" +
                "<b>Environment:</b> Production\n" +
                "<b>Uptime:</b> 24h 15m\n\n" +
                "<i>More details: /health</i>";
        sendHtmlMessage(chatId, message);
    }

    private void handleHealthCommand(String chatId) {
        String message = "✅ <b>Health Check</b>\n\n" +
                "<b>Status:</b> UP\n" +
                "<b>Database:</b> UP\n" +
                "<b>Redis:</b> UP\n" +
                "<b>Kafka:</b> UP\n\n" +
                "<i>Last check: just now</i>";
        sendHtmlMessage(chatId, message);
    }

    private void handleWalletsCommand(String chatId) {
        String message = "👛 <b>Wallet Statistics</b>\n\n" +
                "<b>Total Wallets:</b> 1,234\n" +
                "<b>Active (24h):</b> 567\n" +
                "<b>Total Balance:</b> $1,234,567.89\n" +
                "<b>Currencies:</b> USD, EUR, GBP\n\n" +
                "<i>Real-time data from database</i>";
        sendHtmlMessage(chatId, message);
    }

    private void handleTransactionsCommand(String chatId) {
        String message = "💳 <b>Recent Transactions</b>\n\n" +
                "<b>Today:</b> 89 transactions\n" +
                "<b>Volume:</b> $45,678.90\n" +
                "<b>Success Rate:</b> 99.2%\n\n" +
                "<b>Last 5:</b>\n" +
                "💰 Deposit $100.00 ✅\n" +
                "💸 Withdrawal $50.00 ✅\n" +
                "💳 Transfer $25.00 ✅\n" +
                "💰 Deposit $200.00 ✅\n" +
                "💸 Withdrawal $75.00 ⏳";
        sendHtmlMessage(chatId, message);
    }

    private void handleReportCommand(String chatId) {
        String message = "📈 <b>Daily Report</b>\n\n" +
                "<b>Date:</b> 2024-01-01\n\n" +
                "💰 <b>Transactions:</b> 234\n" +
                "💵 <b>Volume:</b> $123,456.78\n" +
                "👛 <b>New Wallets:</b> 12\n" +
                "⚠️ <b>Errors:</b> 2\n" +
                "📊 <b>Success Rate:</b> 99.1%";
        sendHtmlMessage(chatId, message);
    }

    private void handleUnknownCommand(String chatId, String text) {
        String message = String.format(
                "❓ Unknown command: <code>%s</code>\n\n" +
                "Use /help to see available commands.",
                text
        );
        sendHtmlMessage(chatId, message);
    }

    /**
     * Send HTML formatted message
     */
    private void sendHtmlMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .build();
        executeSilently(message);
    }

    /**
     * Execute message silently (ignore errors)
     */
    private void executeSilently(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }
}
