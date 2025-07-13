FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN adduser --system --group appuser && chown appuser:appuser /app
USER appuser
COPY --from=builder /workspace/target/course-service.jar /app/
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/course-service.jar"]