## Mô tả dự án Jobmate

Jobmate là một hệ thống tuyển dụng và kết nối việc làm được xây dựng theo kiến trúc microservice trên nền tảng Spring Boot. Dự án bao gồm các service chính:
- **jobmate-connect**: service lõi, xử lý toàn bộ nghiệp vụ người dùng, công việc, ứng tuyển, đánh giá, báo cáo, gợi ý việc làm, xác thực & phân quyền, dashboard cho admin/employer, audit log, tích hợp Elasticsearch và Kafka.
- **Chat-Service**: service chat thời gian thực giữa người dùng (ứng viên) và nhà tuyển dụng, quản lý hội thoại và tin nhắn.
- **Notification-Service**: service gửi thông báo qua email (OTP, xác nhận, thông báo trạng thái, v.v.).
- **api-gateway**: gateway đầu vào, định tuyến request đến các service phía sau và giao tiếp với dịch vụ danh tính.

---

## Technology Stack (thực tế trong dự án)

- **Frontend**
  - React (dự kiến / ở repo khác), redirect OAuth: `http://localhost:5173` (Vite/React).

- **Backend**
  - Java 21.
  - Spring Boot 3.5.6 (REST API, microservices).
  - Spring Cloud Gateway (API Gateway, WebFlux).
  - Spring Cloud OpenFeign (service-to-service call).

- **Database & Storage**
  - PostgreSQL (JPA/Hibernate) cho `jobmate-connect`.
  - MongoDB cho `Chat-Service` (lưu hội thoại, tin nhắn).
  - Redis cho cache / token / notification (dùng trong `jobmate-connect` và `Notification-Service`).
  - Elasticsearch cho search/gợi ý job.
  - AWS S3 (SDK v2) lưu file (CV, media).

- **Messaging & Realtime**
  - Apache Kafka + Zookeeper (event-driven giữa `jobmate-connect` và `Notification-Service`).
  - netty-socketio cho realtime chat (WebSocket) trong `Chat-Service`.

- **Security**
  - Spring Security.
  - OAuth2 Resource Server + JWT (access/refresh token).
  - OTP verification (kết hợp Notification-Service gửi email OTP).

- **External Services**
  - Google OAuth2 (đăng nhập bằng tài khoản Google).
  - Geocoding / Nearby jobs (sử dụng service geocoding nội bộ, có thể gọi ra API bản đồ ngoài).

- **DevOps & Tooling**
  - Docker & Docker Compose (PostgreSQL, MongoDB, Redis, Kafka, Elasticsearch).
  - Lombok, MapStruct, springdoc-openapi (Swagger UI).
  - Git/GitHub, Postman (test API), Figma (thiết kế UI/UX) – dùng bên ngoài codebase.

---

## Chức năng chính theo từng service

### 1. Service `jobmate-connect`

- **Quản lý xác thực & tài khoản (Auth & Users)**  
  - Đăng nhập, đăng xuất, refresh token, introspect token.  
  - Đăng ký tài khoản, set mật khẩu lần đầu, quên mật khẩu / đặt lại mật khẩu.  
  - Xác thực OTP (login, xác nhận email, các flow nhạy cảm) và resend OTP.  
  - Bật/tắt 2FA, đổi mật khẩu, cập nhật thông tin cá nhân, nâng cấp tài khoản thường lên employer.  
  - Lấy thông tin người dùng hiện tại (`/users/my-info`), xem user theo ID, xem danh sách user (admin).  

- **Quản lý công việc (Jobs)**  
  - Nhà tuyển dụng / admin tạo job mới, cập nhật job, đóng job, xóa job.  
  - Admin duyệt / từ chối job, cập nhật trạng thái xác minh job.  
  - Ứng viên xem danh sách job đang mở với nhiều tiêu chí lọc (keyword, location, loại job, hình thức làm việc, lương, category).  
  - Xem chi tiết job cơ bản và chi tiết mở rộng; xem danh sách job đã đăng của nhà tuyển dụng.  
  - Tìm job gần vị trí người dùng (nearby jobs – có tích hợp location/geocoding).  

- **Quản lý ứng tuyển (Applications)**  
  - Ứng viên nộp đơn ứng tuyển cho job, kèm cover letter, upload CV hoặc dùng CV trong hồ sơ.  
  - Ứng viên xem chi tiết đơn ứng tuyển và danh sách đơn ứng tuyển của chính mình.  
  - Nhà tuyển dụng / admin xem danh sách ứng viên ứng tuyển cho từng job, lọc theo trạng thái.  
  - Nhà tuyển dụng / admin cập nhật trạng thái ứng tuyển (chấp nhận, từ chối, hủy, v.v.).  
  - Ứng viên có thể tự hủy đơn ứng tuyển của mình.  

- **Gợi ý việc làm & matching (Recommendation & Matching)**  
  - Gợi ý job cho người dùng dựa trên hồ sơ / waiting list, sử dụng Elasticsearch (`JobES`) và dịch vụ matching.  
  - Gợi ý danh sách ứng viên phù hợp cho một job thông qua waiting list.  
  - Sử dụng `MatchingEngine`, `MatchingService`, `RecommendJobsService`, `SkillSynonymService` để tính điểm phù hợp và xử lý từ khóa kỹ năng đồng nghĩa.  

