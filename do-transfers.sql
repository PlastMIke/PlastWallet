-- Переводы между кошельками по 50
-- Alice (user_id=1, USD) -> Bob (user_id=2, EUR): 50 USD
-- Bob (user_id=2, EUR) -> Charlie (user_id=3, GBP): 50 EUR
-- Charlie (user_id=3, GBP) -> Alice (user_id=1, USD): 50 GBP

-- Создаём транзакции
INSERT INTO transactions (id, from_wallet_id, to_wallet_id, amount, status, created_at)
VALUES 
  -- Alice -> Bob: 50 USD
  (gen_random_uuid(), 
   (SELECT id FROM wallets WHERE user_id = 1 AND currency = 'USD'),
   (SELECT id FROM wallets WHERE user_id = 2 AND currency = 'EUR'),
   50.00, 'COMPLETED', NOW()),
   
  -- Bob -> Charlie: 50 EUR
  (gen_random_uuid(),
   (SELECT id FROM wallets WHERE user_id = 2 AND currency = 'EUR'),
   (SELECT id FROM wallets WHERE user_id = 3 AND currency = 'GBP'),
   50.00, 'COMPLETED', NOW()),
   
  -- Charlie -> Alice: 50 GBP
  (gen_random_uuid(),
   (SELECT id FROM wallets WHERE user_id = 3 AND currency = 'GBP'),
   (SELECT id FROM wallets WHERE user_id = 1 AND currency = 'USD'),
   50.00, 'COMPLETED', NOW());

-- Обновляем балансы после переводов
-- Alice: 100 - 50 (отправил) + 50 (получил) = 100
UPDATE wallets SET balance = 100, updated_at = NOW() WHERE user_id = 1 AND currency = 'USD';

-- Bob: 100 + 50 (получил) - 50 (отправил) = 100
UPDATE wallets SET balance = 100, updated_at = NOW() WHERE user_id = 2 AND currency = 'EUR';

-- Charlie: 100 + 50 (получил) - 50 (отправил) = 100
UPDATE wallets SET balance = 100, updated_at = NOW() WHERE user_id = 3 AND currency = 'GBP';

-- Показываем итоговые балансы
SELECT 
  w.id as "Wallet ID",
  w.user_id as "User ID",
  CASE w.user_id
    WHEN 1 THEN 'Alice'
    WHEN 2 THEN 'Bob'
    WHEN 3 THEN 'Charlie'
  END as "Name",
  w.currency as "Currency",
  w.balance as "Balance"
FROM wallets w
ORDER BY w.user_id;

-- Показываем транзакции
SELECT 
  t.id as "Transaction ID",
  fw.user_id as "From User",
  tw.user_id as "To User",
  fw.currency as "Currency",
  t.amount as "Amount",
  t.status as "Status",
  t.created_at as "Created"
FROM transactions t
JOIN wallets fw ON t.from_wallet_id = fw.id
JOIN wallets tw ON t.to_wallet_id = tw.id
ORDER BY t.created_at;
