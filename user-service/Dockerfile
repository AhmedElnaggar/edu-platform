# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS builder

# 1. Install parent POM
WORKDIR /root
COPY pom.xml .
RUN mvn install -N -f pom.xml

# 2. Build user-service
WORKDIR /workspace
COPY user-service/pom.xml .
COPY user-service/src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN adduser --system --group appuser && \
    chown appuser:appuser /app
USER appuser
COPY --from=builder /workspace/target/user-service.jar .
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "user-service.jar"]