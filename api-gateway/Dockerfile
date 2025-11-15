# Multi-stage build cho api-gateway
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml và source code
COPY pom.xml .
COPY src ./src

# Build với retry mechanism để tránh lỗi network
RUN mvn clean package -DskipTests || \
    (sleep 5 && mvn clean package -DskipTests) || \
    (sleep 10 && mvn clean package -DskipTests)

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install wget for healthcheck
RUN apk add --no-cache wget

WORKDIR /app

# Copy JAR từ build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

