FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
# Copy entire project (including parent POM and shared modules)
COPY . .
# Change to service directory and build
WORKDIR /workspace/services/auth-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN adduser --system --group appuser && chown appuser:appuser /app
USER appuser
COPY --from=builder /workspace/services/user-service/target/user-service.jar /app/
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/auth-service.jar"]



