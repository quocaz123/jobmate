# Hướng dẫn Docker cho JobMate Project

## Tổng quan

Dự án JobMate là một microservices architecture với 4 services chính:
- **api-gateway** (Port 8888): API Gateway sử dụng Spring Cloud Gateway
- **jobmate-connect** (Port 8080): Service chính với PostgreSQL, Redis, Kafka, AWS S3
- **chat-service** (Port 8082): Service chat với MongoDB
- **notification-service** (Port 8081): Service thông báo với Redis và Kafka

## Yêu cầu

- Docker Desktop hoặc Docker Engine 20.10+
- Docker Compose 2.0+
- Tối thiểu 4GB RAM trống
- Tối thiểu 10GB dung lượng ổ đĩa

## Cấu trúc Docker

```
.
├── docker-compose.yml          # File orchestration chính
├── api-gateway/
│   ├── Dockerfile              # Dockerfile cho api-gateway
│   └── .dockerignore
├── jobmate-connect/
│   ├── Dockerfile              # Dockerfile cho jobmate-connect
│   └── .dockerignore
├── Chat-Service/
│   ├── Dockerfile              # Dockerfile cho chat-service
│   └── .dockerignore
└── Notification-Service/
    ├── Dockerfile              # Dockerfile cho notification-service
    └── .dockerignore
```

## Cách sử dụng

### 1. Build và chạy toàn bộ dự án

```bash
# Build và start tất cả services
docker-compose up --build

# Chạy ở background
docker-compose up -d --build

# Xem logs
docker-compose logs -f

# Xem logs của một service cụ thể
docker-compose logs -f jobmate-connect
```

### 2. Build từng service riêng lẻ

```bash
# Build api-gateway
cd api-gateway
docker build -t api-gateway:latest .

# Build jobmate-connect
cd jobmate-connect
docker build -t jobmate-connect:latest .

# Build chat-service
cd Chat-Service
docker build -t chat-service:latest .

# Build notification-service
cd Notification-Service
docker build -t notification-service:latest .
```

### 3. Quản lý services

```bash
# Dừng tất cả services
docker-compose down

# Dừng và xóa volumes (xóa dữ liệu)
docker-compose down -v

# Restart một service cụ thể
docker-compose restart jobmate-connect

# Xem trạng thái services
docker-compose ps

# Xem health status
docker-compose ps --format "table {{.Name}}\t{{.Status}}"
```

### 4. Truy cập services

Sau khi start, các services sẽ chạy tại:

- **API Gateway**: http://localhost:8888
- **JobMate Connect**: http://localhost:8080/jobmate
- **Chat Service**: http://localhost:8082/chat
- **Notification Service**: http://localhost:8081/notification

### 5. Truy cập databases

- **PostgreSQL**: 
  - Host: localhost
  - Port: 5432
  - Database: jobmate_db
  - Username: postgres
  - Password: 1234

- **MongoDB**:
  - Host: localhost
  - Port: 27017
  - Username: root
  - Password: root

- **Redis**:
  - Host: localhost
  - Port: 6379

- **Kafka**:
  - Bootstrap Server: localhost:9094

## Cấu hình Environment Variables

Các environment variables có thể được override trong `docker-compose.yml` hoặc thông qua file `.env`:

```env
# Database
POSTGRES_DB=jobmate_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=1234

# MongoDB
MONGO_ROOT_USERNAME=root
MONGO_ROOT_PASSWORD=root

# Redis (no password by default)
# Kafka (no authentication by default)
```

## Troubleshooting

### 1. Port đã được sử dụng

Nếu gặp lỗi port đã được sử dụng, bạn có thể:
- Thay đổi port mapping trong `docker-compose.yml`
- Dừng service đang sử dụng port đó

### 2. Services không kết nối được với nhau

Đảm bảo tất cả services đều trong cùng network `jobmate-network`. Kiểm tra bằng:

```bash
docker network inspect jobmate_jobmate-network
```

### 3. Database connection errors

Đảm bảo database services đã healthy trước khi start application services:

```bash
docker-compose ps
```

Nếu database chưa healthy, chờ thêm vài giây hoặc restart:

```bash
docker-compose restart postgres mongodb redis
```

### 4. Build errors

Nếu gặp lỗi build, thử:

```bash
# Clean build (không dùng cache)
docker-compose build --no-cache

# Xóa images cũ
docker-compose down --rmi all
```

### 5. Out of memory

Nếu gặp lỗi out of memory:
- Tăng RAM cho Docker Desktop (Settings > Resources > Memory)
- Hoặc giảm số lượng services chạy cùng lúc

## Production Considerations

⚠️ **Lưu ý**: Cấu hình hiện tại chỉ phù hợp cho development. Để deploy production, cần:

1. **Security**:
   - Thay đổi passwords mặc định
   - Sử dụng secrets management (Docker Secrets, Vault, etc.)
   - Enable SSL/TLS

2. **Performance**:
   - Tối ưu JVM options
   - Enable resource limits và reservations
   - Sử dụng production-grade database configurations

3. **Monitoring**:
   - Thêm monitoring tools (Prometheus, Grafana)
   - Enable logging aggregation
   - Setup alerting

4. **High Availability**:
   - Sử dụng multiple replicas
   - Setup load balancing
   - Configure database replication

## Cleanup

```bash
# Xóa tất cả containers, networks, và volumes
docker-compose down -v --rmi all

# Xóa tất cả images không sử dụng
docker system prune -a
```

## Liên hệ

Nếu gặp vấn đề, vui lòng kiểm tra logs:

```bash
docker-compose logs [service-name]
```

