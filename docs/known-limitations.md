# Giới hạn đã biết — 2026-11-12

Danh sách này phản ánh kết quả đọc mã nguồn, **không phải** xác nhận đã chạy thử mọi trang trên trình duyệt. `mvn test` trước đó chạy 5 unit tests; chưa có bộ kiểm thử end-to-end. Dùng issue để theo dõi bản vá; đừng đánh dấu hoàn thành chỉ vì build thành công.

## Chặn triển khai công khai

- `DataInitializer` tạo tài khoản đặc quyền với mật khẩu cố định khi bảng user trống, kể cả profile MySQL; tách seed demo khỏi cấu hình production.
- `/uploads/**` được phép truy cập ẩn danh và ánh xạ trực tiếp vào file ảnh người dùng; thêm endpoint có kiểm tra quyền.
- Upload ảnh qua JavaScript thiếu CSRF token, trong khi `/valuation/{id}/photos/upload` không miễn CSRF; dự kiến bị 403.
- Endpoint upload trả JPA `CarPhoto` trỏ đến `ValuationSession` có danh sách ảnh và `User`; có nguy cơ serialization vòng và lộ thuộc tính người dùng. Trả DTO tối thiểu.

## Cần sửa để hoàn tất quy trình

- VIN/ODO frontend gửi `imageBase64`, backend đọc `base64Image`.
- Script nội tuyến của trang wizard nằm ngoài fragment `#main-content` được ghép vào layout; cần xác nhận HTML sau render và sửa slot script.
- Templates đọc các thuộc tính không có trong entity (`finalPriceVnd`, `photoUrl`, `damageReportJson`, `EXPERT_REVIEWED`...); dùng view model/DTO nhất quán.
- Server chưa bắt buộc đủ bốn góc ảnh khi định giá; chưa chặn tính lại hồ sơ đã qua thẩm định.
- Dữ liệu mock AI được dùng khi không có key **và** khi API lỗi, nhưng chưa được gắn nhãn nguồn ở kết quả.

## Độ tin cậy và vận hành

- VIN/ODO và dữ liệu xe cần validation ở server; giá khấu trừ ngoại thất hiện lấy trực tiếp từ AI, cần quy tắc và giới hạn của server.
- Giá tham khảo seed và khấu hao theo phân khúc là dữ liệu demo, không phải định giá thị trường được kiểm chứng.
- Default H2 in-memory không giữ metadata sau restart; MySQL profile có password hard-code, `ddl-auto=update`; H2 console bật mặc định.
- Upload dựa vào extension tên file, chưa kiểm tra nội dung ảnh thực. Thiếu test render, API, phân quyền, trạng thái, CSRF và E2E.

Thứ tự ưu tiên: **tài khoản/ảnh/DTO/CSRF → hợp đồng frontend-backend → kiểm thử luồng → xác thực và chất lượng định giá → vận hành**.
