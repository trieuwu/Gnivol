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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

public class MainMenuScreen extends BaseScreen {

    private Stage stage;
    private Texture backgroundTexture;
    private Texture helpTex;
    private long lastClickTime = 0;
    private Texture vignetteTexture;
    private boolean isInitialized = false;

    public MainMenuScreen(GnivolGame game) {
        super(game);
    }

    private Texture createVignetteTexture(int width, int height) {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxDist = (float) Math.sqrt(centerX * centerX + centerY * centerY);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float dist = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                float alpha = dist / maxDist;

                alpha = (float) Math.pow(alpha, 4);
                if (alpha > 0.15f) alpha = 0.15f;

                pixmap.setColor(new Color(0f, 0f, 0f, alpha));
                pixmap.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return tex;
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createConfirmBackground(int width, int height) {
        com.badlogic.gdx.graphics.Pixmap bgPix = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        float centerX = width / 2f;

        int startY = 40;
        int endY = height - 63;

        for (int x = 0; x < width; x++) {
            float distanceToCenter = Math.abs(x - centerX);
            float alpha = 1f - (distanceToCenter / centerX);
            float finalAlpha = Math.max(0f, alpha * 0.85f);

            bgPix.setColor(new Color(0f, 0f, 0f, finalAlpha));

            bgPix.drawLine(x, startY, x, endY);
        }

        Texture texture = new Texture(bgPix);
        bgPix.dispose();

        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawable =
            new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));

        drawable.setMinWidth(width);
        drawable.setMinHeight(height);

