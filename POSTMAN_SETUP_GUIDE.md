# 🚀 Hướng Dẫn Sử Dụng Postman Collection - JobMate Connect

## 📋 Tổng Quan

File Postman Collection này chứa tất cả các API endpoints của hệ thống JobMate Connect, được tổ chức theo từng module chức năng.

## 📁 Files Cần Import

1. **JobMate_Connect_API_Collection.postman_collection.json** - Collection chính
2. **JobMate_Connect_Environment.postman_environment.json** - Environment variables

## 🔧 Cài Đặt

### Bước 1: Import Collection
1. Mở Postman
2. Click **Import** button
3. Chọn file `JobMate_Connect_API_Collection.postman_collection.json`
4. Click **Import**

### Bước 2: Import Environment
1. Click **Import** button
2. Chọn file `JobMate_Connect_Environment.postman_environment.json`
3. Click **Import**

### Bước 3: Chọn Environment
1. Ở góc trên bên phải, click dropdown environment
2. Chọn **"JobMate Connect Environment"**

## 🎯 Cấu Trúc Collection

### 🔐 Authentication
- **Google OAuth Login** - Đăng nhập bằng Google
- **Traditional Login** - Đăng nhập thông thường
- **Verify OTP** - Xác thực OTP
- **Resend OTP** - Gửi lại OTP
- **Set Password** - Đặt mật khẩu cho user mới
- **Logout** - Đăng xuất

### 👤 User Management
- **User Registration** - Đăng ký user mới
- **Get My Info** - Lấy thông tin cá nhân
- **Update User Info** - Cập nhật thông tin
- **Get User by ID** - Lấy thông tin user theo ID
- **Get Top Rated Users** - Danh sách user được đánh giá cao
- **Get Top 10 Users** - Top 10 user
- **Get All Users (ADMIN)** - Quản lý tất cả user (ADMIN)

### 💼 Job Management
- **Create Job (EMPLOYER)** - Tạo job mới
- **Get Available Jobs** - Tìm kiếm job
- **Get Nearby Jobs** - Job gần đây
- **Get Job Detail** - Chi tiết job
- **Get My Posted Jobs** - Job đã đăng
- **Update Job** - Cập nhật job
- **Verify Job (ADMIN)** - Duyệt job
- **Get All Jobs (ADMIN)** - Quản lý tất cả job (ADMIN)

### 📝 Application Management
- **Apply for Job** - Ứng tuyển job
- **Get My Applications** - Đơn ứng tuyển của tôi
- **Get Job Applications (EMPLOYER)** - Đơn ứng tuyển của job
- **Update Application Status (EMPLOYER)** - Cập nhật trạng thái đơn
- **Cancel Application** - Hủy đơn ứng tuyển

### ⭐ Rating System
- **Create Rating** - Tạo đánh giá
- **Get User Ratings** - Đánh giá của user
- **Get My Ratings** - Đánh giá của tôi
- **Get User Rating Stats** - Thống kê đánh giá
- **Update Rating** - Cập nhật đánh giá
- **Delete Rating** - Xóa đánh giá

### 🔔 Notification
- **Send Notification** - Gửi thông báo
- **Get My Notifications** - Thông báo của tôi
- **Mark Notification as Read** - Đánh dấu đã đọc

### 📁 File Upload
- **Upload Avatar** - Upload ảnh đại diện
- **Upload Resume** - Upload CV

## 🔑 Environment Variables

### Variables Có Sẵn:
- `base_url`: `http://localhost:8888/api/v1`
- `access_token`: JWT token cho user thường
- `employer_token`: JWT token cho employer
- `admin_token`: JWT token cho admin
- `user_id`: ID của user
- `job_id`: ID của job
- `application_id`: ID của application
- `rating_id`: ID của rating
- `notification_id`: ID của notification
- `rated_user_id`: ID của user được đánh giá
- `google_auth_code`: Code từ Google OAuth

## 🚀 Workflow Test

### 1. Test Authentication Flow
```
1. User Registration → Lưu user_id
2. Traditional Login → Lưu access_token
3. Get My Info → Verify token works
```

### 2. Test Job Flow (EMPLOYER)
```
1. Login as EMPLOYER → Lưu employer_token
2. Create Job → Lưu job_id
3. Get My Posted Jobs → Verify job created
4. Update Job → Verify update works
```

### 3. Test Application Flow (USER)
```
1. Login as USER → Lưu access_token
2. Get Available Jobs → Browse jobs
3. Apply for Job → Lưu application_id
4. Get My Applications → Verify application
```

### 4. Test Rating Flow
```
1. Create Rating → Lưu rating_id
2. Get User Ratings → Verify rating created
3. Update Rating → Verify update works
4. Get User Rating Stats → Verify stats
```

### 5. Test Admin Flow
```
1. Login as ADMIN → Lưu admin_token
2. Get All Users → Verify admin access
3. Get All Jobs → Verify admin access
4. Verify Job → Approve/reject job
```

## 📝 Lưu Ý Quan Trọng

### 🔐 Authentication
- **Bearer Token**: Tất cả protected endpoints cần `Authorization: Bearer {token}`
- **Role-based Access**: Một số endpoint chỉ dành cho EMPLOYER/ADMIN
- **Token Expiry**: Token hết hạn sau 1 giờ

### 📊 Pagination
- Tất cả list endpoints hỗ trợ `page` và `size` parameters
- Default: `page=0`, `size=10`

### 📁 File Upload
- Sử dụng **FormData** cho file upload
- Supported types: AVATAR, RESUME, DOCUMENT
- File size limit: Check server configuration

### 🔄 Response Format
```json
{
  "code": 1000,        // 1000 = success, others = error
  "message": "Success",
  "data": { ... }      // Response data
}
```

### ⚠️ Error Handling
- **401 Unauthorized**: Token invalid/expired
- **403 Forbidden**: Không có quyền truy cập
- **400 Bad Request**: Request body invalid
- **404 Not Found**: Resource không tồn tại

## 🎯 Test Scenarios

### Scenario 1: Complete User Journey
```
1. Register new user
2. Login and get token
3. Update profile
4. Browse available jobs
5. Apply for a job
6. Check application status
7. Rate another user
8. Logout
```

### Scenario 2: Employer Journey
```
1. Login as employer
2. Create multiple jobs
3. Check job applications
4. Update application status
5. Manage posted jobs
```

### Scenario 3: Admin Journey
```
1. Login as admin
2. View all users
3. View all jobs
4. Verify/reject jobs
5. Manage system
```

## 🔧 Troubleshooting

### Common Issues:
1. **401 Unauthorized**: Check token validity
2. **403 Forbidden**: Check user role permissions
3. **Connection Error**: Check if services are running
4. **File Upload Error**: Check file size and type

### Debug Steps:
1. Check environment variables
2. Verify base URL
3. Check token format
4. Review request body format
5. Check server logs

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra server logs
2. Verify database connection
3. Check Kafka/Redis services
4. Review API documentation

---

**Happy Testing! 🚀**

