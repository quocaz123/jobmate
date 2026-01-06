# 📘 JobMate Connect - Mô Tả Dự Án

## 🎯 Tổng Quan Dự Án

**JobMate Connect** là một nền tảng kết nối việc làm thông minh, sử dụng công nghệ AI và thuật toán matching để kết nối người tìm việc với nhà tuyển dụng một cách hiệu quả. Dự án được xây dựng theo kiến trúc **Microservices** với Spring Boot, cung cấp các tính năng từ quản lý công việc, ứng tuyển, đến hệ thống gợi ý thông minh.

---

## 🏗️ Kiến Trúc Hệ Thống

### **Microservices Architecture**

Dự án được chia thành các service độc lập:

1. **jobmate-connect** (Main Service)
   - Service chính xử lý business logic
   - Quản lý Job, Application, User, Matching
   - Port: 8080

2. **api-gateway**
   - API Gateway tập trung
   - Xử lý authentication và routing
   - Port: 8081

3. **Chat-Service**
   - Service chat giữa ứng viên và nhà tuyển dụng
   - Port: 8082

4. **Notification-Service**
   - Service gửi thông báo (Email, Push)
   - Port: 8083

---

## 🚀 Công Nghệ Sử Dụng

### **Backend Framework**
- **Spring Boot 3.5.6**
- **Java 21**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **Spring Data Elasticsearch**

### **Database & Storage**
- **PostgreSQL**: Database chính
- **Elasticsearch**: Full-text search và indexing
- **Redis**: Caching
- **AWS S3**: File storage (CV, Resume, Images)

### **Message Queue & Event Streaming**
- **Apache Kafka**: Event-driven architecture
- **Spring Kafka**: Integration

### **Other Technologies**
- **MapStruct**: Object mapping
- **Lombok**: Code generation
- **OpenAPI/Swagger**: API documentation
- **Docker**: Containerization

---

## ✨ Tính Năng Chính

### **1. Quản Lý Công Việc (Job Management)**

#### **Cho Nhà Tuyển Dụng (Employer)**
- ✅ **Tạo công việc mới**: Đăng tin tuyển dụng với đầy đủ thông tin
  - Tiêu đề, mô tả, yêu cầu, quyền lợi
  - Địa điểm, tọa độ GPS
  - Mức lương, đơn vị lương (giờ/ngày/tháng/ca/dự án...)
  - Loại công việc (Full-time/Part-time)
  - Kỹ năng yêu cầu
  - Lịch làm việc (ngày, giờ)
  - Số lượng ứng viên cần tuyển

- ✅ **Quản lý công việc đã đăng**
  - Xem danh sách công việc của mình
  - Sắp xếp theo công việc có applicant mới nhất
  - Cập nhật, chỉnh sửa công việc
  - Đóng công việc khi đủ số lượng ứng viên đã chấp nhận
  - Xóa công việc

- ✅ **Tìm kiếm công việc gần đây**: Tìm công việc trong bán kính nhất định

#### **Cho Người Tìm Việc (User)**
- ✅ **Tìm kiếm công việc có sẵn**
  - Tìm kiếm theo từ khóa, địa điểm
  - Lọc theo loại công việc, mức lương
  - Lọc theo danh mục, chế độ làm việc
  - Xem chi tiết công việc

#### **Cho Admin**
- ✅ **Duyệt công việc**: Phê duyệt hoặc từ chối công việc
- ✅ **Xem tất cả công việc**: Quản lý toàn bộ công việc trong hệ thống

**API Endpoints:**
- `POST /jobs` - Tạo công việc
- `GET /jobs/available` - Tìm kiếm công việc
- `GET /jobs/my-jobs` - Công việc của tôi
- `GET /jobs/nearby` - Công việc gần đây
- `GET /jobs/{jobId}` - Chi tiết công việc
- `PUT /jobs/{jobId}` - Cập nhật công việc
- `PUT /jobs/{jobId}/verify-job` - Duyệt công việc
- `PUT /jobs/{id}/close` - Đóng công việc
- `PUT /jobs/{id}/delete` - Xóa công việc

