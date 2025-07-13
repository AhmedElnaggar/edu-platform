#!/bin/bash

echo "🚀 Starting Phase 1 Implementation..."

# Create databases directory if not exists
mkdir -p databases/{auth-db,user-db,course-db}

# Start infrastructure services
echo "📦 Starting infrastructure services..."
docker-compose up -d auth-db user-db course-db redis consul

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 30

# Build all services
echo "🔨 Building all services..."
mvn clean install -DskipTests

# Start microservices
echo "🎯 Starting microservices..."
docker-compose up -d api-gateway auth-service user-service course-service

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 60

# Health check
echo "🏥 Performing health checks..."
curl -f http://localhost:8080/actuator/health || echo "❌ API Gateway health check failed"
curl -f http://localhost:8081/actuator/health || echo "❌ Auth Service health check failed"
curl -f http://localhost:8082/actuator/health || echo "❌ User Service health check failed"
curl -f http://localhost:8083/actuator/health || echo "❌ Course Service health check failed"

echo "✅ Phase 1 startup complete!"
echo "📍 Services available at:"
echo "   - API Gateway: http://localhost:8080"
echo "   - Auth Service: http://localhost:8081"
echo "   - User Service: http://localhost:8082"
echo "   - Course Service: http://localhost:8083"
echo "   - Consul UI: http://localhost:8500"