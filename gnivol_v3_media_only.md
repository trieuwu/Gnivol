# GNIVOL — Phân Công v3 (Triệu = Media Only, Assets xong tuần 3)

---

## THAY ĐỔI SO VỚI v2

**Bỏ khỏi Triệu:** Tất cả JSON data, GameState.java, JsonParser.java
**Chuyển data cho:**
- `items.json` + `recipes.json` → **Tùng** (người parse ItemDatabase + CraftingManager)
- `rooms/*.json` → **Duy Anh** (người parse DataManager + SceneManager)
- `dialogue/*.json` → **Thành** (người parse DialogueEngine)
- `GameState.java` + `JsonParser.java` → **Tùng** (người làm Save pipeline)

**Triệu chỉ còn:** Sprite, background, texture, audio, font, visual effect code, UI render, intro screen, jumpscare visual.

**Tuần 3 Triệu xong TẤT CẢ asset** (sprite, background, atlas, audio, font). Tuần 4+ Triệu chỉ code effect + UI visual.

---

## PHÂN VÙNG MỚI

### Triệu — Media Only
- AudioManager code + tất cả file audio (.ogg/.wav)
- Tất cả sprite/background/texture + TextureAtlas pipeline
- Vietnamese font (.ttf + FreeType setup)
- GlitchComponent visual (swap texture, color distortion, flicker)
- SceneRenderer (renderNormal vs renderGlitched)
- CRT scanline, chromatic aberration, screen shake visual
- Glitch text render (ký tự bị thay random)
- Typewriter effect render
- GameUI visual (dialogue box, RS bar, inventory bar, notification)
- InventoryUIController visual (slot render, highlight, tooltip)
- SaveUIController visual (saving icon)
- Intro boot screen visual + animation
- Jumpscare visual + audio
- EndingScreen visual (text + credits)
- Puzzle overlay visual (3 ô số, nút ▲▼, animation đúng/sai)

### Duy Anh — Core Engine + Scene + UI Nav + Room Data
*(Thêm: rooms JSON, DataManager load room)*
- Project setup, Constants, GnivolGame, Launcher, package structure
- SceneManager, Scene, SceneController, ScreenFader
- InputHandler, DataManager
- Component abstract, GameObject
- Menu navigation logic (MainMenu, Pause, Setting, LoadGame)
- **MỚI:** Tạo `rooms/room_bedroom.json`, `rooms/room_bathroom.json`
- Performance, dispose, JAR packaging

### Thành — RS + Dialogue + Ending + Dialogue Data
*(Thêm: dialogue JSON)*
- RSManager, RSEvent, RSEventType, RSListener, RSMonitorSystem, StabilityManager
- DialogueEngine (load, traverse, choices, variable substitution)
- MetaHorrorManager, EndingManager, FlagManager
- **MỚI:** Tạo `dialogue/scene1_phone_call.json` + dialogue phòng 2

### Tùng — Inventory + Puzzle + Save + Item/Game Data
*(Thêm: items.json, recipes, GameState, JsonParser)*
- InventoryManager, CraftingManager, ItemDatabase, PlayerInteractionSystem
- 5 Component classes
- PuzzleManager + puzzle logic
- Save pipeline (AutoSaveManager → AsyncFileWriter, 8 class)
- **MỚI:** Tạo `items.json` (6 item), `recipes.json`
- **MỚI:** `GameState.java`, `JsonParser.java`
- Testing & QA

---

## LỊCH TRÌNH TUẦN

### TUẦN 1 — Thiết kế

| Ai | Việc |
|---|---|
| **Duy Anh** | README + kiến trúc. Định nghĩa room JSON schema (id, objects[], mỗi object có id/x/y/w/h/type/properties) |
| **Thành** | Class Diagram. Định nghĩa dialogue JSON schema (nodes, speaker, text, choices, rs_change, next, trigger) |
| **Tùng** | 6 Activity Diagram. Định nghĩa item JSON schema (id, name, description, textureRegion, collectible, rsModifier, category) |
| **Triệu** | GitHub repo + branch + board. Lên asset list (bao nhiêu sprite, bao nhiêu audio, style guide) |
| **Cả team** | Thống nhất JSON schema, interface method signatures, coding convention |

