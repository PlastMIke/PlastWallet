-- ============================================================================
-- WALLET SERVICE - OPTIMIZED DATABASE SCHEMA WITH INDEXES
-- ============================================================================
-- This script creates optimized tables with indexes for improved query performance
-- Database: PostgreSQL 15+
-- ============================================================================

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for users table
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users(LOWER(email));

-- ============================================================================
-- WALLETS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL CHECK (currency ~ '^[A-Z]{3}$'),
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint (optional - enable if users table is reference)
    -- CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users(id)
    
    -- Ensure unique constraint for user_id (one wallet per user)
    CONSTRAINT uk_wallets_user_id UNIQUE (user_id)
);

-- Indexes for wallets table
CREATE UNIQUE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallets_currency ON wallets(currency);
CREATE INDEX IF NOT EXISTS idx_wallets_balance ON wallets(balance);
CREATE INDEX IF NOT EXISTS idx_wallets_created_at ON wallets(created_at);
CREATE INDEX IF NOT EXISTS idx_wallets_currency_balance ON wallets(currency, balance);
CREATE INDEX IF NOT EXISTS idx_wallets_user_currency ON wallets(user_id, currency);

-- ============================================================================
-- TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_wallet_id UUID,
    to_wallet_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints (optional - enable if wallets table is reference)
    -- CONSTRAINT fk_transactions_from_wallet FOREIGN KEY (from_wallet_id) REFERENCES wallets(id),
    -- CONSTRAINT fk_transactions_to_wallet FOREIGN KEY (to_wallet_id) REFERENCES wallets(id)
    
    -- Check constraint for valid transaction
    CONSTRAINT chk_transaction_wallets CHECK (from_wallet_id IS NOT NULL OR to_wallet_id IS NOT NULL),
    CONSTRAINT chk_different_wallets CHECK (from_wallet_id IS NULL OR to_wallet_id IS NULL OR from_wallet_id != to_wallet_id)
);

-- Indexes for transactions table
CREATE INDEX IF NOT EXISTS idx_transactions_from_wallet_id ON transactions(from_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_to_wallet_id ON transactions(to_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_transactions_amount ON transactions(amount);
CREATE INDEX IF NOT EXISTS idx_transactions_from_status ON transactions(from_wallet_id, status);
CREATE INDEX IF NOT EXISTS idx_transactions_to_status ON transactions(to_wallet_id, status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_status ON transactions(created_at, status);
CREATE INDEX IF NOT EXISTS idx_transactions_wallets_combined ON transactions(from_wallet_id, to_wallet_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transactions_status_created ON transactions(status, created_at);

-- Composite index for date range queries with status
CREATE INDEX IF NOT EXISTS idx_transactions_status_date ON transactions(status, created_at DESC);

-- Partial indexes for common queries (PostgreSQL specific)
CREATE INDEX IF NOT EXISTS idx_transactions_pending ON transactions(created_at) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_transactions_completed ON transactions(created_at DESC) WHERE status = 'COMPLETED';

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for users table
DROP TRIGGER IF EXISTS trigger_users_updated_at ON users;
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for wallets table
DROP TRIGGER IF EXISTS trigger_wallets_updated_at ON wallets;
CREATE TRIGGER trigger_wallets_updated_at
    BEFORE UPDATE ON wallets
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- VIEWS FOR COMMON QUERIES
-- ============================================================================

-- View for wallet statistics
CREATE OR REPLACE VIEW v_wallet_statistics AS
SELECT 
    currency,
    COUNT(*) as total_wallets,
    SUM(balance) as total_balance,
    AVG(balance) as avg_balance,
    MIN(balance) as min_balance,
    MAX(balance) as max_balance
FROM wallets
GROUP BY currency;

-- View for transaction statistics by status
CREATE OR REPLACE VIEW v_transaction_statistics AS
SELECT 
    status,
    COUNT(*) as total_transactions,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount,
    MIN(amount) as min_amount,
    MAX(amount) as max_amount
FROM transactions
GROUP BY status;

-- View for wallet transaction summary
CREATE OR REPLACE VIEW v_wallet_transaction_summary AS
SELECT 
    w.id as wallet_id,
    w.user_id,
    w.currency,
    w.balance,
    COALESCE(sent.total_sent, 0) as total_sent,
    COALESCE(received.total_received, 0) as total_received,
    COALESCE(sent.tx_count, 0) as sent_count,
    COALESCE(received.tx_count, 0) as received_count
FROM wallets w
LEFT JOIN (
    SELECT from_wallet_id, SUM(amount) as total_sent, COUNT(*) as tx_count
    FROM transactions WHERE from_wallet_id IS NOT NULL
    GROUP BY from_wallet_id
) sent ON w.id = sent.from_wallet_id
LEFT JOIN (
    SELECT to_wallet_id, SUM(amount) as total_received, COUNT(*) as tx_count
    FROM transactions WHERE to_wallet_id IS NOT NULL
    GROUP BY to_wallet_id
) received ON w.id = received.to_wallet_id;

-- ============================================================================
-- MAINTENANCE QUERIES (Run periodically)
-- ============================================================================

-- Analyze tables for query optimizer
ANALYZE users;
ANALYZE wallets;
ANALYZE transactions;

-- Vacuum analyze for dead tuple cleanup
-- VACUUM ANALYZE users;
-- VACUUM ANALYZE wallets;
-- VACUUM ANALYZE transactions;

-- ============================================================================
-- ARCHIVE/CLEANUP QUERIES (For maintenance scripts)
-- ============================================================================

-- Archive old completed transactions (example - older than 90 days)
-- CREATE TABLE IF NOT EXISTS transactions_archive (LIKE transactions INCLUDING ALL);
-- INSERT INTO transactions_archive 
-- SELECT * FROM transactions 
-- WHERE status = 'COMPLETED' AND created_at < CURRENT_TIMESTAMP - INTERVAL '90 days';

-- Delete archived transactions
-- DELETE FROM transactions 
-- WHERE status = 'COMPLETED' AND created_at < CURRENT_TIMESTAMP - INTERVAL '90 days';

-- ============================================================================
-- EXPLAIN ANALYZE EXAMPLES (For query optimization)
-- ============================================================================

-- Example: Check query performance
-- EXPLAIN ANALYZE SELECT * FROM transactions WHERE from_wallet_id = '...' OR to_wallet_id = '...';
-- EXPLAIN ANALYZE SELECT * FROM transactions WHERE status = 'PENDING' AND created_at < NOW();
-- EXPLAIN ANALYZE SELECT COUNT(*) FROM wallets WHERE currency = 'USD';
