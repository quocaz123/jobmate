# 📋 Hướng Dẫn Test API với Postman - JobMate Connect

## 🚀 Setup

### 1. Import Collection và Environment

1. Mở Postman
2. Import file `JobMate_Connect_Test_Collection.postman_collection.json`
3. Import file `JobMate_Connect_Test_Environment.postman_environment.json`
4. Chọn environment "JobMate Connect - Test Environment"

### 2. Cấu hình Base URL

Đảm bảo biến `base_url` trong environment là:
```
http://localhost:8888/api/v1/jobmate
```

---

## 📝 Luồng Test Chi Tiết

### **BƯỚC 1: Authentication (Đăng ký & Đăng nhập)**

#### 1.1. Register Job Seeker
```
POST /users/registration
```
**Request Body:**
```json
{
    "email": "jobseeker@test.com",
    "password": "password123",
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0123456789"
}
```
**Expected:**
- Status: 200
- Response có `id`, `email`
- Lưu `user_id` vào environment

#### 1.2. Register Employer
```
POST /users/registration
```
**Request Body:**
```json
{
    "email": "employer@test.com",
    "password": "password123",
    "fullName": "Công ty ABC",
    "phoneNumber": "0987654321"
}
```

#### 1.3. Login Job Seeker
```
POST /auth/login
```
**Request Body:**
```json
{
    "email": "jobseeker@test.com",
    "password": "password123"
}
```
**Expected:**
- Status: 200
- Response có `token`
- Lưu `jobseeker_token` và `jobseeker_user_id` vào environment

#### 1.4. Login Employer
```
POST /auth/login
```
**Request Body:**
```json
{
    "email": "employer@test.com",
    "password": "password123"
}
```
**Expected:**
- Lưu `employer_token` và `employer_user_id` vào environment

---

### **BƯỚC 2: User Management (Test các trường mới)**

#### 2.1. Update User Profile với các trường mới
```
PUT /users
Authorization: Bearer {{jobseeker_token}}
```
**Request Body:**
```json
{
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0123456789",
    "address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
    "skills": "Tiếng Anh, Giao tiếp, Photoshop, Giảng dạy",
    "preferredJobType": "PART_TIME",
    "availableDays": "Thứ 2, 4, 6",
    "availableTime": "Buổi tối",
    "preferredMinSalary": 200000
}
```
**Expected:**
- Status: 200
- Response có các field mới: `skills`, `preferredJobType`, `availableDays`, `availableTime`, `preferredMinSalary`

#### 2.2. Get My Info
```
GET /users/my-info
Authorization: Bearer {{jobseeker_token}}
```
**Expected:**
- Status: 200
- Response có đầy đủ các field mới đã update

---

### **BƯỚC 3: Job Management (Test các trường mới)**

#### 3.1. Create Job với các trường mới
```
POST /jobs
Authorization: Bearer {{employer_token}}
```
**Request Body:**
```json
{
    "title": "Gia sư Tiếng Anh - Lớp 12",
    "description": "Tìm gia sư Tiếng Anh cho học sinh lớp 12. Yêu cầu có kinh nghiệm giảng dạy, phát âm chuẩn.",
    "location": "Quận 1, TP.HCM",
    "salary": 250000,
    "jobType": "PART_TIME",
    "startAt": "2024-02-01T19:00:00",
    "deadline": "2024-01-31T23:59:59",
    "skills": "Tiếng Anh, Giao tiếp",
    "companyName": "Trung tâm Anh ngữ ILA",
    "salaryUnit": "buổi",
    "workingHours": "19:00-21:00",
    "workingDays": "Thứ 2, 4, 6",
    "workMode": "ONSITE",
    "category": "Giáo dục",
    "contactPhone": "0123456789"
}
```
**Expected:**
- Status: 200
- Response có các field mới:
  - `companyName`: "Trung tâm Anh ngữ ILA"
  - `applicationCount`: 0
  - `salaryUnit`: "buổi"
  - `workingHours`: "19:00-21:00"
  - `workingDays`: "Thứ 2, 4, 6"
  - `workMode`: "ONSITE"
  - `category`: "Giáo dục"
  - `viewsCount`: 0
  - `contactPhone`: "0123456789"
- Lưu `job_id` vào environment

#### 3.2. Admin Approve Job (Nếu có admin token)
```
PUT /jobs/{{job_id}}/verify-job?status=APPROVED
Authorization: Bearer {{admin_token}}
```

#### 3.3. Get Job Detail - Kiểm tra viewsCount tăng
```
GET /jobs/{{job_id}}
```
**Expected:**
- Status: 200
- Response có đầy đủ các field mới
- `viewsCount` tăng lên sau mỗi lần xem

