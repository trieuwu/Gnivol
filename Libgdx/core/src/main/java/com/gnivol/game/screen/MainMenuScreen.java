package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

public class MainMenuScreen extends BaseScreen {

    private Stage stage;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private FreeTypeFontGenerator fontGenerator;
    private Texture backgroundTexture;

    public MainMenuScreen(GnivolGame game) {
        super(game);
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        backgroundTexture = new Texture(Gdx.files.internal("textures/backgrounds/final_login_bg.png"));

        // FreeType font
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellEnglish.ttf"));

        //Options
        FreeTypeFontGenerator.FreeTypeFontParameter btnParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        btnParam.size = 28;
        btnParam.color = Color.WHITE;
        btnParam.borderWidth = 1f;
        btnParam.borderColor = Color.BLACK;
        buttonFont = fontGenerator.generateFont(btnParam);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;

        //Title
        FreeTypeFontGenerator.FreeTypeFontParameter btnTitle = new FreeTypeFontGenerator.FreeTypeFontParameter();
        btnTitle.size = 36;
        btnTitle.color = Color.WHITE;
        btnTitle.borderWidth = 1.21f;
        btnTitle.borderColor = Color.BLACK;
        titleFont = fontGenerator.generateFont(btnTitle);

        TextButton.TextButtonStyle titleStyle = new TextButton.TextButtonStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = Color.WHITE;
        titleStyle.overFontColor = Color.YELLOW;



        // Layout - căn trái, nằm phía dưới
        Table table = new Table();
        table.setFillParent(true);
        table.left().bottom();            // Căn sang trái + xuống dưới
        table.padLeft(125f);              // Cách mép trái 100px
        table.padBottom(150f);             // Cách mép dưới 80px



        // Nút New Game
        TextButton newGameBtn = new TextButton("New Game", buttonStyle);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });

        // Nút Load Game
        TextButton loadBtn = new TextButton("Load Game", buttonStyle);

        // Nút Settings
        TextButton settingBtn = new TextButton("Settings", buttonStyle);
        settingBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingScreen(game, MainMenuScreen.this));
            }
        });

        // Nút Quit
        TextButton quitBtn = new TextButton("Quit", buttonStyle);
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        TextButton Title = new TextButton("GNIVOL", titleStyle);

        float btnWidth = 25f;
        table.add(Title).left().width(btnWidth).padBottom(25f).row();
        table.add(newGameBtn).left().width(btnWidth).padBottom(25f).row();
        table.add(loadBtn).left().width(btnWidth).padBottom(25f).row();
        table.add(settingBtn).left().width(btnWidth).padBottom(25f).row();
        table.add(quitBtn).left().width(btnWidth).padBottom(25f).row();
        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.getBatch().begin();
        stage.getBatch().draw(backgroundTexture, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        stage.getBatch().end();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}
