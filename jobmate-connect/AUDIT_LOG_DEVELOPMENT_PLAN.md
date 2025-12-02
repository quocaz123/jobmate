# Kế Hoạch Phát Triển Audit Log System

## 📋 Tổng Quan Hiện Trạng

### ✅ Đã Có:
1. **Entity**: `AuditLog` với các trường:
   - `id` (UUID)
   - `user` (User - người thực hiện action)
   - `action` (String - hành động)
   - `targetId` (UUID - ID của đối tượng bị tác động)
   - `description` (String - mô tả chi tiết)
   - `createdAt` (LocalDateTime)

2. **Repository**: `AuditLogRepository` với các query methods:
   - Tìm theo user
   - Tìm theo action
   - Tìm theo target ID
   - Tìm theo user + action
   - Tìm theo khoảng thời gian
   - Đếm theo action

### ❌ Chưa Có:
- Service để tạo và quản lý audit log
- Controller để xem audit log
- Integration với các service hiện tại
- DTO cho audit log response
- Aspect/Interceptor để tự động log

---

## 🎯 Các Action Cần Log

### 1. **User Management Actions**
| Action | Mô tả | Target ID | Priority |
|--------|-------|-----------|----------|
| `USER_CREATED` | Tạo tài khoản mới | User ID | HIGH |
| `USER_UPDATED` | Cập nhật thông tin user | User ID | MEDIUM |
| `USER_PASSWORD_CHANGED` | Đổi mật khẩu | User ID | HIGH |
| `USER_2FA_ENABLED` | Bật 2FA | User ID | HIGH |
| `USER_2FA_DISABLED` | Tắt 2FA | User ID | HIGH |
| `USER_VERIFIED` | Admin xác thực user | User ID | HIGH |
| `USER_REJECTED` | Admin từ chối xác thực | User ID | HIGH |
| `USER_BANNED` | Admin khóa tài khoản | User ID | CRITICAL |
| `USER_STATUS_CHANGED` | Thay đổi trạng thái user | User ID | HIGH |

### 2. **Job Management Actions**
| Action | Mô tả | Target ID | Priority |
|--------|-------|-----------|----------|
| `JOB_CREATED` | Tạo job mới | Job ID | HIGH |
| `JOB_UPDATED` | Cập nhật job | Job ID | MEDIUM |
| `JOB_APPROVED` | Admin duyệt job | Job ID | HIGH |
| `JOB_REJECTED` | Admin từ chối job | Job ID | HIGH |
| `JOB_CLOSED` | Đóng job | Job ID | MEDIUM |
| `JOB_AUTO_CLOSED` | Hệ thống tự động đóng job | Job ID | HIGH |
| `JOB_VIEWED` | Xem chi tiết job | Job ID | LOW |

### 3. **Application Actions**
| Action | Mô tả | Target ID | Priority |
|--------|-------|-----------|----------|
| `APPLICATION_CREATED` | Nộp đơn ứng tuyển | Application ID | HIGH |
| `APPLICATION_ACCEPTED` | Chấp nhận đơn | Application ID | HIGH |
| `APPLICATION_REJECTED` | Từ chối đơn | Application ID | MEDIUM |
| `APPLICATION_CANCELLED` | Hủy đơn | Application ID | MEDIUM |

### 4. **Rating Actions**
| Action | Mô tả | Target ID | Priority |
|--------|-------|-----------|----------|
| `RATING_CREATED` | Tạo đánh giá | Rating ID | HIGH |
| `RATING_DELETED` | Xóa đánh giá | Rating ID | MEDIUM |

### 5. **Report Actions**
| Action | Mô tả | Target ID | Priority |
|--------|-------|-----------|----------|
| `REPORT_CREATED` | Tạo báo cáo | Report ID | HIGH |
| `REPORT_REVIEWED` | Admin review báo cáo | Report ID | HIGH |
| `REPORT_REJECTED` | Admin từ chối báo cáo | Report ID | MEDIUM |
| `REPORT_AUTO_REVIEWED` | Hệ thống tự động review | Report ID | MEDIUM |

### 6. **Authentication Actions**
| Action | Mô tả | Target ID | Priority |
|--------|-------|-----------|----------|
| `LOGIN_SUCCESS` | Đăng nhập thành công | User ID | MEDIUM |
| `LOGIN_FAILED` | Đăng nhập thất bại | null | MEDIUM |
| `LOGOUT` | Đăng xuất | User ID | LOW |
| `TOKEN_REFRESHED` | Refresh token | User ID | LOW |
| `PASSWORD_SET` | Set password lần đầu | User ID | HIGH |

### 7. **Admin Actions**
| Action | Mô tả | Target ID | Priority |
|--------|-------|-----------|----------|
| `ADMIN_VIEW_REPORTS` | Admin xem danh sách report | null | LOW |
| `ADMIN_VIEW_USERS` | Admin xem danh sách user | null | LOW |
| `ADMIN_VIEW_JOBS` | Admin xem danh sách job | null | LOW |

---

## 🏗️ Cấu Trúc Code Đề Xuất

### 1. **AuditLogService**
```java
@Service
public class AuditLogService {
    // Tạo audit log
    void logAction(String action, UUID userId, UUID targetId, String description);
    
    // Tìm kiếm audit log
    PageResponse<AuditLogResponse> getAuditLogs(...);
    
    // Thống kê
    Map<String, Long> getActionStatistics(...);
}
```

### 2. **AuditLogController** (Chỉ cho ADMIN)
```java
@RestController
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    // Xem danh sách audit log với filter
    // Thống kê audit log
}
```

### 3. **DTOs**
- `AuditLogResponse` - Response khi xem audit log
- `AuditLogRequest` - Request để filter audit log (optional)
- `AuditLogStatisticsResponse` - Thống kê

