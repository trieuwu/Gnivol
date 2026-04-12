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

/**
 * Man hinh cai dat — 2 slider volume (nhac nen + hieu ung) + nut Quay lai.
 * Dung LibGDX default Skin (uiskin) de tao UI nhanh.
 */
public class SettingScreen extends BaseScreen {

    // Screen truoc do — de quay lai khi nhan "Quay lai"
    private final Screen previousScreen;

    // AudioManager — doc/ghi gia tri volume
    private final AudioManager audioManager;

    // Stage chua UI
    private Stage stage;

    // Skin mac dinh cua LibGDX (cho Slider style)
    private Skin skin;

    // Font IM Fell English
    private BitmapFont titleFont;
    private BitmapFont labelFont;
    private FreeTypeFontGenerator fontGenerator;

    // Label hien thi % volume hien tai
    private Label musicValueLabel;
    private Label sfxValueLabel;

    /**
     * @param game           GnivolGame chinh
     * @param previousScreen screen truoc do (PauseScreen hoac MainMenu)
     */
    public SettingScreen(GnivolGame game, Screen previousScreen) {
        super(game);
        this.previousScreen = previousScreen;
        this.audioManager = game.getAudioManager();
    }

    @Override
    public void show() {
        // Load Skin mac dinh (dung cho Slider style)
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // --- Font IM Fell English ---
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellEnglish.ttf"));

        // Font tieu de — co 42
        FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParam.size = 42;
        titleParam.color = Color.WHITE;
        titleParam.borderWidth = 2f;
        titleParam.borderColor = Color.DARK_GRAY;
        titleFont = fontGenerator.generateFont(titleParam);

        // Font label + button — co 24
        FreeTypeFontGenerator.FreeTypeFontParameter labelParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        labelParam.size = 24;
        labelParam.color = Color.WHITE;
        labelParam.borderWidth = 1f;
        labelParam.borderColor = Color.BLACK;
        labelFont = fontGenerator.generateFont(labelParam);

        // --- Style dung font IM Fell English ---
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label.LabelStyle labelStyle = new Label.LabelStyle(labelFont, Color.WHITE);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = labelFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;

        // Tao Stage
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // --- Layout ---
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Tieu de
        table.add(new Label("SETTINGS", titleStyle)).colspan(3).padBottom(60f).row();

        // --- Slider Music ---
        table.add(new Label("Music:", labelStyle)).padRight(20f);

        // Tao slider: min=0, max=1, buoc nhay=0.01, nam ngang
        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(audioManager.getMusicVolume()); // set gia tri ban dau
        table.add(musicSlider).width(300f).padRight(10f);

        // Label hien thi % hien tai
        musicValueLabel = new Label((int)(audioManager.getMusicVolume() * 100) + "%", labelStyle);
        table.add(musicValueLabel).width(50f).row();

        // Lang nghe khi keo slider → cap nhat AudioManager + label
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = musicSlider.getValue();
                audioManager.setMusicVolume(value);
                musicValueLabel.setText((int)(value * 100) + "%");
            }
        });

        // --- Slider SFX ---
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
        if (titleFont != null) titleFont.dispose();
        if (labelFont != null) labelFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
    }
}
