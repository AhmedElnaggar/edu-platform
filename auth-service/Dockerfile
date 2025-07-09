# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS builder

# 1. First install the parent POM
WORKDIR /root
COPY pom.xml .
RUN mvn install -N -f pom.xml  # -N means non-recursive

# 2. Then build the auth-service
WORKDIR /workspace
COPY auth-service/pom.xml .
COPY auth-service/src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN adduser --system --group appuser && \
    chown appuser:appuser /app
USER appuser
COPY --from=builder /workspace/target/auth-service.jar .
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "auth-service.jar"]