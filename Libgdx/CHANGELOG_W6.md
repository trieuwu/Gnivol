# W6 — Backend Frameworks (JAV-28 Duy Anh)

> Ngay: 2026-04-20 | Branch: `feature/core-engine`

---

## Tong quan

| # | File | Trang thai | Mo ta |
|---|------|-----------|-------|
| 1 | `data/FlagManager.java` | IMPLEMENT | Co trang thai game + Save/Load |
| 2 | `audio/AudioManager.java` | UPGRADE | Full BGM/SFX/Ambient + crossfade |
| 3 | `system/scene/CutsceneManager.java` | MOI | JSON-driven cutscene voi 10 step types |
| 4 | `ui/InventoryOverlay.java` | MOI | Overlay container (tu lanh, tu quan ao) |
| 5 | `ui/PhoneMapOverlay.java` | MOI | Ban do 3 dia diem + RS gate |
| 6 | `ui/PhoneOverlay.java` | MOI | Phone UI: Call + Map mode |
| 7 | `ui/QRPopup.java` | MOI | URL + number input + PuzzleManager |
| 8 | `model/dialogue/DialogueNode.java` | SUA | +onEnterCutscene field |
| 9 | `system/dialogue/DialogueEngine.java` | SUA | +cutscene trigger khi enter node |
| 10 | `system/scene/SceneManager.java` | SUA | +auto BGM theo scene |
| 11 | `system/rs/RSManager.java` | SUA | +isMapUnlocked() + threshold hooks 50/65 |
| 12 | `system/rs/RSListener.java` | SUA | +onSpecificThreshold() default method |
| 13 | `GnivolGame.java` | SUA | +FlagManager tich hop |
| 14 | `Constants.java` | SUA | +SCENE_HALLWAY, SCENE_OPPOSITE, RS_MAP_UNLOCK |
| 11 | `system/rs/RSEventType.java` | SUA | +OBJECT_INTERACTION |
| 12 | `system/rs/RSManager.java` | SUA | +isMapUnlocked() |
| 13 | `assets/data/items.json` | FIX | Thieu dau phay dong 27 |
| 14 | `assets/data/cutscenes.json` | MOI | Skeleton + vi du hand_under_bed |
| 15 | `assets/data/overlays.json` | MOI | Skeleton + vi du fridge_interior |
| 16 | `assets/data/puzzles.json` | MOI | Skeleton + puzzle_drawer + qr_proptit |
| 17 | `assets/audio/bgm/` | MOI | Thu muc rong cho Thanh |
| 18 | `assets/audio/sfx/` | MOI | Thu muc rong cho Thanh |
| 19 | `assets/audio/ambient/` | MOI | Thu muc rong cho Thanh |

**Build:** Compile thanh cong (0 errors)

---

# CHI TIET THAY DOI

---

## 1. FlagManager.java — IMPLEMENT

**Path:** `core/src/main/java/com/gnivol/game/data/FlagManager.java`
**Truoc do:** Class rong

**API:**
```java
flags.set("first_time_bed");           // set = true
flags.set("key_broke", false);         // set = value
flags.get("fingerprint_ok");           // false neu chua set
flags.has("toilet_clogged");           // check ton tai
flags.clear("key");                    // xoa 1 flag
flags.reset();                         // xoa tat ca
```

**Save/Load:** ISaveable, key `"flagManager"` trong save JSON.

**Diff:**
```diff
- public class FlagManager {
- }
+ public class FlagManager implements ISaveable {
+     private final Map<String, Boolean> flags;
+     public void set(String key) { ... }
+     public void set(String key, boolean value) { ... }
+     public boolean get(String key) { ... }
+     public boolean has(String key) { ... }
+     public void clear(String key) { ... }
+     public void reset() { ... }
+     public void save(Json json) { ... }
+     public void load(JsonValue jsonValue) { ... }
+ }
```

---

## 2. AudioManager.java — UPGRADE

**Path:** `core/src/main/java/com/gnivol/game/audio/AudioManager.java`
**Truoc do:** Chi co get/set volume (2 field, 4 method)

**API moi:**
```java
// BGM (Music, looping, audio/bgm/{id}.ogg)
audioManager.playBGM("bgm_bedroom");
audioManager.stopBGM();
audioManager.crossfadeBGM("bgm_hallway", 2f);
audioManager.update(delta);  // goi moi frame cho crossfade

// SFX (Sound, one-shot, audio/sfx/{id}.wav)
audioManager.playSFX("sfx_pickup");
audioManager.playSFX("sfx_scream", 0.8f);

// Ambient (Music, looping, audio/ambient/{id}.ogg)
audioManager.playAmbient("ambient_creepy");
audioManager.stopAmbient();

// Volume (giu nguyen API cu + alias moi)
audioManager.setBGMVolume(0.5f);    // alias setMusicVolume
audioManager.setSFXVolume(0.8f);    // alias setSfxVolume

audioManager.dispose();  // giai phong tat ca cache
```

