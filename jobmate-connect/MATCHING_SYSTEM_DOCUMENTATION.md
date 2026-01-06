# 📋 Tài Liệu Hệ Thống Matching - JobMate Connect

## 🎯 Tổng Quan

Hệ thống matching của JobMate Connect là một công cụ thông minh để kết nối người tìm việc với công việc phù hợp và ngược lại. Hệ thống sử dụng thuật toán tính điểm dựa trên nhiều tiêu chí để đánh giá mức độ phù hợp giữa User và Job.

---

## 🔬 Kỹ Thuật & Phương Pháp Được Sử Dụng

### **1. Content-Based Filtering (Lọc dựa trên nội dung)**
- **Mô tả**: Hệ thống phân tích và so sánh các thuộc tính của User và Job để tìm sự phù hợp
- **Áp dụng**: 
  - So sánh skills, salary, schedule, location giữa user và job
  - Không dựa vào hành vi người dùng khác, chỉ dựa trên profile của chính user đó

### **2. Multi-Criteria Decision Making (MCDM) - Ra quyết định đa tiêu chí**
- **Mô tả**: Tính điểm tổng hợp dựa trên nhiều tiêu chí khác nhau
- **Tiêu chí**:
  - Skills (50 điểm)
  - Salary (20 điểm)
  - Distance (20 điểm)
  - Schedule (10 điểm)
- **Phương pháp**: Weighted Sum Model (Mô hình tổng trọng số)

### **3. Elasticsearch Full-Text Search**
- **Kỹ thuật**: 
  - **Fuzzy Matching**: Tìm kiếm với độ sai lệch cho phép (fuzziness="AUTO")
  - **Boolean Query**: Kết hợp nhiều điều kiện với must, should, filter
  - **Minimum Should Match**: Yêu cầu ít nhất 1 skill match
  - **Range Query**: Lọc salary theo khoảng giá trị

### **4. Semantic Matching (Matching ngữ nghĩa)**
- **Kỹ thuật**: Skill Synonym Matching
- **Phương pháp**:
  - Sử dụng từ điển synonyms để nhận diện kỹ năng tương đương
  - Ví dụ: "Spring Boot" = "Spring Framework", "MySQL" = "Database"
- **File dữ liệu**: `skill_synonyms.json`

### **5. Fuzzy String Matching (Matching chuỗi mờ)**
- **Thuật toán**: Levenshtein Distance
- **Mục đích**: Tìm kiếm kỹ năng với độ sai lệch cho phép (typo, viết tắt)
- **Ngưỡng**: Cho phép sai lệch ≤ 2 ký tự
- **Ví dụ**: "Java" có thể match với "Javva" (sai 1 ký tự)

### **6. Geographic Distance Calculation (Tính khoảng cách địa lý)**
- **Thuật toán**: Haversine Formula
- **Mục đích**: Tính khoảng cách giữa 2 điểm GPS trên bề mặt Trái Đất
- **Độ chính xác**: Cao (xét đến độ cong của Trái Đất)
- **Công thức**:
  ```
  a = sin²(Δφ/2) + cos(φ1) × cos(φ2) × sin²(Δλ/2)
  c = 2 × atan2(√a, √(1-a))
  distance = R × c
  ```
  Trong đó: R = 6371 km (bán kính Trái Đất)

### **7. Text Normalization (Chuẩn hóa văn bản)**
- **Kỹ thuật**: 
  - Unicode Normalization (loại bỏ dấu tiếng Việt)
  - Lowercase conversion
  - Trim whitespace
- **Mục đích**: So sánh text không phân biệt hoa thường và dấu
- **Ví dụ**: "JAVA" = "java" = "Javá"

### **8. Salary Normalization (Chuẩn hóa lương)**
- **Kỹ thuật**: Unit Conversion (Chuyển đổi đơn vị)
- **Phương pháp**: Quy đổi tất cả đơn vị lương về VND_PER_HOUR
- **Mục đích**: So sánh công bằng giữa các đơn vị khác nhau
- **Ví dụ**: 
  - VND_PER_DAY → chia 8
  - VND_PER_MONTH → chia 208
  - VND_PER_SHIFT → chia 4

