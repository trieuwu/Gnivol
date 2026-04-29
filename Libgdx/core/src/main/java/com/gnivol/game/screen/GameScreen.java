package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gnivol.game.component.BoundsComponent;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.input.InputHandler;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.model.dialogue.DialogueTree;
import com.gnivol.game.system.FontManager;
import com.gnivol.game.system.dialogue.DialogueEngine;
import com.gnivol.game.system.dialogue.ThoughtManager;
import com.gnivol.game.system.interaction.InteractionCallback;
import com.gnivol.game.system.interaction.PlayerInteractionSystem;
import com.gnivol.game.system.interaction.RoomInteractionHandler;
import com.gnivol.game.system.rs.RSListener;
import com.gnivol.game.system.scene.RoomScene;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.system.scene.ScreenFader;
import com.gnivol.game.ui.DialogueUI;
import com.gnivol.game.ui.InventoryUI;
import com.gnivol.game.ui.RSUI;

import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import java.util.*;

public class GameScreen extends BaseScreen {

    private SpriteBatch batch;
    private SceneManager sceneManager;
    private ScreenFader screenFader;
    private InputHandler inputHandler;
    private PlayerInteractionSystem interactionSystem;
    private boolean firstShow = true;

    private DialogueEngine dialogueEngine;
    private DialogueUI dialogueUI;
    private Map<String, DialogueTree> dialogueDatabase;

    private RSUI rsUI;
    private Label inspectLabel;
    private Table inspectTable;

    private ShapeRenderer dimRenderer;
    private InventoryUI inventoryUI;

    private com.gnivol.game.system.scene.OverlayManager overlayManager;
    private com.gnivol.game.system.puzzle.PuzzleManager puzzleManager;
    private com.gnivol.game.ui.PuzzleDrawerUI puzzleDrawerUI;
    private com.badlogic.gdx.scenes.scene2d.ui.Skin defaultSkin;

    private com.gnivol.game.ui.InventoryOverlay inventoryOverlaySystem;
    private com.gnivol.game.system.debug.DebugRenderer debugManager;

    private com.gnivol.game.system.scene.CutsceneManager cutsceneManager;
    private boolean isFlashing = false;
    private float flashAlpha = 0f;
    private Color flashColor = Color.WHITE;
    // Video playback
    private VideoPlayer videoPlayer;
    private boolean videoPlaying = false;
    private final float[] videoRect = {-1, -1, -1, -1}; // x, y, w, h

    // Chroma key shader cho video (xóa nền xanh)
    private ShaderProgram chromaShader;
    // Màu key (xanh lá), threshold, smoothing — chỉnh ở đây nếu nền không khớp
    private static final float[] CHROMA_KEY_RGB = {0.0f, 1.0f, 0.0f};
    private static final float CHROMA_THRESHOLD = 0.4f;
    private static final float CHROMA_SMOOTHING = 0.1f;
    // Cutscene sprite
    private Texture cutsceneSprite;
    private float cutsceneSpriteTimer;
    private float cutsceneSpriteDuration;
    private final float[] cutsceneSpriteRect = {-1, -1, -1, -1}; // x, y, w, h
    private boolean isGameOver = false;
    private Texture vignetteTexture;
    private boolean isInitialized = false;

    public GnivolGame getGnivolGame() { return game; }
    public SceneManager getSceneManager() { return sceneManager; }
    public InventoryUI getInventoryUI() { return inventoryUI; }
    public com.gnivol.game.system.puzzle.PuzzleManager getPuzzleManager() { return puzzleManager; }
    public com.gnivol.game.system.scene.CutsceneManager getCutsceneManager() { return cutsceneManager; }
    public DialogueEngine getDialogueEngine() { return dialogueEngine; }
    public DialogueUI getDialogueUI() { return dialogueUI; }

    // --- BIẾN HIỆU ỨNG TÂM LÝ (RS GLITCH) ---
    private float rsCycleTimer = 0f;          // Đồng hồ đếm chu kỳ 10s
    private boolean isRsGlitching = false;    // Đang glitch hay không
    private float rsGlitchDurationTimer = 0f; // Đồng hồ đếm thời gian giật (kéo dài 0.75s)
    private float shaderTime = 0f;            // Biến truyền cho Shader
    private ShaderProgram glitchShader;
    private Vector2 originalCameraPos = new Vector2(); // Ghi nhớ vị trí gốc để rung xong trả về

    public RoomData.RoomObject getRoomObjectData(String id) {
        if (sceneManager.getCurrentScene() == null) return null;
        for (RoomData.RoomObject obj : sceneManager.getCurrentScene().getRoomData().objects) {
            if (obj.id.equals(id)) return obj;
        }
        return null;
    }

    public GameScreen(GnivolGame game) {
        super(game);
    }

