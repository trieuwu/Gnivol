## 4. Công nghệ

### 4.1. Công nghệ sử dụng

* **Java (JDK 8+)**
  Ngôn ngữ lập trình chính, tận dụng lập trình hướng đối tượng để xây dựng các hệ thống phức tạp.

* **LibGDX 1.14.0**
  Framework phát triển game mã nguồn mở, cung cấp API cho đồ họa, âm thanh và xử lý input.

* **LWJGL3**
  Backend cho nền tảng Desktop, hỗ trợ giao tiếp trực tiếp với phần cứng và driver đồ họa.

* **Gradle**
  Công cụ tự động hóa build, quản lý thư viện và đóng gói dự án theo mô hình đa module.

* **GLSL**
  Ngôn ngữ shader trên GPU, được sử dụng để xây dựng các hiệu ứng hình ảnh như glitch và chroma key.

---

### 4.2. Quản lý dữ liệu

Dự án được thiết kế theo hướng **data-driven**, cho phép thay đổi nội dung mà không cần chỉnh sửa mã nguồn:

* **JSON**
  Định dạng dữ liệu chính cho các hệ thống như phòng chơi (Room), hội thoại (Dialogue), cắt cảnh (Cutscene), vật phẩm (Item) và suy nghĩ nhân vật (Thought).

* **Reflection (LibGDX Json)**
  Tự động ánh xạ dữ liệu từ file JSON vào các đối tượng Java (POJO) thông qua `DataManager`.

* **AssetManager**
  Quản lý việc nạp tài nguyên (texture, âm thanh, font) theo cơ chế bất đồng bộ, giúp giảm giật lag khi chuyển cảnh.

---

### 4.3. Kiến trúc phần mềm

Dự án áp dụng nhiều mẫu thiết kế nhằm đảm bảo tính mở rộng và khả năng bảo trì:

* **Manager Pattern**
  Tập trung hóa các hệ thống chuyên biệt như `AudioManager`, `RSManager`, `SceneManager`.

* **Observer Pattern**
  Sử dụng cơ chế lắng nghe (listener) để đồng bộ dữ liệu giữa các hệ thống (ví dụ: `RSListener` cập nhật UI khi chỉ số RS thay đổi).

* **Stack-based Architecture**
  Quản lý các cảnh và lớp phủ (overlay) dưới dạng ngăn xếp, cho phép hiển thị nhiều lớp tương tác đồng thời.

* **Entity Component System (ECS)**
  Áp dụng thông qua thư viện **Ashley**, giúp quản lý các thực thể theo hướng thành phần, tối ưu hiệu năng và khả năng mở rộng.