---

### TUẦN 2 — Skeleton

| Ai | Việc | Output |
|---|---|---|
| **Duy Anh** | GDX-Liftoff setup. Constants. GnivolGame. Launcher. Package structure + .gitkeep. Push develop. Component abstract + GameObject | Project chạy được |
| **Thành** | RSEvent, RSEventType enum, RSListener interface, RSManager skeleton. Unit test RS cộng/trừ/clamp | RS skeleton + test |
| **Tùng** | ItemData model. ItemDatabase skeleton (loadFromJson). InventoryManager (CRUD). CraftingManager skeleton. ISaveable interface. Tạo `items.json` 6 item + `recipes.json`. GameState.java + JsonParser.java | Item data + inventory logic + save models |
| **Triệu** | Tìm/tạo font .ttf tiếng Việt → test FreeType generate 16/24/32px. Bắt đầu vẽ sprite: 6 item icon, bắt đầu background phòng ngủ. Source/tạo audio placeholder (1 BGM bình thường, 1 BGM creepy, SFX pickup, SFX click) | Font + sprite WIP + audio WIP |
| **Cả team** | Pull develop, `./gradlew desktop:run` chạy OK |

---

### TUẦN 3 — Nền tảng + TRIỆU XONG ASSET

| Ai | Việc |
|---|---|
| **Duy Anh** | Scene abstract (enter/update/render/exit/dispose). SceneManager (changeScene, push/pop, HashMap RoomData). ScreenFader (state machine, alpha tween, callback). InputHandler (InputMultiplexer). BaseScreen abstract. DataManager: loadRoomData(path) parse room JSON → list GameObject. Tạo `room_bedroom.json` (6 object: giường, điện thoại, cửa sổ, mảnh giấy 1/2, ngăn kéo, cửa nhà tắm — mỗi cái có id/x/y/w/h/type). Chuyển cảnh Menu → GameScreen chạy |
| **Thành** | RSManager hoàn chỉnh: processEvent → addRS → clampRS → notifyListeners → onThresholdCrossed. getRSPercentage(), isAboveThreshold(). StabilityManager wrapper. Tạo `scene1_phone_call.json` (start → hnil_explain → choice A: rs -20/bad ending, choice B: rs +15/continue → hnil_continue). Test observer pattern |
| **Tùng** | 5 Component: Transform, Bounds, ItemInfo, Collectible, RSModifier. PlayerInteractionSystem: unproject mouse → loop objects → bounds.contains → dispatch. Nhặt item flow: click collectible → addItem → setCollected → fire RSEvent. InventoryManager implement ISaveable |
| **Triệu** | **HOÀN THÀNH TẤT CẢ ASSET:** |
| | — Tất cả item sprite (6 cái) final quality |
| | — Background phòng ngủ hoàn chỉnh |
| | — Background phòng tắm hoàn chỉnh |
| | — Alternate texture cho mỗi object có GlitchComponent (phiên bản méo/creepy) |
| | — TexturePacker gom → .atlas + .png (đặt trong assets/) |
| | — Tất cả audio files final: BGM bình thường, BGM creepy, SFX pickup, SFX click, SFX puzzle đúng, SFX puzzle sai, SFX jumpscare beep |
| | — Font .ttf final + FreeType generate 3 size cache |
| | — UI asset: button skin, slot border, tooltip bg, RS bar graphic |
| | — Intro screen asset: terminal font style |
| | — Jumpscare image (placeholder: màn đỏ + text hoặc vẽ) |
| | — `AudioManager.java` singleton: playMusic, stopMusic, playSound, setVolume, crossfadeToCreepy |
| | — MainMenuScreen visual: 5 nút Scene2D, hover effect, background |

**Milestone tuần 3:** Menu → New Game → Fade → Phòng ngủ hiện đúng background + vật thể → Click → Nhặt item → RS thay đổi. **Tất cả asset đã có — không cần placeholder nữa.** Tiếng Việt đúng.

---