        return drawable;
    }

    @Override
    public void show() {
        if (!isInitialized) {
            game.getStage().clear();
            stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
            backgroundTexture = new Texture(Gdx.files.internal("images/final_login_bg.png"));

            com.badlogic.gdx.scenes.scene2d.ui.Image bgImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(backgroundTexture);
            bgImage.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
            bgImage.setOrigin(Align.center);
            stage.addActor(bgImage);

            vignetteTexture = createVignetteTexture(512, 512);
            com.badlogic.gdx.scenes.scene2d.ui.Image vignetteImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(vignetteTexture);
            vignetteImage.setSize(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
            vignetteImage.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            stage.addActor(vignetteImage);

            // Phát nhạc menu
            if (game.getAudioManager() != null) {
                game.getAudioManager().playBGM("menu_bgm");
            }

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

                    if (Gdx.files.external(".gnivol/save_slot_1.json").exists()) {
                        showNewGameConfirmDialog(buttonStyle);
                    } else {
                        startNewGame();
                    }
                }
            });

            // Nút Load Game
            TextButton loadGameBtn = new TextButton("Load Game", buttonStyle);
            loadGameBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (com.badlogic.gdx.utils.TimeUtils.millis() - lastClickTime < 500) return;
                    lastClickTime = com.badlogic.gdx.utils.TimeUtils.millis();

                    boolean isLoaded = game.loadGame();
                    if (isLoaded) {
                        game.isLoadedGame = true;

                        if (game.getScreenFader() != null) {
                            game.getScreenFader().startFade(() -> {
                                Gdx.app.postRunnable(() -> {
                                    game.setScreen(new LoadingScreen(game, LoadingScreen.LoadingTarget.LOAD_GAME, null));
                                    MainMenuScreen.this.dispose();
                                });
                            });
                        } else {
                            game.setScreen(new LoadingScreen(game, LoadingScreen.LoadingTarget.LOAD_GAME, null));
                            MainMenuScreen.this.dispose();
                        }
                    } else {
                        showNoSaveDataDialog();
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

            try {
                helpTex = new Texture(Gdx.files.internal("images/UI/thinking_final.png"));
                com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable helpDrawable = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(helpTex));
                com.badlogic.gdx.scenes.scene2d.ui.ImageButton helpBtn = new com.badlogic.gdx.scenes.scene2d.ui.ImageButton(helpDrawable);

                Table helpTable = new Table();
                helpTable.setFillParent(true);
                helpTable.top().right().pad(30f);
                helpTable.add(helpBtn).size(60, 60);

                helpBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (com.badlogic.gdx.utils.TimeUtils.millis() - lastClickTime < 500) return;
                        lastClickTime = com.badlogic.gdx.utils.TimeUtils.millis();
                        showHelpOverlay();
                    }
                });
                stage.addActor(helpTable);
            } catch (Exception e) {
                Gdx.app.error("MainMenuScreen", "Không tìm thấy file: images/UI/help_icon.png", e);
            }
            if (game.getScreenFader() != null) {
                game.getScreenFader().startFadeIn();
            }
            isInitialized = true;
        }
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();


        if (game.getScreenFader() != null) {
            game.getScreenFader().update(delta);
            game.getScreenFader().render();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createDialogBackground(int width, int height) {
        com.badlogic.gdx.graphics.Pixmap bgPix = new com.badlogic.gdx.graphics.Pixmap(width, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        float centerX = width / 2f;

//        int startY = 40;
//        int endY = 240;

        for (int x = 0; x < width; x++) {
            float distanceToCenter = Math.abs(x - centerX);
            float alpha = 1f - (distanceToCenter / centerX);
            float finalAlpha = Math.max(0f, alpha * 0.85f); // Ngăn alpha bị âm

            bgPix.setColor(new Color(0f, 0f, 0f, finalAlpha));
            bgPix.drawLine(x, 0, x, height);
        }

        Texture texture = new Texture(bgPix);
        bgPix.dispose();

        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable drawable =
            new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));

        drawable.setMinWidth(width);
        drawable.setMinHeight(height);

        return drawable;
    }


    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createColoredCircleDrawable(Color color) {
        int size = 80;
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0));
        pixmap.fill();
        pixmap.setColor(color);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
        com.badlogic.gdx.graphics.Texture texture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));
    }

    private void showNewGameConfirmDialog(TextButton.TextButtonStyle originalStyle) {
        com.badlogic.gdx.graphics.Pixmap dimPix = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        dimPix.setColor(new Color(0f, 0f, 0f, 0.7f));
        dimPix.fill();

        final Texture dimTex = new Texture(dimPix);
        dimPix.dispose();

        final Table overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(dimTex)));
        overlayTable.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        stage.addActor(overlayTable);

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont = game.getFontManager().fontVietnamese;
        windowStyle.background = createConfirmBackground(1000, 300);

        com.badlogic.gdx.scenes.scene2d.ui.Dialog dialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("", windowStyle) {

            @Override
            public float getPrefWidth() { return 1000f; }
            @Override
            public float getPrefHeight() { return 300f; }

            @Override
            protected void result(Object object) {
                overlayTable.remove();
                dimTex.dispose();
                if ((Boolean) object) {
                    startNewGame();
                }
            }
        };

        Color darkGreen = new Color(0, 0.25f, 0, 1f);
        Color darkRed = new Color(0.35f, 0, 0, 1f);

        TextButton.TextButtonStyle yesStyle = new TextButton.TextButtonStyle();
        yesStyle.font = game.getFontManager().fontVietnamese;
        yesStyle.up = createColoredCircleDrawable(darkGreen);
        yesStyle.down = createColoredCircleDrawable(new Color(0, 0.1f, 0, 1f));

        TextButton.TextButtonStyle noStyle = new TextButton.TextButtonStyle();
        noStyle.font = game.getFontManager().fontVietnamese;
        noStyle.up = createColoredCircleDrawable(darkRed);
        noStyle.down = createColoredCircleDrawable(new Color(0.15f, 0, 0, 1f));

        dialog.getContentTable().clearChildren();
        Label msg = new Label("Starting a new game will overwrite your current game.\nAre you sure?",
            new Label.LabelStyle(game.getFontManager().fontVietnamese, Color.WHITE));
        msg.setAlignment(Align.center);


        dialog.getContentTable().add(msg).expand().center().padTop(40);


        dialog.getButtonTable().clearChildren();
        TextButton btnYes = new TextButton("V", yesStyle);
        TextButton btnNo = new TextButton("X", noStyle);


        dialog.getButtonTable().add(btnNo).size(85).expandX().right().padRight(120).padBottom(20);
        dialog.getButtonTable().add(btnYes).size(85).expandX().left().padLeft(120).padBottom(20);

        dialog.getCell(dialog.getContentTable()).expand().fill();
        dialog.getCell(dialog.getButtonTable()).expandX().fillX().bottom();

        dialog.setObject(btnYes, true);
        dialog.setObject(btnNo, false);

        dialog.show(stage);
    }

    private void showNoSaveDataDialog() {
        com.badlogic.gdx.graphics.Pixmap dimPix = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        dimPix.setColor(new Color(0f, 0f, 0f, 0.7f));
        dimPix.fill();

        final Texture dimTex = new Texture(dimPix);
        dimPix.dispose();

        final Table overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(dimTex)));
        overlayTable.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        stage.addActor(overlayTable);

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont = game.getFontManager().fontVietnamese;
        windowStyle.background = createDialogBackground(1000, 300);

        final com.badlogic.gdx.scenes.scene2d.ui.Dialog dialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("", windowStyle) {
            @Override
            public float getPrefWidth() { return 1000f; }
            @Override
            public float getPrefHeight() { return 300f; }
        };

        dialog.getContentTable().clearChildren();
        Label msg = new Label("No save data found!",
            new Label.LabelStyle(game.getFontManager().fontVietnamese, Color.WHITE));
        msg.setAlignment(Align.center);


        dialog.getContentTable().add(msg).expand().center();
        dialog.getButtonTable().clearChildren();

        dialog.getCell(dialog.getContentTable()).expand().fill();
        dialog.show(stage);

        Runnable closeDialogTask = new Runnable() {
            boolean isClosed = false;
            @Override
            public void run() {
                if (!isClosed) {
                    isClosed = true;
                    dialog.hide();
                    overlayTable.remove();
                    dimTex.dispose();
                }
            }
        };

        dialog.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(3f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.run(closeDialogTask)
        ));

        overlayTable.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialogTask.run();
            }
        });

        dialog.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialogTask.run();
            }
        });
    }

    private void showHelpOverlay() {
        com.badlogic.gdx.graphics.Pixmap dimPix = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        dimPix.setColor(new Color(0f, 0f, 0f, 0.75f));
        dimPix.fill();
        final Texture dimTex = new Texture(dimPix);
        dimPix.dispose();

        final Table overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(dimTex)));
        overlayTable.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        stage.addActor(overlayTable);

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont = game.getFontManager().fontVietnamese;
        windowStyle.background = createDialogBackground(800, 500);

        final com.badlogic.gdx.scenes.scene2d.ui.Dialog dialog = new com.badlogic.gdx.scenes.scene2d.ui.Dialog("", windowStyle) {
            @Override public float getPrefWidth() { return 800f; }
            @Override public float getPrefHeight() { return 500f; }
        };

        Label.LabelStyle titleStyle = new Label.LabelStyle(game.getFontManager().fontTitle, Color.valueOf("#8B0000")); // Đổi sang màu đỏ máu (Blood Red) thay vì vàng gold
        Label titleLabel = new Label("SURVIVAL GUIDE", titleStyle);

        Label.LabelStyle textStyle = new Label.LabelStyle(game.getFontManager().fontVietnamese, Color.LIGHT_GRAY); // Chữ xám nhạt u ám

        // Văn bản tiếng Anh với phong cách rùng rợn
        String helpText =
            "\"They tell you it's just a dream. They lie.\"\n\n" +
                "You are trapped in a place where reality is fragile. The shadows hold forgotten secrets, and every object you touch carries a curse from those who came before you.\n\n" +
                "To survive, you must scavenge for fragments of the truth. Examine them closely. Some artifacts are broken and must be merged together to unlock doors that should have stayed closed.\n\n" +
                "But above all, guard your mind. Your Reality Stability (RS) is constantly slipping. Let the terror consume you (100), and your sanity will shatter. Lose all emotion (0), and you will become one of the hollow shells wandering in the dark.\n\n" +
                "Tread carefully. The dark is listening.";

        Label contentLabel = new Label(helpText, textStyle);
        contentLabel.setWrap(true);
        contentLabel.setAlignment(Align.left);

        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scrollPane = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(contentLabel);
        scrollPane.setScrollingDisabled(true, false); // Khóa cuộn ngang, chỉ cho phép cuộn dọc
        scrollPane.setFadeScrollBars(false);

        TextButton.TextButtonStyle closeStyle = new TextButton.TextButtonStyle();
        closeStyle.font = game.getFontManager().fontTitle;
        closeStyle.fontColor = Color.RED;
        closeStyle.overFontColor = Color.WHITE;
        TextButton closeBtn = new TextButton("X", closeStyle);

        Runnable closeTask = () -> {
            dialog.hide();
            overlayTable.remove();
            dimTex.dispose();
        };

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeTask.run();
            }
        });

        Table topRow = new Table();
        topRow.add(titleLabel).expandX().center().padLeft(40f);
        topRow.add(closeBtn).right().padRight(20f);

        com.badlogic.gdx.graphics.Pixmap linePix = new com.badlogic.gdx.graphics.Pixmap(1, 2, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        linePix.setColor(new Color(0.8f, 0.7f, 0.4f, 0.6f));
        linePix.fill();
        Texture lineTex = new Texture(linePix);
        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable lineDrawable =
            new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(lineTex));
        linePix.dispose();

        com.badlogic.gdx.scenes.scene2d.ui.Image separatorLine = new com.badlogic.gdx.scenes.scene2d.ui.Image(lineDrawable);

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeTask.run();
            }
        });

        dialog.clearListeners();
        dialog.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    if (com.badlogic.gdx.utils.TimeUtils.millis() - lastClickTime < 200) return true;
                    lastClickTime = com.badlogic.gdx.utils.TimeUtils.millis();
                    closeTask.run();
                    return true;
                }
                return false;
            }
        });

        dialog.getContentTable().clearChildren();
        dialog.getContentTable().add(topRow).fillX().padTop(20f).row();
        dialog.getContentTable().add(separatorLine).width(650).height(2).center().padTop(15f).padBottom(25f).row();
        dialog.getContentTable().add(scrollPane).expand().fill().padLeft(40f).padRight(40f).padBottom(40f);

        dialog.show(stage);
        stage.setKeyboardFocus(dialog);
        stage.setScrollFocus(scrollPane);
    }

    private void startNewGame() {
        game.getRsManager().reset();
        game.getInventoryManager().clearInventory();
        game.getFlagManager().reset();

        if (game.getGameState() != null) game.getGameState().reset();
        if (game.getSceneManager() != null) game.getSceneManager().reset();
        if (game.getPuzzleManager() != null) game.getPuzzleManager().reset();

        if (game.getAutoSaveManager() != null) game.getAutoSaveManager().setGameOver(false);

        com.badlogic.gdx.files.FileHandle saveFile = Gdx.files.external(".gnivol/save_slot_1.json");
        if (saveFile.exists()) {
            saveFile.delete();
        }
        game.isLoadedGame = false;

        if (game.getScreenFader() != null) {
            game.getScreenFader().startFade(() -> {
                Gdx.app.postRunnable(() -> {
                    game.setScreen(new LoadingScreen(game, LoadingScreen.LoadingTarget.NEW_GAME, null));
                    MainMenuScreen.this.dispose();
                });
            });
        } else {
            game.setScreen(new LoadingScreen(game, LoadingScreen.LoadingTarget.NEW_GAME, null));
            this.dispose();
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (helpTex != null) helpTex.dispose();
        if (vignetteTexture != null) vignetteTexture.dispose();
    }
}
