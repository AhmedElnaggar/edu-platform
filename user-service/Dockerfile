# Build stage
FROM eclipse-temurin:17-jdk as builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/user-service.jar ./app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]