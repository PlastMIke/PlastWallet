#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================="
echo "🧪 Проверка API PlastWallet"
echo "========================================="

# Alice login
echo -e "\n🔐 Login: Alice..."
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

echo "Response:"
echo "$RESP" | jq .

TOKEN=$(echo "$RESP" | jq -r '.data.token')

echo -e "\n💳 Getting wallet..."
WALLET=$(curl -s "$BASE_URL/api/v1/wallets/user/1" \
  -H "Authorization: Bearer $TOKEN")

echo "Wallet Response:"
echo "$WALLET" | jq .

echo -e "\n📜 Getting transactions..."
TXS=$(curl -s "$BASE_URL/api/v1/wallets/$(echo $WALLET | jq -r '.data.id')/transactions" \
  -H "Authorization: Bearer $TOKEN")

echo "Transactions Response:"
echo "$TXS" | jq .

echo -e "\n========================================="
echo "✅ Проверка завершена!"
echo "========================================="
