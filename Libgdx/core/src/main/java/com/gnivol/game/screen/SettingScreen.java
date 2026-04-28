package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.audio.AudioManager;

public class SettingScreen extends BaseScreen {
    private final Screen previousScreen;
    private final AudioManager audioManager;
    private Stage stage;
    private Skin skin;
    private Label musicValueLabel;
    private Label sfxValueLabel;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer dimRenderer;

    public SettingScreen(GnivolGame game, Screen previousScreen) {
        super(game);
        this.previousScreen = previousScreen;
        this.audioManager = game.getAudioManager();
    }

    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        dimRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        com.gnivol.game.system.FontManager fm = game.getFontManager();

        Label.LabelStyle titleStyle = new Label.LabelStyle(fm.fontTitle, Color.WHITE);
        Label.LabelStyle labelStyle = new Label.LabelStyle(fm.fontButton, Color.WHITE); // Dùng fontButton cho label

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = fm.fontButton;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(new Label("SETTINGS", titleStyle)).colspan(3).padBottom(60f).row();
        table.add(new Label("Music:", labelStyle)).padRight(20f);
        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(audioManager.getMusicVolume());
        table.add(musicSlider).width(300f).padRight(10f);

        musicValueLabel = new Label((int)(audioManager.getMusicVolume() * 100) + "%", labelStyle);
        table.add(musicValueLabel).width(50f).row();

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = musicSlider.getValue();
                audioManager.setMusicVolume(value);
                musicValueLabel.setText((int)(value * 100) + "%");
            }
        });

        table.add(new Label("SFX:", labelStyle)).padRight(20f).padTop(20f);

        final Slider sfxSlider = new Slider(0f, 1f, 0.01f, false, skin);
        sfxSlider.setValue(audioManager.getSfxVolume());
        table.add(sfxSlider).width(300f).padRight(10f).padTop(20f);

        sfxValueLabel = new Label((int)(audioManager.getSfxVolume() * 100) + "%", labelStyle);
        table.add(sfxValueLabel).width(50f).padTop(20f).row();

        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = sfxSlider.getValue();
                audioManager.setSfxVolume(value);
                sfxValueLabel.setText((int)(value * 100) + "%");
            }
        });

        // --- Nut Quay lai ---
        TextButton backBtn = new TextButton("Back", buttonStyle);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stage.getRoot().setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

                stage.getRoot().addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.5f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> game.setScreen(previousScreen))
                ));
            }
        });
        table.add(backBtn).colspan(3).padTop(50f).row();

        stage.addActor(table);

        stage.getRoot().getColor().a = 0f;
        stage.getRoot().addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.5f));
    }

    @Override
    public void render(float delta) {
        if (previousScreen instanceof PauseScreen) {
            ((PauseScreen) previousScreen).getGameScreen().render(0f);
        } else if (previousScreen != null) {
            previousScreen.render(0f);
        }

        stage.getViewport().apply();
        dimRenderer.setProjectionMatrix(stage.getCamera().combined);

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        dimRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

        float fadeAlpha = stage.getRoot().getColor().a;
        float dimAlpha = fadeAlpha;

        if (previousScreen instanceof PauseScreen) {
            dimAlpha = 1.0f;
        }

        Color colorBottom = new Color(0f, 0f, 0f, 0.9f * dimAlpha);
        Color colorTop = new Color(0f, 0f, 0f, 0.4f * dimAlpha);

        dimRenderer.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT,
            colorBottom, colorBottom, colorTop, colorTop);

        dimRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage != null) stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (dimRenderer != null) dimRenderer.dispose();
    }
}
