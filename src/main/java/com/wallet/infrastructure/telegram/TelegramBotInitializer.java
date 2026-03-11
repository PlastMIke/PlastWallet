package com.wallet.infrastructure.telegram;

import com.wallet.infrastructure.config.TelegramConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Telegram Bot Initializer
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "telegram.bot", name = "token")
public class TelegramBotInitializer {

    private final WalletMonitorBot bot;
    private final TelegramBotsApi botsApi;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Telegram Bot...");
            
            // Register bot
            botsApi.registerBot(bot);
            
            // Register commands
            registerCommands();
            
            log.info("✅ Telegram Bot initialized successfully!");
        } catch (Exception e) {
            log.error("❌ Failed to initialize Telegram Bot: {}", e.getMessage(), e);
        }
    }

    /**
     * Register bot commands
     */
    private void registerCommands() {
        try {
            List<BotCommand> commands = List.of(
                new BotCommand("/start", "Запустить бота"),
                new BotCommand("/help", "Показать справку"),
                new BotCommand("/status", "Статус системы"),
                new BotCommand("/health", "Проверка здоровья"),
                new BotCommand("/wallets", "Статистика кошельков"),
                new BotCommand("/transactions", "Последние транзакции"),
                new BotCommand("/report", "Дневной отчёт")
            );

            SetMyCommands setCommands = SetMyCommands.builder()
                    .scope(BotCommandScopeDefault.builder().build())
                    .commands(commands)
                    .build();

            bot.execute(setCommands);
            log.info("✅ Bot commands registered: {}", commands.size());
        } catch (Exception e) {
            log.error("Failed to register bot commands: {}", e.getMessage());
        }
    }
}
