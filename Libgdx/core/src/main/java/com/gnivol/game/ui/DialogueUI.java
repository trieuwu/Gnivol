package com.gnivol.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.model.dialogue.Choice;
import com.gnivol.game.model.dialogue.DialogueNode;
import com.gnivol.game.system.dialogue.DialogueEngine;
import com.gnivol.game.system.dialogue.GlitchTextRenderer;
import com.gnivol.game.system.rs.RSManager;

public class DialogueUI {
    private Table rootTable;
    private Label speakerLabel;
    private Label contentLabel;
    private Table choicesTable;

    // --- THÊM 3 BIẾN NÀY CHO KHUNG SUY NGHĨ ---
    private Table thoughtTable;
    private Label thoughtLabel;
    private Label activeTypingLabel; // Con trỏ quyết định gõ chữ vào khung nào

    private DialogueEngine engine;
    private Label.LabelStyle labelStyle;
    private TextButton.TextButtonStyle btnStyle;

    private String fullContentText = "";      // Chứa toàn bộ nội dung câu thoại
    private float typeTimer = 0f;             // Đồng hồ đếm ngược để gõ chữ
    private int typeIndex = 0;                // Vị trí chữ đang gõ tới đâu
    private final float TYPE_SPEED = 0.05f;   // 0.05 giây hiện 1 chữ
    private boolean isTyping = false;         // Đang gõ hay đã gõ xong
    private final RSManager rsManager;
    private GnivolGame game;
    // Callback khi dialogue kết thúc — GameScreen dùng để chain dialogue tiếp
    private Runnable onFinished;

