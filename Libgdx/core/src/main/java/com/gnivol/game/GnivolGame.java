package com.gnivol.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.ashley.core.Engine;
import com.gnivol.game.input.InputHandler;
import com.gnivol.game.screen.MainMenuScreen;
import com.gnivol.game.system.interaction.PlayerInteractionSystem;
import com.gnivol.game.system.inventory.InventoryManager;
import com.gnivol.game.system.rs.RSManager;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.system.scene.ScreenFader;

/**
 * Lớp chính của game Gnivol.
 * Quản lý lifecycle, giữ các manager dùng chung.
 */
public class GnivolGame extends Game {

    private Stage stage;
    private Engine ashleyEngine;

    // Các manager dùng chung
    private SceneManager sceneManager;
    private ScreenFader screenFader;
    private InputHandler inputHandler;
    private RSManager rsManager;
    private InventoryManager inventoryManager;
    private PlayerInteractionSystem playerInteractionSystem;

    @Override
    public void create() {
        // Viewport giữ tỉ lệ 1280x720
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));

        // Ashley ECS Engine
        ashleyEngine = new Engine();

        // Khởi tạo các manager
        sceneManager = new SceneManager();
        screenFader = new ScreenFader(2.5f, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        inputHandler = new InputHandler();
        rsManager = new RSManager();
        inventoryManager = new InventoryManager();
        playerInteractionSystem = new PlayerInteractionSystem(sceneManager, inventoryManager, rsManager);

        // Bắt đầu từ MainMenu
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (sceneManager != null) sceneManager.dispose();
        if (screenFader != null) screenFader.dispose();
        if (getScreen() != null) getScreen().dispose();
    }

    // --- Getter ---

    public Stage getStage() {
        return stage;
    }

    public Engine getAshleyEngine() {
        return ashleyEngine;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public ScreenFader getScreenFader() {
        return screenFader;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public RSManager getRsManager() {
        return rsManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public PlayerInteractionSystem getPlayerInteractionSystem() {
        return playerInteractionSystem;
    }
}