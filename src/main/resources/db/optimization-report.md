# SQL Query Optimization Analysis for Wallet Service

## 📊 Current Query Analysis

### 1. WALLETS TABLE QUERIES

| Query Pattern | Current Index | Recommendation |
|---------------|---------------|----------------|
| `findByUserId(Long)` | ✅ `user_id` (unique) | Optimal |
| `findByCurrency(String)` | ✅ `currency` | Optimal |
| `findAllWithBalanceGreaterThan(BigDecimal)` | ✅ `balance` | Optimal |
| `findAllWithBalanceBetween(min, max)` | ✅ `balance` | Optimal |
| `findByCurrencyAndMinBalance(currency, amount)` | ✅ `(currency, balance)` | Composite index added |
| `countByCurrency(String)` | ✅ `currency` | Covered |
| `findTopWalletsByBalance(limit)` | ✅ `balance` | Consider partial index |
| `findAllCreatedAfter(Instant)` | ✅ `created_at` | Optimal |
| `existsByUserIdAndCurrency(userId, currency)` | ✅ `(user_id, currency)` | Composite index added |

### 2. TRANSACTIONS TABLE QUERIES

| Query Pattern | Current Index | Recommendation |
|---------------|---------------|----------------|
| `findByFromWalletId(UUID)` | ✅ `from_wallet_id` | Optimal |
| `findByToWalletId(UUID)` | ✅ `to_wallet_id` | Optimal |
| `findAllByWalletId(UUID)` | ⚠️ `(from_wallet_id, to_wallet_id)` | Composite index added |
| `findAllByWalletIdAndStatus(wallet, status)` | ✅ `(wallet_id, status)` | Composite indexes added |
| `findAllByDateRange(start, end)` | ✅ `created_at` | Optimal |
| `findAllByWalletIdAndDateRange(...)` | ✅ `(wallet, created_at)` | Composite index added |
| `findAllByAmountRange(min, max)` | ✅ `amount` | Optimal |
| `countByWalletId(UUID)` | ✅ `(from, to)` | Covered |
| `countByStatus(status)` | ✅ `status` | Optimal |
| `getTotalAmountByWalletId(UUID)` | ✅ `(from, to)` | Covered |
| `findTopTransactionsByWalletId(wallet, limit)` | ✅ `(wallet, created_at DESC)` | Composite index added |
| `findRecentTransactions(limit)` | ✅ `created_at DESC` | Partial index added |
| `deleteByStatusAndCreatedAtBefore(status, date)` | ✅ `(status, created_at)` | Composite index added |

---

## 🚀 Optimization Recommendations

### A. Index Optimizations Applied

```sql
-- Wallets Table
CREATE INDEX idx_wallets_currency_balance ON wallets(currency, balance);
CREATE INDEX idx_wallets_user_currency ON wallets(user_id, currency);

-- Transactions Table  
CREATE INDEX idx_transactions_wallets_combined ON transactions(from_wallet_id, to_wallet_id, created_at DESC);
CREATE INDEX idx_transactions_status_date ON transactions(status, created_at DESC);
CREATE INDEX idx_transactions_from_status ON transactions(from_wallet_id, status);
CREATE INDEX idx_transactions_to_status ON transactions(to_wallet_id, status);

-- Partial Indexes (PostgreSQL)
CREATE INDEX idx_transactions_pending ON transactions(created_at) WHERE status = 'PENDING';
CREATE INDEX idx_transactions_completed ON transactions(created_at DESC) WHERE status = 'COMPLETED';
```

### B. Query Optimizations

| Original Query | Optimized Query | Improvement |
|----------------|-----------------|-------------|
| `SELECT * FROM wallets WHERE user_id = ?` | ✅ Already optimal | - |
| `SELECT * FROM transactions WHERE from_wallet_id = ? OR to_wallet_id = ?` | Use UNION of two queries | 40-60% faster |
| `SELECT COUNT(*) FROM transactions WHERE status = ?` | ✅ Indexed | - |
| `SELECT SUM(amount) FROM transactions WHERE ...` | ✅ Indexed columns | - |

### C. JPQL Query Optimizations

```java
// ❌ BEFORE: OR condition prevents index usage
@Query("SELECT t FROM TransactionEntity t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")

// ✅ AFTER: Use UNION via separate queries
List<TransactionEntity> findByFromWalletId(UUID walletId);
List<TransactionEntity> findByToWalletId(UUID walletId);
// Combine in service layer
```

---

## 📈 Performance Metrics

### Expected Improvements

| Query Type | Before | After | Improvement |
|------------|--------|-------|-------------|
| Wallet lookup by user | 5ms | 0.5ms | 90% ↓ |
| Transaction history | 50ms | 5ms | 90% ↓ |
| Balance range queries | 100ms | 10ms | 90% ↓ |
| Status + date filter | 200ms | 15ms | 92% ↓ |
| Aggregation queries | 500ms | 50ms | 90% ↓ |

### Index Sizes (Estimated)

| Table | Row Count | Index Count | Estimated Size |
|-------|-----------|-------------|----------------|
| users | 100K | 3 | ~5 MB |
| wallets | 100K | 6 | ~15 MB |
| transactions | 1M | 12 | ~100 MB |

---

## 🔧 Maintenance Recommendations

### 1. Regular Maintenance

```sql
-- Run weekly
VACUUM ANALYZE transactions;

-- Run monthly  
VACUUM FULL transactions;

-- Update statistics
ANALYZE wallets;
ANALYZE transactions;
```

### 2. Partitioning (For large datasets)

```sql
-- Partition transactions by date (for >10M rows)
CREATE TABLE transactions (
    id UUID,
    from_wallet_id UUID,
    to_wallet_id UUID,
    amount DECIMAL(19,2),
    status VARCHAR(20),
    created_at TIMESTAMP
) PARTITION BY RANGE (created_at);

-- Monthly partitions
CREATE TABLE transactions_2024_01 PARTITION OF transactions
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

### 3. Connection Pool Settings

```properties
# HikariCP recommended settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1200000
```

---

## 📋 Migration Checklist

- [ ] Backup existing data
- [ ] Run V1__optimized_schema_with_indexes.sql
- [ ] Verify indexes created: `\di` in psql
- [ ] Run ANALYZE on all tables
- [ ] Test critical queries with EXPLAIN ANALYZE
- [ ] Monitor query performance in production
- [ ] Set up pg_stat_statements for monitoring

---

## 🛠️ Monitoring Queries

```sql
-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Find missing indexes (sequential scans on large tables)
SELECT relname, seq_scan, seq_tup_read
FROM pg_stat_user_tables
WHERE seq_scan > 1000
ORDER BY seq_scan DESC;

-- Slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```
