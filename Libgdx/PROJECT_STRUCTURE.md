# Gnivol - Cấu Trúc Dự Án

> Game point-and-click kinh dị meta-horror, xây dựng bằng **LibGDX** + **Ashley ECS**.
> Người chơi điều khiển nhân vật **Hnil**, khám phá các phòng, thu thập vật phẩm, giải puzzle,
> với hệ thống **Reality Stability (RS)** ảnh hưởng trực tiếp đến thế giới game.

---

## Tổng quan cây thư mục

```
Libgdx/                          <-- Root project (Gradle multi-module)
│
├── assets/                      <-- TẤT CẢ tài nguyên game (hình ảnh, data, font)
│   ├── data/                    <-- Dữ liệu game định nghĩa bằng JSON
│   │   ├── dialogues.json       <-- Cây hội thoại (node-based dialogue tree)
│   │   ├── items.json           <-- Database vật phẩm (tên, mô tả, RS effect)
│   │   ├── recipes.json         <-- Công thức ghép vật phẩm (A + B = C)
│   │   └── rooms/               <-- Định nghĩa từng phòng/scene
│   │       └── room_bedroom.json
│   │
│   ├── fonts/                   <-- Font chữ (.ttf)
│   │   └── arial.ttf
│   │
│   ├── images/                  <-- Hình gốc (có thể là bản draft/legacy)
│   │
│   └── textures/                <-- Hình chính thức dùng trong game
│       ├── backgrounds/         <-- Hình nền các phòng (bg_bedroom.png, ...)
│       ├── characters/          <-- Sprite nhân vật (linh.png, npc1.png)
│       └── objects/             <-- Sprite vật thể tương tác (wardrobe, desk, ...)
│
├── core/                        <-- MODULE CHÍNH - toàn bộ logic game
│   └── src/main/java/com/gnivol/game/
│       ├── GnivolGame.java      <-- Entry point, khởi tạo tất cả manager
│       ├── Constants.java       <-- Hằng số toàn cục (kích thước, RS threshold, ...)
│       │
│       ├── audio/               <-- Quản lý âm thanh
│       ├── component/           <-- ECS Components (dữ liệu gắn vào entity)
│       ├── data/                <-- Đọc/ghi dữ liệu, JSON parser
│       ├── entity/              <-- Game object / Entity
│       ├── input/               <-- Xử lý input (chuột, phím)
│       ├── model/               <-- Data class / POJO (ItemData, RoomData, ...)
│       ├── render/              <-- Vẽ scene lên màn hình
│       ├── screen/              <-- Các màn hình game (menu, gameplay, pause, ...)
│       ├── system/              <-- TẤT CẢ hệ thống game (logic chính)
│       │   ├── dialogue/        <-- Hệ thống hội thoại
│       │   ├── interaction/     <-- Xử lý tương tác người chơi với scene
│       │   ├── inventory/       <-- Hệ thống kho đồ + ghép đồ
│       │   │   └── ui/          <-- UI của inventory (hiển thị trong game)
│       │   ├── meta/            <-- Meta-horror: glitch, ending, độ ổn định
│       │   ├── puzzle/          <-- Hệ thống puzzle/đố
│       │   ├── rs/              <-- Reality Stability - hệ thống cốt lõi
│       │   ├── save/            <-- Lưu/tải game (auto-save, async write)
│       │   └── scene/           <-- Quản lý scene/phòng, chuyển cảnh
│       │
│       └── ui/                  <-- UI components dùng chung
│
├── desktop/                     <-- Launcher cho Desktop (legacy)
├── lwjgl3/                      <-- Launcher Desktop chính (LWJGL3 backend)
│   ├── icons/                   <-- Icon ứng dụng (.ico, .icns, .png)
│   └── src/.../Lwjgl3Launcher   <-- Main class khởi động game trên Desktop
│
├── build.gradle                 <-- Gradle config root
├── settings.gradle              <-- Khai báo các module
├── gradle.properties            <-- Phiên bản thư viện
└── run.bat                      <-- Script chạy nhanh trên Windows
```

---

## Chi tiết từng folder

### `assets/` - Tài nguyên game

Chứa **tất cả** file mà game load lúc runtime. Không chứa code.

