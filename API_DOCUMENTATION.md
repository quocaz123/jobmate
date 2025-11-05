# JobMate Connect - API Documentation

## 📋 Tổng Quan

**Base URL**: `http://localhost:8888/api/v1`  
**Authentication**: Bearer Token (JWT)  
**Content-Type**: `application/json` (trừ file upload)

---

## 🔐 Authentication APIs

### 1. Google OAuth Login
```http
POST /auth/outbound/authentication?code={google_auth_code}
```

**Mô tả**: Đăng nhập bằng Google OAuth2  
**Request**: Query parameter `code` từ Google OAuth callback  
**Response**:
```json
{
  "code": 1000,
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isTwoFaEnabled": false,
    "message": "Login successful",
    "otpExpiryTime": null,
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "requiresPasswordSetup": false,
    "userEmail": "user@gmail.com",
    "userName": "John Doe"
  }
}
```

### 2. Traditional Login
```http
POST /auth/login
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**: Tương tự Google OAuth

### 3. OTP Verification
```http
POST /auth/verify-otp
```

**Request Body**:
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "otp": "123456"
}
```

**Response**: Tương tự login response

### 4. Resend OTP
```http
POST /auth/resend-otp
```

**Request Body**:
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response**:
```json
{
  "code": 1000,
  "message": "OTP sent successfully",
  "data": {
    "message": "OTP has been sent to your email",
    "otpExpiryTime": 1640995200000
  }
}
```

### 5. Set Password (for new users)
```http
POST /auth/set-password?userId={user_id}
```