    public DialogueUI(GnivolGame game, Stage stage, BitmapFont font, DialogueEngine engine, RSManager rsManager) {
        this.game = game;
        this.engine = engine;
        this.rsManager = rsManager;
        labelStyle = new Label.LabelStyle(font, Color.WHITE);

        Pixmap btnPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        btnPix.setColor(new Color(0.3f, 0.3f, 0.3f, 0.8f));
        btnPix.fill();
        TextureRegionDrawable btnBg = new TextureRegionDrawable(new TextureRegion(new Texture(btnPix)));
        btnPix.dispose();

        btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.up = btnBg;

        // 2. Tạo nền đen mờ 80% cho khung hội thoại
        int texWidth = 512; // Chiều ngang để tính toán dải màu (càng to càng mượt)
        int texHeight = 2;  // Chiều cao 2px là đủ vì LibGDX sẽ tự kéo giãn nó theo khung thoại
        Pixmap bgPix = new Pixmap(texWidth, texHeight, Pixmap.Format.RGBA8888);

        float centerX = texWidth / 2f;

        // Chạy vòng lặp tô màu từng pixel theo chiều ngang
        for (int x = 0; x < texWidth; x++) {
            // Tính khoảng cách từ điểm hiện tại đến tâm
            float distanceToCenter = Math.abs(x - centerX);

            // Tính độ mờ (Alpha): Ở giữa = 1 (Đậm), càng ra xa mép = 0 (Trong suốt)
            float alpha = 1f - (distanceToCenter / centerX);

            // Giới hạn max alpha là 0.85 (85%) để khung thoại không bị đen kịt mà vẫn nhìn xuyên được cảnh game
            float finalAlpha = alpha * 0.85f;

            // Set màu đen với độ mờ vừa tính được và vẽ 1 đường dọc
            bgPix.setColor(new Color(0f, 0f, 0f, finalAlpha));
            bgPix.drawLine(x, 0, x, texHeight);
        }

        TextureRegionDrawable transparentBlackBg = new TextureRegionDrawable(new TextureRegion(new Texture(bgPix)));
        bgPix.dispose();

        // 3. Setup UI Layout (Nằm dưới đáy màn hình)
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom().padBottom(30);

        // Khung nền chứa Text
        Table dialogBox = new Table();
        dialogBox.setBackground(transparentBlackBg);
        dialogBox.pad(20);

        // Khi click vào khung nền (nhưng không trúng nút choice) -> Next câu thoại
        dialogBox.setTouchable(Touchable.enabled);
        dialogBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isTyping) {
                    // Đang gõ thì click để hiện full text luôn
                    typeIndex = fullContentText.length();
                    isTyping = false;
                    updateContentLabel();
                    showChoices();
                }
                else if (engine.getCurrentNode() != null && !engine.getCurrentNode().hasChoice()) {
                    // Đã gõ xong và không có lựa chọn -> đi tiếp
                    engine.advance();
                    displayNode(engine.getCurrentNode());
                }
            }
        });

        Pixmap linePix = new Pixmap(1, 2, Pixmap.Format.RGBA8888);
        linePix.setColor(new Color(0.8f, 0.7f, 0.4f, 0.6f)); // Màu vàng nhạt, hơi mờ
        linePix.fill();
        TextureRegionDrawable lineDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(linePix)));
        linePix.dispose();

        com.badlogic.gdx.scenes.scene2d.ui.Image separatorLine = new com.badlogic.gdx.scenes.scene2d.ui.Image(lineDrawable);
        speakerLabel = new Label("", labelStyle);
        speakerLabel.setAlignment(Align.center); // Căn giữa
        speakerLabel.setColor(Color.valueOf("#F3C300")); // Mã màu vàng Gold giống hình

        contentLabel = new Label("", labelStyle);
        contentLabel.setWrap(true);
        contentLabel.setAlignment(Align.center); // Căn giữa

        choicesTable = new Table();

        // 3. Xếp hình vào Bảng (Table) theo thứ tự: Tên -> Đường kẻ -> Nội dung
        dialogBox.add(speakerLabel).expandX().center().padBottom(5).row();
        // Chiều rộng đường kẻ tùy chỉnh (ví dụ 600px), chiều cao 2px
        dialogBox.add(separatorLine).width(600).height(2).center().padBottom(15).row();
        dialogBox.add(contentLabel).width(800).minHeight(50).center().row();

        rootTable.add(choicesTable).padBottom(10).row();
        rootTable.add(dialogBox).width(900);

        rootTable.setVisible(false);
        stage.addActor(rootTable);
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    // --- Thêm biến lưu vị trí gốc để không bị lệch sau khi rung ---
    private float originalDialogueX, originalDialogueY;
    private float originalThoughtX, originalThoughtY;

    // --- Hàm tạo hiệu ứng rung lắc (Shake Action) ---
    private void applyShakeEffect(com.badlogic.gdx.scenes.scene2d.Actor target, float rs) {
        target.clearActions(); // Xóa các rung lắc cũ

        if (rs < 35f || rs > 65f) {
            // Tính cường độ rung dựa trên độ lệch (càng xa 50 rung càng mạnh)
            float intensity = (Math.abs(rs - 50f) - 15f) / 35f;
            float amount = 4f * intensity; // Tối đa rung 4 pixel
            float duration = 0.03f; // Tốc độ giựt (càng nhỏ càng nhanh)

            target.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(amount, amount, duration),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(-amount * 2, -amount, duration),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(amount, -amount, duration),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(amount, amount * 2, duration),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(-amount, -amount, duration)
                )
            ));
        }
    }

    public void displayNode(DialogueNode node) {
        if (node == null) {
            rootTable.setVisible(false);
            if (onFinished != null) {
                Runnable callback = onFinished;
                onFinished = null;
                callback.run();
            }
            return;
        }

        rootTable.setVisible(true);

        boolean isThought = "Suy nghĩ".equals(node.speaker) || node.speaker == null || node.speaker.isEmpty();

        // Xử lý Inner Thoughts (Suy nghĩ trong đầu)
        if (isThought) {
            speakerLabel.setText("");
            contentLabel.setColor(0.7f, 0.7f, 0.7f, 1f); // Màu xám nhạt
        } else {
            speakerLabel.setText(node.speaker);
            contentLabel.setColor(Color.WHITE);
            // Tone shift nếu RS > 65
            if (rsManager != null && rsManager.getRS() > 65f) {
                contentLabel.setColor(1f, 0.4f, 0.4f, 1f); // Đỏ rùng rợn
            }
        }

        // Replace {player} placeholder
        String rawText = node.content;
        if (game != null && game.getGameState() != null) {
            String playerName = game.getGameState().getPlayerName();
            rawText = rawText.replace("{player}", playerName);
        }

        // Glitch text nếu có RSManager và không phải suy nghĩ
        if (rsManager != null && !isThought) {
            fullContentText = GlitchTextRenderer.applyGlitch(rawText, rsManager.getRS());
        } else {
            fullContentText = rawText;
        }

        typeIndex = 0;
        isTyping = true;
        typeTimer = 0f;

        updateContentLabel(); // Hiện khung thoại trống trơn trước
        choicesTable.clearChildren(); // Giấu hết nút bấm đi, bao giờ gõ xong mới hiện
    }

    // Hàm này sẽ được GameScreen gọi liên tục 60 lần/giây
    public void update(float delta) {
        if (!rootTable.isVisible()) return;

        if (isTyping) {
            typeTimer += delta;
            // Cứ qua 0.05 giây thì nhích thêm 1 ký tự
            if (typeTimer >= TYPE_SPEED) {
                typeTimer = 0f;
                typeIndex++;
                if (typeIndex >= fullContentText.length()) {
                    // Gõ xong rồi
                    typeIndex = fullContentText.length();
                    isTyping = false;
                    showChoices(); // Gõ xong mới ném nút A/B ra
                }
                updateContentLabel();
            }
        }
    }

    private void updateContentLabel() {
        contentLabel.setText(fullContentText.substring(0, typeIndex));
    }

    private void showChoices() {
        DialogueNode node = engine.getCurrentNode();
        if (node == null) return;

        choicesTable.clearChildren();

        if (node.hasChoice()) {
            for (int i = 0; i < node.choices.size(); i++) {
                final int index = i;
                Choice choice = node.choices.get(i);

                String choiceText = choice.content;
                if (game != null && game.getGameState() != null) {
                    choiceText = choiceText.replace("{player}", game.getGameState().getPlayerName());
                }

                TextButton btn = new TextButton(choice.content, btnStyle);
                if (choice.rsChange < 0) {
                    btn.setColor(1f, 0.5f, 0.5f, 1f);
                }

                btn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        engine.selectChoice(index);
                        displayNode(engine.getCurrentNode());
                    }
                });
                choicesTable.add(btn).width(600).pad(5).row();
            }
        } else {
            Label hintLabel = new Label("▼ Click để tiếp tục", labelStyle);
            hintLabel.setColor(0.6f, 0.6f, 0.6f, 1f);
            choicesTable.add(hintLabel).align(Align.right).padRight(20);
        }
    }

    public boolean isVisible() {
        return rootTable.isVisible();
    }
}
