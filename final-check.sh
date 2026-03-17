#!/bin/bash

echo "🧪 Финальный тест API..."

# Login
RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

echo "Login:"
echo "$RESP" | jq '.message'

TOKEN=$(echo "$RESP" | jq -r '.data.token')

# Get wallet
echo -e "\nWallet (user_id=1):"
WALLET=$(curl -s "http://localhost:8080/api/v1/wallets/user/1" \
  -H "Authorization: Bearer $TOKEN")

echo "$WALLET" | jq '.data'

# Get transactions
WALLET_ID=$(echo "$WALLET" | jq -r '.data.id')
echo -e "\nTransactions (wallet_id=$WALLET_ID):"
TXS=$(curl -s "http://localhost:8080/api/v1/wallets/$WALLET_ID/transactions" \
  -H "Authorization: Bearer $TOKEN")

echo "$TXS" | jq '.data'

echo -e "\n✅ Тест завершён!"