### **9. Time Interval Overlap Detection (Phát hiện chồng chéo thời gian)**
- **Thuật toán**: Interval Overlap Check
- **Logic**: 
  ```
  Two intervals overlap if: start1 ≤ end2 AND start2 ≤ end1
  ```
- **Mục đích**: Kiểm tra xem thời gian rảnh của user có overlap với thời gian làm việc của job không

### **10. Sentinel Value Pattern (Mẫu giá trị đánh dấu)**
- **Kỹ thuật**: Sử dụng giá trị đặc biệt (-1) để đánh dấu trạng thái "không xác định"
- **Áp dụng**: `distance = -1` khi không tính được khoảng cách
- **Lợi ích**: Phân biệt rõ ràng giữa "không tính được" và "khoảng cách = 0"

### **11. Multi-Stage Filtering (Lọc nhiều giai đoạn)**
- **Giai đoạn 1**: Elasticsearch query filter (status, jobType, skills, salary)
- **Giai đoạn 2**: In-memory filter (distance radius)
- **Giai đoạn 3**: Score-based ranking và limit top results
- **Lợi ích**: Giảm số lượng dữ liệu cần xử lý ở mỗi giai đoạn

### **12. Score-Based Ranking (Xếp hạng theo điểm)**
- **Phương pháp**: Tính điểm tổng hợp và sắp xếp giảm dần
- **Công thức**: `Total Score = Skill Score + Salary Score + Distance Score + Schedule Score`
- **Kết quả**: Top 20 jobs/users có điểm cao nhất

### **13. Inverted Index (Chỉ mục đảo ngược)**
- **Công nghệ**: Elasticsearch sử dụng inverted index
- **Mục đích**: Tìm kiếm nhanh trong large dataset
- **Áp dụng**: Index các field: skills, title, description, status, jobType

### **14. Caching Strategy (Chiến lược cache)**
- **Mục đích**: Tối ưu performance
- **Có thể áp dụng**:
  - Cache skill synonyms (load 1 lần khi khởi động)
  - Cache matching results (Redis)
  - Cache geocoding results

### **15. Pagination & Limiting (Phân trang và giới hạn)**
- **Kỹ thuật**: 
  - Limit kết quả từ ES: 300 jobs
  - Limit kết quả cuối: Top 20
- **Mục đích**: Giảm memory usage và response time

---

## 📊 Tổng Hợp Kỹ Thuật Theo Component

| Component | Kỹ Thuật/Phương Pháp | Mục Đích |
|-----------|---------------------|----------|
| **Elasticsearch Query** | Boolean Query, Fuzzy Search, Range Query | Lọc job nhanh chóng |
| **Skill Matching** | Synonym Matching, Fuzzy String Matching, Text Normalization | Tìm kỹ năng tương đương |
| **Distance Calculation** | Haversine Formula | Tính khoảng cách chính xác |
| **Salary Comparison** | Unit Normalization | So sánh công bằng |
| **Schedule Matching** | Interval Overlap Detection | Kiểm tra thời gian phù hợp |
| **Score Calculation** | Weighted Sum Model (MCDM) | Tính điểm tổng hợp |
| **Ranking** | Score-Based Sorting | Sắp xếp kết quả |
| **Error Handling** | Sentinel Value Pattern | Xử lý trường hợp đặc biệt |

---

## 🔄 Hai Hướng Matching

### 1. **Matching User với Job** (Recommend Jobs for User)
- **Mục đích**: Gợi ý công việc phù hợp cho người tìm việc
- **API**: `GET /recommend/jobs`
- **Service**: `RecommendJobsService.recommend()`
- **Method tính điểm**: `MatchingEngine.calculateScoreJobForUser()`

### 2. **Matching Job với User** (Recommend Users for Job)
- **Mục đích**: Gợi ý ứng viên phù hợp cho nhà tuyển dụng
- **API**: `GET /recommend/users?jobId={jobId}`
- **Service**: `RecommendJobsService.recommendWaitingListForJob()`
- **Method tính điểm**: `MatchingEngine.calculateScoreUserForJob()`