#### 3.4. Get Available Jobs
```
GET /jobs/available?page=0&size=10
```
**Expected:**
- Status: 200
- Mỗi job trong danh sách có đầy đủ các field mới

---

### **BƯỚC 4: Application Management (Test applicationCount)**

#### 4.1. Apply Job
```
POST /applications/apply
Authorization: Bearer {{jobseeker_token}}
Content-Type: multipart/form-data
```
**Form Data:**
- `jobId`: `{{job_id}}`
- `coverLetter`: "Tôi rất quan tâm đến vị trí này. Tôi có kinh nghiệm giảng dạy Tiếng Anh 2 năm."

**Expected:**
- Status: 200
- Application được tạo thành công
- Lưu `application_id` vào environment

#### 4.2. Get Job Detail After Apply - Kiểm tra applicationCount
```
GET /jobs/{{job_id}}
```
**Expected:**
- Status: 200
- `applicationCount` >= 1 (đã tăng sau khi apply)

#### 4.3. Cancel Application
```
PUT /applications/{{application_id}}/cancel
Authorization: Bearer {{jobseeker_token}}
```

#### 4.4. Get Job Detail After Cancel - Kiểm tra applicationCount giảm
```
GET /jobs/{{job_id}}
```
**Expected:**
- Status: 200
- `applicationCount` = 0 (vì CANCELLED không được tính)

---

### **BƯỚC 5: Rating Management (Test averageRating và ratingCount)**

#### 5.1. Create Rating for Job
```
POST /ratings
Authorization: Bearer {{employer_token}}
```
**Request Body:**
```json
{
    "toUserId": "{{jobseeker_user_id}}",
    "jobId": "{{job_id}}",
    "score": 4.8,
    "comment": "Rất chuyên nghiệp và đúng giờ!"
}
```

#### 5.2. Get Job Detail After Rating - Kiểm tra rating
```
GET /jobs/{{job_id}}
```
**Expected:**
- Status: 200
- `averageRating`: 4.8 (Float)
- `ratingCount`: 1 (Integer)

---

## 🧪 Test Cases - Dữ Liệu Mẫu

### **Test Case 1: Job với đầy đủ thông tin**

**Job 1 - Gia sư Tiếng Anh:**
```json
{
    "title": "Gia sư Tiếng Anh - Lớp 12",
    "companyName": "Trung tâm Anh ngữ ILA",
    "salary": 250000,
    "salaryUnit": "buổi",
    "workingHours": "19:00-21:00",
    "workingDays": "Thứ 2, 4, 6",
    "workMode": "ONSITE",
    "category": "Giáo dục",
    "contactPhone": "0123456789"
}
```

**Job 2 - Thiết kế Poster:**
```json
{
    "title": "Sự kiện Poster thiết kế",
    "companyName": "Công ty Event ABC",
    "salary": 500000,
    "salaryUnit": "áp phích",
    "workingHours": "Linh hoạt",
    "workingDays": "Linh hoạt",
    "workMode": "REMOTE",
    "category": "Thiết kế",
    "contactPhone": "0987654321"
}
```

**Job 3 - Phục vụ bàn:**
```json
{
    "title": "Phục vụ bàn - Nhà hàng Nhật Bản",
    "companyName": "Sushi Hokkaido",
    "salary": 120000,
    "salaryUnit": "ca",
    "workingHours": "17:00-22:00",
    "workingDays": "Cuối tuần",
    "workMode": "ONSITE",
    "category": "Dịch vụ",
    "contactPhone": "0912345678"
}
```

### **Test Case 2: User với đầy đủ thông tin**

**Job Seeker 1:**
```json
{
    "skills": "Tiếng Anh, Giao tiếp, Photoshop, Giảng dạy",
    "preferredJobType": "PART_TIME",
    "availableDays": "Thứ 2, 4, 6",
    "availableTime": "Buổi tối",
    "preferredMinSalary": 200000
}
```

**Job Seeker 2:**
```json
{
    "skills": "Photoshop, Illustrator, Design",
    "preferredJobType": "FREELANCE",
    "availableDays": "Linh hoạt",
    "availableTime": "Cả ngày",
    "preferredMinSalary": 400000
}
```

---

## ✅ Checklist Test

