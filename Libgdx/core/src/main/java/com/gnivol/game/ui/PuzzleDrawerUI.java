package com.gnivol.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gnivol.game.system.puzzle.PuzzleManager;
import com.gnivol.game.system.rs.RSEvent;
import com.gnivol.game.system.rs.RSEventType;
import com.gnivol.game.system.rs.RSManager;

public class PuzzleDrawerUI {
    private Group rootGroup;
    private Image dimBackground;
    private Label[] digitLabels;
    private int[] digits = {0, 0, 0};

    private final PuzzleManager puzzleManager;
    private final RSManager rsManager;
    private final InventoryUI inventoryUI;

    public interface PuzzleResultListener {
        void onPuzzleSolved(String puzzleId);
    }
    private PuzzleResultListener listener;

    public PuzzleDrawerUI(Skin skin, Stage stage, PuzzleManager puzzleManager, RSManager rsManager, InventoryUI inventoryUI) {
        this.puzzleManager = puzzleManager;
        this.rsManager = rsManager;
        this.inventoryUI = inventoryUI;

        // ================= 1. TẠO LỚP KÍNH "TÀNG HÌNH" CHẶN CLICK =================
        Pixmap dimPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPix.setColor(new Color(0f, 0f, 0f, 0f));
        dimPix.fill();
        TextureRegionDrawable dimDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(dimPix)));
        dimPix.dispose();

        dimBackground = new Image(dimDrawable);
        dimBackground.setFillParent(true);
        dimBackground.setVisible(false);
        dimBackground.setTouchable(Touchable.enabled);
        dimBackground.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Nuốt click
            }
        });
        stage.addActor(dimBackground);

        // ================= 2. GROUP CHÍNH CHỨA Ổ KHÓA =================
        float assetWidth = 800f;
        float assetHeight = 450f;

        rootGroup = new Group();
        rootGroup.setSize(assetWidth, assetHeight);
        rootGroup.setPosition((1280 - assetWidth) / 2f, (720 - assetHeight) / 2f);
        rootGroup.setVisible(false);

        Texture bgTex = new Texture(Gdx.files.internal("images/locker.png"));
        Image bgImage = new Image(bgTex);
        bgImage.setSize(assetWidth, assetHeight);
        rootGroup.addActor(bgImage);


        // ================= TẠO 3 Ô SỐ VÀ ANIMATION =================
        digitLabels = new Label[3];
        float[] digitX = {215f, 379f, 542f};
        float digitY = 199f;

        final Label.LabelStyle textStyle = new Label.LabelStyle(skin.getFont("default-font"), new Color(0.05f, 0.05f, 0.05f, 1f));

        for (int i = 0; i < 3; i++) {
            final int index = i;
            final float baseX = digitX[i];
            final float baseY = digitY;

            digitLabels[i] = new Label("0", textStyle);
            digitLabels[i].setFontScale(4.0f);
            digitLabels[i].setPosition(baseX, baseY);
            rootGroup.addActor(digitLabels[i]);

            Image clickArea = new Image();
            clickArea.setSize(100f, 150f);
            clickArea.setPosition(digitX[i] - 30f, digitY - 50f);
            clickArea.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int oldDigit = digits[index];

                    digits[index] = (digits[index] + 1) % 10;
                    int newDigit = digits[index];

                    Label oldLabel = new Label(String.valueOf(oldDigit), textStyle);
                    oldLabel.setFontScale(4.0f);
                    oldLabel.setPosition(baseX, baseY);
                    rootGroup.addActor(oldLabel);

                    oldLabel.addAction(Actions.sequence(
                        Actions.parallel(
                            Actions.moveBy(0, 35f, 0.15f),
                            Actions.fadeOut(0.15f)
                        ),
                        Actions.removeActor()
                    ));

                    digitLabels[index].setText(String.valueOf(newDigit));
                    digitLabels[index].clearActions();
                    digitLabels[index].setPosition(baseX, baseY - 35f);
                    digitLabels[index].getColor().a = 0f;

                    digitLabels[index].addAction(Actions.parallel(
                        Actions.moveTo(baseX, baseY, 0.15f),
                        Actions.fadeIn(0.15f)
                    ));
                }
            });
            rootGroup.addActor(clickArea);
        }

        // ================= NÚT SUBMIT TÀNG HÌNH =================
        Image submitArea = new Image();
        submitArea.setSize(220f, 75f);
        submitArea.setPosition(145f, 40f);
        submitArea.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                checkAnswer();
            }
        });
        rootGroup.addActor(submitArea);

        // ================= NÚT CLOSE TÀNG HÌNH =================
        Image closeArea = new Image();
        closeArea.setSize(220f, 75f);
        closeArea.setPosition(440f, 40f);
        closeArea.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        rootGroup.addActor(closeArea);

        stage.addActor(rootGroup);
    }

    public void setListener(PuzzleResultListener listener) {
        this.listener = listener;
    }

    public boolean isOpen() {
        return rootGroup != null && rootGroup.isVisible();
    }

    public void show() {
        if (inventoryUI != null) {
            inventoryUI.setVisible(false);
        }

        float[] digitX = {215f, 379f, 542f};
        float digitY = 199f;

        for (int i = 0; i < 3; i++) {
            digits[i] = 0;
            digitLabels[i].setText("0");
            digitLabels[i].clearActions();
            digitLabels[i].getColor().a = 1f;
            digitLabels[i].setPosition(digitX[i], digitY);
        }

        dimBackground.setVisible(true);

        rootGroup.setVisible(true);
        rootGroup.getColor().a = 0f;
        rootGroup.addAction(Actions.fadeIn(0.3f));
    }

    public void hide() {
        if (inventoryUI != null) {
            inventoryUI.setVisible(true);
        }

        dimBackground.setVisible(false);
        rootGroup.addAction(Actions.sequence(Actions.fadeOut(0.3f), Actions.visible(false)));

        if (rootGroup.getStage() != null) {
            rootGroup.getStage().setKeyboardFocus(null);
            rootGroup.getStage().setScrollFocus(null);
        }
    }

    private void checkAnswer() {
        String answer = digits[0] + "" + digits[1] + "" + digits[2];

        if (puzzleManager.submitAnswer("puzzle_drawer", answer)) {
            rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_SOLVED, 10, "puzzle_drawer"));
            hide();
            if (listener != null) {
                listener.onPuzzleSolved("puzzle_drawer");
            }
        } else {
            // LẤY SỐ LẦN SAI TỪ HỆ THỐNG LƯU TRỮ
            if(puzzleManager.getFailCount("puzzle_drawer") < 2) {
                rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_FAILED, -5, "puzzle_drawer"));
                puzzleManager.incrementFailCount("puzzle_drawer"); // CẬP NHẬT SỐ LẦN SAI
            }
            rootGroup.addAction(Actions.sequence(
                Actions.color(Color.RED, 0.1f),
                Actions.moveBy(20, 0, 0.05f), Actions.moveBy(-40, 0, 0.05f), Actions.moveBy(20, 0, 0.05f),
                Actions.moveBy(20, 0, 0.05f), Actions.moveBy(-40, 0, 0.05f), Actions.moveBy(20, 0, 0.05f),
                Actions.color(Color.WHITE, 0.2f)
            ));
        }
    }
}
