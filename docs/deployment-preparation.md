# Chuẩn bị triển khai web (chưa triển khai)

Ứng dụng Spring Boot phục vụ cả HTML client (Thymeleaf/JS) và server từ **một tiến trình**. Người truy cập link HTTPS không cần cài Java; máy chủ phải chạy liên tục. Tài liệu này là checklist, **không phải** bằng chứng ứng dụng đã sẵn sàng đưa lên Internet.

## Điều kiện bắt buộc trước khi đưa lên mạng

1. Chọn máy chủ hỗ trợ Java 21, HTTPS và MySQL được sao lưu. Chọn nơi lưu file có khả năng giữ dữ liệu qua restart/redeploy; gắn volume cho đường dẫn `UPLOAD_DIR`, sao lưu cùng database. Một instance duy nhất là cấu hình phù hợp với filesystem hiện tại; nhiều instance cần shared storage hoặc thiết kế object storage.
2. Đặt `SPRING_PROFILES_ACTIVE=prod`, `DATABASE_URL` (JDBC MySQL URL), `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `UPLOAD_DIR` (đường dẫn tuyệt đối có quyền ghi và bền), `GEMINI_API_KEY` qua secret manager của nhà cung cấp. Không đặt mật khẩu, token hay key trong git/log. Kiểm tra phiên bản model Gemini, hạn mức và chi phí thực tế trước khi chạy. Profile `prod` không tạo tài khoản mẫu và không dùng AI mock: thiếu key sẽ khiến ứng dụng không khởi động; lỗi API không được thay bằng dữ liệu giả.
3. Dùng reverse proxy HTTPS tin cậy (proxy phải ghi đè/loại bỏ mọi header `Forwarded`/`X-Forwarded-*` do client gửi); app bật forwarded headers và cookie session Secure/HttpOnly/SameSite=Lax. Không phơi cổng HTTP 8080 trực tiếp ra Internet. Không expose `/uploads`, `/h2-console`, DB hay volume ảnh trực tiếp; kiểm tra quyền đọc ảnh qua app.
4. Tạo chiến lược tạo tài khoản ADMIN đầu tiên được chủ dự án chấp thuận, **không dùng tài khoản mẫu**. Hiện chưa có lệnh bootstrap hay màn hình cấp quyền ADMIN đầu tiên trong `prod`; tài khoản tự đăng ký chỉ là USER. Không tự nâng quyền bằng chỉnh DB không được chủ dự án duyệt.
5. Thực hiện staging trên dữ liệu không nhạy cảm: kiểm thử đăng ký/đăng nhập/logout, wizard VIN/ODO/ảnh, report và vai trò; thử backup/restore MySQL + uploads; giám sát lỗi và quản lý ngân sách API. Giá mẫu và kết quả định giá chưa được kiểm chứng để phục vụ giao dịch.

## Cấu hình hiện có

`src/main/resources/application-prod.properties` dùng MySQL và vùng upload bền theo biến môi trường, tắt H2 console và seed tài khoản demo, bật cache template/cookie HTTPS. Chỉ khởi chạy sau khi mọi điều kiện trên đã được chuẩn bị. `application-mysql.properties` dành cho local, **không dùng cùng profile `prod`**; cấu hình mặc định H2 chỉ dành cho local/test.

Lệnh kiểm tra trước khi đóng gói: `mvn verify`. Chưa có triển khai cloud, domain, SSL certificate, hosting hoặc link công khai nào được tạo trong bước chuẩn bị này.
