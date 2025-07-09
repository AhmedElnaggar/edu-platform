# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS builder

# 1. Install parent POM
WORKDIR /root
COPY pom.xml .
RUN mvn install -N -f pom.xml

# 2. Build api-gateway
WORKDIR /workspace
COPY api-gateway/pom.xml .
COPY api-gateway/src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN adduser --system --group appuser && \
    chown appuser:appuser /app
USER appuser
COPY --from=builder /workspace/target/api-gateway.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]