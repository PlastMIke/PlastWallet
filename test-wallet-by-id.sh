#!/bin/bash

BASE_URL="http://localhost:8080"
WALLET_ID="fc61b045-bbc2-4743-b783-d955f0dc0985"

echo "Login as Alice..."
RESP=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

TOKEN=$(echo "$RESP" | jq -r '.data.token')
echo "Token: ${TOKEN:0:80}..."

echo -e "\n\nGetting wallet by ID: $WALLET_ID..."
WALLET=$(curl -s -w "\nHTTP Status: %{http_code}" "$BASE_URL/api/v1/wallets/$WALLET_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "$WALLET"
