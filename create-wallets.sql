-- Создаём кошельки для пользователей Alice, Bob, Charlie
INSERT INTO wallets (id, user_id, currency, balance, created_at, updated_at)
VALUES 
  (gen_random_uuid(), '3f672c1a-71c4-424d-b650-eaf9ecbdba89', 'USD', 100, NOW(), NOW()),
  (gen_random_uuid(), '9817ce67-4060-4ed2-a1bd-9421b6649acf', 'EUR', 100, NOW(), NOW()),
  (gen_random_uuid(), 'd1e91951-7281-43c6-a8c1-2dcdb4c6e99a', 'GBP', 100, NOW(), NOW());

-- Показываем все кошельки
SELECT w.id, w.user_id, u.name, u.email, w.currency, w.balance 
FROM wallets w 
JOIN users u ON w.user_id = u.id
ORDER BY w.currency;
