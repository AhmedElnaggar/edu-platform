#!/bin/bash
# Build all images from project root

# Build parent POM first (only needed once)
mvn install -N

# Build services
docker build -t auth-service -f auth-service/Dockerfile .
docker build -t user-service -f user-service/Dockerfile .
docker build -t course-service -f course-service/Dockerfile .
docker build -t api-gateway -f api-gateway/Dockerfile .

echo "All images built successfully!"