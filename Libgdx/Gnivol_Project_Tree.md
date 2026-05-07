# Gnivol — Cấu trúc Cây Thư mục & Diễn giải

> Tài liệu chi tiết toàn bộ layout của project Gnivol (LibGDX 1.14.0 + Java) — mỗi folder/file dùng để làm gì, vai trò trong kiến trúc.

---

## 📦 Root Project

```
Libgdx/                              ← Repo root (sub-folder của Gnivol)
├── .editorconfig                    ← Cấu hình editor (line endings, indent)
├── .gitattributes / .gitignore      ← Git config
├── .gradle/ .idea/                  ← Gradle cache + IntelliJ project files
├── README.md                        ← Mô tả ngắn project
├── PROJECT_STRUCTURE.md             ← Tài liệu kiến trúc legacy
├── build.gradle                     ← Gradle root build script
├── gradle.properties                ← Java version, memory settings
├── gradlew / gradlew.bat            ← Gradle wrapper (chạy gradle không cần install)
├── settings.gradle                  ← Multi-project setup (core, desktop, lwjgl3)
├── core/                            ← Game logic (platform-independent)
├── desktop/                         ← Desktop launcher (LWJGL3 backend)
├── lwjgl3/                          ← Alternative LWJGL3 launcher
├── assets/                          ← Tất cả game assets (data, images, audio, shaders)
└── build/                           ← Gradle build output (auto-generated, gitignored)
```

---

## 🎮 `core/` — Game logic platform-independent

Tất cả Java code chạy được trên mọi platform (desktop, Android tương lai). Không phụ thuộc vào platform-specific APIs.

```
core/
├── build.gradle                     ← Core module dependencies (LibGDX, Ashley, gdx-video)
└── src/main/java/com/gnivol/game/   ← Source root
    ├── GnivolGame.java              ← Game class (extends Game) — root state holder
    ├── Constants.java               ← World size, paths, MAX_INVENTORY_SLOTS, etc
    │
    ├── audio/                       ← Audio subsystem
    ├── data/                        ← Data layer (JSON loaders, flag/item DBs)
    ├── entity/                      ← Game object containers
    ├── input/                       ← Input multiplexer wrapper
    ├── model/                       ← POJO data models (parsed từ JSON)
    ├── screen/                      ← LibGDX Screen implementations
    ├── system/                      ← Manager classes (singleton-like services)
    └── ui/                          ← Scene2D UI components
```

### Root files của package `com.gnivol.game`

| File | Vai trò |
|------|---------|
| `GnivolGame.java` | **Root** — extends `com.badlogic.gdx.Game`. Init tất cả managers, set initial screen (LoginScreen). Chứa: AudioManager, SceneManager, InventoryManager, RSManager, FlagManager, GameSnapshot, AutoSaveManager, ItemDatabase. |
| `Constants.java` | Hằng số toàn cục: `WORLD_WIDTH=1280`, `WORLD_HEIGHT=720`, `MAX_INVENTORY_SLOTS=25`, paths tới data files. |

### `audio/` — Hệ thống âm thanh

| File | Vai trò |
|------|---------|
| `AudioManager.java` | BGM (Music) crossfade + SFX (Sound) one-shot/loop. Cache files trong HashMap. Volume preferences saved qua `Gdx.app.getPreferences("GnivolSettings")`. Multi-path loader: thử `audio/bgm/{id}.ogg/.mp3` rồi `sfx/{id}.mp3/.ogg`. |

### `data/` — Data layer

| File | Vai trò |
|------|---------|
| `DataManager.java` | Singleton load JSON room data. Parse `RoomData` qua LibGDX `Json` reflection. |
| `JsonParser.java` | Helper parser (legacy, ít dùng). |
| `FlagManager.java` | `Map<String, Boolean>` lưu game flags (chu_tro_visited_1, toilet_clogged, etc). Implements `ISaveable`. |
| `ItemDatabase.java` | Singleton parse `items.json` → `Map<itemID, ItemData>`. `getAllItemIds()` cho cheat. |

### `entity/`

