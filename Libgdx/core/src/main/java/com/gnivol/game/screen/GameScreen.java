package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gnivol.game.component.BoundsComponent;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.input.InputHandler;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.model.dialogue.DialogueTree;
import com.gnivol.game.system.dialogue.DialogueEngine;
import com.gnivol.game.system.interaction.InteractionCallback;
import com.gnivol.game.system.interaction.PlayerInteractionSystem;
import com.gnivol.game.system.rs.RSListener;
import com.gnivol.game.system.scene.RoomScene;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.system.scene.ScreenFader;
import com.gnivol.game.ui.DialogueUI;
import com.gnivol.game.ui.InventoryUI;
import com.gnivol.game.ui.RSUI;

public class GameScreen extends BaseScreen {

    private SpriteBatch batch;
    private SceneManager sceneManager;
    private ScreenFader screenFader;
    private InputHandler inputHandler;
    private PlayerInteractionSystem interactionSystem;
    private boolean firstShow = true;
    // Khai báo hệ thống hội thoại
    private DialogueEngine dialogueEngine;
    private DialogueUI dialogueUI;
    private java.util.Map<String, DialogueTree> dialogueDatabase;

    private RSUI rsUI;
    private BitmapFont rsFont;
    private FreeTypeFontGenerator rsFontGenerator;
    private java.util.Set<String> finishedDialogues = new java.util.HashSet<>();

    // UI inspect text
    private Label inspectLabel;
    private Table inspectTable;
    private BitmapFont vietnameseFont;
    private FreeTypeFontGenerator fontGenerator;

    // Overlay system (tủ mở, xem chi tiết...)
    private Texture overlayTexture;
    private boolean overlayActive;
    private float overlayAlpha;       // fade-in animation
    private ShapeRenderer dimRenderer; // vẽ nền mờ đen
    private InventoryUI inventoryUI;

    private com.gnivol.game.system.puzzle.PuzzleManager puzzleManager;
    private com.gnivol.game.ui.PuzzleDrawerUI puzzleDrawerUI;

    // Debug overlay (F1 để bật/tắt)
    private boolean debugMode = false;
    private ShapeRenderer debugRenderer;
    private BitmapFont debugFont;

    // Debug drag & resize
    private RoomData.RoomObject dragTarget;     // object đang được kéo
    private boolean dragResizing;                // true = resize, false = move
    private float dragOffsetX, dragOffsetY;      // offset chuột so với góc object

    private static final String VIETNAMESE_CHARS =
            "aăâbcdđeêfghijklmnoôơpqrstuưvwxyz"
                    + "AĂÂBCDĐEÊFGHIJKLMNOÔƠPQRSTUƯVWXYZ"
                    + "àáảãạằắẳẵặầấẩẫậèéẻẽẹềếểễệìíỉĩịòóỏõọồốổỗộờớởỡợùúủũụừứửữựỳýỷỹỵ"
                    + "ÀÁẢÃẠẰẮẲẴẶẦẤẨẪẬÈÉẺẼẸỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌỒỐỔỖỘỜỚỞỠỢÙÚỦŨỤỪỨỬỮỰỲÝỶỸỴ"
                    + "0123456789.,;:!?'\"-()[]{}…—–/\\@#$%^&*+=<>~`| ";

    public GameScreen(GnivolGame game) {
        super(game);
    }

