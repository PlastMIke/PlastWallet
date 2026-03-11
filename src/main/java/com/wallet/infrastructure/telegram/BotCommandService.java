package com.wallet.infrastructure.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for managing bot commands
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BotCommandService {

    private final WalletMonitorBot bot;
    
    @Value("${telegram.bot.token}")
    private String botToken;

    /**
     * Register commands when application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application ready, registering bot commands...");
        bot.registerCommands();
    }

    /**
     * Get bot token
     */
    public String getBotToken() {
        return botToken;
    }
}
