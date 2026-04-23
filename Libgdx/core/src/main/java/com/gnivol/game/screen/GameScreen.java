package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    // Cutscene sprite
    private Texture cutsceneSprite;
    private float cutsceneSpriteTimer;
    private float cutsceneSpriteDuration;
    private final float[] cutsceneSpriteRect = {-1, -1, -1, -1}; // x, y, w, h

    public GnivolGame getGnivolGame() { return game; }
    public SceneManager getSceneManager() { return sceneManager; }
    public InventoryUI getInventoryUI() { return inventoryUI; }
    public com.gnivol.game.system.puzzle.PuzzleManager getPuzzleManager() { return puzzleManager; }
    public com.gnivol.game.system.scene.CutsceneManager getCutsceneManager() { return cutsceneManager; }
    public DialogueEngine getDialogueEngine() { return dialogueEngine; }
    public DialogueUI getDialogueUI() { return dialogueUI; }

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
        game.getStage().clear();

        overlayManager = new com.gnivol.game.system.scene.OverlayManager();
        sceneManager = game.getSceneManager();
        screenFader = game.getScreenFader();
        inputHandler = game.getInputHandler();
        interactionSystem = game.getPlayerInteractionSystem();
        batch = new SpriteBatch();
        dimRenderer = new ShapeRenderer();
        debugManager = new com.gnivol.game.system.debug.DebugRenderer(game);

        FontManager fm = game.getFontManager();

        inventoryUI = new InventoryUI(game.getStage(), game.getInventoryManager(), game.getCraftingManager(), game.getRsManager(), fm.fontVietnamese);
        inventoryUI.refreshUI();

        this.puzzleManager = game.getPuzzleManager();
        defaultSkin = new com.badlogic.gdx.scenes.scene2d.ui.Skin(Gdx.files.internal("ui/uiskin.json"));

        puzzleDrawerUI = new com.gnivol.game.ui.PuzzleDrawerUI(defaultSkin, game.getStage(), puzzleManager, game.getRsManager());

        setupPuzzleListeners();

        puzzleDrawerUI.setListener(new com.gnivol.game.ui.PuzzleDrawerUI.PuzzleResultListener() {
            @Override
            public void onPuzzleSolved(String puzzleId) {
                if ("puzzle_drawer".equals(puzzleId)) {
                    game.getInventoryManager().addItem("chia_khoa_final");
                    inventoryUI.refreshUI();
                    showNotification("Cạch! Ngăn kéo đã mở. Nhận được chìa khóa!", Color.GREEN);
                    if (sceneManager.getCurrentScene() instanceof com.gnivol.game.system.scene.RoomScene) {
                        ((com.gnivol.game.system.scene.RoomScene) sceneManager.getCurrentScene()).setObjectState("drawer", "open");
                    }
                    game.getStage().setKeyboardFocus(null);
                    if (game.getAutoSaveManager() != null) {
                        game.getAutoSaveManager().onSaveTrigger("puzzle_" + puzzleId);
                    }
                }
            }
        });

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
            public void onOverlayClosed(String overlayId) {}
        });
        dialogueEngine = new DialogueEngine(game.getRsManager());
        dialogueUI = new DialogueUI(game, game.getStage(), fm.fontVietnamese, dialogueEngine, game.getRsManager());
        loadDialogueDatabase();
        dialogueUI.setOnFinished(() -> {
            // NẾU ĐANG TRONG CUTSCENE THÌ BÁO KẾT THÚC THOẠI
            if (cutsceneManager.isPlaying()) {
                cutsceneManager.onDialogueFinished();
            }
            // ... logic intro cũ giữ nguyên ...
        });

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
            }
            @Override
            public void onThresholdCrossed(boolean isAbove) {}
        });
        rsUI.updateRS(game.getRsManager().getRS());

        setupInteractionSystem();
        setupInputProcessors();

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
                triggerDialogue(dialogueId);
            }

            @Override public void onShowSprite(String sprite, float duration, float x, float y, float w, float h) {
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
            @Override public void onSwapSprite(String target, String newSprite, float x, float y, float w, float h) {
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
            @Override public void onShake(float intensity, float duration) {}
            @Override public void onFadeOut(float duration) { screenFader.startFade(() -> {}); }
            @Override public void onFadeIn(float duration) { screenFader.startFadeIn(); }
            @Override public void onChangeScene(String sceneId) {
                sceneManager.changeScene(sceneId);
                game.getGameState().setCurrentRoom(sceneId);
            }
            @Override public void onCutsceneFinished(String cutsceneId) {
                if (cutsceneSprite != null) {
                    cutsceneSprite.dispose();
                    cutsceneSprite = null;
                }
                Gdx.app.log("Cutscene", "Finished: " + cutsceneId);
            }
        });

    }

    private void setupPuzzleListeners() {
        // Chỉ còn lắng nghe kết quả từ ngăn kéo (drawer)
        puzzleDrawerUI.setListener(puzzleId -> {
            if ("puzzle_drawer".equals(puzzleId)) {
                game.getInventoryManager().addItem("chia_khoa_final");
                inventoryUI.refreshUI();
                showNotification("Cạch! Ngăn kéo đã mở. Nhận được chìa khóa!", Color.GREEN);
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
                screenFader.startFade(() -> {
                    game.setScreen(new com.gnivol.game.screen.LaserScreen(game, GameScreen.this));
                });
            }
            else if ("puzzle_sliding_marble".equals(puzzleId)) {
                if (screenFader.isFading()) return;
                screenFader.startFade(() -> {
                    game.setScreen(new com.gnivol.game.screen.SlidingScreen(game, GameScreen.this));
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

                if (debugManager.isDebugMode() && button == Input.Buttons.LEFT) {
                    debugManager.handleDebugClick(screenX, screenY, camera, viewport, sceneManager.getCurrentScene());
                    return true;
                }

                if (dialogueUI != null && dialogueUI.isVisible()) return false;

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
                if (keycode == Input.Keys.ESCAPE) {
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
            DialogueTree intro = dialogueDatabase.get("intro_thought");
            if (intro != null) {
                dialogueEngine.loadDialogue(intro);
                dialogueUI.displayNode(dialogueEngine.getCurrentNode());
                game.getGameState().markDialogueFinished("intro_thought");
                dialogueUI.setOnFinished(() -> {
                    DialogueTree call = dialogueDatabase.get("intro_phone_call");
                    if (call != null) {
                        dialogueEngine.loadDialogue(call);
                        dialogueUI.displayNode(dialogueEngine.getCurrentNode());
                        game.getGameState().markDialogueFinished("intro_phone_call");
                    }
                });
            }
        }
        game.isLoadedGame = false;
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
        ScreenUtils.clear(0, 0, 0, 1);
        sceneManager.update(delta);
        screenFader.update(delta);
        if (dialogueUI != null) dialogueUI.update(delta);
        game.getStage().act(delta);
        if (inventoryUI != null) inventoryUI.setVisible(!dialogueUI.isVisible());

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        sceneManager.render(batch);
        batch.end();

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
        game.getStage().draw();


        if (cutsceneManager != null) cutsceneManager.update(delta);
        if (game.getAudioManager() != null) game.getAudioManager().update(delta);
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

    public void changeSceneWithFade(String targetSceneId) {
        if (screenFader.isFading()) return;
        screenFader.startFade(() -> {
            sceneManager.changeScene(targetSceneId);
            game.getGameState().setCurrentRoom(targetSceneId);
            if (game.getAutoSaveManager() != null) game.getAutoSaveManager().onSaveTrigger("enter_room_" + targetSceneId);
        });
    }

    public void triggerDialogue(String dialogueId) {
        hideInspectText();
        com.gnivol.game.model.dialogue.DialogueTree tree = dialogueDatabase.get(dialogueId);
        if (tree != null) {
            dialogueEngine.loadDialogue(tree);
            dialogueUI.displayNode(dialogueEngine.getCurrentNode());
            game.getGameState().markDialogueFinished(dialogueId);
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
        if (inspectTable != null) inspectTable.remove();
        if (overlayManager != null) overlayManager.close();
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
        if (inventoryUI != null) inventoryUI.dispose();
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
