package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

public abstract class BaseScreen implements Screen {
    protected final GnivolGame game;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected SpriteBatch batch;
    protected Stage stage;

    public BaseScreen(GnivolGame game) {
        this.game = game;

        // Camera 2D — "mắt" nhìn vào game world. Orthographic nghĩa là không có phối cảnh (vật xa/gần cùng kích thước), phù hợp cho game 2D.
        this.camera = new OrthographicCamera();

        this.viewport = new FitViewport(
                Constants.WORLD_WIDTH,
                Constants.WORLD_HEIGHT,
                camera
        );
        this.batch = new SpriteBatch();
        this.stage = new Stage(new FitViewport(
                Constants.WORLD_WIDTH,
                Constants.WORLD_HEIGHT
        ));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear màn hình đen
        ScreenUtils.clear(0, 0, 0, 1);

        // Update camera
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        // Subclass override để vẽ game world
        renderWorld(delta);

        // Vẽ UI (Scene2D)
        stage.act(delta);
        stage.draw();
    }

    /** Override method này để vẽ game world */
    protected abstract void renderWorld(float delta);

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }
}