# Kiến trúc client–server

## Hiện trạng

Một Spring Boot 3.3.5 application phục vụ cả client (HTML/CSS/JS) và server (Java). HTTP request được Spring Security kiểm tra; Spring MVC render Thymeleaf hoặc trả JSON. Thông tin định giá được lưu qua JPA; ảnh được lưu trên filesystem; Gemini Vision cung cấp dữ liệu nhận diện khi có khóa hợp lệ và dịch vụ hoạt động.

```text
Browser
  ├─ Thymeleaf HTML đã render + Bootstrap/CSS
  ├─ camera.js / WebRTC / form / fetch
  │
  └────────── HTTP(S) ────────────► Spring Security
                                    ├─ MVC: wizard, auth, expert, admin
                                    ├─ /api/vin, /api/odo, phân tích ảnh
                                    ├─ services: VIN, ODO, damage, pricing
                                    │   └─ Gemini client / NHTSA
                                    └─ JPA → H2 hoặc MySQL
                                       file storage → uploads/
```

Đây là kiến trúc **client–server theo mô hình server-rendered**, không phải frontend SPA tách host. Máy chủ trả HTML cho trang và JSON cho một số tương tác camera/upload. Browser không kết nối trực tiếp database hay Gemini bằng khóa API.

## Ranh giới mã nguồn

| Lớp | Vị trí hiện tại | Trách nhiệm |
| --- | --- | --- |
| Client | `src/main/resources/templates`, `src/main/resources/static` | Hiển thị, camera/file fallback, form và fetch; không là nguồn tin cậy cho xác thực dữ liệu. |
| Giao tiếp HTTP | Controllers trong `src/main/java/com/carvaluation` | Ánh xạ route, kiểm tra request/đặc quyền; API trả DTO thay vì entity là mục tiêu cần làm. |
| Nghiệp vụ | `vin`, `mileage`, `damage`, `pricing`, `valuation`, `expert`, `admin` | Giải mã, tổng hợp, định giá và thay đổi trạng thái hồ sơ. |
| Hạ tầng | `auth`, `ai`, `storage`, `config` và repositories | Session/RBAC, dịch vụ ngoài, lưu file và persistence. |

Luồng chính: tạo `ValuationSession` → xác nhận VIN/xe → xác nhận ODO → upload ảnh ngoại thất → bảng hỏi → `PricingService` tính kết quả → chuyên gia rà soát nếu cần. Session thuộc một user; endpoint của người dùng phải truy vấn theo `id` **và** `userId`. Trạng thái hiện có là `DRAFT`, `AI_EVALUATED`, `EXPERT_REVIEWING`, `COMPLETED`, `CANCELLED`.

## Nếu chuyển sang client độc lập

Đây là **định hướng, chưa triển khai**. Chỉ tách sau khi nhóm thống nhất: (1) hợp đồng API cho đăng nhập, wizard, upload, kết quả, expert/admin; (2) xác thực session/cookie, CSRF và CORS cho origin khác; (3) DTO/validation và quyền đọc ảnh; (4) build/test/deploy riêng cho client; (5) cách di chuyển Thymeleaf mà không làm hỏng các URL hiện có. Một cấu trúc tương lai có thể là `client/` (framework do nhóm chọn), `server/` (Spring Boot), `docs/`; **không** tạo thư mục rỗng hoặc di chuyển `src/` trước khi có ứng dụng client tương ứng và test tích hợp.

Xem [known-limitations.md](known-limitations.md) trước khi thiết kế API hoặc triển khai.
