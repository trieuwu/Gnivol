package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gnivol.game.GnivolGame;

import java.util.function.Supplier;

/**
 * Hiện 1 ảnh fullscreen trong N giây trên nền đen rồi auto chuyển sang screen tiếp theo
 * (do nextScreenFactory tạo). Không skip được.
 */
public class TimedImageScreen implements Screen {

    private final GnivolGame game;
    private final String imagePath;
    private final float duration;
    private final Supplier<Screen> nextScreenFactory;

    private SpriteBatch batch;
    private Texture texture;
    private float elapsed;
    private boolean transitioning;

    public TimedImageScreen(GnivolGame game, String imagePath, float duration,
                            Supplier<Screen> nextScreenFactory) {
        this.game = game;
        this.imagePath = imagePath;
        this.duration = duration;
        this.nextScreenFactory = nextScreenFactory;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        try {
            texture = new Texture(Gdx.files.internal(imagePath));
        } catch (Exception e) {
            Gdx.app.error("TimedImageScreen", "Cannot load: " + imagePath, e);
            texture = null;
        }
        elapsed = 0f;
        transitioning = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (texture != null) {
            batch.begin();
            batch.draw(texture, 0f, 0f, 1280f, 720f);
            batch.end();
        }

        elapsed += delta;
        if (elapsed >= duration && !transitioning) {
            transitioning = true;
            Gdx.app.postRunnable(() -> {
                Screen next = nextScreenFactory.get();
                game.setScreen(next);
                dispose();
            });
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }
}