---

## 🧮 Công Thức Tính Điểm Matching

### **Tổng điểm tối đa: 100 điểm**

Hệ thống tính điểm dựa trên 4 tiêu chí chính:

### 1. **Kỹ Năng (Skills)** - Tối đa 50 điểm

#### Cách tính:
- **Direct Match**: Mỗi kỹ năng của user khớp trực tiếp với job → **+10 điểm/skill**
- **Synonym Match**: Kỹ năng khớp qua từ đồng nghĩa → **+10 điểm/skill**
- **Fuzzy Match**: Kỹ năng khớp qua thuật toán Levenshtein (sai lệch ≤ 2 ký tự) → **+10 điểm/skill**
- **Giới hạn**: Tối đa **50 điểm** (dù có nhiều skill match)

#### Công nghệ sử dụng:
- **SkillSynonymService**: 
  - Normalize text (loại bỏ dấu, lowercase)
  - Load synonyms từ file `skill_synonyms.json`
  - Fuzzy matching với Levenshtein distance
- **Elasticsearch**: 
  - Tìm kiếm job với fuzziness="AUTO" trong query
  - Minimum should match: ít nhất 1 skill match

#### Ví dụ:
```
User skills: "Java, Spring Boot, MySQL"
Job skills: "Java, Spring Framework, Database"

- "Java" → Direct match → +10 điểm
- "Spring Boot" → Synonym match với "Spring Framework" → +10 điểm  
- "MySQL" → Synonym match với "Database" → +10 điểm

Tổng: 30 điểm (trong giới hạn 50)
```

---

### 2. **Lương (Salary)** - Tối đa 20 điểm

#### Cách tính:
- **Normalize Salary**: Chuyển đổi tất cả đơn vị lương về **VND_PER_HOUR** để so sánh
- **Điểm số**:
  - Lương job ≥ lương mong muốn của user → **20 điểm**
  - Lương job ≥ 80% lương mong muốn → **10 điểm**
  - Còn lại → **0 điểm**

#### Công thức Normalize Salary:

| Đơn vị | Công thức quy đổi |
|--------|-------------------|
| VND_PER_HOUR | `salary` (giữ nguyên) |
| VND_PER_SHIFT | `salary / 4` (ca 4 tiếng) |
| VND_PER_DAY | `salary / 8` (8 tiếng/ngày) |
| VND_PER_WEEK | `salary / 48` (6 ngày × 8 tiếng) |
| VND_PER_MONTH | `salary / 208` (26 ngày × 8 tiếng) |
| VND_PER_PROJECT | `salary / 20` (ước lượng 20 giờ) |
| VND_PER_PRODUCT | `salary / 5` (ước lượng 5 giờ) |
| VND_PER_TASK | `salary / 3` (ước lượng 3 giờ) |
| VND_PER_ORDER | `salary / 0.3` (18 phút = 0.3h) |
| VND_PER_KM | `salary / 0.1` (6 phút = 0.1h) |
| VND_PER_SESSION | `salary / 2` (2 tiếng/buổi) |
| VND_PER_STUDENT | `salary / 1` (1 giờ/học sinh) |
| COMMISSION, BONUS, NEGOTIABLE | `0` (không normalize được) |

#### Ví dụ:
```
User mong muốn: 50,000 VND/giờ
Job trả: 200,000 VND/ca (VND_PER_SHIFT)

Normalize: 200,000 / 4 = 50,000 VND/giờ
So sánh: 50,000 ≥ 50,000 → 20 điểm ✅
```

---

### 3. **Khoảng Cách (Distance)** - Tối đa 20 điểm

#### Cách tính:
- **Công thức**: Sử dụng **Haversine Formula** để tính khoảng cách giữa 2 điểm GPS
- **Điểm số**:
  - Khoảng cách ≤ bán kính tìm kiếm → **20 điểm**
  - Khoảng cách ≤ (bán kính + 3km) → **10 điểm**
  - Còn lại → **0 điểm**

#### 📐 Haversine Formula - Công Thức Chi Tiết

