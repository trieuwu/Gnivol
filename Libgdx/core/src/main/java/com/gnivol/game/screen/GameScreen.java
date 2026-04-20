package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

    private Texture overlayTexture;
    private boolean overlayActive;
    private float overlayAlpha;
    private ShapeRenderer dimRenderer;
    private InventoryUI inventoryUI;

    private com.gnivol.game.system.puzzle.PuzzleManager puzzleManager;
    private com.gnivol.game.ui.PuzzleDrawerUI puzzleDrawerUI;
    private com.gnivol.game.ui.LaserUI laserUI;

    private boolean debugMode = false;
    private ShapeRenderer debugRenderer;

    private RoomData.RoomObject dragTarget;
    private boolean dragResizing;
    private float dragOffsetX, dragOffsetY;

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

        FontManager fm = game.getFontManager();


        inventoryUI = new InventoryUI(game.getStage(), game.getInventoryManager(), game.getCraftingManager(), fm.fontVietnamese);
        inventoryUI.refreshUI();

        this.puzzleManager = game.getPuzzleManager();
        com.badlogic.gdx.scenes.scene2d.ui.Skin defaultSkin = new com.badlogic.gdx.scenes.scene2d.ui.Skin(Gdx.files.internal("ui/uiskin.json"));

        puzzleDrawerUI = new com.gnivol.game.ui.PuzzleDrawerUI(defaultSkin, game.getStage(), puzzleManager, game.getRsManager());
        laserUI = new com.gnivol.game.ui.LaserUI(defaultSkin, game.getStage());


        setupPuzzleListeners();

        dialogueEngine = new DialogueEngine(game.getRsManager());
        dialogueUI = new DialogueUI(game, game.getStage(), fm.fontVietnamese, dialogueEngine, game.getRsManager());
        loadDialogueDatabase();

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
    }

    private void setupPuzzleListeners() {
        laserUI.setListener(puzzleId -> {
            puzzleManager.markSolved(puzzleId);
            inventoryUI.refreshUI();
            if (game.getAutoSaveManager() != null) {
                game.getAutoSaveManager().onSaveTrigger("puzzle_" + puzzleId);
            }
        });

        puzzleManager.setCallback(puzzleId -> {
            if ("puzzle_drawer".equals(puzzleId)) puzzleDrawerUI.show();
            else if ("puzzle_laser".equals(puzzleId)) laserUI.show();
        });

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
        interactionSystem.setCallback(new InteractionCallback() {
            @Override public void onShowInspectText(String text) { showInspectText(text); }
            @Override public void onEmptyClick() { hideInspectText(); }
            @Override public void onInventoryFull() { showNotification("MAX INVENTORY!", Color.RED); }

            @Override
            public void onItemCollected(GameObject obj, String itemId) {
                inventoryUI.refreshUI();
                hideInspectText();
                showItemNotification(itemId);
                if ("keo_502_final".equals(itemId)) {
                    if (sceneManager.getCurrentScene() instanceof RoomScene) {
                        ((RoomScene) sceneManager.getCurrentScene()).changeBackground("images/bathroom_no_bottle.png");
                    }
                }
                if (game.getAutoSaveManager() != null) game.getAutoSaveManager().onSaveTrigger("pickup_" + itemId);
            }

            @Override
            public void onDoorInteracted(GameObject obj) {
                RoomData roomData = sceneManager.getCurrentScene().getRoomData();
                for (RoomData.RoomObject roomObj : roomData.getObjects()) {
                    if (roomObj.id.equals(obj.getId()) && roomObj.properties != null && roomObj.properties.targetScene != null) {
                        changeSceneWithFade(roomObj.properties.targetScene);
                        return;
                    }
                }
            }

            @Override
            public void onObjectInteracted(GameObject obj) {
                if ("drawer".equals(obj.getId())) {
                    if (puzzleManager.isPuzzleSolved("puzzle_drawer")) showNotification("Ngăn kéo đã trống rỗng.", Color.LIGHT_GRAY);
                    else puzzleManager.openPuzzle("puzzle_drawer");
                    return;
                }
                RoomData roomData = sceneManager.getCurrentScene().getRoomData();
                for (RoomData.RoomObject roomObj : roomData.getObjects()) {
                    if (!roomObj.id.equals(obj.getId()) || roomObj.properties == null) continue;
                    if (roomObj.properties.dialogueId != null && !game.getGameState().isDialogueFinished(roomObj.properties.dialogueId)) {
                        onDialogueTriggered(roomObj.properties.dialogueId);
                        return;
                    }
                    DialogueTree thoughtTree = new ThoughtManager().getThoughtTree(obj.getId(), game.getRsManager().getRS());
                    if (thoughtTree != null) {
                        dialogueEngine.loadDialogue(thoughtTree);
                        dialogueUI.displayNode(dialogueEngine.getCurrentNode());
                        return;
                    }
                    if (roomObj.properties.altTextures != null && !roomObj.properties.altTextures.isEmpty()) {
                        openOverlay(roomObj.properties.altTextures.values().iterator().next());
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
                    game.getGameState().markDialogueFinished(dialogueId);
                }
            }
            @Override public void onOpenPuzzleOverlay(String puzzleId) {}
            @Override public void onPuzzleFailed(String puzzleId) {}
        });
    }

    private void setupInputProcessors() {
        inputHandler.clear();
        inputHandler.addStage(game.getStage());
        inputHandler.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (debugMode && button == Input.Buttons.LEFT) {
                    handleDebugClick(screenX, screenY);
                    return true;
                }
                if (dialogueUI != null && dialogueUI.isVisible()) return false;
                if (overlayActive) { closeOverlay(); return true; }
                if (inventoryUI.isOpen()) return false;
                return interactionSystem.handleClick(screenX, screenY, viewport);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (debugMode && dragTarget != null) {
                    handleDebugDrag(screenX, screenY);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) { dragTarget = null; return false; }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F1) { debugMode = !debugMode; return true; }
                if (keycode == Input.Keys.F2 && debugMode) { exportDebugCoordinates(); return true; }
                if (keycode == Input.Keys.ESCAPE) {
                    if (overlayActive) closeOverlay();
                    else game.setScreen(new PauseScreen(game, GameScreen.this));
                    return true;
                }
                return false;
            }
        });
        inputHandler.activate();
    }

    private void handleDebugClick(int screenX, int screenY) {
        com.badlogic.gdx.math.Vector3 world = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
        camera.unproject(world, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        List<RoomData.RoomObject> objs = sceneManager.getCurrentScene().getRoomData().getObjects();
        for (int i = objs.size() - 1; i >= 0; i--) {
            RoomData.RoomObject obj = objs.get(i);
            if (world.x >= obj.x && world.x <= obj.x + obj.w && world.y >= obj.y && world.y <= obj.y + obj.h) {
                dragTarget = obj;
                dragResizing = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                dragOffsetX = world.x - obj.x;
                dragOffsetY = world.y - obj.y;
                break;
            }
        }
    }

    private void handleDebugDrag(int screenX, int screenY) {
        com.badlogic.gdx.math.Vector3 world = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
        camera.unproject(world, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        if (dragResizing) {
            dragTarget.w = Math.max(10, world.x - dragTarget.x);
            dragTarget.h = Math.max(10, world.y - dragTarget.y);
        } else {
            dragTarget.x = world.x - dragOffsetX;
            dragTarget.y = world.y - dragOffsetY;
        }
        GameObject go = sceneManager.getCurrentScene().findObjectById(dragTarget.id);
        if (go != null) go.getComponent(BoundsComponent.class).hitbox.set(dragTarget.x, dragTarget.y, dragTarget.w, dragTarget.h);
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

        if (overlayActive) renderOverlay(delta);
        if (debugMode) renderDebugOverlay();
        game.getStage().draw();
        screenFader.render();
    }

    private void renderOverlay(float delta) {
        overlayAlpha = Math.min(overlayAlpha + delta * 4f, 1f);
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        dimRenderer.setProjectionMatrix(camera.combined);
        dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dimRenderer.setColor(0, 0, 0, 0.65f * overlayAlpha);
        dimRenderer.rect(0, 0, 1280, 720);
        dimRenderer.end();
        if (overlayTexture != null) {
            batch.begin();
            batch.setColor(1, 1, 1, overlayAlpha);
            batch.draw(overlayTexture, (1280 - 700) / 2f, (720 - 550) / 2f, 700, 550);
            batch.setColor(Color.WHITE);
            batch.end();
        }
    }

    private void renderDebugOverlay() {
        FontManager fm = game.getFontManager();
        com.gnivol.game.system.scene.Scene currentScene = sceneManager.getCurrentScene();
        if (currentScene == null || currentScene.getRoomData() == null) return;

        com.badlogic.gdx.math.Vector3 mouseWorld = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);

        for (RoomData.RoomObject obj : currentScene.getRoomData().objects) {
            boolean hovered = mouseWorld.x >= obj.x && mouseWorld.x <= obj.x + obj.w && mouseWorld.y >= obj.y && mouseWorld.y <= obj.y + obj.h;
            debugRenderer.setColor(hovered ? Color.YELLOW : Color.RED);
            debugRenderer.rect(obj.x, obj.y, obj.w, obj.h);
        }

        debugRenderer.setColor(Color.GREEN);
        debugRenderer.line(mouseWorld.x - 10, mouseWorld.y, mouseWorld.x + 10, mouseWorld.y);
        debugRenderer.line(mouseWorld.x, mouseWorld.y - 10, mouseWorld.x, mouseWorld.y + 10);
        debugRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.begin();
        for (RoomData.RoomObject obj : currentScene.getRoomData().objects) {
            boolean hovered = mouseWorld.x >= obj.x && mouseWorld.x <= obj.x + obj.w && mouseWorld.y >= obj.y && mouseWorld.y <= obj.y + obj.h;
            fm.fontDebug.setColor(hovered ? Color.YELLOW : Color.RED);
            String info = obj.id + " [" + (int) obj.x + "," + (int) obj.y + " " + (int) obj.w + "x" + (int) obj.h + "]";
            fm.fontDebug.draw(batch, info, obj.x, obj.y + obj.h + 16);
        }

        fm.fontDebug.setColor(Color.GREEN);
        fm.fontDebug.draw(batch, "Mouse: " + (int) mouseWorld.x + ", " + (int) mouseWorld.y, 10, Constants.WORLD_HEIGHT - 10);
        fm.fontDebug.draw(batch, "[F1] Toggle | [F2] Export | Drag=Move | Shift+Drag=Resize", 10, Constants.WORLD_HEIGHT - 28);
        fm.fontDebug.draw(batch, "Room: " + currentScene.getSceneId(), 10, Constants.WORLD_HEIGHT - 46);

        if (dragTarget != null) {
            fm.fontDebug.setColor(Color.CYAN);
            String dragInfo = (dragResizing ? "RESIZE " : "MOVE ") + dragTarget.id
                + " → x:" + (int) dragTarget.x + " y:" + (int) dragTarget.y
                + " w:" + (int) dragTarget.w + " h:" + (int) dragTarget.h;
            fm.fontDebug.draw(batch, dragInfo, 10, Constants.WORLD_HEIGHT - 64);
        }
        batch.end();
    }

    public void showNotification(String text, Color color) {
        Label.LabelStyle style = new Label.LabelStyle(game.getFontManager().fontVietnamese, color); // Dùng fontVietnamese
        Label label = new Label(text, style);
        label.setPosition((1280 - label.getPrefWidth()) / 2f, 75f);
        label.getColor().a = 0f;
        game.getStage().addActor(label);
        label.addAction(Actions.sequence(Actions.parallel(Actions.fadeIn(1f), Actions.moveBy(0, 50f, 1f)), Actions.delay(3f), Actions.parallel(Actions.fadeOut(1.5f), Actions.moveBy(0, -30f, 1.5f)), Actions.removeActor()));
    }

    private void showItemNotification(String itemId) {
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

    private void openOverlay(String path) { try { overlayTexture = new Texture(Gdx.files.internal(path)); overlayActive = true; overlayAlpha = 0; } catch (Exception e) { Gdx.app.error("Overlay", "Error", e); } }
    private void closeOverlay() { overlayActive = false; if (overlayTexture != null) overlayTexture.dispose(); overlayTexture = null; hideInspectText(); }
    private void showInspectText(String text) { inspectLabel.setText(text); inspectTable.setVisible(true); inspectTable.getColor().a = 0f; inspectTable.addAction(Actions.fadeIn(0.3f)); }
    private void hideInspectText() { if (inspectTable.isVisible()) inspectTable.addAction(Actions.sequence(Actions.fadeOut(0.3f), Actions.visible(false))); }
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
        if (overlayTexture != null) overlayTexture.dispose();
    }
}