---

### **2. Hệ Thống Matching Thông Minh (Intelligent Matching)**

#### **Gợi ý Công Việc cho Người Tìm Việc**
- ✅ **Matching dựa trên nhiều tiêu chí**:
  - **Kỹ năng (50 điểm)**: So sánh skills với synonym matching và fuzzy matching
  - **Lương (20 điểm)**: So sánh lương đã normalize về VND/giờ
  - **Khoảng cách (20 điểm)**: Tính bằng Haversine formula
  - **Lịch làm việc (10 điểm)**: Kiểm tra overlap thời gian

- ✅ **Elasticsearch Search**: Tìm kiếm nhanh với fuzzy search
- ✅ **Sắp xếp theo điểm matching**: Top 20 công việc phù hợp nhất

#### **Gợi ý Ứng Viên cho Nhà Tuyển Dụng**
- ✅ **Matching ngược lại**: Tìm ứng viên phù hợp với công việc
- ✅ **Tính điểm tương tự**: Dựa trên skills, salary, distance, schedule

**API Endpoints:**
- `GET /recommend/jobs?waitingListId={uuid}` - Gợi ý công việc
- `GET /recommend/users?jobId={uuid}` - Gợi ý ứng viên

**Tài liệu chi tiết**: Xem `MATCHING_SYSTEM_DOCUMENTATION.md`

---

### **3. Quản Lý Ứng Tuyển (Application Management)**

#### **Cho Ứng Viên**
- ✅ **Nộp đơn ứng tuyển**
  - Upload CV/Resume
  - Viết cover letter
  - Sử dụng CV từ profile
  - Tính điểm matching tự động

- ✅ **Xem đơn ứng tuyển của mình**
  - Danh sách đơn đã nộp
  - Trạng thái: PENDING, ACCEPTED, REJECTED, CANCELLED
  - Điểm matching với công việc

- ✅ **Hủy đơn ứng tuyển**: Hủy đơn đang chờ xử lý

#### **Cho Nhà Tuyển Dụng**
- ✅ **Xem danh sách ứng viên**
  - Xem tất cả đơn ứng tuyển cho công việc
  - Lọc theo trạng thái
  - Xem điểm matching

- ✅ **Xử lý đơn ứng tuyển**
  - Chấp nhận ứng viên (ACCEPTED)
  - Từ chối ứng viên (REJECTED) với lý do
  - Hủy đơn (CANCELLED)

- ✅ **Tự động đóng công việc**: Khi đủ số lượng ứng viên đã chấp nhận

**API Endpoints:**
- `POST /applications/apply` - Nộp đơn
- `GET /applications/my-applications` - Đơn của tôi
- `GET /applications/job/{jobId}` - Đơn của một job
- `GET /applications/{id}` - Chi tiết đơn
- `PUT /applications/{applicationId}/status` - Cập nhật trạng thái
- `DELETE /applications/{applicationId}` - Hủy đơn

---

### **4. Mời Ứng Viên (Job Invitation)**

- ✅ **Nhà tuyển dụng mời ứng viên**
  - Gửi lời mời trực tiếp cho ứng viên
  - Ứng viên có thể chấp nhận hoặc từ chối

- ✅ **Quản lý lời mời**
  - Xem lời mời đã gửi
  - Xem lời mời đã nhận
  - Tự động expire lời mời khi job đóng

**API Endpoints:**
- `POST /invitations` - Gửi lời mời
- `POST /invitations/{id}/accept` - Chấp nhận
- `POST /invitations/{id}/reject` - Từ chối
- `GET /invitations/received` - Lời mời đã nhận
- `GET /invitations/sent` - Lời mời đã gửi

---

### **5. Danh Sách Mong Muốn (Waiting List)**

- ✅ **Tạo waiting list**
  - Lưu preferences của user (skills, salary, location, schedule)
  - Sử dụng để gợi ý công việc phù hợp