**Request Body**:
```json
{
  "password": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

### 6. Logout
```http
POST /auth/logout
```

**Request Body**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 👤 User Management APIs

### 1. User Registration
```http
POST /users/registration
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+84901234567"
}
```

**Response**:
```json
{
  "code": 1000,
  "message": "User created successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "fullName": "John Doe",
    "address": null,
    "avatarUrl": null,
    "roles": [
      {
        "id": "role-id",
        "name": "USER",
        "description": "Regular user"
      }
    ],
    "isTwoFaEnabled": false,
    "verificationStatus": "UNVERIFIED",
    "verifiedAt": null,
    "trustScore": 0.0,
    "badgeLevel": null,
    "reviewCount": 0,
    "violationCount": 0,
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 2. Get My Info
```http
GET /users/my-info
```

**Headers**: `Authorization: Bearer {token}`  
**Response**: UserResponse object

### 3. Update User Info
```http
PUT /users
```

**Headers**: `Authorization: Bearer {token}`  
**Request Body**:
```json
{
  "fullName": "John Doe Updated",
  "phoneNumber": "+84901234567",
  "address": "123 Main St, Ho Chi Minh City",
  "avatarUrl": "https://s3.amazonaws.com/avatar.jpg"
}
```

### 4. Get User by ID
```http
GET /users/{user_id}
```

**Response**: UserResponse object

### 5. Get Top Rated Users
```http
GET /users/top-rated?page=0&size=10
```

**Response**:
```json
{
  "code": 1000,
  "message": "Success",
  "data": {
    "currentPage": 0,
    "totalPages": 5,
    "pageSize": 10,
    "totalElements": 50,
    "data": [
      {
        "id": "user-id",
        "fullName": "Top User",
        "trustScore": 4.8,
        "reviewCount": 25,
        "badgeLevel": "GOLD"
      }
    ]
  }
}
```

### 6. Get Top 10 Users
```http
GET /users/top-10
```

**Response**: Array of UserResponse objects

---

## 💼 Job Management APIs

### 1. Create Job (EMPLOYER/ADMIN only)
```http
POST /jobs
```

**Headers**: `Authorization: Bearer {token}`  
**Request Body**:
```json
{
  "title": "Senior Java Developer",
  "description": "We are looking for an experienced Java developer...",
  "location": "Ho Chi Minh City",
  "salary": 25000000,
  "jobType": "FULL_TIME",
  "startAt": "2024-02-01T09:00:00",
  "deadline": "2024-01-31T23:59:59",
  "skills": "Java, Spring Boot, PostgreSQL, AWS"
}
```

**Response**:
```json
{
  "code": 1000,
  "message": "Job created successfully",
  "data": {
    "id": "job-id",
    "title": "Senior Java Developer",
    "description": "We are looking for an experienced Java developer...",
    "location": "Ho Chi Minh City",
    "latitude": 10.8231,
    "longitude": 106.6297,
    "salary": 25000000,
    "jobType": "FULL_TIME",
    "skills": "Java, Spring Boot, PostgreSQL, AWS",
    "status": "PENDING_REVIEW",
    "createdByName": "John Doe",
    "createdAt": "2024-01-01T10:00:00",
    "deadline": "2024-01-31T23:59:59",
    "distance": null,
    "rejectionReason": null
  }
}
```

### 2. Get Available Jobs
```http
GET /jobs/available?page=0&size=10&keyword=java&location=ho chi minh
```

**Query Parameters**:
- `page`: Số trang (default: 0)
- `size`: Số item per page (default: 10)
- `keyword`: Từ khóa tìm kiếm (optional)
- `location`: Vị trí tìm kiếm (optional)

**Response**: PageResponse<JobResponse>

### 3. Get Nearby Jobs
```http
GET /jobs/nearby?radiusInKm=10&page=0&size=10
```

**Query Parameters**:
- `radiusInKm`: Bán kính tìm kiếm (km)
- `page`, `size`: Pagination

**Response**: PageResponse<JobResponse>

### 4. Get Job Detail
```http
GET /jobs/{job_id}
```

**Response**: JobResponse object

### 5. Get My Posted Jobs
```http
GET /jobs/my-jobs?page=0&size=10
```

**Headers**: `Authorization: Bearer {token}`  
**Response**: PageResponse<JobResponse>

### 6. Update Job
```http
PUT /jobs/{job_id}
```

**Headers**: `Authorization: Bearer {token}` (EMPLOYER/ADMIN)  
**Request Body**: JobCreationRequest

### 7. Verify Job (ADMIN only)
```http
PUT /jobs/{job_id}/verify-job?status=APPROVED&reason=Job looks good
```

**Query Parameters**:
- `status`: APPROVED | REJECTED | CLOSED
- `reason`: Lý do (optional)

---

## 📝 Application Management APIs

### 1. Apply for Job
```http
POST /applications/apply?jobId={job_id}&coverLetter={cover_letter}
```

**Headers**: `Authorization: Bearer {token}`  
**Request**: FormData
- `jobId`: UUID của job
- `coverLetter`: Thư xin việc
- `resumeFile`: File CV (optional)

**Response**:
```json
{
  "code": 1000,
  "message": "Application submitted successfully",
  "data": {
    "id": "application-id",
    "jobId": "job-id",
    "jobTitle": "Senior Java Developer",
    "userId": "user-id",
    "userName": "John Doe",
    "userEmail": "john@example.com",
    "status": "PENDING",
    "coverLetter": "I am very interested in this position...",
    "resumeUrl": "https://s3.amazonaws.com/resume.pdf",
    "appliedAt": "2024-01-01T10:00:00",
    "cancelledAt": null,
    "rejectionReason": null,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 2. Get My Applications
```http
GET /applications/my-applications?page=0&size=10
```

**Headers**: `Authorization: Bearer {token}`  
**Response**: PageResponse<ApplicationResponse>

### 3. Get Job Applications (EMPLOYER/ADMIN)
```http
GET /applications/job/{job_id}?page=0&size=10
```

**Headers**: `Authorization: Bearer {token}`  
**Response**: PageResponse<ApplicationResponse>

### 4. Update Application Status (EMPLOYER/ADMIN)
```http
PUT /applications/{application_id}/status?status=ACCEPTED&rejectionReason=null
```

**Query Parameters**:
- `status`: PENDING | ACCEPTED | REJECTED | CANCELLED
- `rejectionReason`: Lý do từ chối (optional)

### 5. Cancel Application
```http
PUT /applications/{application_id}/cancel
```

**Headers**: `Authorization: Bearer {token}`

---

## ⭐ Rating System APIs

### 1. Create Rating
```http
POST /ratings
```

**Headers**: `Authorization: Bearer {token}`  
**Request Body**:
```json
{
  "toUserId": "user-to-rate-id",
  "jobId": "job-id",
  "score": 4.5,
  "comment": "Great work! Very professional and reliable."
}
```

**Response**:
```json
{
  "code": 1000,
  "message": "Rating created successfully",
  "data": {
    "id": "rating-id",
    "fromUserId": "rater-id",
    "fromUserName": "John Doe",
    "toUserId": "rated-user-id",
    "toUserName": "Jane Smith",
    "jobId": "job-id",
    "jobTitle": "Senior Java Developer",
    "score": 4.5,
    "comment": "Great work! Very professional and reliable.",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 2. Get User Ratings
```http
GET /ratings/user/{user_id}?page=0&size=10
```

**Response**: PageResponse<RatingResponse>

### 3. Get My Ratings
```http
GET /ratings/my-ratings?page=0&size=10
```

**Headers**: `Authorization: Bearer {token}`  
**Response**: PageResponse<RatingResponse>

### 4. Get User Rating Stats
```http
GET /ratings/user/{user_id}/stats
```

**Response**:
```json
{
  "code": 1000,
  "message": "Success",
  "data": {
    "averageScore": 4.2,
    "totalRatings": 15,
    "scoreDistribution": {
      "5": 8,
      "4": 5,
      "3": 2,
      "2": 0,
      "1": 0
    }
  }
}
```

### 5. Update Rating
```http
PUT /ratings/{rating_id}
```

**Headers**: `Authorization: Bearer {token}`  
**Request Body**: RatingRequest

### 6. Delete Rating
```http
DELETE /ratings/{rating_id}
```

**Headers**: `Authorization: Bearer {token}`

---

## 🔔 Notification APIs

### 1. Send Notification
```http
POST /notification
```

**Headers**: `Authorization: Bearer {token}`  
**Request Body**:
```json
{
  "userId": "user-id",
  "title": "New Job Application",
  "message": "You have received a new job application",
  "type": "APPLICATION"
}
```

### 2. Get My Notifications
```http
GET /notification/me
```

**Headers**: `Authorization: Bearer {token}`  
**Response**: Array of NotificationResponse

### 3. Mark Notification as Read
```http
POST /notification/{notification_id}/read
```

**Headers**: `Authorization: Bearer {token}`

---

## 📁 File Upload APIs

### 1. Upload File
```http
POST /files/upload
```

**Headers**: `Authorization: Bearer {token}`  
**Request**: FormData
- `file`: File to upload
- `type`: AVATAR | RESUME | DOCUMENT

**Response**:
```json
{
  "code": 1000,
  "message": "File uploaded successfully",
  "data": {
    "id": "file-id",
    "fileName": "resume.pdf",
    "fileUrl": "https://s3.amazonaws.com/bucket/resume.pdf",
    "fileSize": 1024000,
    "fileType": "application/pdf",
    "uploadedAt": "2024-01-01T10:00:00"
  }
}
```

---

## 🛡️ Admin APIs

### 1. Get All Users (ADMIN only)
```http
GET /users?page=0&size=10
```

**Headers**: `Authorization: Bearer {token}` (ADMIN)  
**Response**: PageResponse<UserResponse>

### 2. Get All Jobs (ADMIN only)
```http
GET /jobs?page=0&size=10
```

**Headers**: `Authorization: Bearer {token}` (ADMIN)  
**Response**: PageResponse<JobResponse>

---

## 📊 Common Response Format

### Success Response
```json
{
  "code": 1000,
  "message": "Success",
  "data": { ... }
}
```

### Error Response
```json
{
  "code": 1401,
  "message": "Unauthenticated",
  "data": null
}
```

### Pagination Response
```json
{
  "code": 1000,
  "message": "Success",
  "data": {
    "currentPage": 0,
    "totalPages": 5,
    "pageSize": 10,
    "totalElements": 50,
    "data": [ ... ]
  }
}
```

---

## 🔑 Authentication Flow

### 1. Google OAuth Flow
1. Redirect user to Google OAuth
2. Get authorization code from callback
3. Call `/auth/outbound/authentication?code={code}`
4. If 2FA enabled, verify OTP
5. If new user, set password
6. Store JWT token for subsequent requests

### 2. Traditional Login Flow
1. Call `/auth/login` with email/password
2. If 2FA enabled, verify OTP
3. Store JWT token

### 3. Token Usage
- Include `Authorization: Bearer {token}` in all protected endpoints
- Token expires after 1 hour
- Use refresh token to get new access token

---

## 📝 Notes for Frontend Developers

1. **Base URL**: Always use `http://localhost:8888/api/v1` as base URL
2. **Authentication**: Store JWT token in localStorage/sessionStorage
3. **File Upload**: Use FormData for file uploads
4. **Pagination**: All list endpoints support pagination with `page` and `size` parameters
5. **Error Handling**: Check `code` field in response (1000 = success, others = error)
6. **Role-based Access**: Some endpoints require specific roles (USER, EMPLOYER, ADMIN)
7. **File Types**: Supported file types for upload: PDF, DOC, DOCX, JPG, PNG
8. **Location**: For nearby jobs, user location is required
9. **Real-time**: Notifications are sent via email, no WebSocket implementation yet
10. **Validation**: All input fields have validation rules, check error messages

---

## 🚀 Getting Started

1. Start all services (API Gateway, Jobmate Connect, Notification Service)
2. Ensure PostgreSQL, Redis, Kafka are running
3. Use the base URL `http://localhost:8888/api/v1` for all API calls
4. Implement authentication flow first
5. Test with different user roles (USER, EMPLOYER, ADMIN)

---

*Last Updated: January 2024*
