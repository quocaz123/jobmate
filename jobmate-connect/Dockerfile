# ===== STAGE 1: Build =====
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy file pom và source vào container
COPY pom.xml .
COPY src ./src

# Build jar (bỏ qua test nếu muốn nhanh)
RUN mvn clean package -DskipTests

# ===== STAGE 2: Runtime (JRE only) =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy file jar đã build sang
COPY --from=builder /app/target/*.jar app.jar

# Port mặc định của jobmate-connect (nếu khác, sửa lại ở đây)
EXPOSE 8080

# Có thể bật profile qua biến môi trường:
# ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java","-jar","/app/app.jar"]