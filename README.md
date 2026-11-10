# CAR VALUATION AI - NỀN TẢNG ĐỊNH GIÁ XE Ô TÔ THÔNG MINH

Hệ thống định giá xe ô tô đã qua sử dụng tích hợp AI (Gemini Vision OCR số khung VIN, ODO và nhận diện xước móp ngoại thất) kết hợp động cơ định giá thị trường viết bằng Java (**Java Pricing Engine**).

Dự án được xây dựng theo kiến trúc **Modular Monolith** trên nền tảng **Java 21**, **Spring Boot 3.3.5**, **Spring Security 6** và **Thymeleaf + Bootstrap 5**.

---

## 🌟 4 TÍNH NĂNG CỐT LÕI (MANDATORY CORE FEATURES)

1. **Quét Số VIN Bằng Camera & Giải Mã Chuẩn ISO 3779**:
   - Chụp ảnh số khung từ kính lái, tem kiểm định hoặc B-pillar bằng WebRTC Browser Camera (`navigator.mediaDevices.getUserMedia`).
   - Gemini Vision OCR nhận diện chuỗi 17 ký tự, loại bỏ ký tự cấm (I, O, Q), kiểm tra Check Digit (ký tự thứ 9 Modulo 11).
   - Hệ thống giải mã Composite kết hợp **vPIC NHTSA REST API** và **Nhà cung cấp quy tắc xe nội địa Việt Nam (VinFast, THACO, Toyota, Hyundai, Mazda, Ford, Kia)**.
   - Người dùng có thể đối soát và chỉnh sửa thông tin trước khi xác nhận.

2. **OCR Đọc Đồng Hồ Công Tơ Mét (ODO Mileage)**:
   - Chụp cụm đồng hồ táp-lô khi bật khóa điện.
   - AI Gemini Vision đọc chỉ số km lăn bánh thực tế.
   - Chuẩn hóa số nguyên, hiển thị đối soát người dùng trước khi lưu vào CSDL.

3. **AI Kiểm Tra Ngoại Thất & Nhận Diện Xước Móp**:
   - Tải lên 4 góc ảnh chính (**Trước, Sau, Trái, Phải**) + Ảnh cận cảnh vết xước (tùy chọn).
   - Gemini Vision phát hiện và phân loại khuyết tật: `SCRATCH` (Xước), `DENT` (Móp/lõm), `CRACK` (Nứt đèn/kính), `PAINT_CHIP` (Tróc sơn) theo các mức độ `LOW`, `MEDIUM`, `HIGH`.
   - **Cam kết minh bạch**: Hệ thống chỉ đánh giá bề mặt thân vỏ nhìn thấy được từ ảnh, *tuyệt đối không phán xét tai nạn đâm đụng khung gầm nặng hoặc ngập nước/thủy kích*.

4. **Quản Lý Tài Khoản & Phân Quyền RBAC 3 Cấp**:
   - **USER (Khách hàng)**: Đăng ký tài khoản, thực hiện định giá xe, quản lý lịch sử định giá của riêng mình.
   - **EXPERT (Chuyên gia thẩm định)**: Truy cập bàn thẩm định (`/expert/dashboard`), xem hồ sơ và ảnh xe khách hàng tải lên, đối soát và điều chỉnh giá kèm ghi chú thẩm định.
   - **ADMIN (Quản trị viên)**: Dashboard chỉ số toàn hệ thống, quản lý tài khoản người dùng, cấp phát vai trò RBAC, cập nhật bảng giá xe tham khảo thị trường.

---

## 🧮 ĐỘNG CƠ ĐỊNH GIÁ JAVA PRICING ENGINE

Thuật toán định giá 4 lớp độc lập:
$$\text{Giá Định Giá} = \text{Giá Cơ Sở (Base Price)} \pm \text{Hiệu chỉnh ODO} - \text{Khấu trừ Xước Móp} \pm \text{Điểm Bảng Hỏi Tình Trạng}$$

- **Giá cơ sở**: Tra cứu bảng giá thị trường theo Hãng, Dòng xe, Năm sản xuất và tỷ lệ khấu hao.
- **Hiệu chỉnh ODO**: So sánh với định mức trung bình **13.500 km/năm**:
  - Xe chạy ít (xe lướt, giữ gìn): Cộng thưởng giá (tối đa +6%).
  - Xe chạy nhiều (vượt chuẩn): Giảm trừ giá (tối đa -12%).
- **Khấu trừ ngoại thất**: Tính toán chi phí phục hồi sơn/gò hàn theo từng vết xước/móp từ báo cáo AI.
- **Bảng hỏi tình trạng**: Lịch sử bảo dưỡng hãng (Full Service History +2.5%), dàn lốp mới (+4 triệu) hoặc mòn (-6 triệu), thời hạn đăng kiểm còn trên 12 tháng (+2 triệu).

---

## 🔑 TÀI KHOẢN MẪU ĐƯỢC NẠP SẴN (PRE-SEEDED ACCOUNTS)

Hệ thống tự động khởi tạo sẵn các tài khoản mẫu khi khởi động:

| Vai Trò | Tên Đăng Nhập | Mật Khẩu | Quyền Hạn |
|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | Quản trị toàn hệ thống, quản lý người dùng & giá |
| **EXPERT** | `expert` | `expert123` | Bàn thẩm định, đối soát và duyệt giá chuyên gia |
| **USER** | `user` | `user123` | Khách hàng định giá xe và xem lịch sử cá nhân |

