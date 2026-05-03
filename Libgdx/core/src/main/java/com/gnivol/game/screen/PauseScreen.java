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
    private boolean isInitialized = false;

    public PauseScreen(GnivolGame game, GameScreen gameScreen) {
        super(game);
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        if (!isInitialized) {
            stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
            dimRenderer = new ShapeRenderer();

            com.gnivol.game.system.FontManager fm = game.getFontManager();

            Label.LabelStyle titleStyle = new Label.LabelStyle(fm.fontTitle, Color.WHITE);
            TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.font = fm.fontButton;
            buttonStyle.fontColor = Color.WHITE;
            buttonStyle.overFontColor = Color.YELLOW;

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

            isInitialized = true;
        }
        stage.getRoot().getColor().a = 0f;
        stage.getRoot().addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.5f));
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        if (gameScreen != null) {
            gameScreen.render(0f);
        }
        stage.getViewport().apply();
        dimRenderer.setProjectionMatrix(stage.getCamera().combined);

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        dimRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Color colorBottom = new Color(0f, 0f, 0f, 0.9f);
        Color colorTop = new Color(0f, 0f, 0f, 0.4f);

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

    public GameScreen getGameScreen() {
        return gameScreen;
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (dimRenderer != null) dimRenderer.dispose();
    }
}