**Haversine Formula** là công thức toán học để tính khoảng cách giữa hai điểm trên bề mặt của một hình cầu (Trái Đất) dựa trên tọa độ vĩ độ (latitude) và kinh độ (longitude).

##### **Công thức toán học:**

```
a = sin²(Δφ/2) + cos(φ1) × cos(φ2) × sin²(Δλ/2)
c = 2 × atan2(√a, √(1-a))
distance = R × c
```

**Trong đó:**
- `φ1, φ2` (phi): Vĩ độ của điểm 1 và điểm 2 (radian)
- `λ1, λ2` (lambda): Kinh độ của điểm 1 và điểm 2 (radian)
- `Δφ = φ2 - φ1`: Chênh lệch vĩ độ
- `Δλ = λ2 - λ1`: Chênh lệch kinh độ
- `R = 6371 km`: Bán kính trung bình của Trái Đất
- `distance`: Khoảng cách tính bằng kilomet

##### **Implementation trong Code:**

```java
// File: GeocodingService.java
private static final double EARTH_RADIUS_KM = 6371.0;

public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    // Bước 1: Chuyển đổi chênh lệch độ sang radian
    double dLat = Math.toRadians(lat2 - lat1);  // Δφ
    double dLon = Math.toRadians(lon2 - lon1);  // Δλ
    
    // Bước 2: Tính a (hàm haversine)
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) 
            * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    // Bước 3: Tính c (góc trung tâm)
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    // Bước 4: Tính khoảng cách
    double distance = EARTH_RADIUS_KM * c;
    
    // Làm tròn đến 2 chữ số thập phân
    return Math.round(distance * 100.0) / 100.0;
}
```

##### **Giải thích từng bước:**

**Bước 1: Chuyển đổi sang radian**
```java
double dLat = Math.toRadians(lat2 - lat1);  // Ví dụ: 0.012378 radian
double dLon = Math.toRadians(lon2 - lon1);  // Ví dụ: 0.009828 radian
```
- Chuyển chênh lệch độ (degree) sang radian vì các hàm sin/cos trong Java nhận radian

**Bước 2: Tính hàm haversine (a)**
```java
double a = sin²(Δφ/2) + cos(φ1) × cos(φ2) × sin²(Δλ/2)
```
- `sin²(Δφ/2)`: Bình phương sin của nửa chênh lệch vĩ độ
- `cos(φ1) × cos(φ2)`: Tích cos của 2 vĩ độ
- `sin²(Δλ/2)`: Bình phương sin của nửa chênh lệch kinh độ
- Kết quả `a` là một giá trị từ 0 đến 1

**Bước 3: Tính góc trung tâm (c)**
```java
double c = 2 × atan2(√a, √(1-a))
```
- `atan2`: Hàm arctangent 2 tham số, trả về góc trong khoảng [-π, π]
- `c` là góc trung tâm (central angle) giữa 2 điểm trên hình cầu, tính bằng radian

**Bước 4: Tính khoảng cách**
```java
double distance = R × c
```
- Nhân góc trung tâm với bán kính Trái Đất để được khoảng cách thực tế

##### **Ví dụ tính toán chi tiết:**

**Input:**
```
Điểm 1 (User): lat1 = 10.762622°, lon1 = 106.660172°
Điểm 2 (Job):  lat2 = 10.775000°, lon2 = 106.670000°
```

**Tính toán:**

1. **Chênh lệch:**
   ```
   Δlat = 10.775000 - 10.762622 = 0.012378°
   Δlon = 106.670000 - 106.660172 = 0.009828°
   ```

2. **Chuyển sang radian:**
   ```
   dLat = 0.012378 × (π/180) = 0.000216 radian
   dLon = 0.009828 × (π/180) = 0.000172 radian
   ```

3. **Tính a:**
   ```
   a = sin²(0.000216/2) + cos(10.762622°) × cos(10.775000°) × sin²(0.000172/2)
   a = sin²(0.000108) + cos(0.1877) × cos(0.1878) × sin²(0.000086)
   a ≈ 0.0000000117 + 0.9824 × 0.9824 × 0.0000000074
   a ≈ 0.0000000117 + 0.0000000071
   a ≈ 0.0000000188
   ```