    @Override
    public void show() {
        game.getStage().clear();

        sceneManager = game.getSceneManager();
        screenFader = game.getScreenFader();
        inputHandler = game.getInputHandler();
        interactionSystem = game.getPlayerInteractionSystem();
        batch = new SpriteBatch();
        dimRenderer = new ShapeRenderer();
        debugRenderer = new ShapeRenderer();

        // --- FreeType font tiếng Việt ---
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 22;
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        param.color = Color.WHITE;
        param.borderWidth = 1.5f;
        param.borderColor = Color.BLACK;
        vietnameseFont = fontGenerator.generateFont(param);

        // Debug font (nhỏ hơn, dùng cho overlay debug)
        FreeTypeFontGenerator.FreeTypeFontParameter debugParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        debugParam.size = 14;
        debugParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        debugParam.color = Color.WHITE;
        debugParam.borderWidth = 1f;
        debugParam.borderColor = Color.BLACK;
        debugFont = fontGenerator.generateFont(debugParam);

        inventoryUI = new InventoryUI(
            game.getStage(),
            game.getInventoryManager(),
            game.getCraftingManager(),
            vietnameseFont
        );

        this.puzzleManager = game.getPuzzleManager();

        com.badlogic.gdx.scenes.scene2d.ui.Skin defaultSkin = new com.badlogic.gdx.scenes.scene2d.ui.Skin(Gdx.files.internal("ui/uiskin.json"));

        puzzleDrawerUI = new com.gnivol.game.ui.PuzzleDrawerUI(defaultSkin, game.getStage(), puzzleManager, game.getRsManager());

        puzzleManager.setCallback(new com.gnivol.game.system.puzzle.PuzzleManager.PuzzleCallback() {
            @Override
            public void onShowPuzzleOverlay(String puzzleId) {
                if ("puzzle_drawer".equals(puzzleId)) {
                    puzzleDrawerUI.show();
                }
            }
        });

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
                }
            }
        });

        game.getInventoryManager().clearInventory(); // Xóa sạch túi trước khi nạp
        game.getInventoryManager().addItem("ca_vat_final");
    //    game.getInventoryManager().addItem("chia_khoa_final");
        game.getInventoryManager().addItem("chuoi_chia_khoa");
       // game.getInventoryManager().addItem("dien_thoai_final");
        game.getInventoryManager().addItem("keo_502_final");
     //   game.getInventoryManager().addItem("chia_khoa_fixed_final");
        inventoryUI.refreshUI();
        // THÊM MỚI: Khởi tạo Dialogue System
        dialogueEngine = new DialogueEngine(game.getRsManager());
        dialogueUI = new DialogueUI(game, game.getStage(), vietnameseFont, dialogueEngine);

        // --- ĐỌC FILE DIALOGUES.JSON ---
        dialogueDatabase = new java.util.HashMap<>();
        com.badlogic.gdx.utils.Json json = new com.badlogic.gdx.utils.Json();
        json.setIgnoreUnknownFields(true);
        try {
            // Đọc file json và ép kiểu nó thành 1 mảng các DialogueTree
            java.util.ArrayList<DialogueTree> treeList = json.fromJson(
                java.util.ArrayList.class,
                DialogueTree.class,
                Gdx.files.internal("data/dialogues.json")
            );

            // Đổ vào Database để tra cứu cho nhanh
            for (DialogueTree tree : treeList) {
                dialogueDatabase.put(tree.dialogueId, tree);
            }
            Gdx.app.log("GameScreen", "Đã load thành công " + treeList.size() + " cây hội thoại!");
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "LỖI ĐỌC FILE DIALOGUES.JSON", e);
        }

        // --- Inspect text UI ---
        Label.LabelStyle labelStyle = new Label.LabelStyle(vietnameseFont, Color.WHITE);
        inspectLabel = new Label("", labelStyle);
        inspectLabel.setWrap(true);
        inspectLabel.setAlignment(Align.center);

        inspectTable = new Table();
        inspectTable.setFillParent(true);
        inspectTable.bottom().padBottom(30f);
        inspectTable.add(inspectLabel).width(900f).pad(15f);
        inspectTable.setVisible(false);
        game.getStage().addActor(inspectTable);

        // --- RS UI font (IM Fell English) ---
        rsFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellEnglish.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter rsParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        rsParam.size = 24;
        rsParam.color = Color.WHITE;
        rsParam.borderWidth = 1.5f;
        rsParam.borderColor = Color.BLACK;
        rsFont = rsFontGenerator.generateFont(rsParam);

        Label.LabelStyle rsStyle = new Label.LabelStyle(rsFont, Color.WHITE);
        rsUI = new RSUI(game.getStage(), rsStyle);

            // Lắng nghe sự thay đổi từ RSManager
            game.getRsManager().addListener(new RSListener() {
            @Override
            public void onRSChanged(float oldValue, float newValue) {
                rsUI.updateRS(newValue); // Cập nhật số mỗi khi RS thay đổi
            }

            @Override
            public void onThresholdCrossed(boolean isAbove) {
                // Logic glitch đã có ở MonitorSystem
            }
        });
        // --- Interaction callback: GameScreen chỉ xử lý visual ---
        interactionSystem.setCallback(new InteractionCallback() {
            @Override
            public void onShowInspectText(String text) {
                showInspectText(text);
            }

            @Override
            public void onEmptyClick() {
                hideInspectText();
            }

            @Override
            public void onInventoryFull() {
                // Rương đầy -> Gọi hàm cốt lõi với chữ ĐỎ cảnh báo
                showNotification("MAX INVENTORY!", Color.RED);

                // (Tùy chọn) Thêm âm thanh báo lỗi ở đây:
                // TODO: Triệu - play error sound
            }

            @Override
            public void onItemCollected(GameObject obj, String itemId) {
                Gdx.app.log("GameScreen", "Item collected: " + itemId);

                inventoryUI.refreshUI();

                hideInspectText();

                showItemNotification(itemId);

                // TODO: Triệu — play pickup sound (Có thể gọi hàm phát âm thanh từ ItemData sau)
            }

            @Override
            public void onDoorInteracted(GameObject obj) {
                // Lấy targetScene từ RoomData
                RoomData roomData = sceneManager.getCurrentScene().getRoomData();
                if (roomData == null || roomData.getObjects() == null) return;
                for (RoomData.RoomObject roomObj : roomData.getObjects()) {
                    if (roomObj.id.equals(obj.getId()) && roomObj.properties != null
                            && roomObj.properties.targetScene != null) {
                        changeSceneWithFade(roomObj.properties.targetScene);
                        return;
                    }
                }
            }
            @Override
            public void onObjectInteracted(GameObject obj) {
                if ("drawer".equals(obj.getId())) {
                    if (puzzleManager.isPuzzleSolved("puzzle_drawer")) {
                        showNotification("Ngăn kéo đã trống rỗng.", Color.LIGHT_GRAY);
                    } else {
                        puzzleManager.openPuzzle("puzzle_drawer");
                    }
                    return;
                }
                RoomData roomData = sceneManager.getCurrentScene().getRoomData();
                if (roomData == null || roomData.getObjects() == null) return;
                for (RoomData.RoomObject roomObj : roomData.getObjects()) {
                    if (!roomObj.id.equals(obj.getId())) continue;

                    if (roomObj.properties == null) continue;

                    if (roomObj.properties.dialogueId != null
                            && !finishedDialogues.contains(roomObj.properties.dialogueId)) {
                        onDialogueTriggered(roomObj.properties.dialogueId);
                        return;

                    }

                    if (roomObj.properties.altTextures != null
                            && !roomObj.properties.altTextures.isEmpty()) {
                        String firstPath = roomObj.properties.altTextures.values().iterator().next();
                        openOverlay(firstPath);
                        return;
                    }

                }

            }
            @Override
            public void onDialogueTriggered(String dialogueId) {
                hideInspectText();

                DialogueTree tree = dialogueDatabase.get(dialogueId);
                if (tree != null) {
                    dialogueEngine.loadDialogue(tree);
                    dialogueUI.displayNode(dialogueEngine.getCurrentNode());
                    finishedDialogues.add(dialogueId);
                }
            }

            @Override
            public void onOpenPuzzleOverlay(String puzzleId) {
                if (puzzleId.equals("puzzle_drawer")) {

                    //Ví dụ: drawerPuzzleUI.show();
                }
            }

            @Override
            public void onPuzzleFailed(String puzzleId) {

            }




        });

        // --- Input ---
        inputHandler.clear();
        inputHandler.addStage(game.getStage());
        inputHandler.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Debug mode: click để bắt đầu kéo object
                if (debugMode && button == Input.Buttons.LEFT) {
                    com.badlogic.gdx.math.Vector3 world = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                    camera.unproject(world, viewport.getScreenX(), viewport.getScreenY(),
                            viewport.getScreenWidth(), viewport.getScreenHeight());

                    com.gnivol.game.system.scene.Scene scene = sceneManager.getCurrentScene();
                    if (scene != null && scene.getRoomData() != null && scene.getRoomData().getObjects() != null) {
                        boolean shifting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

                        // Duyệt ngược (object trên cùng trước)
                        java.util.List<RoomData.RoomObject> objs = scene.getRoomData().getObjects();
                        for (int i = objs.size() - 1; i >= 0; i--) {
                            RoomData.RoomObject obj = objs.get(i);
                            if (world.x >= obj.x && world.x <= obj.x + obj.w
                                    && world.y >= obj.y && world.y <= obj.y + obj.h) {
                                dragTarget = obj;
                                dragResizing = shifting;
                                dragOffsetX = world.x - obj.x;
                                dragOffsetY = world.y - obj.y;
                                return true;
                            }
                        }
                    }
                }

                // Đang nói chuyện thì chặn không cho click vào đồ vật trong thế giới
                if (dialogueUI != null && dialogueUI.isVisible()) {
                    return false; // Stage sẽ tự bắt lấy event này để next câu thoại, không truyền xuống world
                }
                if (overlayActive) {
                    closeOverlay();
                    return true;
                }
                if (inventoryUI.isOpen()) {
                    return false;
                }
                return interactionSystem.handleClick(screenX, screenY, viewport);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (debugMode && dragTarget != null) {
                    com.badlogic.gdx.math.Vector3 world = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                    camera.unproject(world, viewport.getScreenX(), viewport.getScreenY(),
                            viewport.getScreenWidth(), viewport.getScreenHeight());

                    if (dragResizing) {
                        // Shift+kéo = resize (kéo góc phải-trên)
                        dragTarget.w = Math.max(10, world.x - dragTarget.x);
                        dragTarget.h = Math.max(10, world.y - dragTarget.y);
                    } else {
                        // Kéo = di chuyển
                        dragTarget.x = world.x - dragOffsetX;
                        dragTarget.y = world.y - dragOffsetY;
                    }

                    // Đồng bộ BoundsComponent (hitbox thật trong game)
                    com.gnivol.game.system.scene.Scene scene = sceneManager.getCurrentScene();
                    if (scene != null) {
                        GameObject go = scene.findObjectById(dragTarget.id);
                        if (go != null) {
                            BoundsComponent bounds = go.getComponent(BoundsComponent.class);
                            if (bounds != null) {
                                bounds.hitbox.set(dragTarget.x, dragTarget.y, dragTarget.w, dragTarget.h);
                            }
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (debugMode && dragTarget != null) {
                    Gdx.app.log("Debug", "Moved: " + dragTarget.id
                            + " → x:" + (int) dragTarget.x + " y:" + (int) dragTarget.y
                            + " w:" + (int) dragTarget.w + " h:" + (int) dragTarget.h);
                    dragTarget = null;
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F1) {
                    debugMode = !debugMode;
                    Gdx.app.log("Debug", debugMode ? "ON" : "OFF");
                    return true;
                }
                if (keycode == Input.Keys.F2 && debugMode) {
                    exportDebugCoordinates();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    if (overlayActive) {
                        closeOverlay();
                    } else {
                        game.setScreen(new PauseScreen(game, GameScreen.this));
                    }
                    return true;
                }
                return false;
            }
        });
        inputHandler.activate();

        // --- Load scene (chỉ lần đầu, không load lại khi resume từ PauseScreen) ---
        if (firstShow) {
            sceneManager.changeScene(Constants.SCENE_BEDROOM);
            screenFader.startFadeIn();
            firstShow = false;

            // Hiện dialogue mở đầu ngay khi vào game
            // intro_thought → kết thúc → tự động chạy intro_phone_call
            DialogueTree introTree = dialogueDatabase.get("intro_thought");
            if (introTree != null) {
                dialogueEngine.loadDialogue(introTree);
                dialogueUI.displayNode(dialogueEngine.getCurrentNode());
                finishedDialogues.add("intro_thought");

                dialogueUI.setOnFinished(new Runnable() {
                    @Override
                    public void run() {
                        DialogueTree phoneCall = dialogueDatabase.get("intro_phone_call");
                        if (phoneCall != null) {
                            dialogueEngine.loadDialogue(phoneCall);
                            dialogueUI.displayNode(dialogueEngine.getCurrentNode());
                            finishedDialogues.add("intro_phone_call");
                        }
                    }
                });
            }
        }
    }

    // --- Overlay ---

    private void openOverlay(String texturePath) {
        try {
            overlayTexture = new Texture(Gdx.files.internal(texturePath));
            overlayActive = true;
            overlayAlpha = 0f;
            Gdx.app.log("Overlay", "Opened: " + texturePath);
        } catch (Exception e) {
            Gdx.app.error("Overlay", "Cannot load: " + texturePath, e);
        }
    }

    private void closeOverlay() {
        overlayActive = false;
        if (overlayTexture != null) {
            overlayTexture.dispose();
            overlayTexture = null;
        }
        hideInspectText();
        Gdx.app.log("Overlay", "Closed");
    }

    private void showInspectText(String text) {
        inspectLabel.setText(text);
        inspectTable.setVisible(true);
        inspectTable.getColor().a = 0f;
        inspectTable.addAction(Actions.fadeIn(0.3f));
    }

    private void hideInspectText() {
        if (inspectTable.isVisible()) {
            inspectTable.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
                    Actions.visible(false)
            ));
        }
    }

    // --- Render ---

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        sceneManager.update(delta);
        screenFader.update(delta);
        game.getStage().act(delta);

        if (dialogueUI != null && inventoryUI != null) {
            if (dialogueUI.isVisible()) {
                inventoryUI.setVisible(false);
            } else {
                inventoryUI.setVisible(true);
            }
        }

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        sceneManager.render(batch);
        batch.end();

        // Overlay (nền mờ + ảnh giữa màn hình)
        if (overlayActive) {
            // Fade in
            overlayAlpha = Math.min(overlayAlpha + delta * 4f, 1f);

            // Nền đen mờ
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
            Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
            dimRenderer.setProjectionMatrix(camera.combined);
            dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
            dimRenderer.setColor(0f, 0f, 0f, 0.65f * overlayAlpha);
            dimRenderer.rect(0, 0, 1280, 720);
            dimRenderer.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

            // Ảnh overlay căn giữa
            if (overlayTexture != null) {
                float maxW = 700f;
                float maxH = 550f;
                float imgW = overlayTexture.getWidth();
                float imgH = overlayTexture.getHeight();
                float scale = Math.min(maxW / imgW, maxH / imgH);
                float drawW = imgW * scale;
                float drawH = imgH * scale;
                float drawX = (1280 - drawW) / 2f;
                float drawY = (720 - drawH) / 2f;

                batch.begin();
                batch.setColor(1f, 1f, 1f, overlayAlpha);
                batch.draw(overlayTexture, drawX, drawY, drawW, drawH);
                batch.setColor(Color.WHITE);
                batch.end();
            }
        }

        // Debug overlay (hitbox + tọa độ + chuột)
        if (debugMode) {
            renderDebugOverlay();
        }

        // UI (inspect text)
        game.getStage().draw();

        // Fade
        screenFader.render();
    }

    /** F2: In tọa độ tất cả object ra console — copy vào JSON */
    private void exportDebugCoordinates() {
        com.gnivol.game.system.scene.Scene scene = sceneManager.getCurrentScene();
        if (scene == null || scene.getRoomData() == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== ").append(scene.getSceneId()).append(" ==========\n");
        for (RoomData.RoomObject obj : scene.getRoomData().getObjects()) {
            sb.append(String.format("  \"%s\": { \"x\": %d, \"y\": %d, \"w\": %d, \"h\": %d }%n",
                    obj.id, (int) obj.x, (int) obj.y, (int) obj.w, (int) obj.h));
        }
        sb.append("==========================================");
        Gdx.app.log("Debug-Export", sb.toString());
    }

    private void renderDebugOverlay() {
        com.gnivol.game.system.scene.Scene currentScene = sceneManager.getCurrentScene();
        if (currentScene == null) return;

        RoomData roomData = currentScene.getRoomData();
        if (roomData == null || roomData.getObjects() == null) return;

        // Tọa độ chuột (world space)
        com.badlogic.gdx.math.Vector3 mouseWorld = new com.badlogic.gdx.math.Vector3(
                Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld, viewport.getScreenX(), viewport.getScreenY(),
                viewport.getScreenWidth(), viewport.getScreenHeight());

        // Vẽ hitbox (viền đỏ) + highlight object đang hover (vàng)
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);

        for (RoomData.RoomObject objData : roomData.getObjects()) {
            boolean hovered = mouseWorld.x >= objData.x && mouseWorld.x <= objData.x + objData.w
                    && mouseWorld.y >= objData.y && mouseWorld.y <= objData.y + objData.h;

            if (hovered) {
                debugRenderer.setColor(Color.YELLOW);
            } else {
                debugRenderer.setColor(Color.RED);
            }
            debugRenderer.rect(objData.x, objData.y, objData.w, objData.h);
        }

        // Crosshair tại vị trí chuột
        debugRenderer.setColor(Color.GREEN);
        debugRenderer.line(mouseWorld.x - 10, mouseWorld.y, mouseWorld.x + 10, mouseWorld.y);
        debugRenderer.line(mouseWorld.x, mouseWorld.y - 10, mouseWorld.x, mouseWorld.y + 10);
        debugRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Vẽ text: ID + tọa độ cho từng object
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (RoomData.RoomObject objData : roomData.getObjects()) {
            boolean hovered = mouseWorld.x >= objData.x && mouseWorld.x <= objData.x + objData.w
                    && mouseWorld.y >= objData.y && mouseWorld.y <= objData.y + objData.h;

            if (hovered) {
                debugFont.setColor(Color.YELLOW);
            } else {
                debugFont.setColor(Color.RED);
            }

            String info = objData.id + " [" + (int) objData.x + "," + (int) objData.y
                    + " " + (int) objData.w + "x" + (int) objData.h + "]";
            debugFont.draw(batch, info, objData.x, objData.y + objData.h + 16);
        }

        // Tọa độ chuột + hướng dẫn (góc trên trái)
        debugFont.setColor(Color.GREEN);
        debugFont.draw(batch, "Mouse: " + (int) mouseWorld.x + ", " + (int) mouseWorld.y,
                10, Constants.WORLD_HEIGHT - 10);
        debugFont.draw(batch, "[F1] Toggle | [F2] Export | Drag=Move | Shift+Drag=Resize",
                10, Constants.WORLD_HEIGHT - 28);
        debugFont.draw(batch, "Room: " + currentScene.getSceneId(),
                10, Constants.WORLD_HEIGHT - 46);

        // Hiện object đang kéo
        if (dragTarget != null) {
            debugFont.setColor(Color.CYAN);
            String dragInfo = (dragResizing ? "RESIZE " : "MOVE ") + dragTarget.id
                    + " → x:" + (int) dragTarget.x + " y:" + (int) dragTarget.y
                    + " w:" + (int) dragTarget.w + " h:" + (int) dragTarget.h;
            debugFont.draw(batch, dragInfo, 10, Constants.WORLD_HEIGHT - 64);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        game.getStage().getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        inputHandler.clear();
        if (inspectTable != null) inspectTable.remove();
        closeOverlay();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (dimRenderer != null) dimRenderer.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (vietnameseFont != null) vietnameseFont.dispose();
        if (debugFont != null) debugFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
        if (overlayTexture != null) overlayTexture.dispose();
        if (rsFont != null) rsFont.dispose();
        if (rsFontGenerator != null) rsFontGenerator.dispose();
    }

    private void showNotification(String text, Color color) {
        Label.LabelStyle notifStyle = new Label.LabelStyle(vietnameseFont, color);
        Label notifLabel = new Label(text, notifStyle);

        notifLabel.setPosition((1280 - notifLabel.getPrefWidth()) / 2f, 75f);
        notifLabel.getColor().a = 0f;
        game.getStage().addActor(notifLabel);

        notifLabel.addAction(Actions.sequence(
            Actions.parallel(Actions.fadeIn(1f), Actions.moveBy(0, 50f, 1f)),
            Actions.delay(3f),
            Actions.parallel(Actions.fadeOut(1.5f), Actions.moveBy(0, -30f, 1.5f)),
            Actions.removeActor()
        ));
    }

    private void showItemNotification(String itemId) {
        String itemName = itemId;
        try {
            com.gnivol.game.model.ItemData data = com.gnivol.game.data.ItemDatabase.getInstance().getItemData(itemId);
            if (data != null && data.itemName != null) {
                itemName = data.itemName;
            }
        } catch (Exception e) {
            Gdx.app.error("Notification", "Item no name: " + itemId);
        }

        showNotification(itemName, Color.MAROON);
    }

    public void changeSceneWithFade(String targetSceneId) {
        if (screenFader.isFading()) return;
        screenFader.startFade(() -> sceneManager.changeScene(targetSceneId));
    }
}
