package com.wallet.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.wallet.application.dto.WalletDTO;
import com.wallet.application.dto.UserDTO;
import com.wallet.application.dto.TransactionDTO;

import java.util.UUID;

/**
 * Cache service for wallet-related data
 * Uses Spring Cache abstraction with Redis backend
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WalletCacheService {

    /**
     * Get wallet from cache, or load from database if not cached
     * Cache: wallets::{walletId}
     * TTL: 300 seconds (5 minutes)
     */
    @Cacheable(value = "wallets", key = "#walletId.toString()", unless = "#result == null")
    public WalletDTO getWallet(UUID walletId) {
        log.debug("Cache miss for wallet: {}", walletId);
        // This method should be called from a service that loads from DB
        // The @Cacheable annotation will cache the result
        return null; // Placeholder - actual loading happens in service
    }

    /**
     * Update wallet in cache
     * Cache: wallets::{walletId}
     */
    @CachePut(value = "wallets", key = "#wallet.id.toString()")
    public WalletDTO updateWallet(WalletDTO wallet) {
        log.debug("Updated cache for wallet: {}", wallet.getId());
        return wallet;
    }

    /**
     * Remove wallet from cache
     * Cache: wallets::{walletId}
     */
    @CacheEvict(value = "wallets", key = "#walletId.toString()")
    public void evictWallet(UUID walletId) {
        log.debug("Evicted cache for wallet: {}", walletId);
    }

    /**
     * Get user from cache
     * Cache: users::{userId}
     * TTL: 600 seconds (10 minutes)
     */
    @Cacheable(value = "users", key = "#userId.toString()", unless = "#result == null")
    public UserDTO getUser(Long userId) {
        log.debug("Cache miss for user: {}", userId);
        return null;
    }

    /**
     * Update user in cache
     */
    @CachePut(value = "users", key = "#user.id.toString()")
    public UserDTO updateUser(UserDTO user) {
        log.debug("Updated cache for user: {}", user.getId());
        return user;
    }

    /**
     * Remove user from cache
     */
    @CacheEvict(value = "users", key = "#userId.toString()")
    public void evictUser(Long userId) {
        log.debug("Evicted cache for user: {}", userId);
    }

    /**
     * Get transaction from cache
     * Cache: transactions::{transactionId}
     * TTL: 120 seconds (2 minutes)
     */
    @Cacheable(value = "transactions", key = "#transactionId.toString()", unless = "#result == null")
    public TransactionDTO getTransaction(UUID transactionId) {
        log.debug("Cache miss for transaction: {}", transactionId);
        return null;
    }

    /**
     * Remove transaction from cache
     */
    @CacheEvict(value = "transactions", key = "#transactionId.toString()")
    public void evictTransaction(UUID transactionId) {
        log.debug("Evicted cache for transaction: {}", transactionId);
    }

    /**
     * Clear all wallet-related caches
     */
    @CacheEvict(value = {"wallets", "users", "transactions", "balances"}, allEntries = true)
    public void clearAllCaches() {
        log.info("Cleared all caches");
    }
}
