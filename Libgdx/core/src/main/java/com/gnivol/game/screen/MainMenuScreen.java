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
    private Texture backgroundTexture;

    public MainMenuScreen(GnivolGame game) {
        super(game);
    }

    @Override
    public void show() {
        game.getStage().clear();
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        backgroundTexture = new Texture(Gdx.files.internal("images/final_login_bg.png"));

        com.gnivol.game.system.FontManager fm = game.getFontManager();

        TextButton.TextButtonStyle titleStyle = new TextButton.TextButtonStyle();
        titleStyle.font = fm.fontTitle;
        titleStyle.fontColor = Color.WHITE;
        titleStyle.overFontColor = Color.YELLOW;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = fm.fontButton;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.YELLOW;

        Table table = new Table();
        table.setFillParent(true);
        table.left().bottom();
        table.padLeft(125f);
        table.padBottom(150f);

        TextButton newGameBtn = new TextButton("New Game", buttonStyle);
        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.resetGameState();
                game.setScreen(new GameScreen(game));
            }
        });

        // Nút Load Game
        TextButton loadGameBtn = new TextButton("Load Game", buttonStyle);
        loadGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isLoaded = game.loadGame();
                if (isLoaded) {
                    game.setScreen(new GameScreen(game));
                } else {
                    Gdx.app.log("MainMenu", "No data save to load!");
                    // TODO: Bạn có thể code thêm một dòng thông báo chữ đỏ "No Save Data" lướt qua màn hình ở đây
                }
            }
        });

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
        table.add(loadGameBtn).left().width(btnWidth).padBottom(25f).row();
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
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}
