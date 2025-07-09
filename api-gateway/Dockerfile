# Build stage
FROM eclipse-temurin:17-jdk as builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/api-gateway.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]