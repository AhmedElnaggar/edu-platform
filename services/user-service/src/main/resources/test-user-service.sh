#!/bin/bash

echo "🧪 Testing User Service..."

BASE_URL="http://localhost:8082"
AUTH_URL="http://localhost:8081"

echo "1. Testing Health Check..."
curl -X GET $BASE_URL/actuator/health

echo -e "\n\n2. Getting JWT Token from Auth Service..."
TOKEN_RESPONSE=$(curl -s -X POST $AUTH_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!"
  }')

TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.accessToken')
echo "Token: $TOKEN"

echo -e "\n\n3. Testing Get Current User Profile..."
curl -X GET $BASE_URL/users/me \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n4. Testing Create User Profile..."
curl -X POST $BASE_URL/users/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "displayName": "Test User",
    "bio": "This is a test user profile",
    "location": "Test City",
    "timezone": "America/New_York",
    "language": "en"
  }'

echo -e "\n\n5. Testing Search Users..."
curl -X GET "$BASE_URL/users/search?q=Test" \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n6. Testing Get Public Profiles..."
curl -X GET $BASE_URL/users/public \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n7. Testing User Preferences..."
curl -X GET $BASE_URL/users/preferences \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n\n✅ User Service testing complete!"