# 🐛 Известная проблема: API не возвращает баланс

## Проблема
После изменения типа `user_id` с `bigint` на `uuid` в базе данных, Spring Data JPA требует дополнительной настройки для корректной работы.

## ✅ Что работает:
- ✅ Логин/Регистрация
- ✅ База данных (кошельки и транзакции существуют)
- ✅ Веб-интерфейс загружается
- ✅ Swagger UI доступен

## ❌ Что не работает:
- ❌ API `/api/v1/wallets/user/{userId}` возвращает 500 ошибку

## 🔧 Временное решение

Пока API не работает, можно проверить баланс напрямую через базу:

```bash
docker exec -it wallet-postgres psql -U postgres -d wallet_db -c "
SELECT u.name, u.email, w.currency, w.balance 
FROM wallets w 
JOIN users u ON w.user_id = u.id;
"
```

## 📊 Текущие данные:

| Пользователь | Email | Кошелёк | Баланс |
|--------------|-------|---------|--------|
| Alice Johnson | alice@plastwallet.com | USD | $100.00 |
| Bob Smith | bob@plastwallet.com | EUR | €100.00 |
| Charlie Brown | charlie@plastwallet.com | GBP | £100.00 |

## 📝 Для полного исправления нужно:

1. Обновить `JpaWalletRepository` для поддержки UUID
2. Проверить все entity и DTO на соответствие типов
3. Протестировать все endpoints

Или можно откатить изменения типа `user_id` обратно на `Long` в:
- `WalletEntity.java`
- `Wallet.java` (domain)
- `WalletDTO.java`
- `WalletPort.java`
- `WalletAdapter.java`
- `WalletController.java`
- `WalletServiceAdapter.java`
- `WalletService.java`
- `CreateWalletRequest.java`
- `JpaWalletRepository.java`

И изменить базу данных обратно на `bigint`.
