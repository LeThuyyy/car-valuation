# Giới hạn đã biết — 2026-11-12

Danh sách này phản ánh kết quả đọc mã nguồn và kiểm thử tự động (VIN/pricing, MockMvc cho các bước wizard, ảnh và phân quyền), **không phải** xác nhận đã chạy thử mọi trang trên trình duyệt. Vẫn chưa có bộ kiểm thử end-to-end. Dùng issue để theo dõi bản vá; đừng đánh dấu hoàn thành chỉ vì build thành công.

## Chặn triển khai công khai

- Profile `prod` không chạy `DataInitializer`, nhưng chưa có quy trình được phê duyệt để cấp ADMIN đầu tiên; profile MySQL/local vẫn seed tài khoản demo, không dùng để triển khai công khai.
- Đã chặn đường dẫn public `/uploads/**`; ảnh được phục vụ qua endpoint kiểm tra quyền theo chủ hồ sơ hoặc vai trò chuyên gia/quản trị. Cần tiếp tục kiểm tra phân quyền khi triển khai thật và chính sách quyền truy cập ảnh của chuyên gia.
- Đã gắn CSRF token cho yêu cầu upload từ giao diện và thêm test kiểm tra CSRF; vẫn cần chạy thử thao tác upload trên trình duyệt.
- Đã chuyển response upload sang DTO tối thiểu thay vì trả JPA entity; tránh serialize vòng quan hệ và thuộc tính người dùng.

## Cần sửa để hoàn tất quy trình

- Hợp đồng VIN/ODO và script wizard đã được sửa và có test MockMvc render/POST, nhưng chưa có kiểm thử browser end-to-end với camera, MySQL và AI thật.
- Templates báo cáo, lịch sử và trang EXPERT/ADMIN đã được sửa các thuộc tính sai trong phạm vi test; cần tiếp tục kiểm thử trạng thái hiếm và view model/DTO nhất quán.
- Server chưa bắt buộc đủ bốn góc ảnh khi định giá; chưa chặn tính lại hồ sơ đã qua thẩm định.
- Profile local vẫn dùng mock AI khi thiếu key hoặc API lỗi và chưa gắn nhãn nguồn trong báo cáo; profile `prod` không trả mock và yêu cầu key, nhưng vẫn cần kiểm chứng chất lượng kết quả AI thật và xử lý lỗi UX.

## Độ tin cậy và vận hành

- VIN/ODO và dữ liệu xe cần validation ở server; giá khấu trừ ngoại thất hiện lấy trực tiếp từ AI, cần quy tắc và giới hạn của server.
- Giá tham khảo seed và khấu hao theo phân khúc là dữ liệu demo, không phải định giá thị trường được kiểm chứng.
- Default H2 in-memory không giữ metadata sau restart; MySQL profile local có password hard-code; profile `prod` dùng biến môi trường cho MySQL và storage nhưng vẫn có `ddl-auto=update` (cần migration được kiểm soát). H2 console chỉ dùng cho local, tắt và bị chặn ở prod.
- Upload vẫn dựa vào extension tên file, chưa kiểm tra nội dung ảnh thực. Đã có test MockMvc về upload/CSRF và quyền ảnh, nhưng vẫn thiếu kiểm thử tích hợp thực với database, trạng thái hồ sơ và E2E trình duyệt.

Thứ tự ưu tiên: **tài khoản/ảnh/DTO/CSRF → hợp đồng frontend-backend → kiểm thử luồng → xác thực và chất lượng định giá → vận hành**.