### TUẦN 4 — Chức năng chính (40%)

| Ai | Việc |
|---|---|
| **Duy Anh** | SceneController: requestSceneChange → fade → exit → save RoomData → load → enter → fade. Lưu/restore RoomData. Menu logic: New Game → intro → game, Load Game → LoadGameScreen, Setting → slider volume, Quit → exit. PauseMenu: ESC toggle, Resume/Save/Setting/Back/Quit + confirmation dialog. Tạo `room_bathroom.json` |
| **Thành** | RSMonitorSystem: scan objects → check GlitchComponent → rs > threshold → activate/deactivate. Register RSListener. DialogueEngine hoàn chỉnh: loadDialogue, startDialogue, getCurrentNode, advance, selectChoice → RSEvent(DIALOGUE_CHOICE). Variable substitution {player_name}. MetaHorrorManager skeleton. DialogueEngine implement ISaveable |
| **Tùng** | CraftingManager merge hoàn chỉnh: select 2 → getMergeResult → remove 2 + add result. PuzzleManager: openPuzzle, submitAnswer("314"), isPuzzleSolved. AutoSaveManager full pipeline: trigger → DirtyTracker → SaveGuard → SaveLock → GameSnapshot.capture() → toJson() → AsyncFileWriter.writeAsync → callback. DirtyTracker, SaveGuard, SaveLock, GameSnapshot, AsyncFileWriter. PuzzleManager implement ISaveable |
| **Triệu** | GlitchComponent visual: activate() swap texture + colorDistortion + flicker 0.1s. SceneRenderer: renderNormal vs renderGlitched (CRT + vignette). AudioManager: crossfadeTo(track, duration). GameUI render: dialogue box + inventory 8 slot + RS bar. SaveUIController: showSavingIcon/hide. Puzzle overlay visual: 3 ô + ▲▼ + animation đúng/sai. Dialogue render: typewriter 30ms/char + click skip + glitch text khi RS<40 |

**Milestone tuần 4:** Chơi 1 phòng đầy đủ — nhặt, ghép, puzzle 3-1-4, dialogue choice → RS → glitch, auto save. Âm thanh crossfade. Asset final.

---

### TUẦN 5 — Nâng cao (70%)

| Ai | Việc |
|---|---|
| **Duy Anh** | Load phòng 2 (bathroom) qua SceneManager. Performance: dispose audit, no leak. LoadGameScreen logic: 3 slot, đọc save, click → restore. GameUI state management (gameplay/dialogue/puzzle/pause layer). InventoryUIController slot selection logic: click → highlight → click 2nd → merge. Click slot + click object → use item |
| **Thành** | EndingManager: checkConditions (flags + RS) → EndingType → triggerEnding. 4 endings logic. FlagManager: set/get/getAll, implement ISaveable. 6 flags. MetaHorrorManager: RS≥60 hints, RS≥80 jumpscare eligible → SaveGuard. Fine-tune RS values. Tạo dialogue phòng 2 JSON |
| **Tùng** | Validation: đầy túi, trùng item, merge fail, puzzle đã giải. Load Game logic: parse → restore tất cả system. SilentErrorLogger. Edge case: corrupt save, file xóa, lock stuck. Auto save trigger: vào phòng/nhặt/puzzle/dialogue |
| **Triệu** | Glitch text effect (flicker mỗi frame). CRT scanline (ShapeRenderer đường ngang 3px). Chromatic aberration (RS>60: vẽ 3 lần offset ±2px tint R/G/B). Screen shake (±3px, 0.3s decay). Intro boot screen (đen → xanh lá typewriter → loading bar 3s → TextField → BẮT ĐẦU → CRT + flicker). Jumpscare (flash trắng 0.1s → đỏ 1.5s → beep → fade). InventoryUI visual hoàn thiện (highlight, tooltip, notification popup). RS bar animation (smooth tween, xanh→vàng→đỏ). EndingScreen visual (text + credits + nút về menu) |

**Milestone tuần 5:** 2 phòng, 3+ endings, save/load, glitch đầy đủ, intro, jumpscare, Vietnamese font.

---