### **Job Entity - Các trường mới:**
- [ ] `companyName` - Hiển thị đúng
- [ ] `applicationCount` - Tăng khi có application, giảm khi cancel
- [ ] `salaryUnit` - Hiển thị đúng ("buổi", "ca", "tháng", etc.)
- [ ] `workingHours` - Hiển thị đúng
- [ ] `workingDays` - Hiển thị đúng
- [ ] `workMode` - Hiển thị đúng ("ONSITE", "REMOTE", "HYBRID")
- [ ] `category` - Hiển thị đúng
- [ ] `viewsCount` - Tăng mỗi lần xem chi tiết
- [ ] `contactPhone` - Hiển thị đúng
- [ ] `averageRating` - Tính đúng từ Rating entity
- [ ] `ratingCount` - Tính đúng từ Rating entity

### **User Entity - Các trường mới:**
- [ ] `skills` - Lưu và hiển thị đúng
- [ ] `preferredJobType` - Lưu và hiển thị đúng
- [ ] `availableDays` - Lưu và hiển thị đúng
- [ ] `availableTime` - Lưu và hiển thị đúng
- [ ] `preferredMinSalary` - Lưu và hiển thị đúng

---

## 🔄 Luồng Test Tự Động (Runner)

### Cách chạy Collection Runner:

1. Mở Collection "JobMate Connect - Test Collection (Updated)"
2. Click "Run"
3. Chọn các requests theo thứ tự:
   - 1. Authentication (tất cả)
   - 2. User Management (Update User Profile, Get My Info)
   - 3. Job Management (Create Job, Get Job Detail)
   - 4. Application Management (Apply Job, Get Job Detail, Cancel Application)
   - 5. Rating Management (Create Rating, Get Job Detail)
4. Click "Run JobMate Connect..."

### Thứ tự thực hiện:

```
1. Register Job Seeker
2. Register Employer
3. Login Job Seeker
4. Login Employer
5. Update User Profile (Job Seeker)
6. Get My Info
7. Create Job
8. Admin Approve Job (nếu có)
9. Get Job Detail (lần 1 - viewsCount = 1)
10. Get Job Detail (lần 2 - viewsCount = 2)
11. Apply Job
12. Get Job Detail (applicationCount = 1)
13. Cancel Application
14. Get Job Detail (applicationCount = 0)
15. Create Rating
16. Get Job Detail (averageRating = 4.8, ratingCount = 1)
```

---

## 📊 Kết Quả Mong Đợi

### **Response Mẫu - Job Response:**
```json
{
    "code": 1000,
    "message": "Success",
    "data": {
        "id": "job-uuid",
        "title": "Gia sư Tiếng Anh - Lớp 12",
        "companyName": "Trung tâm Anh ngữ ILA",
        "salary": 250000,
        "salaryUnit": "buổi",
        "workingHours": "19:00-21:00",
        "workingDays": "Thứ 2, 4, 6",
        "workMode": "ONSITE",
        "category": "Giáo dục",
        "applicationCount": 1,
        "viewsCount": 5,
        "averageRating": 4.8,
        "ratingCount": 1,
        "contactPhone": "0123456789",
        ...
    }
}
```

### **Response Mẫu - User Response:**
```json
{
    "code": 1000,
    "message": "Success",
    "data": {
        "id": "user-uuid",
        "fullName": "Nguyễn Văn A",
        "skills": "Tiếng Anh, Giao tiếp, Photoshop, Giảng dạy",
        "preferredJobType": "PART_TIME",
        "availableDays": "Thứ 2, 4, 6",
        "availableTime": "Buổi tối",
        "preferredMinSalary": 200000,
        ...
    }
}
```

---

## 🐛 Troubleshooting

### Lỗi 401 Unauthorized:
- Kiểm tra token trong Authorization header
- Đảm bảo đã login và lưu token vào environment

### Lỗi 404 Not Found:
- Kiểm tra `job_id`, `user_id` trong environment
- Đảm bảo đã chạy các request tạo trước đó

### applicationCount không tăng:
- Kiểm tra query `countByJobId` trong ApplicationRepository
- Đảm bảo không tính CANCELLED applications

### viewsCount không tăng:
- Kiểm tra logic trong `getJobDetails()` method
- Đảm bảo đã save job sau khi tăng viewsCount

### averageRating không hiển thị:
- Kiểm tra query trong RatingRepository
- Đảm bảo đã có rating cho job đó

---

## 📝 Notes

1. **Environment Variables:** Tất cả các ID và token sẽ được lưu tự động vào environment sau mỗi request thành công
2. **Test Scripts:** Mỗi request đều có test scripts để validate response
3. **Data Cleanup:** Sau khi test xong, có thể xóa các records test trong database
4. **Admin Token:** Cần có admin token để approve job (có thể tạo admin user riêng)

---

**Happy Testing! 🚀**

