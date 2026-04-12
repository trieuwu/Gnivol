package com.gnivol.game.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.Constants;

public class PauseScreen extends BaseScreen {
    private final GameScreen gameScreen;
    private Stage stage;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private FreeTypeFontGenerator fontGenerator;

    private static final String VIETNAMESE_CHARS =
            "aăâbcdđeêfghijklmnoôơpqrstuưvwxyz"
                    + "AĂÂBCDĐEÊFGHIJKLMNOÔƠPQRSTUƯVWXYZ"
                    + "àáảãạằắẳẵặầấẩẫậèéẻẽẹềếểễệìíỉĩịòóỏõọồốổỗộờớởỡợùúủũụừứửữựỳýỷỹỵ"
                    + "ÀÁẢÃẠẰẮẲẴẶẦẤẨẪẬÈÉẺẼẸỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌỒỐỔỖỘỜỚỞỠỢÙÚỦŨỤỪỨỬỮỰỲÝỶỸỴ"
                    + "0123456789.,;:!?'\"-()[]{}…—–/\\@#$%^&*+=<>~`| ";

    public PauseScreen(GnivolGame game, GameScreen gameScreen) {
        super(game);
        this.gameScreen = gameScreen;
    }

    public void show() {

        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // FreeType font
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParam.size = 64;
        titleParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        titleParam.color = Color.WHITE;
        titleParam.borderWidth = 2f;
        titleParam.borderColor = Color.DARK_GRAY;
        titleFont = fontGenerator.generateFont(titleParam);

        FreeTypeFontGenerator.FreeTypeFontParameter btnParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        btnParam.size = 28;
        btnParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + VIETNAMESE_CHARS;
        btnParam.color = Color.WHITE;
        btnParam.borderWidth = 1f;
        btnParam.borderColor = Color.BLACK;
        buttonFont = fontGenerator.generateFont(btnParam);

        // Styles
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);

        // Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(new Label("TẠM DỪNG", titleStyle)).padBottom(80f).row();

        TextButton resumeGameBtn = new TextButton("Resume Game", buttonStyle);
        resumeGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(gameScreen);
            }
        });
        table.add(resumeGameBtn).padBottom(25f).row();

//        TextButton loadBtn = new TextButton("Load Game", buttonStyle);
//        table.add(loadBtn).padBottom(25f).row();

        TextButton settingBtn = new TextButton("Settings", buttonStyle);
        settingBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingScreen(game, PauseScreen.this));
            }
        });
        table.add(settingBtn).padBottom(25f).row();

        TextButton quitBtn = new TextButton("Quit", buttonStyle);
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.dispose();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        table.add(quitBtn).padBottom(25f).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        stage.act(delta);   // cập nhật UI
        stage.draw();       // vẽ UI lên màn hình
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
    }
}