- ✅ **Quản lý waiting list**
  - Tạo, cập nhật, xóa
  - Nhiều waiting list cho các mục đích khác nhau

**API Endpoints:**
- `POST /waiting-lists` - Tạo waiting list
- `GET /waiting-lists` - Danh sách waiting list
- `GET /waiting-lists/{id}` - Chi tiết
- `PUT /waiting-lists/{id}` - Cập nhật
- `DELETE /waiting-lists/{id}` - Xóa

---

### **6. Quản Lý Người Dùng (User Management)**

#### **Đăng Ký & Xác Thực**
- ✅ **Đăng ký tài khoản**
- ✅ **Đăng nhập với Google OAuth2**
- ✅ **Xác thực 2FA (OTP)**
- ✅ **JWT Token Authentication**
- ✅ **Refresh Token**
- ✅ **Logout & Token Invalidation**

#### **Quản Lý Profile**
- ✅ **Cập nhật thông tin cá nhân**
  - Họ tên, email, số điện thoại
  - Địa chỉ, tọa độ GPS
  - Kỹ năng, kinh nghiệm
  - Mức lương mong muốn
  - Thời gian rảnh
  - Loại công việc ưa thích

- ✅ **Upload CV/Resume**
  - Lưu trữ trên AWS S3
  - Sử dụng khi nộp đơn

- ✅ **Xác minh tài khoản**
  - User có thể yêu cầu xác minh
  - Admin duyệt xác minh

#### **Quản Lý Tài Khoản**
- ✅ **Banned User**: Chặn user vi phạm
  - Filter kiểm tra banned status mỗi request
  - Token vẫn hợp lệ nhưng bị chặn truy cập

- ✅ **Trust Score**: Điểm uy tín dựa trên đánh giá

**API Endpoints:**
- `POST /users/registration` - Đăng ký
- `POST /auth/login` - Đăng nhập
- `POST /auth/outbound` - Google OAuth2
- `POST /auth/verify-otp` - Xác thực OTP
- `GET /users/profile` - Xem profile
- `PUT /users/profile` - Cập nhật profile
- `POST /users/upload-resume` - Upload CV

---

### **7. Đánh Giá & Xếp Hạng (Rating & Review)**

- ✅ **Đánh giá nhà tuyển dụng**
  - Ứng viên đánh giá sau khi làm việc
  - Điểm từ 1-5 sao
  - Nhận xét

- ✅ **Xem đánh giá**
  - Xem điểm trung bình
  - Xem số lượng đánh giá
  - Hiển thị trên job của nhà tuyển dụng

**API Endpoints:**
- `POST /ratings` - Tạo đánh giá
- `GET /ratings/user/{userId}` - Đánh giá của user

---

### **8. Báo Cáo & Kiểm Duyệt (Report & Moderation)**

- ✅ **Báo cáo vi phạm**
  - User báo cáo job hoặc user khác
  - Admin xem xét và xử lý

- ✅ **Tự động đóng job**
  - Khi có nhiều báo cáo hợp lệ
  - Tính điểm trọng số dựa trên trust score

- ✅ **Quản lý vi phạm**
  - Tăng violation count
  - Tự động ban user nếu vượt ngưỡng

**API Endpoints:**
- `POST /reports` - Tạo báo cáo
- `GET /reports` - Danh sách báo cáo (Admin)
- `PUT /reports/{id}/review` - Duyệt báo cáo

---

### **9. Thông Báo (Notification)**

- ✅ **Thông báo hệ thống**
  - Thông báo khi có đơn ứng tuyển mới
  - Thông báo khi job được duyệt/từ chối
  - Thông báo khi đơn được cập nhật trạng thái
  - Thông báo khi nhận lời mời

- ✅ **Gửi email**
  - Email khi có đơn ứng tuyển mới
  - Email khi trạng thái đơn thay đổi
  - Email thông báo user bị banned

**API Endpoints:**
- `GET /notifications` - Danh sách thông báo
- `PUT /notifications/{id}/read` - Đánh dấu đã đọc