4. **Tính c:**
   ```
   c = 2 × atan2(√0.0000000188, √(1-0.0000000188))
   c = 2 × atan2(0.000137, 0.99999999)
   c ≈ 2 × 0.000137
   c ≈ 0.000274 radian
   ```

5. **Tính khoảng cách:**
   ```
   distance = 6371 × 0.000274
   distance ≈ 1.75 km
   ```

**Kết quả:** Khoảng cách giữa 2 điểm là **1.75 km** (làm tròn 2 chữ số = **1.75 km**)

##### **Tại sao dùng Haversine Formula?**

1. **Độ chính xác cao**: Xét đến độ cong của Trái Đất, phù hợp cho khoảng cách > 1km
2. **Ổn định số học**: Tránh lỗi làm tròn khi tính toán với số nhỏ
3. **Hiệu quả**: Chỉ cần 4 phép tính sin/cos, nhanh hơn các phương pháp khác
4. **Phù hợp GPS**: Được thiết kế đặc biệt cho tọa độ địa lý

##### **So sánh với công thức khác:**

| Phương pháp | Độ chính xác | Phạm vi | Độ phức tạp |
|-------------|--------------|---------|-------------|
| **Haversine** | Cao (99.9%) | Toàn cầu | O(1) |
| Euclidean (phẳng) | Thấp | < 20km | O(1) |
| Vincenty | Rất cao (99.99%) | Toàn cầu | O(1) - phức tạp hơn |

**Kết luận**: Haversine là lựa chọn tốt cho ứng dụng matching job vì cân bằng giữa độ chính xác và hiệu năng.

#### Ví dụ sử dụng:
```
User location: 10.762622, 106.660172 (TP.HCM)
Job location: 10.775000, 106.670000
Bán kính tìm kiếm: 10 km

Khoảng cách tính được: 1.75 km
1.75 km ≤ 10 km → 20 điểm ✅
```

---

### 4. **Lịch Làm Việc (Schedule)** - Tối đa 10 điểm

#### Cách tính:
- **Match ngày**: Kiểm tra xem có ngày nào trùng khớp không
- **Match giờ**: Kiểm tra xem thời gian có overlap không
- **Điểm số**:
  - Cả ngày VÀ giờ đều match → **10 điểm**
  - Chỉ ngày HOẶC chỉ giờ match → **5 điểm**
  - Không match → **0 điểm**

#### Logic kiểm tra:
```java
// Match ngày: jobDays có chứa ít nhất 1 ngày trong userDays
boolean okDay = jobDays.stream().anyMatch(userDays::contains);

// Match giờ: Kiểm tra overlap
// userFrom ≤ jobTo && userTo ≥ jobFrom
boolean okTime = !(userFrom.isAfter(jobTo) || userTo.isBefore(jobFrom));
```

#### Ví dụ:
```
User available: "Monday,Wednesday,Friday" - "08:00-17:00"
Job schedule: "Monday,Friday" - "09:00-18:00"

- Ngày: Monday và Friday match → okDay = true
- Giờ: 08:00-17:00 overlap với 09:00-18:00 → okTime = true

Cả 2 đều match → 10 điểm ✅
```

---

## 🔍 Quy Trình Matching User với Job

### **Bước 1: Lấy thông tin User và WaitingList**
```java
User user = userRepository.findById(userId);
WaitingList wl = waitingListRepository.findById(waitingListId)
    // hoặc build từ user profile nếu không có waitingList
```

### **Bước 2: Query Elasticsearch để lọc Job**
- **Filter theo Status**: Chỉ lấy job `APPROVED`
- **Filter theo JobType**: Nếu user có preferred job type
- **Filter theo Skills**: Tìm job có ít nhất 1 skill match (fuzzy search)
- **Filter theo Salary**: Job phải có lương ≥ lương mong muốn (đã normalize)
- **Limit**: Lấy tối đa 300 jobs từ ES

