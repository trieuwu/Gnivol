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
    private Table choiceOverlayTable;
    private Label activeTypingLabel; // Con trỏ quyết định gõ chữ vào khung nào

    private boolean isCurrentThought = false;
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

    // --- THÊM 2 BIẾN NÀY ĐỂ ĐẾM 1 GIÂY ---
    private float glitchTimer = 0f;
    private boolean isGlitchedState = false;
    public DialogueUI(GnivolGame game, Stage stage, BitmapFont font, DialogueEngine engine, RSManager rsManager) {
        this.game = game;
        this.engine = engine;
        this.rsManager = rsManager;
        labelStyle = new Label.LabelStyle(font, Color.WHITE);

        // Nền nút bình thường: Màu đen sẫm, độ mờ 85% (Trùng với độ mờ lõi khung chat)
        Pixmap btnPixNormal = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        btnPixNormal.setColor(new Color(0.05f, 0.05f, 0.05f, 0.85f));
        btnPixNormal.fill();
        TextureRegionDrawable btnNormal = new TextureRegionDrawable(new TextureRegion(new Texture(btnPixNormal)));
        btnPixNormal.dispose();

        // Nền nút khi trỏ chuột / click vào: Sáng lên một chút thành xám
        Pixmap btnPixDown = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        btnPixDown.setColor(new Color(0.2f, 0.2f, 0.2f, 0.9f));
        btnPixDown.fill();
        TextureRegionDrawable btnDown = new TextureRegionDrawable(new TextureRegion(new Texture(btnPixDown)));
        btnPixDown.dispose();

        btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE; // Chữ bình thường màu trắng
        btnStyle.overFontColor = Color.valueOf("#F3C300"); // Trỏ chuột vào: Chữ vàng Gold (Tone sur tone với tên nhân vật)
        btnStyle.downFontColor = Color.valueOf("#F3C300"); // Click vào: Chữ vàng Gold
        btnStyle.up = btnNormal;
        btnStyle.down = btnDown;
        btnStyle.over = btnDown;

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
                if (choiceOverlayTable != null && choiceOverlayTable.isVisible()) return;
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
        // Tạo ảnh nền kính mờ đen 70%
        Pixmap dimPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPix.setColor(new Color(0f, 0f, 0f, 0.7f));
        dimPix.fill();
        TextureRegionDrawable dimBg = new TextureRegionDrawable(new TextureRegion(new Texture(dimPix)));
        dimPix.dispose();

        choiceOverlayTable = new Table();
        choiceOverlayTable.setFillParent(true);
        choiceOverlayTable.setBackground(dimBg); // Gắn kính mờ
        choiceOverlayTable.setTouchable(Touchable.enabled); // Chặn click xuyên qua nền
        choiceOverlayTable.setVisible(false);
        stage.addActor(rootTable);
        stage.addActor(choiceOverlayTable);
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    // --- Thêm biến lưu vị trí gốc để không bị lệch sau khi rung ---
    private float originalDialogueX, originalDialogueY;
    private float originalThoughtX, originalThoughtY;

    // --- Hàm tạo hiệu ứng rung lắc (Shake Action) ---
    private void applyRSEffect(com.badlogic.gdx.scenes.scene2d.Actor target, float rs) {
        target.clearActions(); // Xóa sạch hiệu ứng cũ
        target.getColor().a = 1f;

        // CHỈ RUNG LẮC KHI RS > 65
        if (rs > 65f) {
            float intensity = (rs - 65f) / 35f;
            float amount = 2f + (3f * intensity);
            float duration = 0.04f;

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
        // NẾU RS <= 65 THÌ KHÔNG LÀM GÌ CẢ (ĐỨNG YÊN)
    }

    public void displayNode(DialogueNode node) {
        if (node == null) {
            rootTable.setVisible(false);
            if (choiceOverlayTable != null) choiceOverlayTable.setVisible(false);
            if (onFinished != null) {
                Runnable callback = onFinished;
                onFinished = null;
                callback.run();
            }
            return;
        }

        rootTable.setVisible(true);

        isCurrentThought = "Suy nghĩ".equals(node.speaker) || node.speaker == null || node.speaker.isEmpty();
        float currentRS = (rsManager != null) ? rsManager.getRS() : 50f;

        // DÙNG CHUNG 1 BẢNG, CHỈ ĐỔI TÊN VÀ MÀU CHỮ
        if (isCurrentThought) {
            speakerLabel.setText("Suy Nghĩ");
            contentLabel.setColor(0.7f, 0.7f, 0.7f, 1f); // Màu xám nhạt
        } else {
            speakerLabel.setText(node.speaker);
            if (currentRS > 65f) contentLabel.setColor(1f, 0.4f, 0.4f, 1f); // Đỏ rùng rợn
            else contentLabel.setColor(Color.WHITE);
        }

        activeTypingLabel = contentLabel; // Luôn luôn gõ vào contentLabel
        applyRSEffect(contentLabel, currentRS);

        String rawText = node.content;
        if (game != null && game.getGameState() != null) {
            String playerName = game.getGameState().getPlayerName();
            rawText = rawText.replace("{player}", playerName);
        }

        fullContentText = rawText;
        typeIndex = 0;
        isTyping = true;
        typeTimer = 0f;

        updateContentLabel();
        choicesTable.clearChildren();
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
        // 2. LOGIC ĐỒNG HỒ 1 GIÂY (Chỉ áp dụng khi RS < 35 và không phải là Suy nghĩ)
        if (activeTypingLabel != null) {
            float currentRS = (rsManager != null) ? rsManager.getRS() : 50f;

            if (currentRS < 35f) {
                glitchTimer += delta;
                if (glitchTimer >= 1.0f) { // Cứ đủ 1.0 giây
                    glitchTimer = 0f;
                    isGlitchedState = !isGlitchedState; // Đảo qua đảo lại (Bình thường <-> Lỗi)
                    updateContentLabel(); // Cập nhật lại giao diện chữ
                }
            } else {
                // Nếu RS phục hồi về mức > 35 thì tắt trạng thái lỗi đi
                if (isGlitchedState) {
                    isGlitchedState = false;
                    updateContentLabel();
                }
            }
        }
    }

    private void updateContentLabel() {
        if (activeTypingLabel != null) {
            float currentRS = (rsManager != null) ? rsManager.getRS() : 50f;
            String currentText = fullContentText.substring(0, typeIndex);

            // --- 1. QUYẾT ĐỊNH HIỆN CHỮ GÌ ---
            if (currentRS < 35f && !isCurrentThought && isGlitchedState) {
                // Rơi vào 1 giây bị lỗi -> Băm nát chữ
                activeTypingLabel.setText(GlitchTextRenderer.applyGlitch(currentText, currentRS));
            } else {
                // Bình thường (RS > 35, hoặc đang trong 1 giây không lỗi) -> Chữ rõ ràng
                activeTypingLabel.setText(currentText);
            }

            // --- 2. QUYẾT ĐỊNH MÀU SẮC ---
            if (isCurrentThought) {
                activeTypingLabel.setColor(0.7f, 0.7f, 0.7f, 1f); // Suy nghĩ luôn màu xám
            } else {
                if (currentRS > 65f) {
                    activeTypingLabel.setColor(1f, 0.4f, 0.4f, 1f); // RS > 65: Màu đỏ
                } else if (currentRS < 35f && isGlitchedState) {
                    activeTypingLabel.setColor(1f, 0.4f, 0.4f, 1f);
                } else {
                    activeTypingLabel.setColor(Color.WHITE); // RS 35-65 hoặc 1 giây bình thường: Màu Trắng
                }
            }
        }
    }

    private void showChoices() {
        DialogueNode node = engine.getCurrentNode();
        if (node == null) return;

        choicesTable.clearChildren();
        choiceOverlayTable.clearChildren();

        if (node.hasChoice()) {
            choiceOverlayTable.setVisible(true); // Bật kính mờ lên
            for (int i = 0; i < node.choices.size(); i++) {
                final int index = i;
                Choice choice = node.choices.get(i);

                String choiceText = choice.content;
                if (game != null && game.getGameState() != null) {
                    choiceText = choiceText.replace("{player}", game.getGameState().getPlayerName());
                }

                TextButton btn = new TextButton(choice.content, btnStyle);
                btn.getLabel().setWrap(true);
                btn.getLabel().setAlignment(Align.center); // Chữ căn giữa

                btn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        choiceOverlayTable.setVisible(false);
                        engine.selectChoice(index);
                        displayNode(engine.getCurrentNode());
                    }
                });
                choiceOverlayTable.add(btn).width(700).minHeight(80).pad(20).row();
            }
        }
    }

    public boolean isVisible() {
        return rootTable.isVisible();
    }
}