*(Khách hàng mới có thể đăng ký tài khoản tự do tại trang `/register`, mặc định nhận quyền `USER`)*.

---

## 🚀 HƯỚNG DẪN KHỞI CHẠY (QUICK START)

### Cách 1: Chạy ngay với H2 In-Memory Database (Mặc định - Cắm là chạy)

Không cần cài đặt MySQL, cơ sở dữ liệu H2 sẽ tự động tạo trong bộ nhớ:

```bash
cd C:\Users\XIAOXIN\Projects\car-valuation
mvn spring-boot:run
```

Truy cập ứng dụng tại: **http://localhost:8080**  
H2 Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:carvaldb`, User: `sa`, Password: để trống).

---

### Cách 2: Chạy với MySQL (Môi trường sản xuất)

1. Tạo CSDL trong MySQL:
   ```sql
   CREATE DATABASE car_valuation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Khởi chạy ứng dụng với profile `mysql`:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 🤖 CẤU HÌNH GOOGLE GEMINI AI KEY

Trong file `src/main/resources/application.properties`:
```properties
gemini.api.key=YOUR_GEMINI_API_KEY
```
Hoặc thiết lập biến môi trường hệ thống:
```bash
set GEMINI_API_KEY=AIzaSy...
```

> **Chế độ Mock Thông Minh (Offline/Keyless Mode):**  
> Nếu bạn chưa điền API Key, hệ thống **tự động kích hoạt cơ chế Smart Mock AI**. Bạn vẫn có thể trải nghiệm 100% quy trình từ quét VIN, OCR ODO, phân tích ảnh 4 góc và xuất báo cáo định giá đầy đủ mà không bị gián đoạn!

---

## 📁 CẤU TRÚC THƯ MỤC DỰ ÁN

```
car-valuation/
├── pom.xml                               # Quản lý thư viện Maven (Java 21, Spring Boot 3.3.5)
├── README.md                             # Tài liệu hướng dẫn
├── src/
│   ├── main/
│   │   ├── java/com/carvaluation/
│   │   │   ├── CarValuationApplication.java
│   │   │   ├── auth/                     # Spring Security, RBAC, User, AuthService
│   │   │   ├── vehicle/                  # Quản lý hồ sơ kỹ thuật xe
│   │   │   ├── vin/                      # ISO 3779, NHTSA API & Vietnam Rule Decoder
│   │   │   ├── mileage/                  # OCR ODO đồng hồ táp-lô
│   │   │   ├── damage/                   # AI phân tích xước móp ngoại thất & báo cáo
│   │   │   ├── pricing/                  # Java Rule-Based Pricing Engine 4 lớp
│   │   │   ├── valuation/                # Session wizard 5 bước, upload ảnh, kết quả
│   │   │   ├── expert/                   # Bàn thẩm định dành cho chuyên gia
│   │   │   ├── admin/                    # Dashboard quản trị, User RBAC, Data xe
│   │   │   ├── ai/                       # Client tích hợp Google Gemini 1.5 Flash
│   │   │   ├── storage/                  # Lưu trữ file ảnh upload
│   │   │   ├── config/                   # WebMvc & DataInitializer nạp dữ liệu mẫu
│   │   │   └── common/                   # ApiResponse wrapper
│   │   └── resources/
│   │       ├── application.properties    # Cấu hình H2, Gemini, Uploads
│   │       ├── application-mysql.properties # Cấu hình MySQL production
│   │       ├── static/
│   │       │   ├── css/custom.css        # Giao diện responsive hiện đại
│   │       │   └── js/camera.js          # WebRTC Camera API & Canvas capture
│   │       └── templates/                # Thymeleaf HTML Templates
│   │           ├── layout/base.html      # Layout chuẩn chung
│   │           ├── index.html            # Trang chủ & giới thiệu tính năng
│   │           ├── about.html            # Giới thiệu công nghệ & kiến trúc
│   │           ├── auth/                 # Login, Register, Profile
│   │           ├── valuation/            # Quy trình định giá 5 bước & Lịch sử
│   │           ├── expert/               # Dashboard thẩm định & duyệt giá
│   │           ├── admin/                # Dashboard Admin, quản lý User, giá xe
│   │           └── error/403.html        # Trang báo lỗi từ chối truy cập
│   └── test/
│       └── java/com/carvaluation/
│           ├── vin/VinValidatorTest.java
│           └── pricing/PricingStrategyTest.java
```

---

## 🛡️ BẢO MẬT & PHÂN QUYỀN URL (SECURITY MATRIX)

| URL Pattern | Quyền Truy Cập | Mục Đích |
|---|---|---|
| `/`, `/about`, `/login`, `/register`, `/css/**`, `/js/**` | `permitAll()` | Trang công khai, khách vãng lai |
| `/valuation/**`, `/history/**`, `/profile` | `ROLE_USER`, `ROLE_EXPERT`, `ROLE_ADMIN` | Định giá xe và quản lý hồ sơ cá nhân |
| `/expert/**` | `ROLE_EXPERT`, `ROLE_ADMIN` | Bàn thẩm định và duyệt giá của chuyên gia |
| `/admin/**` | `ROLE_ADMIN` | Quản trị người dùng và cấu hình giá cơ sở |