**Ky thuat:**
- Load on demand, cache trong HashMap (KISS, khong dung AssetManager)
- BGM/Ambient dung `Music` (streaming), SFX dung `Sound` (in-memory)
- Crossfade: fade out cu + fade in moi dong thoi, can `update(delta)`
- `dispose()` giai phong tat ca Music/Sound da cache

**Diff tom tat:**
```diff
  // Giu nguyen:
  private float musicVolume = 0.7f;
  private float sfxVolume = 1.0f;
  public float getMusicVolume() { ... }
  public void setMusicVolume(float volume) { ... }
  public float getSfxVolume() { ... }
  public void setSfxVolume(float volume) { ... }

+ // Them moi:
+ private Map<String, Music> bgmCache;
+ private Map<String, Sound> sfxCache;
+ private Map<String, Music> ambientCache;
+ private Music currentBGM, currentAmbient;
+ private crossfade state fields;
+
+ public void playBGM(String id) { ... }
+ public void stopBGM() { ... }
+ public void crossfadeBGM(String newId, float duration) { ... }
+ public void update(float delta) { ... }
+ public void playSFX(String id) { ... }
+ public void playSFX(String id, float volume) { ... }
+ public void playAmbient(String id) { ... }
+ public void stopAmbient() { ... }
+ public void setBGMVolume(float v) { ... }  // alias
+ public void setSFXVolume(float v) { ... }  // alias
+ public void dispose() { ... }
+ private Music loadBGM(String id) { ... }
+ private Sound loadSFX(String id) { ... }
+ private Music loadAmbient(String id) { ... }
```

---

## 3. CutsceneManager.java — MOI

**Path:** `core/src/main/java/com/gnivol/game/system/scene/CutsceneManager.java`

**JSON-driven.** Doc `assets/data/cutscenes.json`, chay tung step voi delta timing.

**10 step types:**

| Type | Field dung | Hanh dong |
|------|-----------|-----------|
| `flash` | color, duration | Listener.onFlash() |
| `show_sprite` | sprite, duration | Listener.onShowSprite() |
| `sfx` | id | AudioManager.playSFX() — tuc thi, khong doi |
| `shake` | intensity, duration | Listener.onShake() |
| `fade_out` | duration | Listener.onFadeOut() |
| `fade_in` | duration | Listener.onFadeIn() |
| `wait` | duration | Im lang, doi het duration |
| `rs_change` | value | RSManager.addRS() — tuc thi |
| `dialogue` | id | Listener.onDialogue() — doi onDialogueFinished() |
| `swap_sprite` | id (target), sprite | Listener.onSwapSprite() — tuc thi |

**API:**
```java
CutsceneManager cm = new CutsceneManager();
cm.loadCutscenes("data/cutscenes.json");
cm.setRSManager(rsManager);
cm.setAudioManager(audioManager);
cm.setListener(cutsceneListener);

cm.play("hand_under_bed");
cm.update(delta);          // goi moi frame
cm.isPlaying();            // block input khi true
cm.onDialogueFinished();   // goi khi dialogue ket thuc
```

**CutsceneListener interface (GameScreen implement sau):**
```java
interface CutsceneListener {
    void onFlash(String color, float duration);
    void onShowSprite(String sprite, float duration);
    void onShake(float intensity, float duration);
    void onFadeOut(float duration);
    void onFadeIn(float duration);
    void onSwapSprite(String target, String newSprite);
    void onDialogue(String dialogueId);
    void onCutsceneFinished(String cutsceneId);
}
```

---

## 4. InventoryOverlay.java — MOI

**Path:** `core/src/main/java/com/gnivol/game/ui/InventoryOverlay.java`

Doc `assets/data/overlays.json`. Mo overlay container (tu lanh, tu quan ao).

**API:**
```java
InventoryOverlay io = new InventoryOverlay();
io.loadOverlays("data/overlays.json");
io.setListener(overlayListener);

io.open("fridge_interior");
io.render(batch);
io.handleClick(worldX, worldY);  // true neu trung item
io.close();
io.isOpen();
io.dispose();
```

**OverlayListener:**
```java
interface OverlayListener {
    void onItemCollected(String overlayId, String itemId);
    void onOverlayClosed(String overlayId);
}
```

---

## 5. PhoneMapOverlay.java — MOI

**Path:** `core/src/main/java/com/gnivol/game/ui/PhoneMapOverlay.java`

3 dia diem (PTIT, Benh vien, Phung Khoang). RS < 65 = xam + tooltip do.

**API:**
```java
PhoneMapOverlay map = new PhoneMapOverlay(stage, rsManager, font);
map.setListener(locationListener);
map.show();
map.hide();
map.isVisible();
```

---

## 6. PhoneOverlay.java — MOI

**Path:** `core/src/main/java/com/gnivol/game/ui/PhoneOverlay.java`

Phone UI: 2 tab (Goi / Ban do). Tab Goi hien contacts. Tab Ban do delegate PhoneMapOverlay.

