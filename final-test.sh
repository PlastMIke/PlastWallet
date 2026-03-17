#!/bin/bash

echo "🧪 Тест API с UUID..."

# Login
RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

echo "Login response:"
echo "$RESP" | jq .

TOKEN=$(echo "$RESP" | jq -r '.data.token')

# Get wallet
echo -e "\n\nGetting wallet..."
WALLET=$(curl -s "http://localhost:8080/api/v1/wallets/user/3f672c1a-71c4-424d-b650-eaf9ecbdba89" \
  -H "Authorization: Bearer $TOKEN")

echo "Wallet response:"
echo "$WALLET" | jq .

# Get transactions
WALLET_ID=$(echo "$WALLET" | jq -r '.data.id')
echo -e "\n\nGetting transactions for wallet: $WALLET_ID..."
TXS=$(curl -s "http://localhost:8080/api/v1/wallets/$WALLET_ID/transactions" \
  -H "Authorization: Bearer $TOKEN")

echo "Transactions response:"
echo "$TXS" | jq .
