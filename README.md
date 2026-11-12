# Car Valuation AI

Ứng dụng web hỗ trợ **định giá sơ bộ xe ô tô đã qua sử dụng**: nhận diện VIN và ODO từ ảnh, phân tích dấu hiệu hư hỏng *ngoại thất nhìn thấy được*, tính giá tham khảo theo quy tắc và hỗ trợ chuyên gia rà soát.

> **Trạng thái: MVP đang phát triển — chưa sẵn sàng triển khai công khai hoặc dùng cho quyết định giao dịch.** Kiểm thử hiện có chỉ bao phủ một phần VIN/pricing; luồng trình duyệt và các ranh giới bảo mật vẫn cần hoàn thiện. Không dùng kết quả ảnh để khẳng định tai nạn kết cấu hay ngập nước. Xem [các giới hạn hiện tại](docs/known-limitations.md).

## Kiến trúc client–server hiện tại

```text
Trình duyệt (client)
  ├─ HTML/CSS/JavaScript + Bootstrap, camera WebRTC
  └─ form và fetch (VIN/ODO/photo)
              │ HTTP(S)
              ▼
Spring Boot (server, Java 21)
  ├─ Spring MVC + Thymeleaf: render HTML và xử lý wizard
  ├─ Spring Security: session login, USER / EXPERT / ADMIN
  ├─ các endpoint /api/**: OCR và phân tích ảnh
  ├─ pricing engine + lưu trữ ảnh
  └─ JPA → H2 (demo) hoặc MySQL; Gemini Vision (hoặc mock)
```

**Đây là web client–server dạng server-rendered, chưa phải SPA với frontend và backend triển khai độc lập.** Mã client nằm ở `src/main/resources/templates` và `src/main/resources/static`; mã server nằm ở `src/main/java`. Giữ hai phần trong cùng Spring Boot để dự án vẫn build/chạy được. Nếu nhóm quyết định tách frontend độc lập, cần thiết kế API, xác thực, CSRF/CORS và pipeline riêng trước khi di chuyển file; xem [kiến trúc và hướng phát triển](docs/architecture.md).

## Cấu trúc repository

```text
car-valuation/
├── .github/
│   ├── ISSUE_TEMPLATE/      # Báo lỗi và yêu cầu tính năng
│   ├── workflows/           # CI build và test
│   └── pull_request_template.md
├── docs/                    # Kiến trúc và giới hạn đã biết
├── src/
│   ├── main/java/com/carvaluation/     # Server: MVC, nghiệp vụ, JPA, bảo mật
│   ├── main/resources/templates/       # Client: Thymeleaf HTML
│   ├── main/resources/static/          # Client: CSS, JS camera
│   └── test/java/com/carvaluation/     # Kiểm thử
├── pom.xml                   # Maven/Spring Boot 3.3.5, Java 21
├── CONTRIBUTING.md           # Quy trình làm việc nhóm
├── SECURITY.md               # Báo cáo lỗ hổng và lưu ý triển khai
└── CLAUDE.md                 # Hướng dẫn công cụ phát triển (nếu có tại local)
```

Các package server chính: `auth`, `vehicle`, `vin`, `mileage`, `damage`, `ai`, `pricing`, `valuation`, `expert`, `admin`, `storage`, `config`. Xem [docs/architecture.md](docs/architecture.md) để biết trách nhiệm và luồng dữ liệu.

## Chạy cục bộ

Yêu cầu **JDK 21**, **Maven** và trình duyệt hỗ trợ `getUserMedia` (localhost/HTTPS cho camera). Chạy lệnh từ thư mục gốc repository:

```sh
mvn test
mvn spring-boot:run
```

Mở <http://localhost:8080>. Cấu hình mặc định dùng **H2 in-memory**: dữ liệu trong database mất sau mỗi lần tắt ứng dụng; ảnh lưu trên đĩa theo `app.storage.upload-dir`. `DataInitializer` tạo tài khoản demo khi bảng người dùng trống, kể cả khi bật profile MySQL — **không đưa cấu hình hiện tại lên Internet**. Xem `src/main/java/com/carvaluation/config/DataInitializer.java` để biết seed cục bộ; đổi cách bootstrap và thông tin đăng nhập trước khi triển khai.

Để thử MySQL, tạo database `car_valuation_db`, sau đó chỉnh cấu hình local theo `src/main/resources/application-mysql.properties` và chạy:

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

**Profile MySQL hiện có mật khẩu hard-code và `ddl-auto=update`; chỉ dùng để thử nghiệm, không phải cấu hình production.** Không commit khóa API hay mật khẩu thật. Biến môi trường `GEMINI_API_KEY` được đọc bởi `application.properties`; không có khóa hợp lệ thì client AI dùng mock, và lỗi gọi Gemini cũng có thể rơi về mock. Mock **không phải** kết quả nhận diện từ ảnh thực tế.

## Kiểm thử và cộng tác

```sh
mvn compile
mvn test
mvn -Dtest=VinValidatorTest test
mvn package
```

Nhánh `main` dành cho phát hành; `develop` là nhánh tích hợp. Tạo nhánh tính năng/sửa lỗi từ `develop` và gửi PR về `develop`; PR phát hành đi từ `develop` sang `main`. Workflow GitHub Actions chạy `mvn -B verify` trên JDK 21 cho PR và push vào hai nhánh này. Các bài test về phân quyền ảnh, DTO và CSRF chỉ xác minh các đường đi được kiểm tra; **chưa chứng minh** toàn bộ wizard trình duyệt đã hoạt động. Xem [CONTRIBUTING.md](CONTRIBUTING.md) để tạo issue, nhánh và PR; xem [SECURITY.md](SECURITY.md) khi phát hiện lỗ hổng.

## Phạm vi tính năng

- VIN: ảnh camera/file → Gemini OCR → kiểm tra và giải mã VIN → người dùng xác nhận.
- ODO: ảnh → OCR → người dùng xác nhận số km.
- Ngoại thất: ảnh trước/sau/trái/phải và ảnh cận cảnh tùy chọn → phân loại dấu hiệu nhìn thấy được.
- Định giá: giá tham khảo hoặc khấu hao theo phân khúc, điều chỉnh theo ODO, ngoại thất và bảng hỏi.
- Tài khoản: đăng nhập session, vai trò người dùng/chuyên gia/quản trị, lịch sử theo chủ hồ sơ.

**Lưu ý:** Đây là phạm vi mã nguồn đã có, không phải cam kết rằng tất cả đường đi end-to-end đã qua kiểm thử. Các lỗi đã biết và thứ tự khắc phục nằm ở [docs/known-limitations.md](docs/known-limitations.md).
