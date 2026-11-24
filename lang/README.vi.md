<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher là bản fork bên thứ ba của [HMCL](https://github.com/HMCL-dev/HMCL) dành cho người chơi Trung Quốc, tập trung vào **tải xuống và quản lý máy chủ**.

> Dự án này không phải phiên bản chính thức của HMCL và không liên kết với đội ngũ HMCL. HMCL không hỗ trợ bản fork này.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Bổ sung so với upstream

- Tải máy chủ: máy chủ vanilla và danh sách bản dựng Paper
- Trang cài đặt Paper: plugin cài sẵn, khởi chạy sau khi tải, thư mục máy chủ riêng
- Trình duyệt plugin máy chủ: hai nguồn Modrinth / Hangar có thể chọn
- Danh sách máy chủ: quản lý nhiều thư mục, kiểm tra jar, sửa cấu hình
- Trình sửa server.properties trong ứng dụng
- Sửa hoạt ảnh cửa sổ dính cạnh trên (macOS)

## Biên dịch

Cần JDK 21:

    ./gradlew :HMCL:shadowJar

Tệp kết quả ở HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

ID ứng dụng Microsoft OAuth tùy chỉnh:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Người đóng góp

### HMCL upstream

139 người đóng góp, theo thứ tự kho HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Giấy phép và ghi công

Dự án dùng **GPL-3.0** và giữ bản quyền, ghi công của HMCL. Xem LICENSE và phần đầu tệp nguồn.

Khi phân phối bản sửa đổi:

- Giữ thông báo bản quyền HMCL;
- Dùng tên/phiên bản khác để phân biệt với bản chính thức;
- Không tuyên bố đây là bản dựng chính thức của HMCL.

## Phản hồi

Gửi vấn đề, đề xuất hoặc câu hỏi tại [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Liên kết

- Kho：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Phản hồi：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher là bản fork cộng đồng của HMCL tập trung vào tải xuống và quản lý máy chủ. Không liên kết hoặc được hỗ trợ bởi đội ngũ HMCL. Giấy phép GPL-3.0.
