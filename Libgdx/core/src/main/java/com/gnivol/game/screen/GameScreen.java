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
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.input.InputHandler;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.system.interaction.InteractionCallback;
import com.gnivol.game.system.interaction.PlayerInteractionSystem;
import com.gnivol.game.system.scene.RoomScene;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.system.scene.ScreenFader;

public class GameScreen extends BaseScreen {

    private SpriteBatch batch;
    private SceneManager sceneManager;
    private ScreenFader screenFader;
    private InputHandler inputHandler;
    private PlayerInteractionSystem interactionSystem;

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
        sceneManager = game.getSceneManager();
        screenFader = game.getScreenFader();
        inputHandler = game.getInputHandler();
        interactionSystem = game.getPlayerInteractionSystem();
        batch = new SpriteBatch();
        dimRenderer = new ShapeRenderer();

        // --- FreeType font tiếng Việt ---
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 22;
        param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        param.color = Color.WHITE;
        param.borderWidth = 1.5f;
        param.borderColor = Color.BLACK;
        vietnameseFont = fontGenerator.generateFont(param);

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
            public void onItemCollected(GameObject obj, String itemId) {
                Gdx.app.log("GameScreen", "Item collected: " + itemId);
                // TODO: Triệu — play pickup sound, ẩn sprite, animation
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
                // Object có altTextures → mở overlay
                RoomData roomData = sceneManager.getCurrentScene().getRoomData();
                if (roomData == null || roomData.getObjects() == null) return;
                for (RoomData.RoomObject roomObj : roomData.getObjects()) {
                    if (roomObj.id.equals(obj.getId()) && roomObj.properties != null
                            && roomObj.properties.altTextures != null
                            && !roomObj.properties.altTextures.isEmpty()) {
                        String firstPath = roomObj.properties.altTextures.values().iterator().next();
                        openOverlay(firstPath);
                        return;
                    }
                }
            }
        });

        // --- Input ---
        inputHandler.clear();
        inputHandler.addStage(game.getStage());
        inputHandler.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (overlayActive) {
                    closeOverlay();
                    return true;
                }
                return interactionSystem.handleClick(screenX, screenY, viewport);
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE && overlayActive) {
                    closeOverlay();
                    return true;
                }
                return false;
            }
        });
        inputHandler.activate();

        // --- Load scene ---
        sceneManager.changeScene(Constants.SCENE_BEDROOM);
        screenFader.startFadeIn();
        Gdx.app.log("GameScreen", "Game started");
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

        // Scene
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

        // UI (inspect text)
        game.getStage().draw();

        // Fade
        screenFader.render();
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
        if (vietnameseFont != null) vietnameseFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
        if (overlayTexture != null) overlayTexture.dispose();
    }

    public void changeSceneWithFade(String targetSceneId) {
        if (screenFader.isFading()) return;
        screenFader.startFade(() -> sceneManager.changeScene(targetSceneId));
    }
}