### 4. **Integration Points**
- Thêm `AuditLogService` vào các service cần log
- Gọi `auditLogService.logAction()` sau mỗi action quan trọng
- Hoặc dùng AOP (Aspect-Oriented Programming) để tự động log

---

## 📝 Hướng Phát Triển Code

### **Bước 1: Tạo DTOs**
1. `AuditLogResponse.java` - DTO để trả về khi xem audit log
2. `AuditLogFilterRequest.java` (optional) - DTO để filter audit log

### **Bước 2: Tạo AuditLogService**
1. Method `logAction()` - Tạo audit log entry
2. Method `getAuditLogs()` - Lấy danh sách với filter
3. Method `getStatistics()` - Thống kê (optional)

### **Bước 3: Tạo AuditLogController**
1. Endpoint GET `/admin/audit-logs` - Xem danh sách
2. Endpoint GET `/admin/audit-logs/statistics` - Thống kê (optional)

### **Bước 4: Integration với các Service**
1. **UserService**:
   - `createUser()` → log `USER_CREATED`
   - `updateUser()` → log `USER_UPDATED`
   - `updatePassword()` → log `USER_PASSWORD_CHANGED`
   - `updateTwoFactorStatus()` → log `USER_2FA_ENABLED/DISABLED`

2. **JobService**:
   - `createJob()` → log `JOB_CREATED`
   - `updateJob()` → log `JOB_UPDATED`
   - `updateJobVerificationStatus()` → log `JOB_APPROVED/REJECTED`
   - `closeJob()` → log `JOB_CLOSED`

3. **ApplicationService**:
   - `applyJob()` → log `APPLICATION_CREATED`
   - `updateApplicationStatus()` → log `APPLICATION_ACCEPTED/REJECTED/CANCELLED`

4. **RatingService**:
   - `createRating()` → log `RATING_CREATED`
   - `deleteRating()` → log `RATING_DELETED`

5. **ReportService**:
   - `createReport()` → log `REPORT_CREATED`
   - `reviewReport()` → log `REPORT_REVIEWED/REJECTED`
   - `autoReviewReport()` → log `REPORT_AUTO_REVIEWED`

6. **AuthenticationService**:
   - `authenticate()` → log `LOGIN_SUCCESS/FAILED`
   - `logout()` → log `LOGOUT`
   - `refresh()` → log `TOKEN_REFRESHED`
   - `setPassword()` → log `PASSWORD_SET`

7. **UserVerificationService**:
   - `approveVerification()` → log `USER_VERIFIED`
   - `rejectVerification()` → log `USER_REJECTED`

### **Bước 5: (Optional) Tạo AOP Aspect**
- Tự động log các method được đánh dấu annotation `@AuditLog`
- Giảm code duplication

---

## 🔍 Thông Tin Cần Lưu trong Description

### Format mẫu:
```
"[Action] - [Entity Name] - [Details]"
```

### Ví dụ:
- `"USER_CREATED - Tạo tài khoản mới - Email: user@example.com"`
- `"JOB_APPROVED - Duyệt công việc - Job: Software Engineer tại Hà Nội"`
- `"REPORT_CREATED - Báo cáo vi phạm - Target: Job (ID: xxx), Lý do: Spam"`
- `"USER_BANNED - Khóa tài khoản - Lý do: Vượt quá số lần vi phạm cho phép"`

---

## 📊 Các Query Cần Bổ Sung (Optional)

1. **Thống kê theo thời gian**:
   - Số lượng action mỗi ngày/tuần/tháng
   - Top actions được thực hiện nhiều nhất

2. **Thống kê theo user**:
   - User nào thực hiện nhiều action nhất
   - User nào bị report nhiều nhất

3. **Thống kê theo entity**:
   - Job nào được xem nhiều nhất
   - Job nào bị report nhiều nhất

---

## ⚠️ Lưu Ý Quan Trọng

1. **Performance**: 
   - Audit log có thể tăng nhanh → Cần có strategy để archive/delete log cũ
   - Cân nhắc dùng async logging để không block main transaction

2. **Security**:
   - Chỉ ADMIN mới được xem audit log
   - Không log thông tin nhạy cảm (password, token, etc.)

3. **Data Retention**:
   - Quy định thời gian lưu trữ (VD: 1 năm)
   - Tạo scheduled job để cleanup log cũ

4. **Privacy**:
   - Tuân thủ GDPR/Privacy laws
   - Có thể cần mask một số thông tin trong log

---

## 🚀 Ưu Tiên Triển Khai

### Phase 1 (High Priority):
1. ✅ Tạo `AuditLogService`
2. ✅ Tạo `AuditLogResponse` DTO
3. ✅ Tạo `AuditLogController` (chỉ ADMIN)
4. ✅ Integration với UserService (create, update, password change)
5. ✅ Integration với JobService (create, approve, reject)
6. ✅ Integration với ReportService (create, review)

### Phase 2 (Medium Priority):
1. Integration với ApplicationService
2. Integration với RatingService
3. Integration với AuthenticationService
4. Thêm thống kê cơ bản

### Phase 3 (Low Priority):
1. Tạo AOP Aspect để tự động log
2. Thêm các query thống kê nâng cao
3. Tạo scheduled job để cleanup log cũ
4. Export audit log ra file (CSV/Excel)

---

## 📌 Action Constants

Nên tạo một class `AuditAction` để quản lý các action constants:

```java
public class AuditAction {
    // User
    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_UPDATED = "USER_UPDATED";
    // ... etc
}
```

Hoặc dùng Enum:

```java
public enum AuditAction {
    USER_CREATED,
    USER_UPDATED,
    // ... etc
}
```





