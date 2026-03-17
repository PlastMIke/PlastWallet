-- Создаём кошельки для существующих пользователей
INSERT INTO wallets (user_id, currency, balance, created_at, updated_at)
VALUES 
  ((SELECT id FROM users WHERE email = 'alice@plastwallet.com'), 'USD', 0, NOW(), NOW()),
  ((SELECT id FROM users WHERE email = 'bob@plastwallet.com'), 'EUR', 0, NOW(), NOW()),
  ((SELECT id FROM users WHERE email = 'charlie@plastwallet.com'), 'GBP', 0, NOW(), NOW());

-- Пополняем все кошельки на 100
UPDATE wallets SET balance = 100, updated_at = NOW();

-- Переводы между кошельками (симулируем через транзакции)
-- Alice (USD) -> Bob (EUR): 50 USD
-- Bob (EUR) -> Charlie (GBP): 50 EUR  
-- Charlie (GBP) -> Alice (USD): 50 GBP

-- Для простоты просто обновляем балансы после переводов
UPDATE wallets SET balance = 50 WHERE currency = 'USD';  -- Alice: 100 - 50 = 50
UPDATE wallets SET balance = 100 WHERE currency = 'EUR'; -- Bob: 100 - 50 + 50 = 100
UPDATE wallets SET balance = 100 WHERE currency = 'GBP'; -- Charlie: 100 + 50 - 50 = 100

-- Показываем результат
SELECT w.id, w.user_id, u.name, u.email, w.currency, w.balance 
FROM wallets w 
JOIN users u ON w.user_id = u.id
ORDER BY w.currency;