| Folder | Chức năng | File điển hình |
|--------|-----------|---------------|
| `data/` | Dữ liệu game dạng JSON | `items.json`, `dialogues.json`, `recipes.json` |
| `data/rooms/` | Định nghĩa layout từng phòng: vật thể nào, ở đâu, tương tác gì | `room_bedroom.json` |
| `fonts/` | Font chữ render text trong game | `arial.ttf` |
| `images/` | Hình gốc/draft (có thể là bản cũ) | `Room.png`, `Linh.char.png` |
| `textures/backgrounds/` | Hình nền các phòng (full-screen) | `bg_bedroom.png` |
| `textures/characters/` | Sprite nhân vật | `linh.png`, `npc1.png` |
| `textures/objects/` | Sprite vật thể trong phòng (bàn, tủ, giường...) | `wardrobe.png`, `desk.png` |

**Lưu ý:** Mỗi file JSON trong `data/rooms/` định nghĩa **1 phòng** với danh sách `objects[]`.
Mỗi object có: vị trí (x,y,w,h), texture, loại (`interactable`/`furniture`/`item`/`door`),
và properties (có thể nhặt được không, có glitch không, thay đổi RS bao nhiêu).

---

### `core/` - Module chính (toàn bộ logic)

Đây là **trái tim** của game. Mọi thứ từ game logic đến UI đều nằm ở đây.

#### `GnivolGame.java` - Điểm khởi đầu
- Extend `Game` của LibGDX
- Khởi tạo: Stage, Ashley Engine, tất cả Manager
- Chuyển sang `MainMenuScreen` khi bắt đầu

#### `Constants.java` - Hằng số
- Kích thước cửa sổ: 1280x720
- RS threshold: Glitch (40), Horror (30), Meta (80)
- Inventory max: 8 slot
- Đường dẫn asset

---

#### `audio/` - Âm thanh
| File | Chức năng |
|------|-----------|
| `AudioManager.java` | Quản lý play/stop nhạc nền và SFX |

---

#### `component/` - ECS Components (Ashley)
Mỗi component là **1 mảnh dữ liệu** gắn vào entity. Không chứa logic.

| File | Chức năng |
|------|-----------|
| `BoundsComponent` | Hitbox / vùng va chạm (Rectangle) |
| `CollectibleComponent` | Đánh dấu vật thể có thể nhặt được + trạng thái đã nhặt chưa |
| `GlitchComponent` | Đánh dấu vật thể có hiệu ứng glitch khi RS thấp |
| `ItemInfoComponent` | Thông tin vật phẩm (itemId, tên, mô tả) |
| `RSModifierComponent` | Giá trị thay đổi RS khi tương tác |
| `TransformComponent` | Vị trí, kích thước, scale của entity |

---

#### `data/` - Quản lý dữ liệu
| File | Chức năng |
|------|-----------|
| `DataManager` | Trung tâm đọc/ghi dữ liệu game |
| `FlagManager` | Quản lý cờ/trạng thái game (đã mở cửa chưa, đã nói chuyện chưa...) |
| `ItemDatabase` | Load và tra cứu thông tin vật phẩm từ `items.json` |
| `JsonParser` | Tiện ích đọc file JSON |

---

#### `entity/` - Entity
| File | Chức năng |
|------|-----------|
| `GameObject` | Lớp cơ bản đại diện cho mọi vật thể trong game (mang nhiều component) |

---

#### `input/` - Xử lý đầu vào
| File | Chức năng |
|------|-----------|
| `InputHandler` | Xử lý click chuột, phím tắt, chuyển input đến các system |

---

#### `model/` - Data class (POJO)
Các class **chỉ chứa dữ liệu**, không có logic. Dùng để map từ JSON hoặc truyền dữ liệu giữa các system.

| File | Chức năng |
|------|-----------|
| `GameState` | Trạng thái hiện tại của game (RS, phòng hiện tại, cờ...) |
| `ItemData` | Dữ liệu 1 vật phẩm (tên, mô tả, RS effect, cursed?) |
| `PlayerData` | Dữ liệu người chơi (vị trí, inventory) |
| `RecipeData` | Công thức ghép 2 vật phẩm thành 1 |
| `RoomData` | Dữ liệu 1 phòng (tên, background, danh sách object) |

