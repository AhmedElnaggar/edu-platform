# Edu-Platform (Microservices)

A Spring Boot Cloud-based microservices project for education management.

## **Services**
| Service          | Port  | Description                          |
|------------------|-------|--------------------------------------|
| **Auth**         | 8081  | JWT authentication                   |
| **User**         | 8082  | User profile management              |
| **Course**       | 8083  | Course enrollment (MongoDB + Kafka)  |
| **API Gateway**  | 8080  | Routes requests to services          |

## **Infrastructure**
- **Service Discovery**: Consul
- **Logging**: ELK Stack
- **Monitoring**: Prometheus + Grafana

## **How to Run**
```bash
# Clone the repo
git clone https://github.com/your-username/edu-platform.git

# Start services (Docker)
docker-compose up -d