    @Override
    public void show() {
        if (!isInitialized) {
            game.getStage().clear();

            overlayManager = new com.gnivol.game.system.scene.OverlayManager();
            sceneManager = game.getSceneManager();
            screenFader = game.getScreenFader();
            inputHandler = game.getInputHandler();
            interactionSystem = game.getPlayerInteractionSystem();
            batch = new SpriteBatch();
            dimRenderer = new ShapeRenderer();
            debugManager = new com.gnivol.game.system.debug.DebugRenderer(game);
            vignetteTexture = createVignetteTexture(512, 512);

            // Load chroma key shader cho video (xóa nền xanh)
            try {
                String vert = Gdx.files.internal("shaders/chromakey.vert").readString();
                String frag = Gdx.files.internal("shaders/chromakey.frag").readString();
                chromaShader = new ShaderProgram(vert, frag);
                if (!chromaShader.isCompiled()) {
                    Gdx.app.error("GameScreen", "ChromaKey shader compile error: " + chromaShader.getLog());
                    chromaShader.dispose();
                    chromaShader = null;
                }
                // 3. Load file FRAGMENT cho GLITCH (Nhiễu sóng)
                // Tự viết một Vertex Shader chuẩn của LibGDX để nó ôm khít vào glitch.frag
                String defaultVert =
                    "attribute vec4 a_position;\n" +
                        "attribute vec4 a_color;\n" +
                        "attribute vec2 a_texCoord0;\n" +
                        "uniform mat4 u_projTrans;\n" +
                        "varying vec4 v_color;\n" +
                        "varying vec2 v_texCoords;\n" +
                        "void main() {\n" +
                        "    v_color = a_color;\n" +
                        "    v_texCoords = a_texCoord0;\n" +
                        "    gl_Position = u_projTrans * a_position;\n" +
                        "}";

                String fragGlitch = Gdx.files.internal("shaders/glitch.frag").readString();
                glitchShader = new ShaderProgram(defaultVert, fragGlitch);

                if (!glitchShader.isCompiled()) {
                    Gdx.app.error("GameScreen", "Glitch shader lỗi: " + glitchShader.getLog());
                    glitchShader = null;
                }
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Failed to load chromakey shader", e);
                chromaShader = null;
            }


            FontManager fm = game.getFontManager();

            inventoryUI = new InventoryUI(game.getStage(), game.getInventoryManager(), game.getCraftingManager(), game.getRsManager(), fm.fontVietnamese);
            inventoryUI.setAudioManager(game.getAudioManager());
            inventoryUI.refreshUI();

            this.puzzleManager = game.getPuzzleManager();
            defaultSkin = new com.badlogic.gdx.scenes.scene2d.ui.Skin(Gdx.files.internal("ui/uiskin.json"));

            puzzleDrawerUI = new com.gnivol.game.ui.PuzzleDrawerUI(defaultSkin, game.getStage(), puzzleManager, game.getRsManager());

            setupPuzzleListeners();


            inventoryUI.refreshUI();

            // --- InventoryOverlay system (fridge, wardrobe — doc lap) ---
            inventoryOverlaySystem = new com.gnivol.game.ui.InventoryOverlay();
            inventoryOverlaySystem.loadOverlays("data/overlays.json");
            inventoryOverlaySystem.setListener(new com.gnivol.game.ui.InventoryOverlay.OverlayListener() {
                @Override
                public void onItemCollected(String overlayId, String itemId) {
                    game.getInventoryManager().addItem(itemId);
                    inventoryUI.refreshUI();
                    showItemNotification(itemId);
                    if (game.getAutoSaveManager() != null) {
                        game.getAutoSaveManager().onSaveTrigger("pickup_" + itemId);
                    }
                }

                @Override
                public void onOverlayClosed(String overlayId) {
                }
            });
            dialogueEngine = new DialogueEngine(game.getRsManager());

            dialogueEngine.setCutsceneListener(new DialogueEngine.DialogueCutsceneListener() {
                @Override
                public void onCutsceneTriggered(String cutsceneId) {
                    // Ép ẩn khung thoại đi để xem Cutscene cho rõ
                    if (dialogueUI != null) {
                        dialogueUI.displayNode(null);
                    }
                    cutsceneManager.play(cutsceneId);
                }
            });

            dialogueUI = new DialogueUI(game, game.getStage(), fm.fontVietnamese, dialogueEngine, game.getRsManager());
            loadDialogueDatabase();
//            dialogueUI.setOnFinished(() -> {
//                if (cutsceneManager.isPlaying()) {
//                    cutsceneManager.onDialogueFinished();
//                }
//                if (game.getAutoSaveManager() != null) {
//                    game.getAutoSaveManager().onSaveTrigger("dialogue_ended");
//                }
//            });

            Label.LabelStyle labelStyle = new Label.LabelStyle(fm.fontVietnamese, Color.WHITE);
            inspectLabel = new Label("", labelStyle);
            inspectLabel.setWrap(true);
            inspectLabel.setAlignment(Align.center);

            inspectTable = new Table();
            inspectTable.setFillParent(true);
            inspectTable.bottom().padBottom(30f);
            inspectTable.add(inspectLabel).width(900f).pad(15f);
            inspectTable.setVisible(false);
            game.getStage().addActor(inspectTable);

            Label.LabelStyle rsStyle = new Label.LabelStyle(fm.fontButton, Color.WHITE);
            rsUI = new RSUI(game.getStage(), rsStyle);
            game.getRsManager().addListener(new RSListener() {
                @Override
                public void onRSChanged(float oldValue, float newValue) {
                    rsUI.updateRS(newValue);
                    game.getGameState().setCurrentRS(newValue);
                    // Nếu RS tụt xuống 0 (hoặc thấp hơn)
                    if (newValue <= 0 && oldValue > 0) {
                        // chờ Hội thoại cũ dọn dẹp xong thì mới kích hoạt Ending
                        Gdx.app.postRunnable(() -> {
                            // buộc hội thoại hiện tại phải kết thúc ngay lập tức
                            if (dialogueUI.isVisible()) {
                                dialogueUI.displayNode(null);
                            }
                            cutsceneManager.play("cutscene_rs_0");
                        });
                    }
                    // Nếu RS chạm mốc 100 (hoặc vượt quá)
                    else if (newValue >= 100 && oldValue < 100) {
                        Gdx.app.postRunnable(() -> {
                            // buộc hội thoại hiện tại phải kết thúc ngay lập tức
                            if (dialogueUI.isVisible()) {
                                dialogueUI.displayNode(null);
                            }
                            cutsceneManager.play("cutscene_rs_100");
                        });
                    }
                }

                @Override
                public void onThresholdCrossed(boolean isAbove) {
                }
            });
            rsUI.updateRS(game.getRsManager().getRS());

            setupInteractionSystem();

            if (firstShow) {
                handleFirstShow();
                firstShow = false;
            }

            // Phát nhạc game (crossfade từ menu)
            if (game.getAudioManager() != null) {
                game.getAudioManager().crossfadeBGM("bedroom_bgm", 1.5f);
            }

            cutsceneManager = new com.gnivol.game.system.scene.CutsceneManager();
            cutsceneManager.setRSManager(game.getRsManager());
            cutsceneManager.setAudioManager(game.getAudioManager());
            cutsceneManager.loadCutscenes("data/cutscenes.json");

            cutsceneManager.setListener(new com.gnivol.game.system.scene.CutsceneManager.CutsceneListener() {
                @Override
                public void onFlash(String color, float duration) {
                    isFlashing = true;
                    flashAlpha = 1f;
                    if ("white".equalsIgnoreCase(color)) flashColor = Color.WHITE;
                    else if ("red".equalsIgnoreCase(color)) flashColor = Color.RED;
                }

                @Override
                public void onDialogue(String dialogueId) {
                    // Clear cutscene sprite overlay khi vào dialogue → dialog hiện trên background phòng,
                    // không bị kẹt sprite jumpscare cuối (vd /1.png của door_neighbor).
                    if (cutsceneSprite != null) {
                        cutsceneSprite.dispose();
                        cutsceneSprite = null;
                    }
                    triggerDialogue(dialogueId);
                }

                @Override
                public void onShowSprite(String sprite, float duration, float x, float y, float w, float h) {
                    if (cutsceneSprite != null) cutsceneSprite.dispose();
                    try {
                        cutsceneSprite = new Texture(Gdx.files.internal(sprite));
                        cutsceneSpriteTimer = 0f;
                        cutsceneSpriteDuration = duration;
                        cutsceneSpriteRect[0] = x;
                        cutsceneSpriteRect[1] = y;
                        cutsceneSpriteRect[2] = w;
                        cutsceneSpriteRect[3] = h;
                    } catch (Exception e) {
                        Gdx.app.error("Cutscene", "Cannot load sprite: " + sprite, e);
                        cutsceneSprite = null;
                    }
                }

                @Override
                public void onSwapSprite(String target, String newSprite, float x, float y, float w, float h) {
                    if (cutsceneSprite != null) cutsceneSprite.dispose();
                    try {
                        cutsceneSprite = new Texture(Gdx.files.internal(newSprite));
                        cutsceneSpriteTimer = 0f;
                        if (x >= 0) cutsceneSpriteRect[0] = x;
                        if (y >= 0) cutsceneSpriteRect[1] = y;
                        if (w >= 0) cutsceneSpriteRect[2] = w;
                        if (h >= 0) cutsceneSpriteRect[3] = h;
                    } catch (Exception e) {
                        Gdx.app.error("Cutscene", "Cannot swap sprite: " + newSprite, e);
                        cutsceneSprite = null;
                    }
                }

                @Override
                public void onPlayVideo(String src, float x, float y, float w, float h) {
                    try {
                        if (videoPlayer != null) {
                            videoPlayer.dispose();
                        }
                        videoPlayer = VideoPlayerCreator.createVideoPlayer();
                        videoPlayer.setOnCompletionListener(player -> {
                            videoPlaying = false;
                            if (cutsceneManager.isPlaying()) {
                                cutsceneManager.onVideoFinished();
                            }
                        });
                        videoRect[0] = x;
                        videoRect[1] = y;
                        videoRect[2] = w;
                        videoRect[3] = h;
                        videoPlayer.play(Gdx.files.internal(src));
                        videoPlaying = true;
                    } catch (Exception e) {
                        Gdx.app.error("Cutscene", "Cannot play video: " + src, e);
                        videoPlaying = false;
                        cutsceneManager.onVideoFinished();
                    }
                }

                @Override
                public void onShake(float intensity, float duration) {
                }

                @Override
                public void onFadeOut(float duration) {
                    screenFader.startFade(() -> {
                    });
                }

                @Override
                public void onFadeIn(float duration) {
                    screenFader.startFadeIn();
                }

                @Override
                public void onChangeScene(String sceneId) {
                    if ("return_to_menu".equals(sceneId)) {
                        Gdx.app.postRunnable(() -> {
                            game.setScreen(new com.gnivol.game.screen.MainMenuScreen(game));
                            GameScreen.this.dispose();
                        });
                        return;
                    }
                    sceneManager.changeScene(sceneId);
                    game.getGameState().setCurrentRoom(sceneId);
                }

                @Override
                public void onCutsceneFinished(String cutsceneId) {
                    if (cutsceneSprite != null) {
                        cutsceneSprite.dispose();
                        cutsceneSprite = null;
                    }
                    if (videoPlayer != null) {
                        videoPlayer.dispose();
                        videoPlayer = null;
                        videoPlaying = false;
                    }
                    Gdx.app.log("Cutscene", "Finished: " + cutsceneId);
                }

                @Override
                public void onOpenMinigame(String minigameId) {
                    if (screenFader.isFading()) return;
                    if ("puzzle_drawer".equals(minigameId)) {
                        puzzleDrawerUI.show();
                        return;
                    }

                    LoadingScreen.LoadingTarget targetType = null;

                    if ("puzzle_laser".equals(minigameId)) {
                        targetType = LoadingScreen.LoadingTarget.LASER_MINIGAME;
                    } else if ("puzzle_sliding_marble".equals(minigameId)) {
                        targetType = LoadingScreen.LoadingTarget.SLIDING_MINIGAME;
                    }

                    if (targetType != null) {
                        final LoadingScreen.LoadingTarget finalTarget = targetType;
                        if (inventoryUI != null) inventoryUI.setVisible(false);

                        screenFader.startFade(() -> {
                            Gdx.app.postRunnable(() -> {
                                game.setScreen(new com.gnivol.game.screen.LoadingScreen(game, finalTarget, GameScreen.this));
                            });
                        });
                    } else {
                        puzzleManager.openPuzzle(minigameId);
                    }
                }
            });
            isInitialized = true;
        }
        setupInputProcessors();
        if (inventoryUI != null) {
            inventoryUI.refreshUI();
        }
    }

