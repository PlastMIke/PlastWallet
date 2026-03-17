#!/bin/bash

BASE_URL="http://localhost:8080"

echo "Login as Alice..."
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

echo "$RESP"

TOKEN=$(echo "$RESP" | jq -r '.data.token')
echo -e "\n\nToken: $TOKEN"

echo -e "\n\nGetting wallet..."
WALLET=$(curl -s -w "\n\nHTTP Status: %{http_code}" "$BASE_URL/api/v1/wallets/user/1" \
  -H "Authorization: Bearer $TOKEN")

echo "Wallet API Response:"
echo "$WALLET"
