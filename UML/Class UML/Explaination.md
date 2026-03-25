## Hệ thống RS (Reality Shift)

**`RSEventType`** là enum liệt kê các loại hành động có thể ảnh hưởng RS: `DIALOGUE_CHOICE` (chọn lựa chọn hội thoại), `ITEM_INTERACTION` (tương tác vật phẩm), `PUZZLE_COMPLETE` (giải xong puzzle), `ROOM_ENTER` (vào phòng mới). Mỗi loại có thể cộng lượng RS khác nhau — ví dụ chọn hội thoại "dark" có thể cộng nhiều RS hơn tương tác vật phẩm bình thường.

**`RSEvent`** đóng gói một sự kiện ảnh hưởng RS, chứa `eventType` (loại gì), `rsAmount` (cộng bao nhiêu RS), và `source` (nguồn gốc, ví dụ "dialogue_chapter2_choice3"). Khi người chơi thực hiện hành động, game tạo RSEvent rồi gửi cho RSManager. Đây là bước đầu tiên trong activity: "Chọn hội thoại / Tương tác vật phẩm".

**`RSListener`** là interface cho Observer pattern. Có 2 method: `onRSChanged()` được gọi mỗi khi giá trị RS thay đổi (nhận giá trị cũ và mới), `onThresholdCrossed()` được gọi khi RS vượt qua hoặc tụt xuống dưới ngưỡng 50%. Bất kỳ class nào muốn "lắng nghe" sự thay đổi RS đều implement interface này — trong diagram, `RSMonitorSystem` là listener chính.

**`RSManager`** là bộ não của hệ thống RS. Thuộc tính `currentRS` là giá trị RS hiện tại, `maxRS` là giá trị tối đa (ví dụ 100), `threshold` là ngưỡng 50% (0.5f). Method `processEvent()` nhận RSEvent và quyết định "Hành động có ảnh hưởng RS không?" — nếu có thì gọi `addRS()`. Sau khi cộng, `clampRS()` giữ giá trị trong khoảng 0–max, rồi `notifyListeners()` báo cho tất cả RSListener, và `notifyMonitor()` báo riêng cho RSMonitorSystem. `getRSPercentage()` trả về phần trăm RS — đây là phép kiểm tra "RS hiện tại > 50%?" trong activity. `isAboveThreshold()` trả về boolean trực tiếp.

**`RSMonitorSystem`** là class thực thi hiệu ứng glitch khi RS cao, implements `RSListener`. Nó giữ danh sách `trackedObjects` (các GameObject trong scene hiện tại), `isGlitchActive` (đang glitch chưa), và reference đến `AudioManager` + `SceneRenderer`. Khi nhận thông báo RS > 50% qua `onRSUpdate()`, nó gọi `scanAllObjects()` — duyệt từng GameObject và kiểm tra "Đối tượng có gắn GlitchComponent?" Nếu có thì `applyGlitchEffects()` kích hoạt glitch, nếu không thì giữ nguyên. `updateSceneVisuals()` quyết định render bình thường hay render glitch cho toàn cảnh.

**`Component`** là abstract class đại diện cho một thành phần gắn vào GameObject, theo Component pattern. Thuộc tính `enabled` cho phép bật/tắt, method `update()` là abstract — mỗi loại component tự định nghĩa logic update riêng. Đây là lớp cha mà `GlitchComponent` kế thừa.

**`GlitchComponent`** kế thừa `Component`, là thành phần gây hiệu ứng méo mó trên một object cụ thể. `originalTexture` lưu texture gốc, `glitchTexture` là texture bị méo. `isGlitched` cho biết đang glitch chưa, `glitchIntensity` điều chỉnh mức độ, `flickerTimer` tạo hiệu ứng nhấp nháy. Method `activate()` bật glitch → `swapTexture()` tráo texture gốc sang texture méo + `applyColorDistortion()` thêm hiệu ứng màu — đây là bước "Tráo đổi Texture màu mè / Hiệu ứng nhiều" trong activity. `deactivate()` khôi phục về bình thường. `update()` override từ Component, chạy mỗi frame để cập nhật flickerTimer tạo hiệu ứng nhấp nháy liên tục.

**`GameObject`** đại diện cho một vật thể trong scene game. Nó chứa `name`, `position` (Vector2), danh sách `components`, và `active` (có đang hoạt động không). Method quan trọng nhất là `hasComponent()` — đây chính là phép kiểm tra "Đối tượng có gắn GlitchComponent?" trong activity. `getComponent()` dùng generic để lấy component theo type, `addComponent()`/`removeComponent()` để gắn/gỡ component lúc runtime. Không phải mọi GameObject đều có GlitchComponent — designer chọn object nào sẽ bị "ám" bằng cách gắn component vào.