**API:**
```java
PhoneOverlay phone = new PhoneOverlay(stage, rsManager, font);
phone.setCallListener(callListener);
phone.getMapOverlay().setListener(locationListener);
phone.open();
phone.close();
phone.isOpen();
```

---

## 7. QRPopup.java — MOI

**Path:** `core/src/main/java/com/gnivol/game/ui/QRPopup.java`

Popup: URL label + TextField nhap so + nut Gui. Delegate PuzzleManager.submitAnswer().

**API:**
```java
QRPopup qr = new QRPopup(stage, font, puzzleManager);
qr.open("https://proptit.club", "qr_proptit");
qr.close();
qr.isOpen();
```

---

## 8. DialogueNode.java — THEM FIELD

**Path:** `core/src/main/java/com/gnivol/game/model/dialogue/DialogueNode.java`

```diff
  public String nextNodeId;
+
+ public String onEnterCutscene;  // cutscene ID trigger khi enter node

  public ArrayList<Choice> choices = new ArrayList<>();
```

**Dung trong JSON:**
```json
{"id": "linh_02", "onEnterCutscene": "linh_creepy", ...}
```

---

## 9. GnivolGame.java — THEM FlagManager

**Path:** `core/src/main/java/com/gnivol/game/GnivolGame.java`

```diff
+ import com.gnivol.game.data.FlagManager;

+ private FlagManager flagManager;

  // create():
+ flagManager = new FlagManager();
+ gameSnapshot.register(flagManager);

  // resetGameState():
+ if (flagManager != null) flagManager.reset();

  // loadGame():
+ if (flagManager != null) flagManager.reset();
+ if (flagManager != null) flagManager.load(root);

  // getter:
+ public FlagManager getFlagManager() {return flagManager;}
```

---

## 10. Constants.java — THEM CONSTANTS

```diff
  public static final String SCENE_BATHROOM = "room_bathroom";
+ public static final String SCENE_HALLWAY = "room_hallway";
+ public static final String SCENE_OPPOSITE = "room_opposite";
+
+ // RS
+ public static final float RS_MAP_UNLOCK = 65f;
```

---

## 11. RSEventType.java — THEM ENUM

```diff
  ITEM_INTERACTION,
+ OBJECT_INTERACTION,
  ROOM_ENTER,
```

---

## 12. RSManager.java — THEM METHOD

```diff
  public boolean isAboveThreshold() { ... }
+ public boolean isMapUnlocked() { return currentRS >= maxThreshold; }
```

---

## 13. items.json — FIX BUG

```diff
- "rsChangeValue": 6
- "isCursed": false
+ "rsChangeValue": 6,
+ "isCursed": false
```

---

## 14-16. Data Skeletons (MOI)

### cutscenes.json
```json
[
  {
    "id": "hand_under_bed",
    "steps": [
      {"type": "flash", "color": "white", "duration": 0.1},
      {"type": "wait", "duration": 0.2},
      {"type": "dialogue", "id": "bed_jumpscare"},
      {"type": "rs_change", "value": 8}
    ]
  }
]
```

### overlays.json
```json
[
  {
    "id": "fridge_interior",
    "background": "images/bg_fridge_interior.png",
    "items": []
  }
]
```

### puzzles.json
```json
[
  {
    "id": "puzzle_drawer",
    "type": "number_input",
    "answer": "912",
    "hint": "Mat ma ngan keo"
  },
  {
    "id": "qr_proptit",
    "type": "number_input",
    "answer": "910",
    "hint": "Sinh nhat CLB ProPTIT"
  }
]
```

---

## 17-19. Audio directories (MOI)

```
assets/audio/bgm/      (rong — Thanh them .ogg)
assets/audio/sfx/      (rong — Thanh them .wav)
assets/audio/ambient/  (rong — Thanh them .ogg)
```

---

# HUONG DAN CHO TEAM

## Thanh (JAV-29) — Audio + Cutscene config

**Dung ngay:**
- Bo audio vao `assets/audio/bgm/`, `sfx/`, `ambient/`
- Viet cutscene JSON theo schema trong `cutscenes.json`
- Goi `AudioManager.playBGM("ten_file_khong_duoi")` de test

**Step types co the dung:**
`flash`, `show_sprite`, `sfx`, `shake`, `fade_out`, `fade_in`, `wait`, `rs_change`, `dialogue`, `swap_sprite`

## Tung (JAV-30) — Items + Overlay + Scene

**Dung ngay:**
- `game.getFlagManager().set("toilet_clogged")` / `.get("key")`
- Config overlay JSON trong `overlays.json` (background + items voi x,y,w,h)
- Config puzzle JSON trong `puzzles.json` (id, type, answer)
- `Constants.SCENE_HALLWAY` / `SCENE_OPPOSITE` cho room JSON

## Trieu (JAV-31) — Dialogue JSON

**Dung ngay:**
- Them `"onEnterCutscene": "linh_creepy"` vao dialogue node JSON
- CutsceneManager se tu dong trigger khi GameScreen doc field nay