- **Quản lý đánh giá & báo cáo (Rating & Report)**  
  - Cho phép tạo/xem/xóa đánh giá (ví dụ đánh giá giữa employer và candidate).  
  - Quản lý báo cáo (report) vi phạm, admin review và thay đổi trạng thái báo cáo.  
  - Từ dữ liệu rating có danh sách/top người dùng được đánh giá cao.  

- **Dashboard & quản trị (Admin / Employer Dashboard)**  
  - Dashboard cho Admin: thống kê tổng quan hệ thống (user, job, ứng tuyển, report, v.v.), theo dõi health hệ thống.  
  - Dashboard cho Employer: thống kê job, ứng viên, hiệu quả tuyển dụng theo từng nhà tuyển dụng.  

- **Audit log hệ thống**  
  - Ghi lại các hành động quan trọng: tạo/cập nhật user, đổi mật khẩu, bật/tắt 2FA, xác thực user, khóa tài khoản, tạo/cập nhật/duyệt job, thao tác với application, rating, report, login/logout, refresh token, v.v.  
  - Cung cấp API cho admin xem danh sách audit log và thống kê theo hành động/thời gian/người dùng.  

- **Waiting list & notification nội bộ**  
  - Quản lý waiting list của ứng viên (đặt mong muốn công việc để hệ thống chủ động gợi ý).  
  - Một lớp `NotificationController`/`NotificationService` nội bộ để tạo request gửi thông báo sang các service khác (ví dụ Notification-Service).  

- **Tích hợp hạ tầng & tiện ích**  
  - Tích hợp **Elasticsearch** để index job và tìm kiếm/gợi ý nhanh.  
  - Tích hợp **Kafka** để phát sự kiện thay đổi trạng thái user/job tới Notification-Service và các consumer khác.  
  - Tích hợp **S3** (hoặc storage tương đương) để lưu file (CV, tài liệu).  
  - Geocoding & Location service để suy luận vị trí người dùng và hỗ trợ job “nearby”.  
  - Scheduled job dọn dẹp token, dọn dữ liệu cũ (qua `TokenCleanupService` và các scheduler khác).  

---

### 2. Service `Chat-Service`

- **Quản lý hội thoại (Conversations)**  
  - Tạo conversation mới giữa các user (ví dụ giữa ứng viên và nhà tuyển dụng).  
  - Lấy danh sách hội thoại của người dùng hiện tại.  
  - Tìm kiếm hội thoại theo từ khóa (tên, nội dung liên quan).  

- **Quản lý tin nhắn (Chat messages)**  
  - Gửi tin nhắn mới trong một conversation.  
  - Lấy danh sách tin nhắn của một conversation.  
  - Hỗ trợ real-time thông qua `SocketHandler` (WebSocket) để đồng bộ tin nhắn tức thời giữa client.  

---

### 3. Service `Notification-Service`

- **Gửi email thông báo**  
  - Endpoint gửi email chung với nội dung, tiêu đề, danh sách người nhận.  
  - Được sử dụng để gửi OTP, xác nhận đăng ký, thông báo trạng thái ứng tuyển, thông báo hệ thống, v.v.  
  - Có consumer Kafka để lắng nghe các sự kiện (ví dụ thay đổi trạng thái user, OTP event) từ các service khác và tự động gửi email tương ứng.  

---

### 4. Service `api-gateway`

- **Gateway cho toàn hệ thống**  
  - Nhận request từ client và định tuyến đến `jobmate-connect`, `Chat-Service`, `Notification-Service`…  
  - Tích hợp với identity/authorization service để xác thực và gắn thông tin người dùng vào request.  
  - Có thể thực hiện cross-cutting concern như logging, filter bảo mật, chuẩn hóa response.  

---

## Luồng tổng quan của hệ thống

- **Người dùng đăng ký và đăng nhập** qua `jobmate-connect` (AuthController), xác thực bằng OTP/email và có thể bật 2FA.  
- **Ứng viên** hoàn thiện hồ sơ, cập nhật vị trí, thêm vào waiting list, sau đó tìm kiếm/gợi ý job phù hợp, nộp đơn ứng tuyển và theo dõi trạng thái.  
- **Nhà tuyển dụng** tạo và quản lý job, duyệt hoặc từ chối ứng viên, xem dashboard hiệu quả tuyển dụng, trò chuyện với ứng viên qua Chat-Service.  
- **Admin** giám sát toàn hệ thống thông qua dashboard, quản lý user/job/report, xem audit log và thống kê.  
- **Thông báo** (OTP, trạng thái ứng tuyển, thông báo hệ thống) được gửi qua Notification-Service, nhiều hành động được phát sự kiện Kafka để đảm bảo tính mở rộng.  

File này chỉ là mô tả tổng quan cấp cao; chi tiết về model dữ liệu và từng API cụ thể có thể xem thêm trong các lớp `controller`, `service`, `dto` và `entity` trong từng module.