---

#### `render/` - Vẽ hình
| File | Chức năng |
|------|-----------|
| `SceneRenderer` | Vẽ background, các object, nhân vật lên màn hình theo thứ tự |

---

#### `screen/` - Các màn hình game
Mỗi screen là **1 trạng thái** của game. Chỉ 1 screen active tại 1 thời điểm.

| File | Chức năng |
|------|-----------|
| `BaseScreen` | Lớp cha chung, chứa logic render/update cơ bản |
| `MainMenuScreen` | Màn hình chính: New Game, Load, Settings, Exit |
| `GameScreen` | **Màn hình chơi game chính** - render scene, xử lý tương tác |
| `PauseScreen` | Màn hình tạm dừng |
| `SettingScreen` | Cài đặt (âm lượng, độ phân giải...) |
| `LoadGameScreen` | Chọn save file để tải game |
| `EndingScreen` | Màn hình kết thúc (nhiều ending khác nhau) |

---

#### `system/` - Tất cả hệ thống game

Đây là folder **quan trọng nhất**, chứa toàn bộ logic gameplay.

##### `system/dialogue/` - Hệ thống hội thoại
| File | Chức năng |
|------|-----------|
| `DialogueEngine` | Điều khiển luồng hội thoại: hiện text, cho người chơi chọn, chuyển node |

Hội thoại được định nghĩa trong `assets/data/dialogues.json` theo dạng **cây node**:
mỗi node có nội dung, có thể có lựa chọn (choices), và trỏ đến node tiếp theo.

##### `system/interaction/` - Tương tác người chơi
| File | Chức năng |
|------|-----------|
| `PlayerInteractionSystem` | Xử lý khi người chơi click vào object: nhặt đồ, mở cửa, xem, ... |
| `InteractionCallback` | Interface để GameScreen phản hồi lại (hiện text, fade, ...) |

Luồng hoạt động:
1. Người chơi click → `InputHandler` bắt sự kiện
2. `PlayerInteractionSystem` xác định click trúng object nào
3. Tùy loại object: nhặt vật phẩm / mở cửa / hiện mô tả / trigger dialogue
4. Gọi `InteractionCallback` để UI cập nhật

##### `system/inventory/` - Kho đồ + Ghép đồ
| File | Chức năng |
|------|-----------|
| `InventoryManager` | Quản lý danh sách vật phẩm người chơi đang giữ (max 8 slot) |
| `CraftingManager` | Ghép 2 vật phẩm theo công thức (từ `recipes.json`) |
| `ui/GameUI` | Giao diện inventory hiển thị trong game |
| `ui/InventoryUIController` | Điều khiển tương tác với UI inventory (kéo thả, click) |

##### `system/meta/` - Meta-Horror
Hệ thống làm cho game "ý thức được chính nó" - yếu tố kinh dị meta.

| File | Chức năng |
|------|-----------|
| `MetaHorrorManager` | Điều phối các sự kiện meta-horror (game "nói chuyện" với người chơi) |
| `StabilityManager` | Quản lý hiệu ứng khi RS thấp (màn hình rung, màu sắc biến đổi) |
| `EndingManager` | Xác định ending nào sẽ xảy ra dựa trên RS và các cờ |

##### `system/puzzle/` - Puzzle
| File | Chức năng |
|------|-----------|
| `PuzzleManager` | Quản lý các câu đố trong game (nhập mật mã, ghép mảnh giấy...) |

##### `system/rs/` - Reality Stability (Hệ thống cốt lõi)
**RS** (Reality Stability) là chỉ số **độ ổn định thực tại** - cốt lõi của gameplay.
- RS = 35-65: Thế giới bình thường
- RS < 35 hoặc RS > 65: Bắt đầu xuất hiện glitch
- RS < 10 hoặc RS > 90: Hiệu ứng kinh dị mạnh

| File | Chức năng |
|------|-----------|
| `RSManager` | Quản lý giá trị RS, tăng/giảm khi tương tác |
| `RSEvent` | Sự kiện khi RS thay đổi |
| `RSEventType` | Enum các loại sự kiện RS |
| `RSListener` | Interface lắng nghe thay đổi RS |
| `RSMonitorSystem` | Theo dõi RS liên tục, trigger hiệu ứng khi vượt ngưỡng |

