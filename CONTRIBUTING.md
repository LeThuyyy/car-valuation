# Đóng góp cho Car Valuation AI

Nhóm dùng GitHub Issues để theo dõi công việc và pull request (PR) để đề xuất thay đổi. `main` là nhánh phát hành, `develop` là nhánh tích hợp, nhánh ngắn hạn dùng cho từng issue. Không push trực tiếp lên `main` hoặc `develop` khi làm việc nhóm; maintainer có thể cấu hình ruleset trên GitHub theo hướng dẫn bên dưới.

## Quy trình làm việc

1. Tạo hoặc nhận một issue; ghi phạm vi, kết quả mong đợi và cách kiểm tra.
2. Tạo nhánh từ `develop` mới nhất: `feat/<mo-ta>`, `fix/<mo-ta>` hoặc `docs/<mo-ta>` (ví dụ `fix/vin-scan-payload`).
3. Thay đổi nhỏ theo chức năng. Sửa test/tài liệu liên quan; không trộn tái cấu trúc lớn với bản vá lỗi.
4. Chạy `mvn test` trước khi mở PR. Khi chỉnh giao diện, kiểm tra luồng bằng trình duyệt; nêu rõ bước nào chưa thử.
5. Mở PR từ nhánh công việc vào `develop`, liên kết issue (`Closes #123` nếu thực sự giải quyết), đợi CI đạt và tự kiểm tra phạm vi thay đổi trước khi merge. **Hiện nhóm không yêu cầu một người khác duyệt code.** Nếu có reviewer tự nguyện, ghi nhận ý kiến trước khi merge.
6. Trước khi đưa thay đổi vào `main`, mở PR từ `develop`; xác nhận CI, kiểm thử liên quan và giới hạn triển khai. Lead kiểm tra giao diện web khi có thay đổi UI; việc đó **không thay thế** kiểm tra chức năng, bảo mật và test. Sửa lỗi khẩn cấp từ `main` trên nhánh `hotfix/<mo-ta>` rồi đưa thay đổi trở lại `develop`. Xóa nhánh công việc sau khi merge nếu không cần giữ.

## Ranh giới thay đổi client–server

- `src/main/resources/templates` và `static`: HTML/JS/CSS chạy ở trình duyệt, nhưng Thymeleaf render trong Spring Boot.
- `src/main/java`: controller, service, entity, repository, bảo mật và tích hợp Gemini chạy ở server.
- Khi đổi request/response, sửa đồng thời JS/template, controller/DTO và bài test tương ứng. Không trả trực tiếp entity JPA qua JSON.
- Khi đổi quyền truy cập, kiểm tra cả API, form POST và đường dẫn file ảnh; không chỉ kiểm tra trang HTML.
- Chỉ chuyển sang frontend riêng khi nhóm thống nhất hợp đồng API và cơ chế xác thực. Xem [docs/architecture.md](docs/architecture.md).

## Kiểm tra trước PR

```sh
mvn compile
mvn test
mvn package
```

JDK 21 và Maven được yêu cầu. Chưa có linter/formatter riêng trong `pom.xml`; không ghi là đã chạy lint nếu dự án chưa cấu hình. Unit tests hiện có không chứng minh toàn bộ luồng; mô tả kiểm tra thủ công trong PR nếu có.

## Quy tắc review và quyền GitHub

Người quản trị repo có thể bật tại **Settings → Branches / Rulesets** cho cả `main` và `develop`: yêu cầu PR, kiểm tra CI `build-and-test` thành công và chặn force push; **không** yêu cầu phê duyệt từ người khác khi nhóm chưa có reviewer. Ruleset có thể hỗ trợ quy trình, nhưng không chứng minh ứng dụng đã vượt qua kiểm thử trình duyệt hoặc đủ an toàn để triển khai. Các thiết lập GitHub này chưa được áp dụng tự động bằng tài liệu. Chỉ thêm CODEOWNERS khi nhóm có danh sách GitHub handle và người chịu trách nhiệm thật; không tự gán tên người đóng góp.

Không đưa tài khoản/mật khẩu demo, dữ liệu khách hàng hay khóa API vào issue/PR. Nếu phát hiện lỗ hổng có thể khai thác, xem [SECURITY.md](SECURITY.md) và trao đổi riêng với maintainer thay vì đăng công khai cách khai thác.