### **Bước 3: Tính khoảng cách và lọc theo bán kính**
```java
// Khởi tạo distance = -1 (giá trị mặc định)
double distance = -1;

// Chỉ tính distance nếu có đủ thông tin location
if (job.getLocation() != null && 
    wl.getLatitude() != null && 
    wl.getLongitude() != null) {
    distance = geocodingService.calculateDistance(
        wl.getLatitude(), wl.getLongitude(),
        job.getLocation().getLat(), job.getLocation().getLon()
    );
}

// Lọc jobs: giữ lại nếu:
// - Không có search radius (null)
// - Distance = -1 (không tính được) → vẫn giữ
// - Distance <= searchRadius
jobs = jobs.filter(j -> 
    wl.getSearchRadius() == null ||
    j.getDistance() == -1 ||
    j.getDistance() <= wl.getSearchRadius()
)
// Sort: distance = -1 được đặt ở cuối (Double.MAX_VALUE)
.sortByDistance()
```

### **Bước 4: Tính điểm Matching cho mỗi Job**
```java
for (Job job : jobs) {
    score = matchingEngine.calculateScoreJobForUser(user, wl, job);
    // score = skillScore + salaryScore + distanceScore + scheduleScore
    job.setScore(score);
}
```

### **Bước 5: Sắp xếp và trả về Top 20**
```java
jobs = jobs.sortByScoreDescending()
    .limit(20);
```

---

## 🔍 Quy Trình Matching Job với User

### **Bước 1: Lấy thông tin Job**
```java
Job job = jobRepository.findById(jobId);
```

### **Bước 2: Lấy tất cả WaitingList**
```java
List<WaitingList> allWaitingLists = waitingListRepository.findAll();
```

### **Bước 3: Tính điểm cho mỗi WaitingList**
```java
for (WaitingList wl : allWaitingLists) {
    distance = calculateDistance(job, wl);
    score = matchingEngine.calculateScoreUserForJob(job, wl, distance);
    // score = skillScore + salaryScore + distanceScore + scheduleScore
}
```

### **Bước 4: Lọc theo bán kính và sắp xếp**
```java
waitingLists = waitingLists
    .filter(wl -> distance <= wl.searchRadius)
    .sortByScoreDescending()
    .limit(20);
```

---

## 🛠️ Công Nghệ Sử Dụng

### 1. **Elasticsearch**
- **Mục đích**: Tìm kiếm và lọc job nhanh chóng
- **Index**: `JobES` (Job Elasticsearch)
- **Features**:
  - Full-text search với fuzziness
  - Range query cho salary
  - Term query cho status và jobType
  - Boolean query với minimum should match

### 2. **SkillSynonymService**
- **Mục đích**: Xử lý matching kỹ năng thông minh
- **Features**:
  - Normalize text (loại bỏ dấu, lowercase)
  - Load synonyms từ JSON file
  - Fuzzy matching với Levenshtein distance
  - Smart synonym lookup (tìm theo từng từ)

### 3. **GeocodingService**
- **Mục đích**: Tính khoảng cách địa lý
- **Công thức**: Haversine Formula
- **Đơn vị**: Kilomet (km)

### 4. **MatchingEngine**
- **Mục đích**: Core engine tính điểm matching
- **Methods**:
  - `calculateScoreJobForUser()`: Tính điểm job cho user
  - `calculateScoreUserForJob()`: Tính điểm user cho job
  - `normalizeSalary()`: Chuyển đổi đơn vị lương
  - `skillScore()`: Tính điểm kỹ năng
  - `salaryScore()`: Tính điểm lương
  - `distanceScore()`: Tính điểm khoảng cách
  - `scheduleScore()`: Tính điểm lịch làm việc

---

## 📊 Ví Dụ Tính Điểm Chi Tiết

### **Scenario: User tìm việc Part-time**

**User Profile:**
- Skills: "Java, Spring Boot, MySQL"
- Expected Salary: 50,000 VND/giờ
- Location: 10.762622, 106.660172 (TP.HCM)
- Search Radius: 10 km
- Available: "Saturday,Sunday" - "08:00-17:00"

