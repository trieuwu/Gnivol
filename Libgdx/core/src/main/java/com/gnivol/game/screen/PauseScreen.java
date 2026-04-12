package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

public class PauseScreen extends BaseScreen {
    private final GameScreen gameScreen;
    private Stage stage;
    private ShapeRenderer dimRenderer;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private FreeTypeFontGenerator fontGenerator;

    public PauseScreen(GnivolGame game, GameScreen gameScreen) {
        super(game);
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        dimRenderer = new ShapeRenderer();

        // IM Fell English font
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/IMFellEnglish.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParam.size = 52;
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

        // Style
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;

        // Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(new Label("PAUSED", titleStyle)).padBottom(60f).row();

        TextButton resumeBtn = new TextButton("Resume Game", buttonStyle);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(gameScreen);
            }
        });
        table.add(resumeBtn).padBottom(25f).row();

        TextButton settingBtn = new TextButton("Settings", buttonStyle);
        settingBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingScreen(game, PauseScreen.this));
            }
        });
        table.add(settingBtn).padBottom(25f).row();

        TextButton quitBtn = new TextButton("Quit to Menu", buttonStyle);
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.dispose();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        table.add(quitBtn).padBottom(25f).row();

        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);

        // ESC → resume
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(gameScreen);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        // Nền mờ đen 50%
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        dimRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dimRenderer.setColor(0f, 0f, 0f, 0.5f);
        dimRenderer.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
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
    public void dispose() {
        if (stage != null) stage.dispose();
        if (dimRenderer != null) dimRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
    }
}
