# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS builder

# 1. Install parent POM
WORKDIR /root
COPY pom.xml .
RUN mvn install -N -f pom.xml

# 2. Build course-service
WORKDIR /workspace
COPY course-service/pom.xml .
COPY course-service/src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN adduser --system --group appuser && \
    chown appuser:appuser /app
USER appuser
COPY --from=builder /workspace/target/course-service.jar .
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "course-service.jar"]