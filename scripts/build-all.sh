#!/bin/bash

echo "🔨 Building all microservices..."

# Build parent project
echo "Building parent project..."
mvn clean install -DskipTests

# Build each service
services=("api-gateway" "auth-service" "user-service" "course-service")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd "services/$service"
    mvn clean package -DskipTests
    cd "../.."
done

echo "✅ All services built successfully!"