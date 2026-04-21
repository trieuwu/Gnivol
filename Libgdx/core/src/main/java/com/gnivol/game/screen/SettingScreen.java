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
                game.setScreen(previousScreen);
            }
        });
        table.add(backBtn).colspan(3).padTop(50f).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
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
    }
}
