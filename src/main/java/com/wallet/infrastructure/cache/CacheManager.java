package com.wallet.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis cache helper for manual cache operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Get value from cache
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting cache key: {}", key, e);
            return null;
        }
    }

    /**
     * Set value in cache with default TTL
     */
    public void set(String key, Object value) {
        set(key, value, 300, TimeUnit.SECONDS);
    }

    /**
     * Set value in cache with custom TTL
     */
    public void set(String key, Object value, long ttl, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
            log.debug("Cached key: {} with TTL: {} {}", key, ttl, timeUnit);
        } catch (Exception e) {
            log.error("Error setting cache key: {}", key, e);
        }
    }

    /**
     * Delete value from cache
     */
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Deleted cache key: {}", key);
            }
        } catch (Exception e) {
            log.error("Error deleting cache key: {}", key, e);
        }
    }

    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking cache key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Get TTL for key in seconds
     */
    public Long getTtl(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return null;
        }
    }

    /**
     * Update TTL for existing key
     */
    public void expire(String key, long ttl, TimeUnit timeUnit) {
        try {
            redisTemplate.expire(key, ttl, timeUnit);
            log.debug("Updated TTL for key: {} to {} {}", key, ttl, timeUnit);
        } catch (Exception e) {
            log.error("Error updating TTL for key: {}", key, e);
        }
    }

    /**
     * Clear all caches
     */
    public void clearAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.info("Cleared all Redis caches");
        } catch (Exception e) {
            log.error("Error clearing all caches", e);
        }
    }

    /**
     * Clear specific cache pattern
     */
    public void clearPattern(String pattern) {
        try {
            redisTemplate.getConnectionFactory().getConnection()
                    .keys(pattern.getBytes()).forEach(key -> 
                        redisTemplate.delete(new String(key))
                    );
            log.info("Cleared cache pattern: {}", pattern);
        } catch (Exception e) {
            log.error("Error clearing cache pattern: {}", pattern, e);
        }
    }
}
