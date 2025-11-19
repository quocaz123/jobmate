# Hướng Dẫn Test Waiting List API

## Tổng Quan
Waiting List cho phép người dùng đăng ký yêu cầu tìm việc và hệ thống sẽ tự động match với các công việc phù hợp.

## Base URL
```
http://localhost:8080/jobmate/waiting-list
```

## Yêu Cầu Trước Khi Test
1. **Đăng nhập và lấy JWT Token** (tất cả API đều yêu cầu authentication)
2. **User phải có location** (latitude/longitude) - nếu chưa có, cần update location trước
3. **Elasticsearch đang chạy** (để index dữ liệu)

---

## 1. Tạo Waiting List (POST /waiting-list)

### Request
```http
POST /jobmate/waiting-list
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

### Request Body
```json
{
  "jobType": "FULL_TIME",
  "skills": "Java;Spring Boot;SQL;Docker",
  "expectedMinSalary": 15000000,
  "searchRadius": 10,
  "availableDays": "Thứ 2 - Thứ 6",
  "availableTime": "8 giờ/ngày",
  "note": "Tìm việc lập trình viên Java, có kinh nghiệm 2+ năm"
}
```

### Response (Success - 200)
```json
{
  "code": 1000,
  "message": "Success",
  "data": {
    "id": "uuid-here",
    "userId": "user-uuid-here",
    "jobType": "FULL_TIME",
    "skills": "Java;Spring Boot;SQL;Docker",
    "expectedMinSalary": 15000000,
    "latitude": 10.762622,
    "longitude": 106.660172,
    "searchRadius": 10,
    "availableDays": "Thứ 2 - Thứ 6",
    "availableTime": "8 giờ/ngày",
    "note": "Tìm việc lập trình viên Java, có kinh nghiệm 2+ năm",
    "status": "PENDING",
    "createdAt": "2025-11-16T15:00:00",
    "updatedAt": null
  }
}
```

### Test Cases

#### ✅ Test Case 1: Tạo waiting list thành công
```bash
curl -X POST http://localhost:8080/jobmate/waiting-list \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jobType": "FULL_TIME",
    "skills": "Java;Spring Boot",
    "expectedMinSalary": 15000000,
    "searchRadius": 10,
    "availableDays": "Thứ 2 - Thứ 6",
    "availableTime": "8 giờ/ngày",
    "note": "Tìm việc Java developer"
  }'
```

#### ❌ Test Case 2: Tạo quá 5 waiting list active (sẽ lỗi)
- Tạo 6 waiting list liên tiếp → lỗi: "User already has an active waiting list"

#### ❌ Test Case 3: Thiếu required fields
- Bỏ trống `jobType` → validation error

---

## 2. Lấy Danh Sách Waiting List Của Tôi (GET /waiting-list/my-waiting)

### Request
```http
GET /jobmate/waiting-list/my-waiting
Authorization: Bearer {JWT_TOKEN}
```

### Response (Success - 200)
```json
{
  "code": 1000,
  "message": "Success",
  "data": [
    {
      "id": "uuid-1",
      "userId": "user-uuid",
      "jobType": "FULL_TIME",
      "skills": "Java;Spring Boot",
      "expectedMinSalary": 15000000,
      "latitude": 10.762622,
      "longitude": 106.660172,
      "searchRadius": 10,
      "availableDays": "Thứ 2 - Thứ 6",
      "availableTime": "8 giờ/ngày",
      "note": "Tìm việc Java developer",
      "status": "PENDING",
      "createdAt": "2025-11-16T15:00:00",
      "updatedAt": null
    },
    {
      "id": "uuid-2",
      "userId": "user-uuid",
      "jobType": "PART_TIME",
      "skills": "React;JavaScript",
      "expectedMinSalary": 5000000,
      "latitude": 10.762622,
      "longitude": 106.660172,
      "searchRadius": 5,
      "availableDays": "Thứ 7, Chủ nhật",
      "availableTime": "4 giờ/ngày",
      "note": "Tìm việc part-time frontend",
      "status": "PENDING",
      "createdAt": "2025-11-16T14:00:00",
      "updatedAt": null
    }
  ]
}
```

### Test Case
```bash
curl -X GET http://localhost:8080/jobmate/waiting-list/my-waiting \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 3. Xem Danh Sách Ứng Viên (GET /waiting-list/candidates) - Cho Employer

### Request
```http
GET /jobmate/waiting-list/candidates?jobType=FULL_TIME&skills=Java&minSalary=10000000
Authorization: Bearer {JWT_TOKEN}
```