**Job:**
- Skills: "Java, Spring Framework, Database"
- Salary: 200,000 VND/ca (VND_PER_SHIFT)
- Location: 10.775000, 106.670000
- Schedule: "Saturday" - "09:00-18:00"
- JobType: PARTTIME

**Tính điểm:**

1. **Skill Score**: 
   - Java: Direct match → +10
   - Spring Boot: Synonym với Spring Framework → +10
   - MySQL: Synonym với Database → +10
   - **Tổng: 30 điểm** (giới hạn 50)

2. **Salary Score**:
   - Normalize: 200,000 / 4 = 50,000 VND/giờ
   - 50,000 ≥ 50,000 → **20 điểm**

3. **Distance Score**:
   - Distance: 1.5 km
   - 1.5 ≤ 10 km → **20 điểm**

4. **Schedule Score**:
   - Ngày: Saturday match → okDay = true
   - Giờ: 08:00-17:00 overlap 09:00-18:00 → okTime = true
   - Cả 2 match → **10 điểm**

**Tổng điểm: 30 + 20 + 20 + 10 = 80 điểm** ⭐

---

## 🎯 Điểm Khác Biệt Giữa 2 Hướng Matching

| Tiêu chí | User → Job | Job → User |
|----------|-----------|------------|
| **Skill Score** | Tối đa 50 điểm | Tối đa 50 điểm |
| **Salary Score** | Job ≥ User expect → 20 điểm | Job ≥ User expect → 20 điểm |
| **Distance Score** | Dựa trên user search radius | Dựa trên user search radius |
| **Schedule Score** | Tối đa 10 điểm | Tối đa 10 điểm |
| **Data Source** | Elasticsearch (JobES) | Database (WaitingList) |
| **Filter** | ES Query với filters | In-memory filter |

---

## 📈 Tối Ưu Hóa

### 1. **Elasticsearch Query Optimization**
- Sử dụng `must` và `filter` thay vì `should` khi có thể
- Limit kết quả ban đầu (300 jobs) trước khi tính điểm
- Index các field thường query: `status`, `jobType`, `skills`, `salaryPerHour`

### 2. **Caching**
- Có thể cache kết quả matching trong Redis
- Cache skill synonyms để tránh load lại

### 3. **Performance**
- Tính điểm chỉ cho jobs đã pass filter
- Sắp xếp theo distance trước, sau đó mới tính score
- Limit kết quả cuối cùng (Top 20)

---

## 🔧 Cấu Hình

### **Default Search Radius**: 25 km
```java
private static final int DEFAULT_SEARCH_RADIUS_KM = 25;
```

### **Max Results**: 20 jobs/users
```java
.limit(20)
```

### **ES Query Limit**: 300 jobs
```java
.withPageable(PageRequest.of(0, 300))
```

---

## 📝 Lưu Ý

1. **Normalize Salary**: Luôn normalize về VND_PER_HOUR để so sánh công bằng
2. **Skill Matching**: Sử dụng synonyms và fuzzy matching để tăng độ chính xác
3. **Distance Calculation**: Sử dụng Haversine formula cho độ chính xác cao
4. **Schedule Overlap**: Kiểm tra overlap thời gian, không chỉ exact match
5. **Error Handling**: Nếu thiếu thông tin, điểm tương ứng = 0 (không throw exception)
6. **Distance = -1**: Giá trị đặc biệt đánh dấu không thể tính được khoảng cách

---

## ❓ Giải Thích: Distance = -1

### **Tại sao distance có thể là -1?**

`distance = -1` là một **giá trị sentinel** (giá trị đặc biệt) được sử dụng để đánh dấu rằng **không thể tính được khoảng cách** giữa user và job.

### **Khi nào distance = -1?**

Distance sẽ là `-1` trong các trường hợp sau:

1. **Job không có location** (`job.getLocation() == null`)
   - Job chưa được cập nhật tọa độ GPS
   - Job chỉ có địa chỉ text, chưa được geocode

2. **User/WaitingList không có tọa độ** (`wl.getLatitude() == null` hoặc `wl.getLongitude() == null`)
   - User chưa cập nhật vị trí trong profile
   - WaitingList không có thông tin location

