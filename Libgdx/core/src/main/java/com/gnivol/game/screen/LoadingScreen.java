package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

public class LoadingScreen extends BaseScreen {
    // Enum để định nghĩa các đích đến
    public enum LoadingTarget { NEW_GAME, LOAD_GAME, SLIDING_MINIGAME, LASER_MINIGAME }

    private Stage stage;
    private Label loadingLabel;
    private Label hintLabel;
    private float stateTime = 0f;
    private boolean isFadingOut = false;

    private final LoadingTarget target;
    private final GameScreen previousGameScreen;

    private boolean isTargetReady = false;
    private SlidingScreen preLoadedSliding;
    private LaserScreen preLoadedLaser;

    private final String[] HINTS = {
        "Thiên thượng thiên hạ, Duy Anh độc tôn",
        "Một số cánh cửa tốt nhất là không bao giờ nên mở. Với bạn thì là cửa sổ tâm hồn",
        "2822 8686 8866 MB, please...",
        "Mẹo qua minigame: Mẹo m bé",
        "Đã đi mọi phương trời nhưng người chỉ nhớ về Phương Anh.",
        "Chúc Thành 8.0+ ielts nhé :))) Vua Ren lít",
        "byvn.net/jVT0",
        "Có 4 ending và 1 true ending",
        "Tài trợ bởi Claude",
        "1907 6122 5710 14 Techcombank hjhjhj",
        "0906279876 MB, gimme bobux pls"
    };

    public LoadingScreen(GnivolGame game, LoadingTarget target, GameScreen previousGameScreen) {
        super(game);
        this.target = target;
        this.previousGameScreen = previousGameScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));
        Label.LabelStyle loadStyle = new Label.LabelStyle(game.getFontManager().fontVietnamese, Color.WHITE);
        Label.LabelStyle hintStyle = new Label.LabelStyle(game.getFontManager().fontVietnamese, Color.LIGHT_GRAY);

        loadingLabel = new Label("", loadStyle);
        loadingLabel.setAlignment(Align.center);

        String randomHint = HINTS[MathUtils.random(0, HINTS.length - 1)];
        hintLabel = new Label(randomHint, hintStyle);
        hintLabel.setAlignment(Align.center);
        hintLabel.setWrap(true);

        Table table = new Table();
        table.setFillParent(true);
        table.add(loadingLabel).expand().center().row();
        table.add(hintLabel).width(900).bottom().padBottom(60f);

        stage.addActor(table);

        if (target == LoadingTarget.SLIDING_MINIGAME) {
            preLoadedSliding = new SlidingScreen(game, previousGameScreen);
            preLoadedSliding.generateMapAsync(() -> {
                isTargetReady = true;
            });
        } else if (target == LoadingTarget.LASER_MINIGAME) {
            preLoadedLaser = new LaserScreen(game, previousGameScreen);
            preLoadedLaser.initAsync(() -> {
                isTargetReady = true;
            });
        } else {
            isTargetReady = true;
        }
        if (game.getScreenFader() != null) game.getScreenFader().startFadeIn();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stateTime += delta;

        String baseWord = "Loading";
        int typeIndex = Math.min((int)(stateTime * 10f), baseWord.length());
        String currentText = baseWord.substring(0, typeIndex);

        if (typeIndex == baseWord.length()) {
            int dotCount = (int) ((stateTime * 2f) % 4);
            for (int i = 0; i < dotCount; i++) currentText += ".";
        }
        loadingLabel.setText(currentText);

        float alpha = 0.6f + 0.4f * MathUtils.sin(stateTime * 6f);
        loadingLabel.setColor(1f, 1f, 1f, alpha);

        if (stateTime > 3.5f && isTargetReady && !isFadingOut) {
            isFadingOut = true;
            if (game.getScreenFader() != null) {
                game.getScreenFader().startFade(() -> {
                    Gdx.app.postRunnable(this::navigateToTarget);
                });
            } else {
                navigateToTarget();
            }
        }

        stage.act(delta);
        stage.draw();
        if (game.getScreenFader() != null) {
            game.getScreenFader().update(delta);
            game.getScreenFader().render();
        }
    }

    private void navigateToTarget() {
        switch (target) {
            case NEW_GAME:
            case LOAD_GAME:
                game.setScreen(new GameScreen(game));
                break;
            case SLIDING_MINIGAME:
                game.setScreen(preLoadedSliding);
                break;
            case LASER_MINIGAME:
                game.setScreen(preLoadedLaser);
                break;
        }
        this.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