##### `system/save/` - Lưu/Tải game
| File | Chức năng |
|------|-----------|
| `AutoSaveManager` | Tự động lưu game mỗi 5 giây |
| `AsyncFileWriter` | Ghi file không đồng bộ (tránh lag) |
| `GameSnapshot` | Chụp toàn bộ trạng thái game tại 1 thời điểm |
| `ISaveable` | Interface cho các object có thể lưu được |
| `SaveCallback` | Callback khi lưu xong |
| `SaveGuard` | Kiểm tra điều kiện trước khi lưu (đang combat thì không lưu) |
| `SaveLock` | Khóa tránh lưu đồng thời |
| `SaveUIController` | UI hiển thị trạng thái lưu (đang lưu..., lưu xong) |
| `SilentErrorLogger` | Ghi log lỗi lưu mà không crash game |
| `DirtyTracker` | Theo dõi thay đổi để biết khi nào cần lưu |

##### `system/scene/` - Quản lý Scene/Phòng
| File | Chức năng |
|------|-----------|
| `Scene` | Interface/base cho mỗi scene |
| `RoomScene` | Scene cụ thể cho 1 phòng (load từ JSON, chứa danh sách object) |
| `SceneManager` | Quản lý các scene, chuyển phòng |
| `SceneController` | Điều khiển logic trong scene (animation, trigger) |
| `ScreenFader` | Hiệu ứng fade đen khi chuyển phòng |

---

#### `ui/` - UI Components dùng chung
| File | Chức năng |
|------|-----------|
| `GameUI` | Giao diện tổng hợp trong game |
| `InventoryUIController` | Controller cho UI inventory |
| `SaveUIController` | Controller cho UI save |

> **Lưu ý:** Có sự trùng lặp giữa `ui/` và `system/inventory/ui/`. Đây có thể là bản refactor chưa hoàn tất.

---

### `desktop/` - Desktop Launcher (Legacy)
| File | Chức năng |
|------|-----------|
| `DesktopLauncher.java` | Launcher cũ, có thể không còn dùng |

---

### `lwjgl3/` - Desktop Launcher chính (LWJGL3)
| File | Chức năng |
|------|-----------|
| `Lwjgl3Launcher.java` | **Main class** - cấu hình cửa sổ và khởi động game |
| `StartupHelper.java` | Helper xử lý vấn đề khởi động (vd: macOS threading) |
| `icons/` | Icon ứng dụng cho các nền tảng |
| `resources/` | Icon nhỏ cho taskbar/title bar |

---

### File gốc (Root)
| File | Chức năng |
|------|-----------|
| `build.gradle` | Cấu hình Gradle gốc (dependency chung) |
| `settings.gradle` | Khai báo module: core, desktop, lwjgl3 |
| `gradle.properties` | Phiên bản LibGDX, Ashley, Java |
| `run.bat` | Script chạy nhanh: `gradlew desktop:run` |
| `.gitignore` | Loại trừ build/, .gradle/, IDE files |
| `.editorconfig` | Quy ước format code |

---

## Luồng chạy chính của game

```
Lwjgl3Launcher.main()
    │
    ▼
GnivolGame.create()
    ├── Tạo Stage (1280x720)
    ├── Tạo Ashley Engine
    ├── Khởi tạo: SceneManager, RSManager, InventoryManager, ...
    └── Chuyển đến MainMenuScreen
        │
        ▼
    [Người chơi chọn New Game]
        │
        ▼
    GameScreen
        ├── Load RoomScene từ JSON (room_bedroom.json)
        ├── SceneRenderer vẽ background + objects
        ├── Người chơi click → PlayerInteractionSystem xử lý
        │   ├── Click vật phẩm → InventoryManager thêm vào kho
        │   ├── Click cửa → SceneManager chuyển phòng
        │   ├── Click object → Hiện inspect text / trigger dialogue
        │   └── Mỗi tương tác → RSManager cập nhật RS
        │       ├── RS thấp → GlitchComponent kích hoạt
        │       └── RS quá thấp → MetaHorrorManager trigger sự kiện
        │
        ├── AutoSaveManager lưu game định kỳ
        └── EndingManager kiểm tra điều kiện kết thúc
```