3. **Thiếu một trong hai thông tin location**
   - Cần cả 2 điểm (user location và job location) mới tính được distance

### **Xử lý distance = -1 trong hệ thống:**

#### **1. Filter (Lọc kết quả)**
```java
// Job có distance = -1 VẪN ĐƯỢC GIỮ LẠI trong kết quả
jobs = jobs.filter(j -> 
    wl.getSearchRadius() == null ||           // Không có radius → giữ tất cả
    j.getDistance() == -1 ||                  // Không tính được distance → vẫn giữ
    j.getDistance() <= wl.getSearchRadius()   // Distance hợp lệ → kiểm tra radius
);
```

**Lý do**: Không nên loại bỏ job chỉ vì thiếu thông tin location. Job vẫn có thể phù hợp về skills, salary, schedule.

#### **2. Sort (Sắp xếp)**
```java
// Khi sort theo distance, distance = -1 được đặt ở cuối
jobs = jobs.sorted((a, b) -> Double.compare(
    a.getDistance() == -1 ? Double.MAX_VALUE : a.getDistance(),
    b.getDistance() == -1 ? Double.MAX_VALUE : b.getDistance()
));
```

**Lý do**: Jobs có distance hợp lệ sẽ được ưu tiên hiển thị trước, jobs không có distance sẽ ở cuối danh sách.

#### **3. Tính điểm Matching**
```java
// Trong MatchingEngine.distanceScore()
public double distanceScore(double distance, Integer radius) {
    if (radius == null) return 0;        // Không có radius → 0 điểm
    if (distance <= radius) return 20;   // Trong bán kính → 20 điểm
    if (distance <= radius + 3) return 10; // Gần bán kính → 10 điểm
    return 0;                             // Ngoài bán kính → 0 điểm
}
```

**Lưu ý**: Nếu `distance = -1` được truyền vào, điều kiện `distance <= radius` sẽ là `false`, nên sẽ trả về **0 điểm** cho phần distance score.

### **Ví dụ thực tế:**

**Scenario 1: Job không có location**
```
Job: "Lập trình viên Java - Remote"
Location: null (không có tọa độ GPS)
User location: 10.762622, 106.660172

→ distance = -1
→ Job vẫn được hiển thị (vì có thể làm remote)
→ Distance score = 0 điểm
```

**Scenario 2: User chưa cập nhật location**
```
Job location: 10.775000, 106.670000
User location: null (chưa cập nhật)

→ distance = -1
→ Job vẫn được hiển thị
→ Distance score = 0 điểm
```

**Scenario 3: Có đủ thông tin**
```
Job location: 10.775000, 106.670000
User location: 10.762622, 106.660172

→ distance = 1.5 km (tính được)
→ Distance score = 20 điểm (nếu trong radius)
```

### **Tóm lại:**

- **Distance = -1** = "Không tính được khoảng cách"
- **Vẫn hiển thị job** trong kết quả (không filter ra)
- **Đặt ở cuối** khi sort theo distance
- **Distance score = 0 điểm** trong tính điểm matching
- **Không ảnh hưởng** đến các tiêu chí khác (skills, salary, schedule)

---

## 🚀 API Endpoints

### **1. Recommend Jobs for User**
```
GET /recommend/jobs?waitingListId={uuid}
```
- Trả về danh sách job được sắp xếp theo điểm matching
- Mỗi job có field `score` (điểm matching)

### **2. Recommend Users for Job**
```
GET /recommend/users?jobId={uuid}
```
- Trả về danh sách user (waiting list) được sắp xếp theo điểm matching
- Mỗi user có field `score` (điểm matching)

---

## 📚 File Liên Quan

- `MatchingEngine.java`: Core engine tính điểm
- `MatchingService.java`: Service tính match score cho application
- `RecommendJobsService.java`: Service gợi ý job/user
- `SkillSynonymService.java`: Xử lý skill synonyms
- `skill_synonyms.json`: File chứa danh sách synonyms
- `JobES.java`: Entity Elasticsearch cho Job
- `WaitingList.java`: Entity chứa thông tin user preferences

---

**Tài liệu được cập nhật lần cuối: 2024**

