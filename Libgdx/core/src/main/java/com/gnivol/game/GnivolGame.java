package com.gnivol.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.ashley.core.Engine;
import com.gnivol.game.audio.AudioManager;
import com.gnivol.game.data.FlagManager;
import com.gnivol.game.input.InputHandler;
import com.gnivol.game.screen.LoginScreen;
import com.gnivol.game.screen.MainMenuScreen;
import com.gnivol.game.system.FontManager;
import com.gnivol.game.system.interaction.PlayerInteractionSystem;
import com.gnivol.game.system.inventory.CraftingManager;
import com.gnivol.game.system.inventory.InventoryManager;
import com.gnivol.game.system.rs.RSManager;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.system.scene.ScreenFader;

public class GnivolGame extends Game {
    private Stage stage;
    private Engine ashleyEngine;
    private SceneManager sceneManager;
    private ScreenFader screenFader;
    private InputHandler inputHandler;
    private RSManager rsManager;
    private InventoryManager inventoryManager;
    private CraftingManager craftingManager;
    private PlayerInteractionSystem playerInteractionSystem;
    private AudioManager audioManager;
    private FlagManager flagManager;
    private com.gnivol.game.system.puzzle.PuzzleManager puzzleManager;
    private com.gnivol.game.model.GameState gameState;
    private com.gnivol.game.system.save.GameSnapshot gameSnapshot;
    private com.gnivol.game.system.save.AutoSaveManager autoSaveManager;
    private com.gnivol.game.system.save.SaveUIController saveUIController;
    private com.gnivol.game.system.meta.EndingManager endingManager;
    public boolean isLoadedGame = false;
    private FontManager fontManager;

    @Override
    public void create() {
        fontManager = new FontManager();
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        ashleyEngine = new Engine();
        screenFader = new ScreenFader(2.5f, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        inputHandler = new InputHandler();
        audioManager = new AudioManager();

        puzzleManager = new com.gnivol.game.system.puzzle.PuzzleManager();
        inventoryManager = new InventoryManager();
        craftingManager = new CraftingManager();
        rsManager = new RSManager();
        flagManager = new FlagManager();
        gameState = new com.gnivol.game.model.GameState();

        sceneManager = new SceneManager(puzzleManager);
        sceneManager.setAudioManager(audioManager);
        // Map sceneId → bgmId để SceneManager auto-crossfade khi đổi phòng
        sceneManager.setSceneBGM("room_bedroom", "bedroom_bgm");
        sceneManager.setSceneBGM("room_hallway", "outside");
        sceneManager.setSceneBGM("room_tang_1", "outside");
        sceneManager.setSceneBGM("room_chu_nha", "outside");
        playerInteractionSystem = new PlayerInteractionSystem(sceneManager, inventoryManager, rsManager, puzzleManager);

        endingManager = new com.gnivol.game.system.meta.EndingManager();

        gameSnapshot = new com.gnivol.game.system.save.GameSnapshot();
        autoSaveManager = new com.gnivol.game.system.save.AutoSaveManager(gameSnapshot, saveUIController);

        gameSnapshot.register(gameState);
        gameSnapshot.register(inventoryManager);
        gameSnapshot.register(puzzleManager);
        gameSnapshot.register(flagManager);

        try {
            Pixmap cursorPixmap = new Pixmap(Gdx.files.internal("images/cursor.png"));
            Cursor customCursor = Gdx.graphics.newCursor(cursorPixmap, 0, 0);
            Gdx.graphics.setCursor(customCursor);
            cursorPixmap.dispose();
            Gdx.app.log("Game", "Custom cursor loaded successfully.");
        } catch (Exception e) {
            Gdx.app.error("Game", "Không thể tải con trỏ chuột: " + e.getMessage());
        }
        // --------------------------------------

        setScreen(new LoginScreen(this));


    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F11)) {
            boolean isFullScreen = Gdx.graphics.isFullscreen();

            if (isFullScreen) {
                Gdx.graphics.setWindowedMode(1280, 720);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        super.render();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (autoSaveManager != null) autoSaveManager.dispose();
        if (stage != null) stage.dispose();
        if (sceneManager != null) sceneManager.dispose();
        if (screenFader != null) screenFader.dispose();
        if (getScreen() != null) getScreen().dispose();
        fontManager.dispose();
        super.dispose();
    }

    public void resetGameState() {
        // Safety: reset FORCE_MAX_GLITCH để tránh kẹt qua game session
        com.gnivol.game.screen.GameScreen.FORCE_MAX_GLITCH = false;
        if (inventoryManager != null) inventoryManager.clearInventory();
        if (puzzleManager != null) puzzleManager.reset();
        if (sceneManager != null) sceneManager.reset();
        if (rsManager != null) rsManager.reset();
        if (flagManager != null) flagManager.reset();
        if (gameState != null) gameState.setCurrentRS(35);
        if (autoSaveManager != null) autoSaveManager.setGameOver(false);

        isLoadedGame = false;
        Gdx.app.log("Game", "Cleared");
    }

    public boolean loadGame() {
        com.badlogic.gdx.files.FileHandle file = Gdx.files.external(".gnivol/save_slot_1.json");
        if (!file.exists()) {
            Gdx.app.log("LoadGame", "Not found save_slot_1.json");
            return false;
        }
        try {
            String jsonStr = file.readString("UTF-8");
            com.badlogic.gdx.utils.JsonReader reader = new com.badlogic.gdx.utils.JsonReader();
            com.badlogic.gdx.utils.JsonValue root = reader.parse(jsonStr);

            if (autoSaveManager != null) autoSaveManager.setGameOver(false);
            if (inventoryManager != null) inventoryManager.clearInventory();
            if (puzzleManager != null) puzzleManager.reset();
            if (sceneManager != null) sceneManager.reset();
            if (flagManager != null) flagManager.reset();

            if (gameState != null) gameState.load(root);
            if (inventoryManager != null) inventoryManager.load(root);
            if (puzzleManager != null) puzzleManager.load(root);
            if (flagManager != null) flagManager.load(root);

            if (rsManager != null && gameState != null) {
                 rsManager.setCurrentRS(gameState.getCurrentRS());
            }
            isLoadedGame = true;
            Gdx.app.log("LoadGame", "Load game successful!");
            return true;
        } catch (Exception e) {
            Gdx.app.error("LoadGame", "Error saving: ", e);
            return false;
        }
    }

    public Stage getStage() {return stage;}

    public Engine getAshleyEngine() {return ashleyEngine;}

    public SceneManager getSceneManager() {return sceneManager;}

    public ScreenFader getScreenFader() {return screenFader;}

    public InputHandler getInputHandler() {return inputHandler;}

    public RSManager getRsManager() {return rsManager;}

    public InventoryManager getInventoryManager() {return inventoryManager;}

    public CraftingManager getCraftingManager() {return craftingManager;}

    public PlayerInteractionSystem getPlayerInteractionSystem() {return playerInteractionSystem;}

    public AudioManager getAudioManager() {return audioManager;}

    public FlagManager getFlagManager() {return flagManager;}

    public com.gnivol.game.system.puzzle.PuzzleManager getPuzzleManager() {return puzzleManager;}

    public com.gnivol.game.model.GameState getGameState() {return gameState;}

    public com.gnivol.game.system.save.AutoSaveManager getAutoSaveManager() { return autoSaveManager;}

    public com.gnivol.game.system.meta.EndingManager getEndingManager() { return endingManager; }

    public FontManager getFontManager() {return fontManager;}
}
