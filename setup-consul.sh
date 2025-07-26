#!/bin/bash

# Consul Configuration Setup Script for Edu-Platform
# This script loads all configurations into Consul KV store

set -e

CONSUL_HOST=${CONSUL_HOST:-localhost}
CONSUL_PORT=${CONSUL_PORT:-8500}
CONSUL_URL="http://${CONSUL_HOST}:${CONSUL_PORT}"
CONFIG_DIR="./configs"

echo "🚀 Starting Consul configuration setup..."

# Check if Consul is running
if ! curl -s "${CONSUL_URL}/v1/status/leader" > /dev/null; then
    echo "❌ Consul is not running at ${CONSUL_URL}"
    echo "Please start Consul first: consul agent -dev -ui"
    exit 1
fi

echo "✅ Consul is running at ${CONSUL_URL}"

# Create config directory if it doesn't exist
mkdir -p ${CONFIG_DIR}

# Function to create configuration file
create_config_file() {
    local filename=$1
    local content=$2

    echo "Creating ${CONFIG_DIR}/${filename}"
    cat > "${CONFIG_DIR}/${filename}" << EOF
${content}
EOF
}

# Create global application configuration
create_config_file "application.yml" "# Global configuration for all services
logging:
  level:
    com.eduplatform: INFO
    org.springframework.cloud.consul: DEBUG
    org.springframework.cloud.gateway: DEBUG
  pattern:
    console: \"%d{yyyy-MM-dd HH:mm:ss} - %msg%n\"
    file: \"%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n\"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,refresh
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
  metrics:
    export:
      prometheus:
        enabled: true

spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

eureka:
  client:
    enabled: false"

# Create auth service configuration
create_config_file "auth-service.yml" "# Auth Service specific configuration
jwt:
  secret: \${JWT_SECRET:mySecretKey}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
  issuer: edu-platform-auth

spring:
  datasource:
    url: jdbc:postgresql://\${DB_HOST:localhost}:\${DB_PORT:5432}/\${DB_NAME:auth_db}
    username: \${DB_USERNAME:postgres}
    password: \${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: \${DDL_AUTO:update}
    show-sql: \${SHOW_SQL:false}

security:
  cors:
    allowed-origins:
      - \"http://localhost:3000\"
      - \"http://localhost:8080\"
    allowed-methods:
      - GET
      - POST
      - PUT
      - DELETE
      - OPTIONS
    allowed-headers: \"*\"
    allow-credentials: true"

# Create user service configuration
create_config_file "user-service.yml" "# User Service specific configuration
spring:
  datasource:
    url: jdbc:postgresql://\${DB_HOST:localhost}:\${DB_PORT:5432}/\${DB_NAME:user_db}
    username: \${DB_USERNAME:postgres}
    password: \${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: \${DDL_AUTO:update}
    show-sql: \${SHOW_SQL:false}

user:
  profile:
    default-avatar: \"https://www.gravatar.com/avatar/default\"
    max-file-size: 5MB
    allowed-file-types:
      - jpg
      - jpeg
      - png
      - gif
  notification:
    email:
      enabled: true
    sms:
      enabled: false"

# Create course service configuration
create_config_file "course-service.yml" "# Course Service specific configuration
spring:
  data:
    mongodb:
      uri: mongodb://\${MONGO_HOST:localhost}:\${MONGO_PORT:27017}/\${MONGO_DB:course_db}

  kafka:
    bootstrap-servers: \${KAFKA_BROKERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false
    consumer:
      group-id: course-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: \"com.eduplatform.course.dto\"

course:
  enrollment:
    max-students-per-course: 100
    auto-approval: false
  content:
    max-file-size: 100MB
    allowed-file-types:
      - mp4
      - pdf
      - docx
      - pptx
  kafka:
    topics:
      enrollment: course-enrollment
      completion: course-completion"

# Create API Gateway configuration
create_config_file "api-gateway.yml" "# API Gateway specific configuration
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        # Auth Service Routes
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: auth-service
                fallbackUri: forward:/fallback/auth

        # User Service Routes
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: user-service
                fallbackUri: forward:/fallback/user

        # Course Service Routes
        - id: course-service
          uri: lb://course-service
          predicates:
            - Path=/api/courses/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: course-service
                fallbackUri: forward:/fallback/course

      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: \"*\"
            allowed-methods: \"*\"
            allowed-headers: \"*\"
            allow-credentials: true

resilience4j:
  circuitbreaker:
    instances:
      auth-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 30s
        failure-rate-threshold: 50
      user-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 30s
        failure-rate-threshold: 50
      course-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number