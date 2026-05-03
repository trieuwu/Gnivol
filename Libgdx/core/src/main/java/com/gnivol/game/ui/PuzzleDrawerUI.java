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
import com.badlogic.gdx.utils.Align;
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

    private int failCount = 0;

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

        // --- CÀI ĐẶT MÀU VÀ FONT CHO CHỮ ---
        Color fontColor = Color.WHITE;


        // ================= TẠO 3 Ô SỐ VÀ ANIMATION =================
        digitLabels = new Label[3];
        float[] digitX = {192f, 352f, 512f};
        float digitY = 208f;

        final Label.LabelStyle textStyle = new Label.LabelStyle(skin.getFont("default-font"), new Color(0.05f, 0.05f, 0.05f, 1f));

        for (int i = 0; i < 3; i++) {
            final int index = i;
            final float baseX = digitX[i] + 15;
            final float baseY = digitY + 5;

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
                    // Lưu lại số cũ trước khi tăng
                    int oldDigit = digits[index];

                    // Cập nhật số mới
                    digits[index] = (digits[index] + 1) % 10;
                    int newDigit = digits[index];

                    // --- 1. ANIMATION CHO SỐ CŨ (Bay lên và mờ đi) ---
                    Label oldLabel = new Label(String.valueOf(oldDigit), textStyle);
                    oldLabel.setFontScale(4.0f);
                    oldLabel.setPosition(baseX, baseY);
                    rootGroup.addActor(oldLabel);

                    oldLabel.addAction(Actions.sequence(
                        Actions.parallel(
                            Actions.moveBy(0, 35f, 0.15f), // Bay lên 35px trong 0.15 giây
                            Actions.fadeOut(0.15f)         // Mờ dần
                        ),
                        Actions.removeActor()              // Biến mất hoàn toàn thì tự xóa khỏi bộ nhớ
                    ));

                    // --- 2. ANIMATION CHO SỐ MỚI (Từ dưới chui lên) ---
                    digitLabels[index].setText(String.valueOf(newDigit));
                    digitLabels[index].clearActions(); // Hủy các hiệu ứng cũ nếu người chơi click quá nhanh
                    digitLabels[index].setPosition(baseX, baseY - 35f); // Bắt đầu ở vị trí thấp hơn 35px
                    digitLabels[index].getColor().a = 0f; // Trong suốt lúc bắt đầu

                    digitLabels[index].addAction(Actions.parallel(
                        Actions.moveTo(baseX, baseY, 0.15f), // Trượt về vị trí gốc
                        Actions.fadeIn(0.15f)                // Rõ dần
                    ));
                }
            });
            rootGroup.addActor(clickArea);
        }

        // ================= NÚT SUBMIT =================
        Image submitArea = new Image();
        submitArea.setSize(180f, 60f);
        submitArea.setPosition(182f, 48f);
        submitArea.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                checkAnswer();
            }
        });
        rootGroup.addActor(submitArea);

        Label submitLabel = new Label("SUBMIT", skin, "default");
        submitLabel.setColor(fontColor);
        submitLabel.setFontScale(1.4f);
        submitLabel.setSize(180f, 60f);
        submitLabel.setPosition(182f, 48f);
        submitLabel.setAlignment(Align.center);
        submitLabel.setTouchable(Touchable.disabled);
        rootGroup.addActor(submitLabel);

        // ================= NÚT CLOSE =================
        Image closeArea = new Image();
        closeArea.setSize(180f, 60f);
        closeArea.setPosition(423f, 48f);
        closeArea.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        rootGroup.addActor(closeArea);

        Label closeLabel = new Label("CLOSE", skin, "default");
        closeLabel.setColor(fontColor);
        closeLabel.setFontScale(1.4f);
        closeLabel.setSize(180f, 60f);
        closeLabel.setPosition(423f, 48f);
        closeLabel.setAlignment(Align.center);
        closeLabel.setTouchable(Touchable.disabled);
        rootGroup.addActor(closeLabel);

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

        for (int i = 0; i < 3; i++) {
            digits[i] = 0;
            digitLabels[i].setText("0");
            digitLabels[i].clearActions();
            digitLabels[i].getColor().a = 1f;
            // Trả số về vị trí gốc
            digitLabels[i].setPosition(190f + i * 160f + 15, 208f + 5);
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
            if(failCount < 2) {
                rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_FAILED, -5, "puzzle_drawer"));
                failCount++;
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
