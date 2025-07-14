#!/bin/bash

echo "🧪 Testing Auth Service..."

# Wait for service to start
sleep 10

BASE_URL="http://localhost:8081"

echo "1. Testing Health Check..."
curl -X GET $BASE_URL/actuator/health

echo -e "\n\n2. Testing User Registration..."
curl -X POST $BASE_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

echo -e "\n\n3. Testing User Login..."
curl -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

echo -e "\n\n4. Testing Login with Default Admin..."
curl -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!"
  }'

echo -e "\n\n✅ Auth Service testing complete!"