### Query Parameters (tất cả đều optional)
- `jobType`: Lọc theo loại công việc (FULL_TIME, PART_TIME)
- `skills`: Tìm kiếm theo kỹ năng (tìm trong skills của waiting list)
- `minSalary`: Lọc theo mức lương tối thiểu (chỉ hiển thị waiting list có expectedMinSalary <= minSalary)

### Response (Success - 200)
```json
{
  "code": 1000,
  "message": "Success",
  "data": [
    {
      "id": "uuid-1",
      "userId": "user-uuid-1",
      "jobType": "FULL_TIME",
      "skills": "Java;Spring Boot;SQL",
      "expectedMinSalary": 15000000,
      "latitude": 10.762622,
      "longitude": 106.660172,
      "searchRadius": 10,
      "availableDays": "Thứ 2 - Thứ 6",
      "availableTime": "8 giờ/ngày",
      "note": "Tìm việc Java developer",
      "status": "PENDING",
      "createdAt": "2025-11-16T15:00:00",
      "updatedAt": null
    },
    {
      "id": "uuid-2",
      "userId": "user-uuid-2",
      "jobType": "FULL_TIME",
      "skills": "Java;React;JavaScript",
      "expectedMinSalary": 12000000,
      "latitude": 10.762622,
      "longitude": 106.660172,
      "searchRadius": 5,
      "availableDays": "Thứ 2 - Thứ 6",
      "availableTime": "8 giờ/ngày",
      "note": "Fullstack developer",
      "status": "PENDING",
      "createdAt": "2025-11-16T14:00:00",
      "updatedAt": null
    }
  ]
}
```

### Test Cases

#### ✅ Test Case 1: Lấy tất cả candidates (không filter)
```bash
curl -X GET "http://localhost:8080/jobmate/waiting-list/candidates" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ✅ Test Case 2: Lọc theo jobType
```bash
curl -X GET "http://localhost:8080/jobmate/waiting-list/candidates?jobType=FULL_TIME" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ✅ Test Case 3: Lọc theo skills
```bash
curl -X GET "http://localhost:8080/jobmate/waiting-list/candidates?skills=Java" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ✅ Test Case 4: Lọc theo minSalary
```bash
curl -X GET "http://localhost:8080/jobmate/waiting-list/candidates?minSalary=10000000" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ✅ Test Case 5: Kết hợp nhiều filter
```bash
curl -X GET "http://localhost:8080/jobmate/waiting-list/candidates?jobType=FULL_TIME&skills=Java&minSalary=12000000" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ❌ Test Case 6: User không phải EMPLOYER (sẽ lỗi 403)
- User thường không có quyền truy cập endpoint này

### Lưu ý:
- **Chỉ hiển thị waiting list có status = PENDING** (active)
- Waiting list có status = CLOSED, CANCELLED, MATCHED sẽ không hiển thị
- Tất cả query parameters đều optional, có thể kết hợp nhiều filter

---

## 4. Xóa/Đóng Waiting List (DELETE /waiting-list/{id})

### Request
```http
DELETE /jobmate/waiting-list/{id}
Authorization: Bearer {JWT_TOKEN}
```

### Response (Success - 200)
```json
{
  "code": 1000,
  "message": "Success",
  "data": null
}
```

### Test Cases

#### ✅ Test Case 1: Xóa thành công
```bash
curl -X DELETE http://localhost:8080/jobmate/waiting-list/{waiting-list-id} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### ❌ Test Case 2: Xóa waiting list của user khác (sẽ lỗi)
- Lỗi: "Bạn không có quyền"

#### ❌ Test Case 3: Xóa waiting list không tồn tại
- Lỗi: "Waiting list not found"

---

## 5. Kiểm Tra Dữ Liệu Trong Elasticsearch

### Qua API
```bash
# Xem tất cả waiting requests trong ES
GET /jobmate/admin/elasticsearch/waiting-requests?page=0&size=10

# Xem waiting request theo ID
GET /jobmate/admin/elasticsearch/waiting-requests/{id}

# Đếm số lượng
GET /jobmate/admin/elasticsearch/waiting-requests/count
```

### Qua Elasticsearch REST API
```bash
# Xem tất cả documents
curl http://localhost:9200/waiting_requests_index/_search?pretty

# Xem document theo ID
curl http://localhost:9200/waiting_requests_index/_doc/{id}?pretty

# Đếm số documents
curl http://localhost:9200/waiting_requests_index/_count?pretty
```

---

## 6. Quy Trình Test Hoàn Chỉnh

### Bước 1: Đăng nhập và lấy token
```bash
POST /jobmate/auth/login
# Lưu JWT token
```

### Bước 2: Đảm bảo user có location
```bash
PUT /jobmate/users/location
# Update location nếu chưa có
```

### Bước 3: Tạo waiting list
```bash
POST /jobmate/waiting-list
# Tạo waiting list mới
```

