## 6. Các vấn đề gặp phải

### 6.1. Tiến độ sản xuất Assets không đồng bộ với tiến độ lập trình

Hệ thống mã nguồn phát triển nhanh trong khi quá trình sản xuất hình ảnh và âm thanh theo phong cách Sketch-style tốn nhiều thời gian, dẫn đến tình trạng “nghẽn cổ chai” ảnh hưởng đến tiến độ chung của dự án.

#### Hành động để giải quyết

- **Module hóa Assets để tối ưu nguồn lực:**  
  Chia nhỏ các bộ Assets thành nhiều thành phần riêng biệt như layers hoặc individual objects để các thành viên khác có thể hỗ trợ tìm kiếm hoặc tham gia sản xuất.

- **Ưu tiên Placeholder Assets:**  
  Hoàn thiện trước các Assets khung nhằm giúp anh em code có thể triển khai logic gameplay với dữ liệu mẫu trong khi chờ tài nguyên chính thức.

#### Kết quả

Tiến độ giữa bộ phận Lập trình và Mỹ thuật đạt trạng thái cân bằng hơn. Thời gian chờ đợi dữ liệu để debug giảm đáng kể, giúp Engine luôn có tài nguyên để vận hành liên tục.


### 6.2. Xung đột mã nguồn (Conflicts) và quy trình phối hợp nhóm chưa chặt chẽ

Việc nhiều thành viên cùng chỉnh sửa các Manager cốt lõi dẫn đến xung đột mã nguồn thường xuyên khi Merge. Ngoài ra, một số lỗi phát sinh do quy trình Code Review chưa được thực hiện đầy đủ.

#### Hành động để giải quyết

- **Chuẩn hóa quy trình làm việc nhóm:**  
  Sử dụng công cụ [Linear](https://linear.app?utm_source=chatgpt.com) để phân chia nhiệm vụ chi tiết, đảm bảo mỗi thành viên phụ trách một module riêng nhằm hạn chế chồng chéo mã nguồn.

- **Thiết lập quy trình Code Review bắt buộc:**  
  Thành viên trong nhóm phải kiểm tra mã nguồn của nhau trước khi thực hiện Pull Request (PR) và Merge vào nhánh chính.

- **Tổ chức họp kỹ thuật định kỳ:**  
  Thống nhất kiến trúc hệ thống và cùng xử lý các lỗi phát sinh từ nhiều phần việc khác nhau.

#### Kết quả

Tỷ lệ xung đột mã nguồn giảm đáng kể, quá trình Merge trở nên ổn định và mượt mà hơn. Chất lượng mã nguồn được cải thiện khi nhiều lỗi logic được phát hiện ngay từ bước Review thay vì đến giai đoạn kiểm thử.


### 6.3. Hệ thống nạp dữ liệu (Loading System) phức tạp và phát sinh nhiều lỗi kỹ thuật

Kiến trúc Data-driven yêu cầu nạp đồng thời nhiều loại dữ liệu như Rooms, Dialogues và Items từ các tệp JSON, dẫn đến nhiều lỗi liên quan đến quản lý tài nguyên bất đồng bộ hoặc ánh xạ dữ liệu không chính xác.

#### Hành động để giải quyết

- **Tối ưu hóa hệ thống DataManager và AssetManager:**  
  Tập trung quản lý việc nạp tài nguyên nhằm đảm bảo tính đồng bộ và ổn định của dữ liệu.

- **Xây dựng lại LoadingScreen:**  
  Đảm bảo toàn bộ hình ảnh, âm thanh và dữ liệu JSON được tải hoàn chỉnh trước khi chuyển vào màn hình chơi chính.

- **Rà soát các lớp dữ liệu:**  
  Tối ưu và sửa lỗi cho các lớp như `RoomData`, `ItemData` nhằm đảm bảo quá trình đọc dữ liệu JSON luôn chính xác.

#### Kết quả

Hệ thống Load game hoạt động ổn định hơn, loại bỏ phần lớn tình trạng crash do thiếu tài nguyên hoặc lỗi dữ liệu. Trải nghiệm chuyển cảnh của người chơi trở nên mượt mà và đáng tin cậy hơn.


### 6.4. Rào cản từ các mảng kiến thức chuyên môn mới

Dự án áp dụng nhiều công nghệ chuyên sâu như ECS, Shaders và Meta-file system — những lĩnh vực mà các thành viên chưa từng tiếp cận trước đây — khiến kiến trúc ban đầu chưa thể bao quát hết các vấn đề phát sinh.

#### Hành động để giải quyết

- **Áp dụng tư duy học tập thích nghi nhanh (Rapid Learning):**  
  Chủ động nghiên cứu tài liệu, thử nghiệm công nghệ mới và liên tục tái cấu trúc hệ thống trong quá trình phát triển.

- **Ứng dụng Design Patterns:**  
  Sử dụng các mô hình như Manager Pattern, Observer (Listener) và Strategy nhằm tách biệt các hệ thống phức tạp và hỗ trợ xử lý bug hiệu quả hơn.

- **Ưu tiên tối ưu trải nghiệm người chơi:**  
  Tập trung xử lý các bug tồn đọng và cải thiện tính ổn định trước khi bổ sung thêm tính năng mới.

#### Kết quả

Đội ngũ đã làm chủ được nhiều công nghệ khó trong thời gian ngắn. Kiến trúc dự án từ trạng thái rời rạc dần trở thành một hệ thống có tính mở rộng cao (Scalable), tạo nền tảng vững chắc cho việc phát triển toàn bộ cốt truyện trong tương lai.