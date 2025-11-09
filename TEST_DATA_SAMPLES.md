# 📦 Dữ Liệu Test Mẫu - JobMate Connect

## 🎯 Mục Đích
File này chứa các dữ liệu test mẫu để test các tính năng mới của hệ thống.

---

## 👤 USER TEST DATA

### User 1: Job Seeker (Sinh viên tìm việc part-time)
```json
{
    "email": "jobseeker1@test.com",
    "password": "password123",
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

### User 2: Job Seeker (Freelancer thiết kế)
```json
{
    "email": "jobseeker2@test.com",
    "password": "password123",
    "fullName": "Trần Thị B",
    "phoneNumber": "0987654321",
    "address": "456 Lê Lợi, Quận 3, TP.HCM",
    "skills": "Photoshop, Illustrator, Design, UI/UX",
    "preferredJobType": "FREELANCE",
    "availableDays": "Linh hoạt",
    "availableTime": "Cả ngày",
    "preferredMinSalary": 400000
}
```

### User 3: Employer (Trung tâm giáo dục)
```json
{
    "email": "employer1@test.com",
    "password": "password123",
    "fullName": "Trung tâm Anh ngữ ILA",
    "phoneNumber": "0123456789"
}
```

### User 4: Employer (Công ty sự kiện)
```json
{
    "email": "employer2@test.com",
    "password": "password123",
    "fullName": "Công ty Event ABC",
    "phoneNumber": "0987654321"
}
```

### User 5: Employer (Nhà hàng)
```json
{
    "email": "employer3@test.com",
    "password": "password123",
    "fullName": "Sushi Hokkaido",
    "phoneNumber": "0912345678"
}
```

---

## 💼 JOB TEST DATA

### Job 1: Gia sư Tiếng Anh (Part-time, Onsite)
```json
{
    "title": "Gia sư Tiếng Anh - Lớp 12",
    "description": "Tìm gia sư Tiếng Anh cho học sinh lớp 12. Yêu cầu:\n- Có kinh nghiệm giảng dạy Tiếng Anh\n- Phát âm chuẩn, có bằng cấp liên quan\n- Nhiệt tình, có trách nhiệm\n- Thời gian: Thứ 2, 4, 6 từ 19:00-21:00",
    "location": "Quận 1, TP.HCM",
    "salary": 250000,
    "jobType": "PART_TIME",
    "startAt": "2024-02-01T19:00:00",
    "deadline": "2024-01-31T23:59:59",
    "skills": "Tiếng Anh, Giao tiếp, Giảng dạy",
    "companyName": "Trung tâm Anh ngữ ILA",
    "salaryUnit": "buổi",
    "workingHours": "19:00-21:00",
    "workingDays": "Thứ 2, 4, 6",
    "workMode": "ONSITE",
    "category": "Giáo dục",
    "contactPhone": "0123456789"
}
```

### Job 2: Thiết kế Poster (Freelance, Remote)
```json
{
    "title": "Sự kiện Poster thiết kế",
    "description": "Thiết kế poster cho sự kiện công ty. Yêu cầu:\n- Sử dụng thành thạo Photoshop, Illustrator\n- Có portfolio về thiết kế sự kiện\n- Có thể làm việc từ xa\n- Deadline: 2 ngày",
    "location": "Quận 3, TP.HCM",
    "salary": 500000,
    "jobType": "FREELANCE",
    "startAt": "2024-02-01T09:00:00",
    "deadline": "2024-01-31T23:59:59",
    "skills": "Photoshop, Illustrator",
    "companyName": "Công ty Event ABC",
    "salaryUnit": "áp phích",
    "workingHours": "Linh hoạt",
    "workingDays": "Linh hoạt",
    "workMode": "REMOTE",
    "category": "Thiết kế",
    "contactPhone": "0987654321"
}
```

### Job 3: Phục vụ bàn (Part-time, Onsite)
```json
{
    "title": "Phục vụ bàn - Nhà hàng Nhật Bản",
    "description": "Tìm nhân viên phục vụ bàn cho nhà hàng Nhật Bản. Yêu cầu:\n- Nhiệt tình, nhanh nhẹn\n- Có thể làm cuối tuần\n- Biết giao tiếp cơ bản\n- Thời gian: 17:00-22:00 cuối tuần",
    "location": "Quận 7, TP.HCM",
    "salary": 120000,
    "jobType": "PART_TIME",
    "startAt": "2024-02-01T17:00:00",
    "deadline": "2024-01-31T23:59:59",
    "skills": "Giao tiếp, Phục vụ",
    "companyName": "Sushi Hokkaido",
    "salaryUnit": "ca",
    "workingHours": "17:00-22:00",
    "workingDays": "Cuối tuần",
    "workMode": "ONSITE",
    "category": "Dịch vụ",
    "contactPhone": "0912345678"
}
```

### Job 4: Gia sư Toán (Part-time, Hybrid)
```json
{
    "title": "Gia sư Toán - Lớp 10",
    "description": "Tìm gia sư Toán cho học sinh lớp 10. Có thể dạy online hoặc tại nhà.",
    "location": "Quận 2, TP.HCM",
    "salary": 300000,
    "jobType": "PART_TIME",
    "startAt": "2024-02-01T18:00:00",
    "deadline": "2024-01-31T23:59:59",
    "skills": "Toán, Giảng dạy",
    "companyName": "Trung tâm Gia sư ABC",
    "salaryUnit": "buổi",
    "workingHours": "18:00-20:00",
    "workingDays": "Thứ 3, 5, 7",
    "workMode": "HYBRID",
    "category": "Giáo dục",
    "contactPhone": "0123456789"
}
```

### Job 5: Nhân viên bán hàng (Full-time, Onsite)
```json
{
    "title": "Nhân viên bán hàng - Shop thời trang",
    "description": "Tìm nhân viên bán hàng cho shop thời trang. Làm việc full-time.",
    "location": "Quận 1, TP.HCM",
    "salary": 8000000,
    "jobType": "FULL_TIME",
    "startAt": "2024-02-01T09:00:00",
    "deadline": "2024-01-31T23:59:59",
    "skills": "Bán hàng, Giao tiếp",
    "companyName": "Shop Thời Trang XYZ",
    "salaryUnit": "tháng",
    "workingHours": "09:00-18:00",
    "workingDays": "Thứ 2 - Thứ 6",
    "workMode": "ONSITE",
    "category": "Bán hàng",
    "contactPhone": "0123456789"
}
```

---

## 📝 APPLICATION TEST DATA

### Application 1: Job Seeker 1 apply Job 1
```json
{
    "jobId": "{{job_id}}",
    "coverLetter": "Tôi rất quan tâm đến vị trí gia sư Tiếng Anh. Tôi có kinh nghiệm giảng dạy Tiếng Anh 2 năm và có bằng IELTS 7.5. Tôi có thể làm việc vào các buổi tối thứ 2, 4, 6 như yêu cầu."
}
```

### Application 2: Job Seeker 2 apply Job 2
```json
{
    "jobId": "{{job_id_2}}",
    "coverLetter": "Tôi là designer có 3 năm kinh nghiệm. Tôi đã từng thiết kế poster cho nhiều sự kiện lớn. Portfolio của tôi: https://portfolio.example.com"
}
```

---

## ⭐ RATING TEST DATA

### Rating 1: Employer đánh giá Job Seeker sau khi hoàn thành job
```json
{
    "toUserId": "{{jobseeker_user_id}}",
    "jobId": "{{job_id}}",
    "score": 4.8,
    "comment": "Rất chuyên nghiệp và đúng giờ! Học sinh tiến bộ rõ rệt."
}
```

### Rating 2: Job Seeker đánh giá Employer
```json
{
    "toUserId": "{{employer_user_id}}",
    "jobId": "{{job_id}}",
    "score": 4.5,
    "comment": "Công ty rất chuyên nghiệp, thanh toán đúng hạn."
}
```

---

## 🔄 TEST SCENARIOS

### Scenario 1: Complete Flow - Job Seeker tìm việc part-time

**Bước 1:** Register Job Seeker
```json
POST /users/registration
{
    "email": "jobseeker1@test.com",
    "password": "password123",
    "fullName": "Nguyễn Văn A",
    "phoneNumber": "0123456789"
}
```

**Bước 2:** Login
```json
POST /auth/login
{
    "email": "jobseeker1@test.com",
    "password": "password123"
}
```

**Bước 3:** Update Profile với thông tin tìm việc
```json
PUT /users
{
    "skills": "Tiếng Anh, Giao tiếp",
    "preferredJobType": "PART_TIME",
    "availableDays": "Thứ 2, 4, 6",
    "availableTime": "Buổi tối",
    "preferredMinSalary": 200000
}
```

**Bước 4:** Xem danh sách job available
```json
GET /jobs/available?page=0&size=10
```

**Bước 5:** Xem chi tiết job (viewsCount tăng)
```json
GET /jobs/{{job_id}}
```

**Bước 6:** Apply job
```json
POST /applications/apply
{
    "jobId": "{{job_id}}",
    "coverLetter": "Tôi quan tâm đến vị trí này..."
}
```

**Bước 7:** Kiểm tra applicationCount đã tăng
```json
GET /jobs/{{job_id}}
// Expected: applicationCount = 1
```

---

### Scenario 2: Employer tạo job và quản lý applications

**Bước 1:** Register Employer
```json
POST /users/registration
{
    "email": "employer1@test.com",
    "password": "password123",
    "fullName": "Trung tâm Anh ngữ ILA",
    "phoneNumber": "0123456789"
}
```

**Bước 2:** Login
```json
POST /auth/login
{
    "email": "employer1@test.com",
    "password": "password123"
}
```

**Bước 3:** Tạo job với đầy đủ thông tin mới
```json
POST /jobs
{
    "title": "Gia sư Tiếng Anh - Lớp 12",
    "companyName": "Trung tâm Anh ngữ ILA",
    "salary": 250000,
    "salaryUnit": "buổi",
    "workingHours": "19:00-21:00",
    "workingDays": "Thứ 2, 4, 6",
    "workMode": "ONSITE",
    "category": "Giáo dục",
    ...
}
```

**Bước 4:** Admin approve job
```json
PUT /jobs/{{job_id}}/verify-job?status=APPROVED
```

**Bước 5:** Xem danh sách applications
```json
GET /applications/job/{{job_id}}?page=0&size=10
```

**Bước 6:** Accept application
```json
PUT /applications/{{application_id}}/status?status=ACCEPTED
```

**Bước 7:** Sau khi job hoàn thành, tạo rating
```json
POST /ratings
{
    "toUserId": "{{jobseeker_user_id}}",
    "jobId": "{{job_id}}",
    "score": 4.8,
    "comment": "Rất tốt!"
}
```

**Bước 8:** Kiểm tra rating trên job
```json
GET /jobs/{{job_id}}
// Expected: averageRating = 4.8, ratingCount = 1
```

---

## 📊 EXPECTED RESULTS

### Job Response sau khi có application và rating:
```json
{
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
    "contactPhone": "0123456789"
}
```

### User Response sau khi update:
```json
{
    "id": "user-uuid",
    "fullName": "Nguyễn Văn A",
    "email": "jobseeker1@test.com",
    "skills": "Tiếng Anh, Giao tiếp, Photoshop, Giảng dạy",
    "preferredJobType": "PART_TIME",
    "availableDays": "Thứ 2, 4, 6",
    "availableTime": "Buổi tối",
    "preferredMinSalary": 200000
}
```

---

## 🎯 Test Cases Checklist

### ✅ Job Entity Tests:
- [ ] Tạo job với đầy đủ các trường mới
- [ ] `companyName` hiển thị đúng
- [ ] `applicationCount` khởi tạo = 0
- [ ] `viewsCount` khởi tạo = 0
- [ ] `viewsCount` tăng khi xem chi tiết
- [ ] `applicationCount` tăng khi có application
- [ ] `applicationCount` giảm khi cancel application
- [ ] `averageRating` tính đúng từ Rating
- [ ] `ratingCount` tính đúng từ Rating
- [ ] `salaryUnit` hiển thị đúng ("buổi", "ca", "tháng")
- [ ] `workMode` hiển thị đúng ("ONSITE", "REMOTE", "HYBRID")
- [ ] `category` hiển thị đúng
- [ ] `workingHours` và `workingDays` hiển thị đúng

### ✅ User Entity Tests:
- [ ] Update user với các trường mới
- [ ] `skills` lưu và hiển thị đúng
- [ ] `preferredJobType` lưu và hiển thị đúng
- [ ] `availableDays` lưu và hiển thị đúng
- [ ] `availableTime` lưu và hiển thị đúng
- [ ] `preferredMinSalary` lưu và hiển thị đúng

---

**Sử dụng các dữ liệu này để test đầy đủ các tính năng mới! 🚀**


