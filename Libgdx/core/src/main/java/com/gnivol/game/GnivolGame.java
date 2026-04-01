package com.gnivol.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.ashley.core.Engine;
import com.gnivol.game.screen.MainMenuScreen;

/**
 * Lop chinh cua game Gnivol.
 * Extends Game de ho tro chuyen doi giua cac Screen (MainMenu, GameScreen, v.v.)
 */
public class GnivolGame extends Game {

    // Stage dung chung cho UI overlay (dialogue, inventory, menu)
    private Stage stage;

    // Ashley ECS engine dung chung cho tat ca entity/component/system
    private Engine ashleyEngine;

    @Override
    public void create() {
        // Tao Stage voi FitViewport giu ti le 1280x720
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // Khoi tao Ashley ECS Engine
        ashleyEngine = new Engine();

        // Bat dau tu man hinh MainMenu
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        // Xoa man hinh
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Goi render cua Screen hien tai (duoc quan ly boi Game)
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }

    /** Tra ve Stage dung chung */
    public Stage getStage() {
        return stage;
    }

    /** Tra ve Ashley ECS Engine dung chung */
    public Engine getAshleyEngine() {
        return ashleyEngine;
    }
}