**`AudioManager`** quản lý âm thanh, tương ứng nhánh "Âm thanh rùng rợn" trong activity. `isCreepyPlaying` theo dõi trạng thái hiện tại. `playCreepyAmbience()` phát nhạc rùng rợn khi RS cao, `playNormalAmbience()` phát nhạc bình thường khi RS thấp. Hai method `crossfadeTo...()` chuyển đổi mượt giữa 2 trạng thái trong khoảng `duration` giây, tránh cắt nhạc đột ngột gây khó chịu.

**`SceneRenderer`** quản lý render toàn scene, tương ứng bước cuối "Cập nhật màn hình & Chờ tương tác" trong activity. `glitchOverlayActive` cho biết có đang hiển thị overlay glitch lên toàn màn không. `renderNormal()` render cảnh bình thường, `renderGlitched()` render với hiệu ứng glitch toàn cục (ngoài glitch từng object riêng lẻ). `setGlitchOverlay()` bật/tắt overlay, `updateScreen()` push frame mới lên màn hình.

## Hệ thống Auto Save

**`SaveTriggerType`** — enum 4 loại trigger: `ROOM_CHANGE`, `PUZZLE_COMPLETE`, `INTERACT`, `DIALOGUE_SHOW`."Sự kiện: Chuyển phòng/xong puzzle/interact/hiển thị thoại".

**`DirtyTracker`** — tương ứng bước "Có thay đổi dữ liệu không?". Mỗi khi game state thay đổi (RS cộng, item nhặt, flag bật...), hệ thống gọi `markDirty()`. Khi save trigger, `hasAnyDirty()` kiểm tra — nếu không có gì thay đổi thì bỏ qua, tránh ghi file thừa.

**`SaveGuard`** — tương ứng bước "Cho phép lưu?". Kiểm tra `isJumpscareActive` và `isEventPlaying` — nếu đang trong jumpscare hoặc event thì `canSave()` trả về false, thoát luồng.

**`SaveLock`** — tương ứng bước "Đang bận lưu" + "Khóa luồng: IsSaving = true" + "Mở khóa: IsSaving = false". Thuộc tính `isSaving` đánh dấu `volatile` cho thread-safe. `tryLock()` kiểm tra và khóa atomic — nếu đang bận thì trả false (bỏ qua lần save này), nếu rảnh thì set true và cho phép tiếp. `unlock()` được gọi ở cuối dù thành công hay thất bại.

**`ISaveable`** — interface cho các hệ thống cung cấp data. Ngoài `toSnapshot()` và `loadFromSnapshot()` như bản cũ, bản này thêm `isDirty()` và `clearDirty()` để mỗi hệ thống tự biết mình có thay đổi không — phối hợp với `DirtyTracker`.

**`GameSnapshot`** — tương ứng bước "Snapshot: RS, Flags, Inventory, DialogueID v.v...". Chụp toàn bộ state game tại thời điểm đó. `capture()` duyệt danh sách ISaveable để gom data, `toJson()` chuyển sang JSON string.

**`AsyncFileWriter`** — tương ứng bước "ghi đè save.json bằng luồng phụ". Dùng `ExecutorService` để chạy IO trên background thread, không block main game thread. `writeAsync()` nhận JSON string và `SaveCallback` để báo kết quả.

**`SaveCallback`** — interface callback cho async write: `onSuccess()` khi ghi thành công, `onFailure()` khi lỗi IO.

**`SaveUIController`** — tương ứng bước "UI: Icon Saving". Chỉ có `showSavingIcon()` và `hideSavingIcon()` — bản này đơn giản hơn bản cũ vì chỉ có auto save, không có manual.

**`SilentErrorLogger`** — tương ứng bước "Exception Handle IO/Silent Log Error". Lỗi IO chỉ được log im lặng, không hiện cho người chơi thấy (khác bản cũ có thông báo lỗi).

**`AutoSaveManager`** — class trung tâm điều phối toàn bộ luồng. `onSaveTrigger()` là entry point, bên trong `executeSave()` chạy đúng thứ tự: check `DirtyTracker` → check `SaveGuard` → `SaveLock.tryLock()` → `captureSnapshot()` → `toJson()` → `AsyncFileWriter.writeAsync()` → callback success/failure → `SaveLock.unlock()`.