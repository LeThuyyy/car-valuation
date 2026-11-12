# Chính sách bảo mật

## Trạng thái dự án

Đây là MVP phục vụ phát triển/kiểm thử; **không nên triển khai công khai khi chưa khắc phục các vấn đề trong [docs/known-limitations.md](docs/known-limitations.md)**. Tài khoản demo được tạo tự động trên database mới; ảnh upload hiện truy cập được qua `/uploads/**` mà không kiểm tra chủ sở hữu. Cấu hình MySQL mẫu chứa mật khẩu và H2 console đang bật trong cấu hình mặc định.

## Báo cáo vấn đề

Không đăng bằng chứng khai thác, dữ liệu cá nhân hoặc khóa bí mật trong issue công khai. Dùng tính năng **Report a vulnerability / Private vulnerability reporting** của GitHub repository nếu người quản trị đã bật; nếu chưa, liên hệ riêng người quản trị repository bằng kênh nhóm đã thống nhất. Tài liệu này không tuyên bố có email hay SLA phản hồi khi nhóm chưa thiết lập chúng.

## Trước khi triển khai thật

- Tắt seed tài khoản đặc quyền ở môi trường thật; cấp phát và xoay vòng thông tin đăng nhập theo quy trình riêng.
- Giới hạn quyền đọc ảnh theo người sở hữu hoặc chuyên gia được phép; xác minh nội dung và kích thước upload.
- Dùng database bền vững, secret từ môi trường/secret manager, migration database và tắt H2 console ngoài dev.
- Giữ CSRF cho request có session, kiểm tra phân quyền ở endpoint và test upload/JSON response.
- Phân biệt kết quả AI thật, mock và lỗi; không trình bày dữ liệu mock như kết luận về xe thực.
