-- Быстрая настройка тестовых данных для PlastWallet
-- Выполнить: docker exec -i wallet-postgres psql -U postgres -d wallet_db < setup-test-data-quick.sql

-- Очищаем существующие кошельки
DELETE FROM wallets;

-- Создаём кошельки с числовыми user_id (1, 2, 3)
INSERT INTO wallets (id, user_id, currency, balance, created_at, updated_at)
VALUES 
  (gen_random_uuid(), 1, 'USD', 100, NOW(), NOW()),
  (gen_random_uuid(), 2, 'EUR', 100, NOW(), NOW()),
  (gen_random_uuid(), 3, 'GBP', 100, NOW(), NOW());

-- Проверяем результат
SELECT 
  w.id as "Wallet ID",
  w.user_id as "User ID",
  u.name as "Name",
  u.email as "Email",
  w.currency as "Currency",
  w.balance as "Balance"
FROM wallets w
LEFT JOIN users u ON w.user_id = u.id
ORDER BY w.currency;
