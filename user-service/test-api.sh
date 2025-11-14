#!/bin/bash

# User Service API Testing Script
# Make sure the service is running on http://localhost:8081

BASE_URL="http://localhost:8081/api/users"

echo "=== Testing User Service API ==="
echo ""

# Health Check
echo "1. Health Check..."
curl -s -X GET "$BASE_URL/health"
echo -e "\n"

# Register User
echo "2. Registering new user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "testpass123",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1234567890"
  }')

echo "$REGISTER_RESPONSE" | jq '.'
TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.data.token // empty')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "ERROR: Registration failed or token not received"
  exit 1
fi

echo -e "\nToken received: ${TOKEN:0:50}...\n"

# Login
echo "3. Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@example.com",
    "password": "testpass123"
  }')

echo "$LOGIN_RESPONSE" | jq '.'
NEW_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token // empty')

if [ -n "$NEW_TOKEN" ] && [ "$NEW_TOKEN" != "null" ]; then
  TOKEN=$NEW_TOKEN
  echo -e "\nNew token received: ${TOKEN:0:50}...\n"
fi

# Get Profile (Protected Endpoint)
echo "4. Getting user profile (protected endpoint)..."
curl -s -X GET "$BASE_URL/profile" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n=== Testing Complete ==="

