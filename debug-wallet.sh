#!/bin/bash

BASE_URL="http://localhost:8080"

# Login
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

echo "Login response:"
echo "$RESP"

TOKEN=$(echo "$RESP" | jq -r '.data.token')
echo -e "\n\nToken: $TOKEN"

# Get wallet
echo -e "\n\nGetting wallet..."
WALLET_RESP=$(curl -s "$BASE_URL/api/v1/wallets/user/1" \
  -H "Authorization: Bearer $TOKEN")

echo "Wallet API response:"
echo "$WALLET_RESP"