### TUẦN 6 — Hoàn thiện (100%)

| Ai | Việc |
|---|---|
| **Duy Anh** | Fix bug chuyển cảnh. Optimize render. JAR packaging + test máy sạch. Multi-resolution test |
| **Thành** | Balance RS (playtest 3 lần). Fix bug RS/dialogue. Test 4 endings. Verify flag logic |
| **Tùng** | Fix bug inventory/puzzle/save. Test all combinations + edge cases. Cross-test module khác |
| **Triệu** | Polish visual: intensity glitch theo RS. Fix render bug (overlay chồng, text che, sprite lệch). Intro timing. Multi-resolution visual test |
| **Cả team** | Cross-testing. Integration test full flow. Chạy trên máy khác |

---

### TUẦN 7 — Báo cáo + Demo (Deadline 04/05)

| Ai | Việc |
|---|---|
| **Duy Anh** | Báo cáo: Giới thiệu + Kiến trúc + Công nghệ. Quay video demo |
| **Thành** | Báo cáo: UML + giải thích RS. Quay video demo |
| **Tùng** | Slide thuyết trình. Bài FB |
| **Triệu** | Báo cáo: Kết quả + Screenshots + Hướng phát triển. Bài FB |
| **Cả team** | Review báo cáo, rehearse |

---

## DATA CHUYỂN CHO AI — TÓM TẮT

| File data | Người tạo | Người parse | Lý do |
|---|---|---|---|
| `items.json` (6 item) | Tùng | Tùng (ItemDatabase) | Tùng biết item cần field gì cho logic |
| `recipes.json` | Tùng | Tùng (CraftingManager) | Tùng biết recipe format |
| `room_bedroom.json` | Duy Anh | Duy Anh (DataManager) | Duy Anh biết object cần field gì cho SceneManager |
| `room_bathroom.json` | Duy Anh | Duy Anh (DataManager) | Tương tự |
| `scene1_phone_call.json` | Thành | Thành (DialogueEngine) | Thành biết dialogue tree cần field gì |
| Dialogue phòng 2 | Thành | Thành (DialogueEngine) | Tương tự |
| `GameState.java` | Tùng | Tùng (Save pipeline) | Tùng quản lý serialize/deserialize |
| `JsonParser.java` | Tùng | Tùng (Save pipeline) | Đi cùng GameState |

**Nguyên tắc:** Ai parse file đó thì người đó tạo file đó — biết rõ cần field gì, format gì, không cần sync.

---

## TRIỆU TUẦN 3 — CHECKLIST ASSET

Triệu cần hoàn thành **TẤT CẢ** các asset sau trước cuối tuần 3:

### Hình ảnh
- [ ] 6 item sprite final (paper_fragment_1, paper_fragment_2, password_note, drawer_key, bathroom_key, phone)
- [ ] Background phòng ngủ (full 1280×720)
- [ ] Background phòng tắm (full 1280×720)
- [ ] Alternate/glitch texture cho mỗi object có GlitchComponent
- [ ] UI asset: button normal/hover, inventory slot border, tooltip bg, RS bar frame
- [ ] Intro screen: terminal-style graphic elements
- [ ] Jumpscare image (1 tấm)
- [ ] TexturePacker → .atlas + .png gom hết sprite

### Âm thanh
- [ ] BGM bình thường (loop, .ogg)
- [ ] BGM creepy (loop, .ogg)
- [ ] SFX: pickup item
- [ ] SFX: click/interact
- [ ] SFX: puzzle đúng
- [ ] SFX: puzzle sai
- [ ] SFX: jumpscare beep
- [ ] SFX: dialogue advance (optional)

### Font
- [ ] File .ttf hỗ trợ Vietnamese đầy đủ
- [ ] FreeType generate test OK: 16px, 24px, 32px
- [ ] Hiển thị đúng: ă, â, ê, ô, ơ, ư, đ + tất cả dấu

### Code (media)
- [ ] AudioManager.java singleton hoàn chỉnh
- [ ] MainMenuScreen visual (5 nút + hover + background)
- [ ] Font integration vào GnivolGame (static font cache)
