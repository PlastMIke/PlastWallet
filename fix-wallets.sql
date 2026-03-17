-- Исправление user_id в кошельках (замена числовых ID на UUID)

-- Обновляем user_id для кошельков
UPDATE wallets SET user_id = (SELECT id FROM users WHERE email = 'alice@plastwallet.com') WHERE user_id = 1 AND currency = 'USD';
UPDATE wallets SET user_id = (SELECT id FROM users WHERE email = 'bob@plastwallet.com') WHERE user_id = 2 AND currency = 'EUR';
UPDATE wallets SET user_id = (SELECT id FROM users WHERE email = 'charlie@plastwallet.com') WHERE user_id = 3 AND currency = 'GBP';

-- Проверяем результат
SELECT 
  w.id as "Wallet ID",
  u.name as "Name",
  u.email as "Email",
  w.currency as "Currency",
  w.balance as "Balance"
FROM wallets w
JOIN users u ON w.user_id = u.id
ORDER BY u.name;

-- Проверяем транзакции
SELECT 
  t.id as "Transaction ID",
  fu.name as "From",
  tu.name as "To",
  fw.currency as "Currency",
  t.amount as "Amount",
  t.status as "Status"
FROM transactions t
JOIN wallets fw ON t.from_wallet_id = fw.id
JOIN wallets tw ON t.to_wallet_id = tw.id
JOIN users fu ON fw.user_id = fu.id
JOIN users tu ON tw.user_id = tu.id
ORDER BY t.created_at;
