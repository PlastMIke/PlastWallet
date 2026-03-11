# Redis Configuration

## Overview

This wallet service uses **Redis** for caching to improve performance and reduce database load.

## 🏗️ Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  WalletService  │────▶│  Redis Cache     │────▶│  PostgreSQL     │
│                 │     │  (localhost:6379)│     │  Database       │
└─────────────────┘     └──────────────────┘     └─────────────────┘
       │                       │
       │                       │
       ▼                       ▼
┌─────────────────┐     ┌──────────────────┐
│  @Cacheable     │     │  CacheManager    │
│  @CachePut      │     │  (Manual ops)    │
│  @CacheEvict    │     │                  │
└─────────────────┘     └──────────────────┘
```

## 📦 Cache Configuration

### Default Settings

| Property | Value | Description |
|----------|-------|-------------|
| TTL | 300s | Default time-to-live |
| Serialization | JSON | Jackson JSON serializer |
| Null values | Disabled | Null values not cached |
| Transaction aware | Yes | Cache updates with transactions |

### Cache Names and TTLs

| Cache Name | TTL | Purpose |
|------------|-----|---------|
| `wallets` | 300s (5 min) | Wallet data |
| `users` | 600s (10 min) | User data |
| `transactions` | 120s (2 min) | Transaction data |
| `balances` | 60s (1 min) | Balance data (frequently updated) |

## 🔧 Configuration

### application.properties

```properties
# Redis Connection
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=2000ms

# Connection Pool
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
spring.data.redis.lettuce.pool.max-wait=-1ms

# Cache TTL
cache.default.ttl=300
cache.wallets.ttl=300
cache.users.ttl=600
cache.transactions.ttl=120
cache.balances.ttl=60
```

## 🚀 Running Redis

### Option 1: Docker (Recommended)

```bash
# Start Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Or with Redis Stack (includes RedisInsight)
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

### Option 2: Local Installation

```bash
# macOS
brew install redis
redis-server

# Linux
sudo apt-get install redis-server
redis-server

# Windows (WSL or Chocolatey)
choco install redis-64
redis-server
```

## 📝 Usage

### Using @Cacheable Annotation

```java
@Service
@RequiredArgsConstructor
public class WalletService {
    
    @Cacheable(value = "wallets", key = "#walletId.toString()", unless = "#result == null")
    public WalletDTO getWallet(UUID walletId) {
        // Load from database (cached on return)
        return walletRepository.findById(walletId)...;
    }
}
```

### Using @CachePut Annotation

```java
@CachePut(value = "wallets", key = "#wallet.id.toString()")
public WalletDTO updateWallet(WalletDTO wallet) {
    // Update database and cache
    return updatedWallet;
}
```

### Using @CacheEvict Annotation

```java
@CacheEvict(value = "wallets", key = "#walletId.toString()")
public void deleteWallet(UUID walletId) {
    // Remove from database and cache
}
```

### Using CacheManager (Manual Operations)

```java
@Autowired
private CacheManager cacheManager;

// Get from cache
WalletDTO wallet = cacheManager.get("wallets::" + walletId, WalletDTO.class);

// Set in cache
cacheManager.set("wallets::" + walletId, wallet, 300, TimeUnit.SECONDS);

// Delete from cache
cacheManager.delete("wallets::" + walletId);

// Check if exists
boolean exists = cacheManager.exists("wallets::" + walletId);

// Get TTL
Long ttl = cacheManager.getTtl("wallets::" + walletId);
```

## 🧪 Testing

### Unit Test with Embedded Redis

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CacheIntegrationTest {

    @Autowired
    private WalletCacheService cacheService;

    @Test
    void shouldCacheWallet() {
        // Given
        WalletDTO wallet = new WalletDTO(...);
        
        // When
        cacheService.updateWallet(wallet);
        WalletDTO cached = cacheService.getWallet(wallet.getId());
        
        // Then
        assertThat(cached).isEqualTo(wallet);
    }
}
```

### Integration Test with Testcontainers

```java
@Testcontainers
@SpringBootTest
class RedisIntegrationTest {

    @Container
    static RedisContainer redis = new RedisContainer(
        DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
}
```

## 📊 Monitoring

### Redis CLI

```bash
# Connect to Redis
redis-cli

# View all keys
KEYS *

# View keys with pattern
KEYS wallets::*

# Get TTL
TTL wallets::550e8400-e29b-41d4-a716-446655440000

# Get value
GET wallets::550e8400-e29b-41d4-a716-446655440000

# Memory usage
MEMORY USAGE wallets::550e8400-e29b-41d4-a716-446655440000

# Stats
INFO stats
INFO memory
```

### RedisInsight (GUI)

```bash
# Start Redis Stack with RedisInsight
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest

# Open browser
http://localhost:8001
```

## 🔍 Cache Key Patterns

| Pattern | Example | Description |
|---------|---------|-------------|
| `wallets::{id}` | `wallets::550e8400` | Wallet by ID |
| `users::{id}` | `users::1` | User by ID |
| `transactions::{id}` | `transactions::abc123` | Transaction by ID |
| `balances::{walletId}` | `balances::550e8400` | Wallet balance |

## 🐛 Troubleshooting

### Connection Refused

```
org.springframework.data.redis.RedisConnectionFailureException
```

**Solution:** Ensure Redis is running: `docker ps | grep redis`

### Cache Not Working

**Check:**
1. `@EnableCaching` is present in config
2. Method is `public`
3. Called from outside the class (proxy requirement)
4. Return value is not null

### Serialization Error

```
com.fasterxml.jackson.databind.JsonMappingException
```

**Solution:** Ensure objects have default constructor and getters/setters

### Memory Limit

```
OOM command not allowed when used memory > 'maxmemory'
```

**Solution:** 
```bash
# Set max memory
redis-cli CONFIG SET maxmemory 256mb
redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

## 📚 References

- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Redis Documentation](https://redis.io/documentation)
- [Lettuce Client](https://lettuce.io/)