### Bước 4: Kiểm tra trong database
```sql
SELECT * FROM waiting_list WHERE user_id = 'your-user-id';
```

### Bước 5: Kiểm tra trong Elasticsearch
```bash
GET /jobmate/admin/elasticsearch/waiting-requests
# Xem dữ liệu đã được index chưa
```

### Bước 6: Lấy danh sách waiting list của tôi
```bash
GET /jobmate/waiting-list/my-waiting
# Xem danh sách waiting list đã tạo
```

### Bước 7: Xóa waiting list
```bash
DELETE /jobmate/waiting-list/{id}
# Đóng waiting list
```

### Bước 8: Kiểm tra lại trong ES
```bash
GET /jobmate/admin/elasticsearch/waiting-requests/{id}
# Xem status đã update thành CLOSED chưa
```

---

## 7. Các Trường Hợp Cần Test

### ✅ Happy Path
1. Tạo waiting list thành công
2. Lấy danh sách waiting list
3. Xóa waiting list thành công
4. Dữ liệu được index vào ES đúng

### ❌ Error Cases
1. Tạo quá 5 waiting list active → lỗi
2. Xóa waiting list của user khác → lỗi
3. Xóa waiting list không tồn tại → lỗi
4. User chưa có location → có thể lỗi hoặc latitude/longitude = null

### 🔍 Edge Cases
1. Tạo waiting list với tất cả fields null/empty
2. Tạo waiting list với searchRadius = 0
3. Tạo nhiều waiting list rồi xóa một số, kiểm tra countActiveByUserId

---

## 8. Kiểm Tra Dữ Liệu Index Vào ES

Sau khi tạo waiting list, kiểm tra:

1. **Trong Database**: 
   - Record được tạo trong bảng `waiting_list`
   - `status` = `PENDING`
   - `createdAt` được set tự động

2. **Trong Elasticsearch**:
   - Document được tạo trong index `waiting_requests_index`
   - `createdAt` được lưu dưới dạng timestamp (Long)
   - `location` (GeoPoint) có đúng latitude/longitude

3. **Sau khi xóa/đóng**:
   - `status` trong database = `CLOSED`
   - Document trong ES được update với status mới

---

## 9. Ví Dụ Test Với Postman/Thunder Client

### Collection JSON
```json
{
  "name": "Waiting List API",
  "requests": [
    {
      "name": "Create Waiting List",
      "method": "POST",
      "url": "http://localhost:8080/jobmate/waiting-list",
      "headers": {
        "Authorization": "Bearer {{token}}",
        "Content-Type": "application/json"
      },
      "body": {
        "jobType": "FULL_TIME",
        "skills": "Java;Spring Boot",
        "expectedMinSalary": 15000000,
        "searchRadius": 10,
        "availableDays": "Thứ 2 - Thứ 6",
        "availableTime": "8 giờ/ngày",
        "note": "Tìm việc Java developer"
      }
    },
    {
      "name": "Get My Waiting Lists",
      "method": "GET",
      "url": "http://localhost:8080/jobmate/waiting-list/my-waiting",
      "headers": {
        "Authorization": "Bearer {{token}}"
      }
    },
    {
      "name": "Delete Waiting List",
      "method": "DELETE",
      "url": "http://localhost:8080/jobmate/waiting-list/{{waitingListId}}",
      "headers": {
        "Authorization": "Bearer {{token}}"
      }
    }
  ]
}
```

---

## 10. Checklist Test

- [ ] Tạo waiting list thành công
- [ ] Dữ liệu được lưu vào database
- [ ] Dữ liệu được index vào Elasticsearch
- [ ] Lấy danh sách waiting list của tôi
- [ ] Xóa waiting list thành công
- [ ] Status được update thành CLOSED
- [ ] ES được update sau khi xóa
- [ ] Test lỗi: tạo quá 5 waiting list
- [ ] Test lỗi: xóa waiting list của user khác
- [ ] Test lỗi: xóa waiting list không tồn tại
- [ ] Test API cho employer: lấy danh sách candidates
- [ ] Test filter theo jobType
- [ ] Test filter theo skills
- [ ] Test filter theo minSalary
- [ ] Test kết hợp nhiều filter
- [ ] Test user không phải EMPLOYER không thể truy cập

---

## 11. Debug Tips

1. **Kiểm tra log**: Xem log để biết có lỗi khi index vào ES không
2. **Kiểm tra database**: Query trực tiếp để xem dữ liệu có đúng không
3. **Kiểm tra ES**: Dùng API hoặc curl để xem document trong ES
4. **Kiểm tra user location**: Đảm bảo user có latitude/longitude trước khi tạo waiting list

