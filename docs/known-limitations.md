# Giới hạn đã biết — 2026-11-12

Danh sách này phản ánh kết quả đọc mã nguồn, **không phải** xác nhận đã chạy thử mọi trang trên trình duyệt. Các unit tests ban đầu bao phủ VIN/pricing; nhánh sửa ảnh bổ sung test MockMvc và quyền đọc ảnh, nhưng vẫn chưa có bộ kiểm thử end-to-end. Dùng issue để theo dõi bản vá; đừng đánh dấu hoàn thành chỉ vì build thành công.

## Chặn triển khai công khai

- `DataInitializer` tạo tài khoản đặc quyền với mật khẩu cố định khi bảng user trống, kể cả profile MySQL; tách seed demo khỏi cấu hình production.
- Đã chặn đường dẫn public `/uploads/**`; ảnh được phục vụ qua endpoint kiểm tra quyền theo chủ hồ sơ hoặc vai trò chuyên gia/quản trị. Cần tiếp tục kiểm tra phân quyền khi triển khai thật và chính sách quyền truy cập ảnh của chuyên gia.
- Đã gắn CSRF token cho yêu cầu upload từ giao diện và thêm test kiểm tra CSRF; vẫn cần chạy thử thao tác upload trên trình duyệt.
- Đã chuyển response upload sang DTO tối thiểu thay vì trả JPA entity; tránh serialize vòng quan hệ và thuộc tính người dùng.

## Cần sửa để hoàn tất quy trình

- VIN/ODO frontend gửi `imageBase64`, backend đọc `base64Image`.
- Script nội tuyến ở nhiều trang wizard nằm ngoài fragment `#main-content`; trang upload ảnh đã được sửa và kiểm tra HTML render trong test, các trang khác vẫn cần xử lý.
- Templates còn đọc nhiều thuộc tính không có trong entity (`finalPriceVnd`, `EXPERT_REVIEWED`...); trang ảnh đã dùng DTO có `photoUrl`/`damageReportJson`, nhưng cần view model/DTO nhất quán cho các trang còn lại.
- Server chưa bắt buộc đủ bốn góc ảnh khi định giá; chưa chặn tính lại hồ sơ đã qua thẩm định.
- Dữ liệu mock AI được dùng khi không có key **và** khi API lỗi, nhưng chưa được gắn nhãn nguồn ở kết quả.

## Độ tin cậy và vận hành

- VIN/ODO và dữ liệu xe cần validation ở server; giá khấu trừ ngoại thất hiện lấy trực tiếp từ AI, cần quy tắc và giới hạn của server.
- Giá tham khảo seed và khấu hao theo phân khúc là dữ liệu demo, không phải định giá thị trường được kiểm chứng.
- Default H2 in-memory không giữ metadata sau restart; MySQL profile có password hard-code, `ddl-auto=update`; H2 console bật mặc định.
- Upload vẫn dựa vào extension tên file, chưa kiểm tra nội dung ảnh thực. Đã có test MockMvc về upload/CSRF và quyền ảnh, nhưng vẫn thiếu kiểm thử tích hợp thực với database, trạng thái hồ sơ và E2E trình duyệt.

Thứ tự ưu tiên: **tài khoản/ảnh/DTO/CSRF → hợp đồng frontend-backend → kiểm thử luồng → xác thực và chất lượng định giá → vận hành**.
