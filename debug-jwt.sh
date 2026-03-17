#!/bin/bash

echo "🔍 Отладка JWT и API..."

# Login
echo -e "\n1. Login..."
RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@plastwallet.com","password":"password123"}')

echo "$RESP" | jq '.message'
TOKEN=$(echo "$RESP" | jq -r '.data.token')

# Decode JWT to see claims
echo -e "\n2. JWT Claims:"
echo "$TOKEN" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq '.'

# Try wallet API
echo -e "\n3. Wallet API (user_id=1):"
WALLET_RESP=$(curl -s -w "\n%{http_code}" "http://localhost:8080/api/v1/wallets/user/1" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$WALLET_RESP" | tail -1)
BODY=$(echo "$WALLET_RESP" | head -n -1)

echo "HTTP Status: $HTTP_CODE"
echo "Body: $BODY"
