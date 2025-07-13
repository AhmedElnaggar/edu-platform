# Enhanced Edu-Platform Microservices Architecture

A comprehensive education management platform built with Spring Boot microservices, featuring advanced observability, security, and resilience patterns.

## 🏗️ Architecture Overview

### Services
- **API Gateway** (Port 8080) - Centralized routing, rate limiting, and security
- **Auth Service** (Port 8081) - Authentication with JWT and OAuth2
- **User Service** (Port 8082) - User profile management with caching
- **Course Service** (Port 8083) - Course management with event sourcing
- **Notification Service** (Port 8084) - Email/SMS notifications
- **Analytics Service** (Port 8085) - Business intelligence and metrics

### Infrastructure
- **Service Discovery**: Consul
- **Databases**: PostgreSQL (Auth, User), MongoDB (Course)
- **Caching**: Redis
- **Message Broker**: Apache Kafka
- **Security**: Keycloak (OAuth2/OIDC)
- **Monitoring**: Prometheus + Grafana + Jaeger
- **Logging**: ELK Stack

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven 3.8+

### Setup
```bash
# Clone the repository
git clone <your-repo-url>
cd edu-platform

# Build all services
./scripts/build-all.sh

# Start infrastructure
docker-compose up -d auth-db user-db course-db redis kafka zookeeper consul keycloak

# Start services
docker-compose up -d

# Check health
curl http://localhost:8080/actuator/health
```

## 📊 Monitoring & Observability

- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Jaeger**: http://localhost:16686
- **Consul**: http://localhost:8500
- **Keycloak**: http://localhost:8090 (admin/admin)

## 🔧 Development

### Running Locally
```bash
# Start databases only
docker-compose up -d auth-db user-db course-db redis kafka zookeeper

# Run services with IDE or:
cd services/auth-service && mvn spring-boot:run
cd services/user-service && mvn spring-boot:run
cd services/course-service && mvn spring-boot:run
cd services/api-gateway && mvn spring-boot:run
```

### Testing
```bash
# Run all tests
mvn test

# Run integration tests
mvn verify -P integration-tests
```

## 📈 Implementation Roadmap

### ✅ High Priority (Completed)
- [x] Database per service pattern
- [x] Circuit breaker implementation
- [x] Health checks & monitoring
- [x] Security with Keycloak

### 🚧 Medium Priority (In Progress)
- [ ] Enhanced API Gateway
- [ ] Caching implementation
- [ ] Distributed tracing
- [ ] Notification service

### 📋 Low Priority (Planned)
- [ ] Event sourcing
- [ ] Advanced analytics
- [ ] Service mesh
- [ ] Machine learning recommendations

## 🔒 Security

- OAuth2/OIDC with Keycloak
- JWT token validation
- Role-based access control
- API rate limiting
- CORS configuration

## 📚 Documentation

- [Service Documentation](./docs/services.md)
- [API Documentation](./docs/api.md)
- [Deployment Guide](./docs/deployment.md)
- [Monitoring Guide](./docs/monitoring.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
