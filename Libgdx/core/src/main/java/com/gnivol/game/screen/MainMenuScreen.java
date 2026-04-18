package com.gnivol.game.screen;

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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

public class MainMenuScreen extends BaseScreen {

    private Stage stage;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private FreeTypeFontGenerator fontGenerator;

    public MainMenuScreen(GnivolGame game) {
        super(game);
    }

    @Override
    public void show() {
        game.getStage().clear();

        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        // FreeType font
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellEnglish.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParam.size = 64;
        titleParam.color = Color.WHITE;
        titleParam.borderWidth = 2f;
        titleParam.borderColor = Color.DARK_GRAY;
        titleFont = fontGenerator.generateFont(titleParam);

        FreeTypeFontGenerator.FreeTypeFontParameter btnParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        btnParam.size = 28;
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

        table.add(new Label("GNIVOL", titleStyle)).padBottom(80f).row();

        TextButton newGameBtn = new TextButton("New Game", buttonStyle);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.resetGameState();
                game.setScreen(new GameScreen(game));
            }
        });
        table.add(newGameBtn).padBottom(25f).row();

        TextButton loadBtn = new TextButton("Load Game", buttonStyle);
        table.add(loadBtn).padBottom(25f).row();

        TextButton settingBtn = new TextButton("Settings", buttonStyle);
        table.add(settingBtn).padBottom(25f).row();

        TextButton quitBtn = new TextButton("Quit", buttonStyle);
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(quitBtn).padBottom(25f).row();

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
    }
}