| File | Vai trò |
|------|---------|
| `GameObject.java` | Container `id` + `type` + `Map<Class, Component>`. Add/get/has component. |

### `input/`

| File | Vai trò |
|------|---------|
| `InputHandler.java` | Wrapper quanh `InputMultiplexer` cho phép nhiều `InputProcessor` chia sẻ input. |

### `model/` — POJO data models

| File | Vai trò |
|------|---------|
| `GameState.java` | RS hiện tại, current room, player name. Implements `ISaveable`. |
| `ItemData.java` | POJO map từ items.json: itemID, itemName, description, inspectText, pickupSoundID, rsChangeValue, isCursed. |
| `RoomData.java` | POJO map từ rooms/*.json: roomId, roomName, background, list `RoomObject` (id/type/x/y/w/h/properties). |
| `PlayerData.java` | Legacy, ít dùng. |
| `RecipeData.java` | Recipe combine 2 items → 1 item (ví dụ: chuoi_chia_khoa + keo_502 → chia_khoa_fixed_final). |
| `dialogue/Choice.java` | Choice trong dialogue: id, content, nextNodeId, rsChange, cutsceneId, textEffects. |
| `dialogue/DialogueNode.java` | Node dialogue: speaker, content, portrait, **textEffects**, **textEffectsMassively**, onEnterSfx, choices. |
| `dialogue/DialogueTree.java` | Cây nodes — startNodeId, list nodes. |

### `screen/` — LibGDX Screens

| File | Vai trò |
|------|---------|
| `BaseScreen.java` | Abstract base — common methods cho mọi screen. |
| `LoginScreen.java` | Màn hình nhập tên player ban đầu. |
| `MainMenuScreen.java` | Menu chính — New Game / Load / Settings / Quit. |
| `SettingScreen.java` | Volume controls. |
| `LoadingScreen.java` | Màn loading khi vào New Game / minigame. Hiện random hint từ HINTS array. |
| `LoadGameScreen.java` | Legacy, không dùng. |
| `GameScreen.java` | **Main game** — orchestrate scene/dialog/cutscene/audio. ~1300 dòng — render pipeline, input handler, glitch shader. |
| `PauseScreen.java` | Pause menu. |
| `SlidingScreen.java` | Minigame "THE SHIFTING GRAVES" — đẩy marbles vào X marks. Reward puzzle_sliding_marble. |
| `LaserScreen.java` | Minigame "THE LABYRINTH" — WASD navigate avoid lasers. Reward `ca_vat_final`. |

### `system/` — Manager classes

```
system/
├── FontManager.java                 ← TTF + bitmap fonts (fontTitle, fontVietnamese, fontButton)
├── debug/
│   └── DebugRenderer.java           ← F1-F4 debug, drag, rotate, export coords
├── dialogue/
│   ├── DialogueEngine.java          ← Tree traversal — loadDialogue, advance, selectChoice
│   ├── ThoughtManager.java          ← RS-aware thoughts.json (LOW/MID/HIGH)
│   └── GlitchTextRenderer.java      ← Apply glitch effect lên text string
├── interaction/
│   ├── PlayerInteractionSystem.java ← Click dispatch — unproject, hitbox check, route via callback
│   ├── RoomInteractionHandler.java  ← Implementation chính — game logic per object id
│   └── InteractionCallback.java     ← Strategy interface (onItemCollected, onDoorInteracted, etc)
├── inventory/
│   ├── InventoryManager.java        ← List<String> items, addItem/removeItem. Savable.
│   └── CraftingManager.java         ← Combine 2 items → 1 (recipes.json)
├── minigame/
│   ├── SlidingLogic.java            ← Sliding minigame state (marble positions, walls, holes)
│   └── LaserLogic.java              ← Laser minigame state (player pos, turrets, lasers)
├── puzzle/
│   └── PuzzleManager.java           ← Track solved flags qua String set. Savable.
├── rs/
│   ├── RSManager.java               ← Reality Stability (0-100, default 35). Notify listeners khi cross 35/65 threshold
│   ├── RSListener.java              ← Interface listener
│   ├── RSEvent.java / RSEventType.java ← Event POJOs
│   └── RSMonitorSystem.java         ← Helper monitor
├── save/
│   ├── ISaveable.java               ← Interface: save(Json) / load(JsonValue)
│   ├── GameSnapshot.java            ← Aggregator — register savables, write/read JSON file
│   ├── AutoSaveManager.java         ← onSaveTrigger(eventKey) — saves on critical events
│   ├── AsyncFileWriter.java         ← Background thread file write
│   ├── DirtyTracker.java            ← Track changes (skip save if no diff)
│   ├── SaveCallback.java            ← Save complete listener
│   ├── SaveGuard.java / SaveLock.java ← Concurrency control
│   ├── SaveUIController.java        ← UI feedback "Saving..."
│   └── SilentErrorLogger.java       ← Silent error path
└── scene/
    ├── Scene.java                   ← Abstract base scene
    ├── RoomScene.java               ← Room scene — bg + gameObjects render. Mirror break event.
    ├── SceneManager.java            ← Stack-based scene switching + sceneBgmMap auto-crossfade
    ├── CutsceneManager.java         ← JSON step-based cutscene playback
    ├── ScreenFader.java             ← Black fade transitions
    └── OverlayManager.java          ← Tủ lạnh / khung QR overlay rendering
```

### `ui/` — Scene2D UI components

| File | Vai trò |
|------|---------|
| `DialogueUI.java` | Box hội thoại — typewriter, portrait (main + sub dim), choice buttons, glitch text effects (RS-aware + textEffectsMassively force max). |
| `InventoryUI.java` | Slot grid 5×5, drag-and-drop, item select, click sound. |
| `InventoryOverlay.java` | Overlay khi click tủ lạnh / wardrobe (zoom-in tới items bên trong). |
| `PuzzleDrawerUI.java` | Mini-puzzle ngăn kéo (mã số). |
| `LaserUI.java` | Phụ trợ Laser minigame (legacy). |
| `RSUI.java` | Bar RS hiện tại trên góc màn hình. |
| `GameUI.java` | Main game UI container (legacy). |
| `InventoryUIController.java` / `SaveUIController.java` | Legacy controllers. |

---

## 🖥 `desktop/` & `lwjgl3/` — Platform launchers

Mỗi launcher tạo `Lwjgl3Application` với cấu hình window (1280×720) và inject `GnivolGame`. Hai folder này functionally giống nhau (legacy duplicate).

```
desktop/
├── build.gradle
└── src/main/java/com/gnivol/game/desktop/
    └── DesktopLauncher.java         ← main() entry point — config window, set Vsync, create GnivolGame

lwjgl3/
└── src/main/java/com/gnivol/game/lwjgl3/
    └── Lwjgl3Launcher.java          ← Alternative entry point
```

> **Chạy game:** `./gradlew :desktop:run` hoặc `./gradlew :lwjgl3:run`.

---

## 🎨 `assets/` — Game assets

```
assets/
├── audio/                           ← BGM (Music format)
│   ├── ambient/                     ← Ambient loops (chưa dùng nhiều)
│   ├── bgm/                         ← Background music — bedroom_bgm.ogg, menu_bgm.ogg
│   └── sfx/                         ← (legacy folder, đa số SFX nằm ở /sfx/)
├── data/                            ← All game content as JSON
│   ├── items.json                   ← 12 items metadata
│   ├── recipes.json                 ← Crafting recipes
│   ├── dialogues.json               ← ~30 dialogue trees
│   ├── cutscenes.json               ← ~12 cutscene step scripts
│   ├── thoughts.json                ← RS-aware inspect text per object (LOW/MID/HIGH)
│   ├── overlays.json                ← Items config bên trong tủ lạnh / wardrobe overlay
│   └── rooms/                       ← 16+ room JSONs
├── fonts/                           ← TTF fonts (Vietnamese support)
├── images/                          ← Tất cả PNG/JPG
│   ├── back_ground/                 ← Backgrounds chia theo room
│   │   ├── bathroom/                ← clear_mirror, break_mirror, ghost_mirror, toilet, toilet_tac
│   │   ├── chu_nha/                 ← chu_nha
│   │   ├── hall/                    ← hall_final
│   │   ├── room/                    ← room.png, blank rooms, the_end, under_bed
│   │   └── tang_1/                  ← tang_1, chua_chay, glitch_door variants, hong_chia_khoa variants, khoa_van_tay
│   ├── characters/                  ← Portraits — Linh.char.png, NPC1.char.png, chu_tro.char.png, nerdy_kid1/2_final.char.png
│   ├── horror/                      ← Jumpscare images (jumpscare.png, removed_backgr1/2/3.png)
│   ├── item/                        ← Sprites items (chia_khoa, keo_502, dien_thoai, ca_vat, fabric_piece, glass_shard, etc)
│   ├── mini_games/                  ← Sprites cho Sliding (mng2: wall, box, X) + Laser
│   └── UI/                          ← UI assets (pointer, qrcode, to_be_continue, you_die, donate.jpg, thinking_final)
├── sfx/                             ← All SFX (mp3) — click, key_break, glass_break, scream1/2, phone_ringing, verification, stairs, cut_fabric, broken-glass, sfx_died, etc.
├── shaders/                         ← GLSL shaders
│   ├── chromakey.vert + .frag       ← Chroma key cho video (xóa nền xanh)
│   └── glitch.frag                  ← Glitch shader (pixel offset + chromatic aberration + invert)
├── textures/                        ← Legacy textures folder
│   ├── backgrounds/
│   ├── characters/
│   └── objects/                     ← fridge_open.png, wardrobe_open.png (legacy)
├── ui/                              ← Legacy UI assets
└── video/                           ← MP4/WebM cho cutscene (mirror_video_jumpscare, plush_toy_cutscene)
```

### Rooms list (16 rooms)

| File | Mô tả |
|------|-------|
| `room_bedroom.json` | Phòng ngủ — bg `new_blank_room_w_chair.png` |
| `room_bathroom.json` | Phòng tắm |
| `room_hallway.json` | Sảnh chung |
| `room_chu_nha.json` | Phòng chủ trọ |
| `room_tang_1.json` | Tầng 1 (có cửa chính) |
| `room_under_bed.json` | Gầm giường |
| `room_toilet_closeup.json` | Closeup bồn cầu |
| `room_chua_chay_closeup.json` | Closeup hộp chữa cháy |
| `room_password_closeup.json` | Closeup máy quét vân tay |
| `room_toilet_clogged.json` | Toilet sau khi tắc |
| `room_opposite.json` | Phòng đối diện (phá bằng rìu) |
| `new_blank_room_chair_on_bed.json` | Bedroom sau khi kê ghế lên giường |
| `the_end.json` | Bedroom sau khi treo cà vạt |
| `tang_1_glass_breaked.json` | Tầng 1 sau khi đập tủ chữa cháy |
| `hong_chia_khoa1.json` | Tủ chữa cháy frame 1 (sau click đầu) |
| `hong_chia_khoa5.json` | Tủ chữa cháy frame 5 (vỡ hoàn toàn) |

---

## 📋 JSON content overview

### `items.json` — 12 items
Cà vạt, chìa khóa gãy, keo 502, chìa khóa fixed, điện thoại, chìa khóa đồng, móc treo, xương, mảnh kính, mảnh vải, rìu, bình cứu hỏa.

### `dialogues.json` — ~30 dialogue trees
intro_thought, intro_phone_call, key_broke, neighbor_door_smell, mot_sach (Bệu), chu_tro_visit_1/2/3, chu_tro_in_bathroom, hand_under_bed, ending_text_rs0/100, confirm_hang_tie, tu_tu, final_ending_*, etc.

### `cutscenes.json` — ~12 cutscenes
hand_under_bed (plush_toy jumpscare), mirror_video_jumpscare, action_cut_bed, action_pickup_van_tay, action_move_chair, cutscene_rs_0/100, cutscene_death_super_doctor/xien, cutscene_mot_sach_kill, cutscene_suicide_ending, action_break_hop_chua_chay, final_ending.

### `thoughts.json` — RS-aware inspect text
3 variants per object (LOW/MID/HIGH theo RS). Phủ đa số object trong rooms.

### `rooms/*.json`
Mỗi room có: roomId, roomName, background path, list objects (id/type/x/y/w/h/properties). Properties hỗ trợ: itemId, targetScene, dialogueId, inspectText, collectible, hasGlitch, rsChange, altTextures, overlayId, repeatable.

---

## 🔄 Game Flow Diagram

```
DesktopLauncher.main()
    ↓
GnivolGame.create()
    │ Init managers (Audio, Scene, Inventory, RS, Save, Item, Flag, Puzzle)
    │ Register sceneBgmMap (bedroom→bedroom_bgm, hallway/chu_nha/tang_1→outside)
    │ setScreen(LoginScreen)
    ↓
LoginScreen → username
    ↓
MainMenuScreen → New Game / Load / Settings / Quit
    ↓
LoadingScreen (preload)
    ↓
GameScreen
    │ Init: SceneManager, DialogueUI, InventoryUI, RSUI, CutsceneManager, DebugRenderer
    │ Show first scene = room_bedroom
    │ Trigger intro: phone_ringing SFX loop + intro_thought dialogue → intro_phone_call
    │
    ├── Player click → PlayerInteractionSystem → RoomInteractionHandler
    │   ├── onItemCollected: addItem + showItemNotification + verification SFX
    │   ├── onDoorInteracted: changeSceneWithFade(targetScene) + open_door/stairs SFX
    │   ├── onObjectInteracted: special logic per id (mirror, plush_toy, drawer, etc)
    │   └── handleGenericInteractions: dialogue / overlay / thought
    │
    ├── Render pipeline (mỗi frame):
    │   1. checkEndGame
    │   2. SceneManager.update + render bg + objects
    │   3. RS glitch shader (nếu RS lệch hoặc FORCE_MAX_GLITCH)
    │   4. cutsceneSprite overlay (nếu cutscene play)
    │   5. videoPlayer overlay (nếu mirror video)
    │   6. DialogueUI portraits + Stage UI (inventory, dialog, RS bar)
    │   7. ScreenFader overlay
    │
    └── End game (RS=0 / RS=100 / death dialogue / final_ending):
        cutscene → fade_out → return_to_menu → MainMenuScreen
```

---

## 🎯 Design philosophy

1. **Data-driven** — Designer/writer thêm content qua JSON, không cần đụng code.
2. **Single source of truth** — Tất cả content config ở `assets/data/`, code chỉ là engine.
3. **Listener pattern** — Decouple producer/consumer (RSListener, CutsceneListener).
4. **Save anywhere via ISaveable** — Manager nào cần save thì implement interface, register vào GameSnapshot.
5. **Cutscene step-based scripting** — JSON-driven cutscene cho phép writer tạo cinematic mà không cần code.
6. **RS-aware everything** — Mỗi system đều phản ứng theo RS (glitch, text, thoughts, BGM).

---

## 📖 Key files cần biết

| Nhiệm vụ | File |
|----------|------|
| Thêm room mới | `assets/data/rooms/<roomId>.json` + register sceneBgmMap nếu cần |
| Thêm dialogue | `assets/data/dialogues.json` |
| Thêm cutscene | `assets/data/cutscenes.json` |
| Thêm item | `assets/data/items.json` + sprite ở `images/item/` |
| Thêm SFX | Drop file `.mp3` vào `assets/sfx/`, gọi qua `audioManager.playSFX("filename_no_ext")` |
| Thêm hint loading | `core/.../screen/LoadingScreen.java` HINTS array |
| Wire object click logic | `core/.../system/interaction/RoomInteractionHandler.java` |
| Wire cutscene step type | `core/.../system/scene/CutsceneManager.java` (step processor) |

---

*Tài liệu này phản ánh state codebase tại May 2026. Cập nhật khi major refactor xảy ra.*