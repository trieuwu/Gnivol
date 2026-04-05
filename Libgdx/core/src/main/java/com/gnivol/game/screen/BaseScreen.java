package com.gnivol.game.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

/**
 * Base class cho tất cả Screen.
 * Cung cấp camera + viewport (1280x720 FitViewport).
 * KHÔNG tạo SpriteBatch/Stage — để screen con tự quản lý.
 */
public abstract class BaseScreen implements Screen {
    protected final GnivolGame game;
    protected OrthographicCamera camera;
    protected Viewport viewport;

    public BaseScreen(GnivolGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(
                Constants.WORLD_WIDTH,
                Constants.WORLD_HEIGHT,
                camera
        );
    }

    @Override
    public void show() {}

    @Override
    public abstract void render(float delta);

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}
}
