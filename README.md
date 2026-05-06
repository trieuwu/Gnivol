# Reverse Loving - Gnivol

## 1. About

**Tên Dự Án:** Reverse Loving - Gnivol
**Link Dự Án:** [GitHub Link](https://github.com/trieuwu/Gnivol)

### Thành viên

* **[Hà Tiến Triệu]**
    GitHub: https://github.com/trieuwu/
    Contact: [optional]

* **[Nguyễn Hoàng Tùng]**
    GitHub: https://github.com/Luffyreals
    Contact: [optional]

* **[Hoàng Duy Anh]**
    GitHub: https://github.com/NaraDuyy
    Contact: [optional]

* **[Nguyễn Văn Thành]**
    GitHub: https://github.com/NvThanh2809
    Contact: [optional]

* **[Nguyễn Thành Trung]** — *Project Advisor*
  Contact / Profile: [link]




### Mô hình làm việc

Nhóm áp dụng phương pháp **Scrumban** (kết hợp giữa Scrum và Kanban) để quản lý tiến độ và tổ chức công việc. Các nhiệm vụ được theo dõi liên tục thông qua hệ thống quản lý **Linear**, đồng thời nhóm duy trì các chu kỳ làm việc ngắn nhằm đánh giá tiến độ và cải tiến quy trình.

- Link linear: [link](https://linear.app/javacore24-finalcontest/team/JAV/all)

Bên cạnh đó, dự án được phát triển theo định hướng **thiết kế dựa trên cốt truyện** (*Narrative-driven development*). Cốt truyện không được cố định ngay từ đầu mà liên tục được xây dựng và điều chỉnh trong suốt quá trình phát triển. Trên cơ sở đó, các yếu tố gameplay, môi trường và trải nghiệm người chơi được thiết kế xoay quanh diễn biến của câu chuyện, nhằm đảm bảo sự nhất quán về mặt cảm xúc và tăng chiều sâu cho trải nghiệm kinh dị tâm lý.

Do giới hạn thời gian phát triển trong 7 tuần, đồng thời toàn bộ thành viên đều chưa có kinh nghiệm làm game và chưa từng sử dụng LibGDX trước đó, phiên bản hiện tại được xác định là một **bản demo**.

Tính đến thời điểm hiện tại, dự án mới triển khai được khoảng **1/8 nội dung cốt truyện** đã đề ra. Trong các giai đoạn tiếp theo, trò chơi sẽ tiếp tục được mở rộng và hoàn thiện cả về nội dung lẫn tính năng.




### Chiến lược quản lý mã nguồn

Nhóm áp dụng mô hình **Gitflow** để tổ chức và kiểm soát mã nguồn. Mỗi thành viên sẽ tạo nhánh từ `develop` để làm việc. Các nhánh này sẽ đặt tên theo cấu trúc `feature/ten_chuc_nang`, sau khi hoàn thành sẽ tạo **Pull Request** để cùng kiểm tra code và **Merge** vào `develop`

Quy trình làm việc:

* Mỗi thành viên tạo nhánh từ `develop` để thực hiện chức năng được phân công
* Quy ước đặt tên nhánh: `feature/ten-chuc-nang`
* Sau khi hoàn thành, tạo **Pull Request** để rà soát mã nguồn
* Chỉ hợp nhất (merge) vào `develop` khi đã được kiểm tra và thông qua

Các nhánh chính:

* `main`: Chứa phiên bản ổn định, đã được kiểm thử đầy đủ và sẵn sàng phát hành
* `develop`: Chứa phiên bản tích hợp mới nhất, đã qua rà soát cơ bản
* `feature/*`: Các nhánh phát triển chức năng riêng lẻ, tồn tại ngắn hạn và sẽ được hợp nhất vào `develop` sau khi hoàn thành

![Sơ đồ Gitflow](version_control.png)


## 2. Giới thiệu dự án

**Gnivol_lovinG** là một trò chơi thuộc thể loại **2D Point-and-Click Meta-Horror**, được phát triển trên nền tảng **Java (JDK 24)** và framework **LibGDX**.

Khác với các trò chơi kinh dị truyền thống chỉ tập trung vào yếu tố hù dọa thị giác (*jumpscare*), dự án hướng tới việc **xóa nhòa ranh giới giữa người chơi và phần mềm**, thông qua các cơ chế tác động trực tiếp đến hệ thống và trải nghiệm tâm lý.

Người chơi được đưa vào một không gian tĩnh lặng, ám ảnh với phong cách đồ họa **sketch thô mộc**. Tại đây, mỗi hành động và lựa chọn không chỉ ảnh hưởng đến diễn biến cốt truyện mà còn tác động trực tiếp đến trạng thái vận hành của chính trò chơi.


### Đặc điểm cốt lõi

* **Reality Stability (RS) — Cơ chế ổn định thực tại**
  Một hệ thống điều khiển trạng thái động, cho phép biến đổi môi trường, hình ảnh và âm thanh dựa trên mức độ “ổn định thực tại” của người chơi.

* **Phá vỡ bức tường thứ tư (Meta-horror elements)**
  Trò chơi có khả năng tương tác với hệ điều hành, tạo và chỉnh sửa tệp tin, từ đó làm mờ ranh giới giữa thực và ảo.

* **Bộ nhớ liên tục (Persistent Memory / Silent Save)**
  Hệ thống lưu trữ ngầm hoạt động liên tục, khiến mọi lựa chọn đều mang tính lâu dài và không thể hoàn tác bằng cách tải lại trò chơi.

* **Kiến trúc mở rộng (Scalable Architecture)**
  Áp dụng mô hình **Entity Component System (ECS)** thông qua thư viện **Ashley**, cho phép mở rộng các cơ chế tương tác phức tạp trong khi vẫn đảm bảo hiệu năng.


### Mục tiêu phát triển

Dự án không chỉ hướng tới việc xây dựng một sản phẩm giải trí, mà còn đóng vai trò như một môi trường thực nghiệm nhằm phát triển cả **năng lực kỹ thuật và quy trình làm việc chuyên nghiệp.**

#### a. Mục tiêu kỹ thuật

Dự án nhằm thực nghiệm áp dụng các kiến trúc và kỹ thuật phát triển phần mềm hiện đại, bao gồm:

* **Manager Pattern** để tổ chức và quản lý hệ thống theo hướng tách biệt, dễ bảo trì và mở rộng.
* **Data-driven Design** quản lý dữ liệu thông qua JSON, cho phép thay đổi nội dung mà không cần can thiệp trực tiếp vào mã nguồn.
* **Tối ưu hóa bộ nhớ** với **Texture Atlas** để giảm số lượng draw call và cải thiện hiệu năng hiển thị.

#### b. Mục tiêu quy trình và phát triểnư

* **Quy trình làm việc chuyên nghiệp**
  Áp dụng Git để quản lý mã nguồn, trong đó mỗi tính năng được phát triển trên nhánh riêng. Tất cả thay đổi đều phải thông qua Pull Request và Code Review trước khi hợp nhất vào `develop`.

* **Hiện thực hóa ý tưởng (Idea Deployment)**
  Chuyển hóa các khái niệm trừu tượng của thể loại Meta-horror thành các hệ thống logic cụ thể. Ví dụ, khái niệm “sự bất ổn thực tại” được triển khai thành hệ thống **RSManager**, có khả năng can thiệp vào các thành phần trong trò chơi.

* **Khả năng học tập nhanh (Rapid Learning)**
  Rèn luyện khả năng nghiên cứu và áp dụng nhanh các công nghệ mới. Các thành viên trực tiếp làm việc với các thư viện như **Ashley ECS**, **Box2D Lights** và các cơ chế thao tác tệp trong môi trường LibGDX.

* **Quản trị dự án thực tế**
  Triển khai dự án từ giai đoạn khởi tạo (Skeleton Project) đến việc xây dựng cấu trúc mã nguồn chuẩn hóa, tạo nền tảng cho bảo trì và mở rộng trong tương lai.


## 3. Các Chức Năng Chính

Dựa trên kiến trúc hệ thống hiện tại, các chức năng chính của trò chơi được tổ chức thành các hệ thống độc lập và có khả năng tương tác lẫn nhau như sau:

### 3.1. Hệ thống quản lý cảnh (SceneManager)

* **Cơ chế hoạt động:**
  Sử dụng mô hình **stack-based scene management**, cho phép chuyển đổi linh hoạt giữa các cảnh chính (Room) và các lớp phủ (Overlay) mà không làm mất trạng thái hiện tại.

* **Quản lý trạng thái:**
  Dữ liệu phòng được lưu trữ và tái sử dụng thông qua cache, đảm bảo tính liên tục khi người chơi quay lại các khu vực đã đi qua.

* **Tích hợp âm thanh:**
  Tự động chuyển đổi nhạc nền (BGM crossfade) khi thay đổi cảnh thông qua `sceneBgmMap`, giúp duy trì trải nghiệm liền mạch.

### 3.2. Hệ thống cắt cảnh theo kịch bản (CutsceneManager)

* **Cơ chế hoạt động:**
  Vận hành theo mô hình **step-based processor**, trong đó mỗi phân cảnh được định nghĩa dưới dạng tập lệnh JSON (`cutscenes.json`) và được thực thi tuần tự theo thời gian.

* **Khả năng mở rộng:**
  Hỗ trợ nhiều loại tác vụ (step types) như:

  * Rung màn hình (*shake*)
  * Hiệu ứng lóe sáng (*flash*)
  * Phát video
  * Thay đổi chỉ số RS
  * Kích hoạt minigame

* **Tích hợp hệ thống:**
  Có thể tương tác trực tiếp với các hệ thống khác như `SceneManager`, `RSManager` và `DialogueEngine`.

### 3.3. Cơ chế thực tại động (RSManager)

* **Mô hình dữ liệu:**
  Chỉ số RS vận hành trong khoảng **0–100**, với vùng ổn định mặc định từ **35–65**.

* **Cơ chế phản hồi:**
  Khi vượt ngưỡng, hệ thống tự động kích hoạt:

  * Hiệu ứng hình ảnh (camera shake, glitch shader theo chu kỳ)
  * Thay đổi trạng thái môi trường và âm thanh

* **Tác động gameplay:**
  Sự biến động của RS ảnh hưởng trực tiếp đến tiến trình trò chơi, bao gồm việc dẫn đến nhiều kết thúc khác nhau.

### 3.4. Hệ thống hội thoại và suy nghĩ (Dialogue & Thought Engine)

* **Hội thoại phân nhánh:**
  Sử dụng cấu trúc **Dialogue Tree** để triển khai các luồng hội thoại phức tạp, với tốc độ hiển thị ~0.05s/ký tự và hỗ trợ hiển thị đồng thời hai chân dung nhân vật.

* **Hiệu ứng hiển thị:**
  Tích hợp các hiệu ứng như typewriter, glitch văn bản và thay đổi trạng thái UI theo ngữ cảnh.

* **Suy nghĩ theo trạng thái RS:**
  Dữ liệu từ `thoughts.json` được phân loại theo các mức RS (**LOW / MID / HIGH**), từ đó thay đổi cách mô tả và cảm nhận của nhân vật đối với môi trường.

### 3.5. Hệ thống lưu trữ và trạng thái trò chơi (Save/Load)

* **Kiến trúc lưu trữ:**
  Sử dụng interface `ISaveable` để đồng bộ dữ liệu từ nhiều hệ thống (Inventory, Puzzle, Flag...) vào một đối tượng `GameSnapshot` duy nhất dưới dạng JSON.

* **Tự động lưu:**
  `AutoSaveManager` kích hoạt cơ chế lưu tại các sự kiện quan trọng như:

  * Nhặt vật phẩm
  * Kết thúc hội thoại
  * Chuyển cảnh

* **Lưu trữ vật lý:**
  Dữ liệu được ghi trực tiếp vào hệ thống tệp tại đường dẫn:
  `.gnivol/save_slot_1.json`, đảm bảo tính liên tục và không thể hoàn tác của trải nghiệm.


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

### 4.2. Quản lý dữ liệu

Dự án được thiết kế theo hướng **data-driven**, cho phép thay đổi nội dung mà không cần chỉnh sửa mã nguồn:

* **JSON**
  Định dạng dữ liệu chính cho các hệ thống như phòng chơi (Room), hội thoại (Dialogue), cắt cảnh (Cutscene), vật phẩm (Item) và suy nghĩ nhân vật (Thought).

* **Reflection (LibGDX Json)**
  Tự động ánh xạ dữ liệu từ file JSON vào các đối tượng Java (POJO) thông qua `DataManager`.

* **AssetManager**
  Quản lý việc nạp tài nguyên (texture, âm thanh, font) theo cơ chế bất đồng bộ, giúp giảm giật lag khi chuyển cảnh.

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

### 4.4. Cấu trúc dự án


## 5. Ảnh và Video Demo

**Ảnh Demo:**
![Ảnh Demo](#)

**Video Demo:**
[Video Link](#)






## 6. Các Vấn Đề Gặp Phải

### Vấn Đề 1: [Mô tả vấn đề]
**Ví dụ:** Game gặp phải vấn đề hiệu năng kém, fps thấp dù không có nhiều đối tượng trên màn hình

### Hành Động Để Giải Quyết

**Giải pháp:** Do việc tạo object quá nhiều, nên dẫn tới tràn ram và giảm hiệu năng
- Sử dụng Design Pattern Object Pool để tái sử dụng object. Khi object không còn sử dụng, sẽ được đưa vào pool để sử dụng lại. 

### Kết Quả

- Sau khi sử dụng Object Pool, hiệu năng game đã được cải thiện, fps tăng lên đáng kể. Từ *30fps lên 60fps* (Rõ ràng hơn với số liệu cụ thể)

### Vấn Đề 2: [Mô tả vấn đề]
**Ví dụ:** Có quá nhiều class quái khác nhau, dù chúng có nhiều điểm chung


### Hành Động Để Giải Quyết

**Giải pháp:** Sử dụng Design Pattern Builder để tạo các object quái với các thuộc tính khác nhau mà không cần tạo nhiều class. Ngoài ra sử dụng Strategy Pattern để tạo các hành vi khác nhau cho các object quái mà không cần tạo nhiều class.

### Kết Quả

- Sau khi sử dụng Builder và Strategy Pattern, việc tạo các object quái đã trở nên dễ dàng hơn, không cần tạo nhiều class. Có thể chỉ cần config các thuộc tính và hành vi cho object quái mà không cần tạo nhiều class.

## 7. Kết Luận

**Kết quả đạt được:** [Mô tả kết quả đạt được sau khi giải quyết các vấn đề]

**Hướng phát triển tiếp theo:** [Mô tả hướng phát triển tiếp theo của dự án]