    private void setupPuzzleListeners() {
        // Chỉ còn lắng nghe kết quả từ ngăn kéo (drawer)
        puzzleDrawerUI.setListener(puzzleId -> {
            if ("puzzle_drawer".equals(puzzleId)) {
                game.getInventoryManager().addItem("keo_502_final");
                inventoryUI.refreshUI();
                showNotification("Cạch! Ngăn kéo đã mở. Nhận được lọ keo 502!", Color.GREEN);
                if (sceneManager.getCurrentScene() instanceof RoomScene) {
                    ((RoomScene) sceneManager.getCurrentScene()).setObjectState("drawer", "open");
                }
                game.getStage().setKeyboardFocus(null);
                if (game.getAutoSaveManager() != null) game.getAutoSaveManager().onSaveTrigger("puzzle_" + puzzleId);
            }
        });

        // Xử lý sự kiện mở minigame
        puzzleManager.setCallback(puzzleId -> {
            if ("puzzle_drawer".equals(puzzleId)) {
                puzzleDrawerUI.show();
            } else if ("puzzle_laser".equals(puzzleId)) {
                if (screenFader.isFading()) return;
                if (inventoryUI != null) inventoryUI.setVisible(false);
                screenFader.startFade(() -> {
                    Gdx.app.postRunnable(() -> {
                        game.setScreen(new com.gnivol.game.screen.LoadingScreen(game, LoadingScreen.LoadingTarget.LASER_MINIGAME, GameScreen.this));
                    });
                });
            }
            else if ("puzzle_sliding_marble".equals(puzzleId)) {
                if (screenFader.isFading()) return;
                if (inventoryUI != null) inventoryUI.setVisible(false);
                screenFader.startFade(() -> {
                    Gdx.app.postRunnable(() -> {
                        game.setScreen(new com.gnivol.game.screen.LoadingScreen(game, LoadingScreen.LoadingTarget.SLIDING_MINIGAME, GameScreen.this));
                    });
                });
            }

        });
    }