---

### **10. Dashboard & Thống Kê**

#### **Dashboard Nhà Tuyển Dụng**
- ✅ **Thống kê công việc**
  - Số lượng job đã đăng
  - Số lượng đơn ứng tuyển
  - Số lượng đơn theo trạng thái
  - Top job có nhiều ứng viên nhất

- ✅ **Thống kê ứng viên**
  - Số lượng ứng viên mới
  - Tỷ lệ chấp nhận/từ chối

#### **Dashboard Admin**
- ✅ **Thống kê tổng quan**
  - Tổng số user, job, application
  - Số lượng job chờ duyệt
  - Số lượng báo cáo chờ xử lý

**API Endpoints:**
- `GET /employer/dashboard` - Dashboard nhà tuyển dụng
- `GET /admin/dashboard` - Dashboard admin

---

### **11. Quản Lý File (File Management)**

- ✅ **Upload file**
  - CV/Resume (PDF, DOC, DOCX)
  - Ảnh avatar
  - Lưu trữ trên AWS S3

- ✅ **Download file**
  - Tải CV của ứng viên
  - Tải ảnh

**API Endpoints:**
- `POST /files/upload` - Upload file
- `GET /files/{fileId}` - Download file

---

### **12. Quản Lý Danh Mục (Category Management)**

- ✅ **Danh mục công việc**
  - Tạo, cập nhật, xóa danh mục
  - Phân loại công việc theo ngành nghề

**API Endpoints:**
- `GET /categories` - Danh sách danh mục
- `POST /categories` - Tạo danh mục (Admin)
- `PUT /categories/{id}` - Cập nhật (Admin)
- `DELETE /categories/{id}` - Xóa (Admin)

---

### **13. Audit Log (Nhật Ký Hoạt Động)**

- ✅ **Ghi lại mọi hoạt động**
  - Tạo, cập nhật, xóa job
  - Nộp đơn, cập nhật trạng thái
  - Thay đổi trạng thái user
  - Hành động của admin

- ✅ **Xem lịch sử hoạt động**
  - Admin xem audit log
  - Tìm kiếm, lọc theo user, action

**API Endpoints:**
- `GET /audit-logs` - Danh sách audit log (Admin)

---

### **14. Chat (Real-time Messaging)**

- ✅ **Chat giữa ứng viên và nhà tuyển dụng**
  - Tạo conversation
  - Gửi tin nhắn real-time
  - Xem lịch sử chat

**Service**: Chat-Service (Microservice riêng)

---

## 🔐 Bảo Mật & Phân Quyền

### **Authentication**
- ✅ **JWT Token**: Stateless authentication
- ✅ **Google OAuth2**: Đăng nhập bằng Google
- ✅ **2FA (OTP)**: Xác thực 2 bước
- ✅ **Token Refresh**: Làm mới token
- ✅ **Token Invalidation**: Vô hiệu hóa token khi logout

### **Authorization**
- ✅ **Role-Based Access Control (RBAC)**
  - **USER**: Người tìm việc
  - **EMPLOYER**: Nhà tuyển dụng
  - **ADMIN**: Quản trị viên

- ✅ **Method Security**: `@PreAuthorize` annotation
- ✅ **Banned User Filter**: Kiểm tra banned status mỗi request

### **Security Features**
- ✅ **User Status Filter**: Chặn user bị banned ngay cả khi token còn hợp lệ
- ✅ **Input Validation**: Validate request với Bean Validation
- ✅ **SQL Injection Prevention**: JPA parameterized queries
- ✅ **XSS Prevention**: Input sanitization

---

## 📊 Cơ Sở Dữ Liệu

### **PostgreSQL Tables**
- `users`: Thông tin người dùng
- `jobs`: Công việc
- `applications`: Đơn ứng tuyển
- `job_invitations`: Lời mời công việc
- `waiting_lists`: Danh sách mong muốn
- `ratings`: Đánh giá
- `reports`: Báo cáo
- `notifications`: Thông báo
- `categories`: Danh mục
- `audit_logs`: Nhật ký hoạt động
- `files`: Thông tin file
- `roles`: Vai trò người dùng

