package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.gnivol.game.GnivolGame;

/**
 * Phát suicide.webm với chromakey rồi tự chuyển MainMenuScreen.
 * Không cho skip — buộc xem hết.
 */
public class SuicideIntroScreen implements Screen {

    private final GnivolGame game;
    private SpriteBatch batch;
    private VideoPlayer videoPlayer;
    private ShaderProgram chromaShader;
    private boolean videoFinished;
    private boolean transitioning;

    private static final float[] CHROMA_KEY_RGB = {0f, 1f, 0f};
    private static final float CHROMA_THRESHOLD = 0.4f;
    private static final float CHROMA_SMOOTHING = 0.1f;

    public SuicideIntroScreen(GnivolGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        try {
            String vert = Gdx.files.internal("shaders/chromakey.vert").readString();
            String frag = Gdx.files.internal("shaders/chromakey.frag").readString();
            chromaShader = new ShaderProgram(vert, frag);
            if (!chromaShader.isCompiled()) {
                Gdx.app.error("SuicideIntro", "ChromaKey shader compile error: " + chromaShader.getLog());
                chromaShader.dispose();
                chromaShader = null;
            }
        } catch (Exception e) {
            Gdx.app.error("SuicideIntro", "Cannot load chromakey shader", e);
            chromaShader = null;
        }

        try {
            videoPlayer = VideoPlayerCreator.createVideoPlayer();
            videoPlayer.setOnCompletionListener(p -> videoFinished = true);
            videoPlayer.play(Gdx.files.internal("video/suicide.webm"));
        } catch (Exception e) {
            Gdx.app.error("SuicideIntro", "Cannot play suicide.webm", e);
            videoFinished = true;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (videoPlayer != null && !videoFinished) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();
            if (frame != null) {
                if (chromaShader != null) batch.setShader(chromaShader);
                batch.begin();
                if (chromaShader != null) {
                    chromaShader.setUniformf("u_keyColor",
                        CHROMA_KEY_RGB[0], CHROMA_KEY_RGB[1], CHROMA_KEY_RGB[2]);
                    chromaShader.setUniformf("u_threshold", CHROMA_THRESHOLD);
                    chromaShader.setUniformf("u_smoothing", CHROMA_SMOOTHING);
                }
                batch.draw(frame, 0f, 0f, 1280f, 720f);
                batch.end();
                if (chromaShader != null) batch.setShader(null);
            }
        }

        if (videoFinished && !transitioning) {
            transitioning = true;
            Gdx.app.postRunnable(() -> {
                game.setScreen(new MainMenuScreen(game));
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
        if (videoPlayer != null) {
            videoPlayer.dispose();
            videoPlayer = null;
        }
        if (chromaShader != null) {
            chromaShader.dispose();
            chromaShader = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }
}