    private void loadDialogueDatabase() {
        dialogueDatabase = new HashMap<>();
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        try {
            ArrayList<DialogueTree> treeList = json.fromJson(ArrayList.class, DialogueTree.class, Gdx.files.internal("data/dialogues.json"));
            for (DialogueTree tree : treeList) dialogueDatabase.put(tree.dialogueId, tree);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "LỖI ĐỌC DIALOGUES.JSON", e);
        }
    }

    private void setupInteractionSystem() {
        RoomInteractionHandler handler = new RoomInteractionHandler(this, dialogueDatabase);
        interactionSystem.setCallback(handler);
    }

    private void setupInputProcessors() {
        inputHandler.clear();
        inputHandler.addStage(game.getStage());
        inputHandler.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Overlay active → xử lý trước mọi thứ
                if (overlayManager.isActive() && overlayManager.getTexture() != null) {
                    com.badlogic.gdx.math.Vector3 touch = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                    camera.unproject(touch, viewport.getScreenX(), viewport.getScreenY(),
                        viewport.getScreenWidth(), viewport.getScreenHeight());

                    // Tìm overlay data từ overlays.json
                    com.gnivol.game.ui.InventoryOverlay.OverlayData overlayData =
                        (inventoryOverlaySystem != null && overlayManager.getSourceId() != null)
                            ? inventoryOverlaySystem.findByObjectId(overlayManager.getSourceId()) : null;

                    if (overlayData != null) {
                        float maxW = 700f, maxH = 550f;
                        float imgW = overlayManager.getTexture().getWidth(), imgH = overlayManager.getTexture().getHeight();
                        float scale = Math.min(maxW / imgW, maxH / imgH);
                        float drawX = (1280 - imgW * scale) / 2f;
                        float drawY = (720 - imgH * scale) / 2f;

                        float relX = (touch.x - drawX) / scale;
                        float relY = (touch.y - drawY) / scale;

                        for (com.gnivol.game.ui.InventoryOverlay.OverlayItem item : overlayData.items) {
                            boolean hitItem = relX >= item.x && relX <= item.x + item.w
                                && relY >= item.y && relY <= item.y + item.h;
                            if (!hitItem) continue;

                            if (debugManager.isDebugMode()) {
                                debugManager.handleOverlayItemClick(relX, relY, overlayData);
                                return true;
                            }

                            if (!game.getInventoryManager().hasItem(item.itemId)) {
                                game.getInventoryManager().addItem(item.itemId);
                                inventoryUI.refreshUI();
                                showItemNotification(item.itemId);
                                if (game.getAutoSaveManager() != null) {
                                    game.getAutoSaveManager().onSaveTrigger("pickup_" + item.itemId);
                                }
                                // Đổi overlay sang ảnh taken nếu có
                                RoomData roomData = sceneManager.getCurrentScene().getRoomData();
                                if (roomData != null) {
                                    for (RoomData.RoomObject roomObj : roomData.getObjects()) {
                                        if (roomObj.id.equals(overlayManager.getSourceId()) && roomObj.properties != null
                                            && roomObj.properties.altTextures != null) {
                                            String takenPath = roomObj.properties.altTextures.get("taken");
                                            if (takenPath != null) {
                                                overlayManager.swapTexture(takenPath);
                                            }
                                            break;
                                        }
                                    }
                                }
                                return true;
                            }
                        }
                    }

                    if (debugManager.isDebugMode()) return true;

                    overlayManager.close();
                    hideInspectText();
                    return true;
                }

                // Debug: drag cutscene sprite
                if (debugManager.isDebugMode() && button == Input.Buttons.LEFT && cutsceneSprite != null
                    && cutsceneSpriteRect[0] >= 0) {
                    com.badlogic.gdx.math.Vector3 csTouch = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                    camera.unproject(csTouch, viewport.getScreenX(), viewport.getScreenY(),
                        viewport.getScreenWidth(), viewport.getScreenHeight());
                    if (debugManager.handleCutsceneSpriteClick(csTouch.x, csTouch.y, cutsceneSpriteRect)) {
                        return true;
                    }
                }

                // Portrait debug drag — xử lý trước debug mode thường
                if (dialogueUI != null && dialogueUI.isDebugPortrait() && dialogueUI.isVisible()
                    && button == Input.Buttons.LEFT) {
                    if (dialogueUI.handlePortraitDebugClick(screenX, screenY)) return true;
                }

                if (debugManager.isDebugMode() && button == Input.Buttons.LEFT) {
                    debugManager.handleDebugClick(screenX, screenY, camera, viewport, sceneManager.getCurrentScene());
                    return true;
                }

                if (dialogueUI != null && dialogueUI.isVisible()) {
                    if (!dialogueUI.canClick()) {
                        return true;
                    }
                    if (dialogueEngine.getCurrentNode() != null) {
                        if (dialogueUI.isTyping()) {
                            dialogueUI.finishTyping();
                        } else {
                            dialogueEngine.advance();
                            // displayNode tự handle cả 2 case (node/null) + tự gọi
                            // cutsceneManager.onDialogueFinished() bên trong nhánh null.
                            // KHÔNG gọi onDialogueFinished ngoài này để tránh double-advance
                            // (sẽ skip step dialogue tiếp theo trong cutscene — bug đã fix ở PR #73).
                            dialogueUI.displayNode(dialogueEngine.getCurrentNode());
                            if (dialogueEngine.isFinished() && game.getAutoSaveManager() != null) {
                                game.getAutoSaveManager().onSaveTrigger("dialogue_ended");
                            }
                        }
                    }
                    return true;
                }

                if (inventoryOverlaySystem != null && inventoryOverlaySystem.isOpen()) {
                    com.badlogic.gdx.math.Vector3 overlayTouch = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                    camera.unproject(overlayTouch, viewport.getScreenX(), viewport.getScreenY(),
                        viewport.getScreenWidth(), viewport.getScreenHeight());
                    if (!inventoryOverlaySystem.handleClick(overlayTouch.x, overlayTouch.y)) {
                        inventoryOverlaySystem.close();
                    }
                    return true;
                }
                if (inventoryUI.isOpen()) return false;
                return interactionSystem.handleClick(screenX, screenY, viewport);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (dialogueUI != null && dialogueUI.isDraggingPortrait()) {
                    dialogueUI.handlePortraitDebugDrag(screenX, screenY);
                    return true;
                }
                if (debugManager.isDebugMode() && debugManager.isDraggingCutsceneSprite()) {
                    com.badlogic.gdx.math.Vector3 t = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                    camera.unproject(t, viewport.getScreenX(), viewport.getScreenY(),
                        viewport.getScreenWidth(), viewport.getScreenHeight());
                    debugManager.handleCutsceneSpriteDrag(t.x, t.y);
                    return true;
                }
                if (debugManager.isDebugMode() && debugManager.isDraggingOverlayItem() && overlayManager.isActive() && overlayManager.getTexture() != null) {
                    com.badlogic.gdx.math.Vector3 t = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                    camera.unproject(t, viewport.getScreenX(), viewport.getScreenY(),
                        viewport.getScreenWidth(), viewport.getScreenHeight());
                    float maxW = 700f, maxH = 550f;
                    float imgW = overlayManager.getTexture().getWidth(), imgH = overlayManager.getTexture().getHeight();
                    float scale = Math.min(maxW / imgW, maxH / imgH);
                    float drawX = (1280 - imgW * scale) / 2f;
                    float drawY = (720 - imgH * scale) / 2f;
                    debugManager.handleOverlayItemDrag((t.x - drawX) / scale, (t.y - drawY) / scale);
                    return true;
                }
                if (debugManager.isDebugMode() && debugManager.hasDragTarget()) {
                    debugManager.handleDebugDrag(screenX, screenY, camera, viewport, sceneManager.getCurrentScene());
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (dialogueUI != null && dialogueUI.isDraggingPortrait()) {
                    dialogueUI.finishPortraitDebugDrag();
                }
                if (debugManager.isDraggingCutsceneSprite()) {
                    debugManager.finishCutsceneSpriteDrag();
                }
                if (debugManager.isDraggingOverlayItem()) {
                    debugManager.finishOverlayItemDrag();
                }
                debugManager.clearDrag();
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F1) { debugManager.toggleDebugMode(); return true; }
                if (keycode == Input.Keys.F2 && debugManager.isDebugMode()) { debugManager.exportDebugCoordinates(sceneManager.getCurrentScene()); return true; }
                if (keycode == Input.Keys.F3) { dialogueUI.toggleDebugPortrait(); return true; }
                if (keycode == Input.Keys.F4 && dialogueUI.isDebugPortrait()) { dialogueUI.exportPortraitCoordinates(); return true; }
                if (keycode == Input.Keys.ESCAPE) {
                    boolean isCutsceneActive = (cutsceneManager != null && cutsceneManager.isPlaying());
                    if (isCutsceneActive || isFlashing || cutsceneSprite != null || videoPlaying) {
                        return false;
                    }

                    if (overlayManager.isActive()) {
                        overlayManager.close();
                        hideInspectText();
                    }
                    else game.setScreen(new PauseScreen(game, GameScreen.this));
                    return true;
                }
                return false;
            }
        });
        inputHandler.activate();
    }

    private void handleFirstShow() {
        String room = game.isLoadedGame ? game.getGameState().getCurrentRoom() : Constants.SCENE_BEDROOM;
        sceneManager.changeScene(room != null ? room : Constants.SCENE_BEDROOM);
        screenFader.startFadeIn();
        if (!game.isLoadedGame) {
            if (game.getAutoSaveManager() != null) {
                game.getAutoSaveManager().onSaveTrigger("new_game_start");
            }
            playIntroSequence();
        } else {
            triggerFirstTimeSceneEvents(room);
            playIntroSequence();
        }
        game.isLoadedGame = false;
    }

    private void playIntroSequence() {
        if (game.getGameState().isDialogueFinished("intro_phone_call")) return;

        if (!game.getGameState().isDialogueFinished("intro_thought")) {
            DialogueTree intro = dialogueDatabase.get("intro_thought");
            if (intro != null) {
                // Phone ringing loop trong suốt intro_thought, dừng khi sang intro_phone_call
                game.getAudioManager().playSFXLoop("phone_ringing");
                dialogueUI.setOnFinished(() -> {
                    game.getAudioManager().stopSFXLoop("phone_ringing");
                    game.getGameState().markDialogueFinished("intro_thought");
                    playIntroPhoneCall(); // Xong suy nghĩ thì gọi điện thoại
                });
                dialogueEngine.loadDialogue(intro);
                dialogueUI.displayNode(dialogueEngine.getCurrentNode());
            }
        } else {
            playIntroPhoneCall();
        }
    }

    private void playIntroPhoneCall() {
        DialogueTree call = dialogueDatabase.get("intro_phone_call");
        if (call != null) {
            dialogueUI.setOnFinished(() -> {
                game.getGameState().markDialogueFinished("intro_phone_call");
                if (game.getAutoSaveManager() != null) {
                    game.getAutoSaveManager().onSaveTrigger("intro_ended");
                }
            });
            dialogueEngine.loadDialogue(call);
            dialogueUI.displayNode(dialogueEngine.getCurrentNode());
        }
    }


    public void openOverlay(String texturePath) {
        overlayManager.open(texturePath, null);
    }

    public void openOverlay(String texturePath, String sourceId) {
        overlayManager.open(texturePath, sourceId);
    }


    public void showInspectText(String text) {
        inspectLabel.setText(text);
        inspectTable.setVisible(true);
        inspectTable.getColor().a = 0f;
        inspectTable.addAction(Actions.fadeIn(0.3f));
    }

    public void hideInspectText() {
        if (inspectTable.isVisible()) {
            inspectTable.addAction(Actions.sequence(
                Actions.fadeOut(0.3f),
                Actions.visible(false)
            ));
        }
    }
    @Override
    public void render(float delta) {
        checkEndGame(delta);

        ScreenUtils.clear(0, 0, 0, 1);
        screenFader.update(delta);
        if (!isGameOver) {
            sceneManager.update(delta);

            // Lưu lại vị trí camera chuẩn xác nhất lúc game mới bật
            if (originalCameraPos.x == 0 && originalCameraPos.y == 0) {
                originalCameraPos.set(camera.position.x, camera.position.y);
            }
            float currentRS = game.getRsManager().getRS();
            // 1. Kiểm tra nếu RS nằm ngoài vùng an toàn (35 -> 65)
            if (currentRS < 35f || currentRS > 65f) {
                rsCycleTimer += delta;

                // Đủ 10 giây -> Kích hoạt cơn điên
                if (rsCycleTimer >= 10f) {
                    rsCycleTimer = 0f;
                    isRsGlitching = true;
                    rsGlitchDurationTimer = 0f;

                    // Phát tiếng dè dè như game đag bị lỗi
                    // if (game.getAudioManager() != null) game.getAudioManager().playSFX("sfx_glitch_noise");
                }
            } else {
                rsCycleTimer = 0f;
                isRsGlitching = false;
            }

            // 2. Xử lý Rung Camera kéo dài 0.75 giây
            if (isRsGlitching) {
                rsGlitchDurationTimer += delta;
                shaderTime += delta;

                // Tính toán độ bạo lực: Càng lệch khỏi mức 50, rung càng mạnh
                float severity = (currentRS < 35f) ? (35f - currentRS) : (currentRS - 65f);
                float shakeAmount = 2f + (severity * 0.15f);

                camera.position.x = originalCameraPos.x + (float)(Math.random() - 0.5) * shakeAmount;
                camera.position.y = originalCameraPos.y + (float)(Math.random() - 0.5) * shakeAmount;
                camera.update();

                // Dừng lại sau 0.75s
                if (rsGlitchDurationTimer > 0.75f) {
                    isRsGlitching = false;
                }
            } else {
                // Gim camera về chỗ cũ, tránh bị kẹt lệch màn hình
                if (camera.position.x != originalCameraPos.x || camera.position.y != originalCameraPos.y) {
                    camera.position.set(originalCameraPos.x, originalCameraPos.y, 0);
                    camera.update();
                }
            }
        }
        if (dialogueUI != null) dialogueUI.update(delta);
        game.getStage().act(delta);
        if (inventoryUI != null) inventoryUI.setVisible(!dialogueUI.isVisible());
        if (cutsceneManager != null) cutsceneManager.update(delta);
        if (game.getAudioManager() != null) game.getAudioManager().update(delta);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        // 1. CHỈ GẮN SHADER VÀO BATCH (TRƯỚC KHI BEGIN)
        if (isRsGlitching && glitchShader != null) {
            batch.setShader(glitchShader);
        }

        // --- BẬT CÔNG TẮC VẼ ---
        batch.begin();

        // 2. TRUYỀN BIẾN (UNIFORM) SAU KHI ĐÃ BEGIN
        if (isRsGlitching && glitchShader != null) {
            glitchShader.setUniformf("u_time", shaderTime);
            // Tính toán cường độ nhiễu (0.0 đến 1.0)
            float currentRS = game.getRsManager().getRS();
            float intensity = (currentRS < 35f) ? (35f - currentRS) / 35f : (currentRS - 65f) / 35f;
            glitchShader.setUniformf("u_intensity", intensity);
        }
        sceneManager.render(batch);
        if (vignetteTexture != null) {
            batch.draw(vignetteTexture, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        }
        batch.end();

        // --- TẮT SHADER (Bảo vệ UI & Khung thoại không bị lỗi màu) ---
        if (batch.getShader() == glitchShader) {
            batch.setShader(null);
        }
        overlayManager.render(delta, batch, camera);
        // Debug overlay items từ overlays.json
        if (debugManager.isDebugMode() && overlayManager.isActive() && overlayManager.getTexture() != null) {
            com.gnivol.game.ui.InventoryOverlay.OverlayData debugOverlay =
                (inventoryOverlaySystem != null && overlayManager.getSourceId() != null)
                    ? inventoryOverlaySystem.findByObjectId(overlayManager.getSourceId()) : null;
            debugManager.renderOverlayItems(batch, camera, viewport, overlayManager.getTexture(), debugOverlay);
        }
        if (inventoryOverlaySystem != null && inventoryOverlaySystem.isOpen()) {
            batch.begin();
            inventoryOverlaySystem.render(batch);
            batch.end();
        }
        if (debugManager.isDebugMode()) debugManager.render(batch, camera, viewport, sceneManager.getCurrentScene());

        // Vẽ portrait trước stage.draw() để dialogue box (Stage) che được portrait
        if (dialogueUI != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            dialogueUI.renderPortraits(batch);
            batch.end();
        }

        game.getStage().draw();

        // Vẽ debug portrait overlay sau stage (luôn hiện trên cùng)
        if (dialogueUI != null && dialogueUI.isDebugPortrait()) {
            dialogueUI.renderPortraitDebug(debugManager.getShapeRenderer(), game.getFontManager().fontDebug, batch);
        }

        if (isFlashing) {
            flashAlpha -= delta * 4f;
            if (flashAlpha <= 0) isFlashing = false;

            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            dimRenderer.setProjectionMatrix(camera.combined);
            dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dimRenderer.setColor(flashColor.r, flashColor.g, flashColor.b, flashAlpha);
            dimRenderer.rect(0, 0, 1280, 720);
            dimRenderer.end();
        }

        // Cutscene sprite rendering
        if (cutsceneSprite != null) {
            cutsceneSpriteTimer += delta;
            if (cutsceneSpriteDuration > 0 && cutsceneSpriteTimer >= cutsceneSpriteDuration) {
                cutsceneSprite.dispose();
                cutsceneSprite = null;
            } else {
                Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                dimRenderer.setProjectionMatrix(camera.combined);
                dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
                dimRenderer.setColor(Color.BLACK);
                dimRenderer.rect(0, 0, 1280, 720);
                dimRenderer.end();

                float drawX, drawY, drawW, drawH;
                if (cutsceneSpriteRect[0] >= 0 && cutsceneSpriteRect[1] >= 0 && cutsceneSpriteRect[2] > 0 && cutsceneSpriteRect[3] > 0) {
                    drawX = cutsceneSpriteRect[0];
                    drawY = cutsceneSpriteRect[1];
                    drawW = cutsceneSpriteRect[2];
                    drawH = cutsceneSpriteRect[3];
                } else {
                    float imgW = cutsceneSprite.getWidth();
                    float imgH = cutsceneSprite.getHeight();
                    float scale = Math.min(1280f / imgW, 720f / imgH);
                    drawW = imgW * scale;
                    drawH = imgH * scale;
                    drawX = (1280 - drawW) / 2f;
                    drawY = (720 - drawH) / 2f;
                }

                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                batch.draw(cutsceneSprite, drawX, drawY, drawW, drawH);
                batch.end();

                // Debug hitbox for cutscene sprite
                if (cutsceneSpriteRect[0] >= 0) {
                    debugManager.renderCutsceneSprite(batch, camera, viewport, cutsceneSpriteRect);
                }
            }
        }

        // Video rendering
        if (videoPlaying && videoPlayer != null) {
            videoPlayer.update();
            Texture videoFrame = videoPlayer.getTexture();
            if (videoFrame != null) {
                float drawX, drawY, drawW, drawH;
                if (videoRect[0] >= 0 && videoRect[1] >= 0 && videoRect[2] > 0 && videoRect[3] > 0) {
                    drawX = videoRect[0];
                    drawY = videoRect[1];
                    drawW = videoRect[2];
                    drawH = videoRect[3];
                } else {
                    drawW = 1280f;
                    drawH = 720f;
                    drawX = 0f;
                    drawY = 0f;
                }
                batch.setProjectionMatrix(camera.combined);
                if (chromaShader != null) {
                    batch.setShader(chromaShader);
                }
                batch.begin();
                if (chromaShader != null) {
                    chromaShader.setUniformf("u_keyColor", CHROMA_KEY_RGB[0], CHROMA_KEY_RGB[1], CHROMA_KEY_RGB[2]);
                    chromaShader.setUniformf("u_threshold", CHROMA_THRESHOLD);
                    chromaShader.setUniformf("u_smoothing", CHROMA_SMOOTHING);
                }
                batch.draw(videoFrame, drawX, drawY, drawW, drawH);
                batch.end();
                if (chromaShader != null) {
                    batch.setShader(null); // restore default shader
                }
            }
        }

        screenFader.render();

    }

    public void showNotification(String text, Color color) {
        Label.LabelStyle style = new Label.LabelStyle(game.getFontManager().fontVietnamese, color); // Dùng fontVietnamese
        Label label = new Label(text, style);
        label.setPosition((1280 - label.getPrefWidth()) / 2f, 75f);
        label.getColor().a = 0f;
        game.getStage().addActor(label);
        label.addAction(Actions.sequence(Actions.parallel(Actions.fadeIn(1f), Actions.moveBy(0, 50f, 1f)), Actions.delay(3f), Actions.parallel(Actions.fadeOut(1.5f), Actions.moveBy(0, -30f, 1.5f)), Actions.removeActor()));
    }

    public void showItemNotification(String itemId) {
        String itemName = itemId;

        try {
            com.gnivol.game.model.ItemData data = com.gnivol.game.data.ItemDatabase.getInstance().getItemData(itemId);
            if (data != null && data.itemName != null) {
                itemName = data.itemName;
            }
        } catch (Exception e) {
            Gdx.app.error("Notification", "Not found: " + itemId);
        }

        showNotification(itemName, Color.MAROON);
    }

    // Scenes chuyển cảnh không phải do mở cửa (chui vào, leo lên, chui ra...) — skip sfx open_door cả 2 chiều
    private static final java.util.Set<String> NON_DOOR_SCENES = new java.util.HashSet<>(java.util.Arrays.asList(
        "room_under_bed",
        "room_chu_nha",
        "room_tang_1",
        "room_toilet_closeup",
        "room_chua_chay_closeup"
    ));

    private boolean isDoorTransition(String targetSceneId) {
        String currentSceneId = game.getGameState() != null ? game.getGameState().getCurrentRoom() : null;
        // HashSet.contains(null) trả false → không cần null-check riêng
        return !NON_DOOR_SCENES.contains(targetSceneId) && !NON_DOOR_SCENES.contains(currentSceneId);
    }

    /** Chuyển cảnh dùng cầu thang: hallway ↔ chu_nha (lên) / tang_1 (xuống). */
    private boolean isStairsTransition(String targetSceneId) {
        String currentSceneId = game.getGameState() != null ? game.getGameState().getCurrentRoom() : null;
        if (currentSceneId == null) return false;
        boolean toStairs = "room_hallway".equals(currentSceneId)
                && ("room_chu_nha".equals(targetSceneId) || "room_tang_1".equals(targetSceneId));
        boolean fromStairs = ("room_chu_nha".equals(currentSceneId) || "room_tang_1".equals(currentSceneId))
                && "room_hallway".equals(targetSceneId);
        return toStairs || fromStairs;
    }

    public void changeSceneWithFade(String targetSceneId) {
        if (screenFader.isFading()) return;
        if (isStairsTransition(targetSceneId)) {
            game.getAudioManager().playSFX("stairs");
        } else if (isDoorTransition(targetSceneId)) {
            game.getAudioManager().playSFX("open_door");
        }
        screenFader.startFade(() -> {
            sceneManager.changeScene(targetSceneId);
            game.getGameState().setCurrentRoom(targetSceneId);
            if (game.getAutoSaveManager() != null) game.getAutoSaveManager().onSaveTrigger("enter_room_" + targetSceneId);
            triggerFirstTimeSceneEvents(targetSceneId);
        });
    }

    /** Cutscene/event chỉ play LẦN ĐẦU vào scene cụ thể. Gate bằng FlagManager. */
    private void triggerFirstTimeSceneEvents(String targetSceneId) {
        if ("room_hallway".equals(targetSceneId) && !game.getFlagManager().get("first_time_hallway")) {
            game.getFlagManager().set("first_time_hallway", true);
            cutsceneManager.play("door_neighbor");
        }
        if (targetSceneId != null && targetSceneId.contains("bathroom")) {
            // Kiểm tra xem đã gọi chủ trọ chưa và chưa bị chửi
            if (game.getFlagManager().get("chu_tro_fixed_toilet") && !game.getFlagManager().get("chu_tro_bathroom_talked")) {
                game.getFlagManager().set("chu_tro_bathroom_talked", true);
                triggerDialogue("chu_tro_in_bathroom");
            }
        }
    }

    public void triggerDialogue(String dialogueId) {
        hideInspectText();
        com.gnivol.game.model.dialogue.DialogueTree tree = dialogueDatabase.get(dialogueId);
        if (tree != null) {
            dialogueUI.setOnFinished(() -> {
                if (cutsceneManager != null && cutsceneManager.isPlaying()) {
                    cutsceneManager.onDialogueFinished();
                }

                game.getGameState().markDialogueFinished(dialogueId);

                if (game.getAutoSaveManager() != null) {
                    game.getAutoSaveManager().onSaveTrigger("dialogue_ended");
                }
            });
            dialogueEngine.loadDialogue(tree);
            dialogueUI.displayNode(dialogueEngine.getCurrentNode());
        }
    }

    public void showToiletConfirmDialog() {
        hideInspectText();
        com.badlogic.gdx.scenes.scene2d.ui.Dialog confirmDialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("", defaultSkin) {
            @Override
            protected void result(Object object) {
                if (object.equals(true)) {
                    game.getFlagManager().set("toilet_clogged", true);
                    game.getInventoryManager().removeItem("ca_vat_final");
                    inventoryUI.clearSelection();
                    inventoryUI.refreshUI();
                    showNotification("Ục ục... Bồn cầu đã tắc!", Color.GREEN);
                    if (game.getAutoSaveManager() != null) {
                        game.getAutoSaveManager().onSaveTrigger("event_toilet_clogged");
                    }
                }
            }
        };

        com.gnivol.game.system.FontManager fm = game.getFontManager();
        Label.LabelStyle lblStyle = new Label.LabelStyle(fm.fontVietnamese, Color.WHITE);
        confirmDialog.text(new Label("Ném vải vào bồn cầu?", lblStyle));

        TextButton.TextButtonStyle btnStyle = defaultSkin.get(TextButton.TextButtonStyle.class);
        TextButton.TextButtonStyle viBtnStyle = new TextButton.TextButtonStyle(btnStyle);
        viBtnStyle.font = fm.fontVietnamese;

        confirmDialog.button(new TextButton("Có", viBtnStyle), true);
        confirmDialog.button(new TextButton("Không", viBtnStyle), false);
        confirmDialog.show(game.getStage());
    }

    @Override
    public void hide() {
        inputHandler.clear();
    }

    private void checkEndGame(float delta) {
        if (isGameOver) return;

        if (game.getRsManager().isEndGame()) {
            isGameOver = true;

            if (game.getAutoSaveManager() != null) {
                game.getAutoSaveManager().setGameOver(true);
            }

            com.badlogic.gdx.files.FileHandle saveFile = Gdx.files.external(".gnivol/save_slot_1.json");
            if (saveFile.exists()) {
                saveFile.delete();
                Gdx.app.log("EndGame", "Data is removed.");
            }
            if (inventoryUI != null) inventoryUI.setVisible(false);
        }
    }

    private Texture createVignetteTexture(int width, int height) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxDist = (float) Math.sqrt(centerX * centerX + centerY * centerY);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float dist = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                float alpha = dist / maxDist;

                alpha = (float) Math.pow(alpha, 2.0);

                if (alpha > 0.85f) alpha = 0.85f;

                pixmap.setColor(new Color(0f, 0f, 0f, alpha));
                pixmap.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return tex;
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (dimRenderer != null) dimRenderer.dispose();
        if (debugManager != null) debugManager.dispose();
        if (overlayManager != null) overlayManager.dispose();
        if (defaultSkin != null) defaultSkin.dispose();
        if (inventoryOverlaySystem != null) inventoryOverlaySystem.dispose();
        if (cutsceneSprite != null) cutsceneSprite.dispose();
        if (videoPlayer != null) videoPlayer.dispose();
        if (chromaShader != null) chromaShader.dispose();
        if (glitchShader != null) glitchShader.dispose();
        if (inventoryUI != null) inventoryUI.dispose();
        if (vignetteTexture != null) vignetteTexture.dispose();
        if (sceneManager != null) sceneManager.dispose();
    }

    public void openInventoryOverlay(String overlayId) {
        if (inventoryOverlaySystem != null) {
            inventoryOverlaySystem.open(overlayId);
        }
    }

    public com.gnivol.game.ui.InventoryOverlay.OverlayData getInventoryOverlayData(String objectId) {
        if (inventoryOverlaySystem != null) {
            return inventoryOverlaySystem.findByObjectId(objectId);
        }
        return null;
    }


}