### **Elasticsearch Indexes**
- `job_es`: Index công việc để tìm kiếm nhanh
  - Full-text search trên title, description, skills
  - Filter theo status, jobType, salary
  - Fuzzy matching cho skills

---

## 🔄 Event-Driven Architecture

### **Kafka Topics**
- `application-created`: Khi có đơn ứng tuyển mới
- `application-status-updated`: Khi trạng thái đơn thay đổi
- `user-status-changed`: Khi user bị banned/unbanned
- `job-status-changed`: Khi job thay đổi trạng thái

### **Event Consumers**
- **Notification-Service**: Lắng nghe events để gửi email/notification
- **Chat-Service**: Lắng nghe events để tạo conversation

---

## 🎯 Điểm Nổi Bật

### **1. Hệ Thống Matching Thông Minh**
- Sử dụng AI và thuật toán phức tạp
- Matching 2 chiều: Job → User và User → Job
- Tính điểm dựa trên 4 tiêu chí: Skills, Salary, Distance, Schedule
- Elasticsearch cho tìm kiếm nhanh
- Synonym matching và fuzzy matching

### **2. Tự Động Hóa**
- Tự động đóng job khi đủ số lượng ứng viên đã chấp nhận
- Tự động đóng job khi có nhiều báo cáo
- Tự động ban user khi vượt ngưỡng vi phạm
- Event-driven với Kafka

### **3. Bảo Mật Cao**
- JWT authentication
- Role-based access control
- Banned user filter
- Input validation

### **4. Kiến Trúc Hiện Đại**
- Microservices architecture
- Event-driven với Kafka
- Elasticsearch cho search
- Redis caching
- AWS S3 storage

---

## 📈 Tính Năng Đang Phát Triển

- [ ] Real-time notifications (WebSocket)
- [ ] Advanced analytics dashboard
- [ ] Machine learning cho matching tốt hơn
- [ ] Video interview integration
- [ ] Mobile app (React Native)

---

## 🛠️ Cách Chạy Dự Án

### **Yêu Cầu**
- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Elasticsearch 8.x
- Redis 7.x
- Kafka 3.x
- AWS S3 (hoặc MinIO cho local)

### **Cấu Hình**
1. Cấu hình database trong `application.yaml`
2. Cấu hình Elasticsearch
3. Cấu hình Redis
4. Cấu hình Kafka
5. Cấu hình AWS S3 credentials

### **Chạy với Docker**
```bash
docker-compose up -d
```

---

## 📚 Tài Liệu

- **Matching System**: `MATCHING_SYSTEM_DOCUMENTATION.md`
- **API Documentation**: Swagger UI tại `/swagger-ui.html`
- **Project Description**: File này

---

## 👥 Vai Trò Người Dùng

### **USER (Người Tìm Việc)**
- Tìm kiếm công việc
- Nộp đơn ứng tuyển
- Xem gợi ý công việc
- Quản lý profile
- Xem đánh giá nhà tuyển dụng

### **EMPLOYER (Nhà Tuyển Dụng)**
- Đăng công việc
- Xem và xử lý đơn ứng tuyển
- Mời ứng viên
- Xem gợi ý ứng viên
- Dashboard thống kê

### **ADMIN (Quản Trị Viên)**
- Duyệt công việc
- Duyệt xác minh user
- Xử lý báo cáo
- Quản lý danh mục
- Xem audit log
- Dashboard tổng quan

---

## 🎓 Công Nghệ & Kỹ Thuật Sử Dụng

Xem chi tiết trong `MATCHING_SYSTEM_DOCUMENTATION.md` phần "Kỹ Thuật & Phương Pháp Được Sử Dụng"

---

**Dự án được phát triển bởi: Quokka Team**  
**Phiên bản: 0.0.1-SNAPSHOT**  
**Cập nhật: 2024**




