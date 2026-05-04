package com.gnivol.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private Label activeTypingLabel;

    // Character portrait — vẽ bằng SpriteBatch với toạ độ tuyệt đối
    private Texture portraitTex1;   // main portrait
    private Texture portraitTex2;   // sub portrait
    private float[] portraitRect1 = new float[4]; // {x, y, w, h} main
    private float[] portraitRect2 = new float[4]; // {x, y, w, h} sub
    private boolean hasPortrait1 = false;
    private boolean hasPortrait2 = false;
    private boolean isMainPortrait1 = true; // true = portrait1 sáng, portrait2 tối

    // Màu tối cho portrait phụ (sub) — làm mờ nhân vật không đang nói
    private static final Color DIM_COLOR = new Color(0.35f, 0.35f, 0.35f, 1f);
    private static final Color BRIGHT_COLOR = new Color(1f, 1f, 1f, 1f);

    // Vị trí mặc định portrait (khi JSON không chỉ định toạ độ)
    private static final float DEFAULT_LEFT_X = 50f;
    private static final float DEFAULT_RIGHT_X = 870f;
    private static final float DEFAULT_CENTER_X = 460f;
    private static final float DEFAULT_Y = 100f;
    private static final float DEFAULT_W = 360f;
    private static final float DEFAULT_H = 480f;

    // Debug portrait
    private boolean debugPortrait = false;
    private ShapeRenderer debugShapeRenderer;
    private BitmapFont debugFont;
    private int draggingPortrait = 0; // 0=none, 1=portrait1, 2=portrait2
    private boolean draggingResize = false; // shift=resize
    private float dragOffsetX, dragOffsetY;
    private DialogueNode currentNodeRef; // giữ ref để export toạ độ

    private boolean isCurrentThought = false;
    private DialogueEngine engine;
    private Label.LabelStyle labelStyle;
    private TextButton.TextButtonStyle btnStyle;

    private float clickDelayTimer = 0f;
    private String fullContentText = "";      // Chứa toàn bộ nội dung câu thoại
    private float typeTimer = 0f;             // Đồng hồ đếm ngược để gõ chữ
    private int typeIndex = 0;                // Vị trí chữ đang gõ tới đâu
    private final float TYPE_SPEED = 0.05f;   // 0.05 giây hiện 1 chữ
    /** Cheat: nếu true → text hiện ngay tức thời (skip typewriter). Toggle qua F2 trong GameScreen. */
    public static boolean CHEAT_INSTANT_DIALOGUE = false;
    private boolean isTyping = false;         // Đang gõ hay đã gõ xong
    private final RSManager rsManager;
    private GnivolGame game;
    // Callback khi dialogue kết thúc — GameScreen dùng để chain dialogue tiếp
    private Runnable onFinished;

    // --- THÊM 2 BIẾN NÀY ĐỂ ĐẾM 1 GIÂY ---
    private float glitchTimer = 0f;
    private boolean isGlitchedState = false;
    private float autoAdvanceTimer = 0f;

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
                if (!canClick()) return;
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

        // Portrait giờ được vẽ bằng SpriteBatch, không nằm trong Table nữa
        debugShapeRenderer = new ShapeRenderer();

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
        boolean forceMax = (currentNodeRef != null && currentNodeRef.textEffects);

        // RUNG LẮC KHI RS > 65 hoặc node có textEffects (force max)
        if (forceMax || rs > 65f) {
            float intensity = forceMax ? 1.0f : (rs - 65f) / 35f;
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
            // Dialogue kết thúc → reset FORCE_MAX_GLITCH để tắt shader/shake
            if (com.gnivol.game.screen.GameScreen.FORCE_MAX_GLITCH) {
                com.badlogic.gdx.Gdx.app.log("GLITCH", "FORCE_MAX_GLITCH = false (dialogue ended)");
            }
            com.gnivol.game.screen.GameScreen.FORCE_MAX_GLITCH = false;
            currentNodeRef = null;
            rootTable.clearActions();
            rootTable.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.5f),

                com.badlogic.gdx.scenes.scene2d.actions.Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        rootTable.setVisible(false);
                        if (choiceOverlayTable != null) choiceOverlayTable.setVisible(false);
                        if (onFinished != null) {
                            Runnable callback = onFinished;
                            onFinished = null;
                            callback.run();
                        }
                    }
                })
            ));
            return;
        }

        if (!rootTable.isVisible()) {
            rootTable.getColor().a = 0f; // Bắt đầu từ trong suốt
            rootTable.setVisible(true);  // Bật hiển thị
            rootTable.clearActions();    // Xóa các hiệu ứng cũ (tránh lỗi nếu click nhanh)
            rootTable.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(1f)); // Mờ dần lên trong 0.5s
        }

        autoAdvanceTimer = 0f;
        currentNodeRef = node;
        // Sync FORCE_MAX_GLITCH với textEffects flag của node hiện tại
        boolean prevForce = com.gnivol.game.screen.GameScreen.FORCE_MAX_GLITCH;
        com.gnivol.game.screen.GameScreen.FORCE_MAX_GLITCH = node.textEffects;
        if (prevForce != node.textEffects) {
            com.badlogic.gdx.Gdx.app.log("GLITCH", "FORCE_MAX_GLITCH = " + node.textEffects + " (node=" + node.id + ", speaker=" + node.speaker + ")");
        }

        // Play one-shot SFX khi vào node (VD: sike, scream2...)
        if (node.onEnterSfx != null && game != null && game.getAudioManager() != null) {
            game.getAudioManager().playSFX(node.onEnterSfx);
        }

        isCurrentThought = "Suy nghĩ".equals(node.speaker) || node.speaker == null || node.speaker.isEmpty();
        float currentRS = (rsManager != null) ? rsManager.getRS() : 50f;

        // Update character portrait
        updatePortrait(node);

        String playerName = "Player";
        if (game != null && game.getGameState() != null) {
            String savedName = game.getGameState().getPlayerName();
            if (savedName != null && !savedName.trim().isEmpty()) {
                playerName = savedName;
            }
        }

        if (isCurrentThought) {
            speakerLabel.setText("Suy Nghĩ");
        } else {
            String finalSpeaker = node.speaker;
            if (finalSpeaker != null) {
                finalSpeaker = finalSpeaker.replace("{player}", playerName);
            }
            speakerLabel.setText(finalSpeaker);
        }

        if (currentRS > 65f) contentLabel.setColor(1f, 0.4f, 0.4f, 1f);
        else contentLabel.setColor(Color.WHITE);

        activeTypingLabel = contentLabel; // Luôn luôn gõ vào contentLabel
        applyRSEffect(contentLabel, currentRS);

        String rawText = node.content;
        if (rawText != null) {
            rawText = rawText.replace("{player}", playerName);
            String roomName = "Khu vực không xác định";
            if (game != null && game.getSceneManager() != null
                && game.getSceneManager().getCurrentScene() != null
                && game.getSceneManager().getCurrentScene().getRoomData() != null) {

                roomName = game.getSceneManager().getCurrentScene().getRoomData().roomName;
            }
            rawText = rawText.replace("{roomName}", roomName);
        }

        fullContentText = rawText;
        typeIndex = 0;
        isTyping = true;
        typeTimer = 0f;
        clickDelayTimer = 0f;
        // Cheat F2: skip typewriter — hiện full text ngay
        if (CHEAT_INSTANT_DIALOGUE && fullContentText != null) {
            typeIndex = fullContentText.length();
            isTyping = false;
        }

        updateContentLabel();
        choicesTable.clearChildren();
    }

    private void updatePortrait(DialogueNode node) {
        // Giữ ref cho debug export
        currentNodeRef = node;

        // Reset
        hasPortrait1 = false;
        hasPortrait2 = false;
        if (portraitTex1 != null) { portraitTex1.dispose(); portraitTex1 = null; }
        if (portraitTex2 != null) { portraitTex2.dispose(); portraitTex2 = null; }

        if (isCurrentThought || node.portrait == null) return;

        String side = node.portraitSide != null ? node.portraitSide : "center";

        // --- Portrait 1 (main) ---
        try {
            portraitTex1 = new Texture(Gdx.files.internal(node.portrait));
            hasPortrait1 = true;

            // Toạ độ: ưu tiên JSON, fallback mặc định theo side
            float defX = "right".equals(side) ? DEFAULT_RIGHT_X : ("left".equals(side) ? DEFAULT_LEFT_X : DEFAULT_CENTER_X);
            portraitRect1[0] = node.portraitX >= 0 ? node.portraitX : defX;
            portraitRect1[1] = node.portraitY >= 0 ? node.portraitY : DEFAULT_Y;
            portraitRect1[2] = node.portraitW > 0 ? node.portraitW : DEFAULT_W;
            portraitRect1[3] = node.portraitH > 0 ? node.portraitH : DEFAULT_H;
        } catch (Exception e) {
            Gdx.app.error("DialogueUI", "Cannot load portrait: " + node.portrait, e);
            hasPortrait1 = false;
        }

        // --- Portrait 2 (sub) ---
        if (node.portrait2 != null) {
            try {
                portraitTex2 = new Texture(Gdx.files.internal(node.portrait2));
                hasPortrait2 = true;

                String side2 = node.portraitSide2 != null ? node.portraitSide2 : "right";
                float defX2 = "right".equals(side2) ? DEFAULT_RIGHT_X : DEFAULT_LEFT_X;
                portraitRect2[0] = node.portrait2X >= 0 ? node.portrait2X : defX2;
                portraitRect2[1] = node.portrait2Y >= 0 ? node.portrait2Y : DEFAULT_Y;
                portraitRect2[2] = node.portrait2W > 0 ? node.portrait2W : DEFAULT_W;
                portraitRect2[3] = node.portrait2H > 0 ? node.portrait2H : DEFAULT_H;
            } catch (Exception e) {
                Gdx.app.error("DialogueUI", "Cannot load portrait2: " + node.portrait2, e);
                hasPortrait2 = false;
            }
        }

        isMainPortrait1 = true; // portrait1 luôn là main (người đang nói)
    }

    /** Vẽ portrait — gọi từ GameScreen TRƯỚC khi stage.draw() */
    public void renderPortraits(SpriteBatch batch) {
        if (!rootTable.isVisible()) return;

        // Vẽ sub trước (phía sau), main sau (phía trước)
        if (hasPortrait2 && portraitTex2 != null) {
            batch.setColor(DIM_COLOR);
            batch.draw(portraitTex2, portraitRect2[0], portraitRect2[1], portraitRect2[2], portraitRect2[3]);
        }
        if (hasPortrait1 && portraitTex1 != null) {
            batch.setColor(BRIGHT_COLOR);
            batch.draw(portraitTex1, portraitRect1[0], portraitRect1[1], portraitRect1[2], portraitRect1[3]);
        }
        batch.setColor(Color.WHITE); // reset
    }

    /** Vẽ debug overlay cho portrait — gọi sau renderPortraits */
    public void renderPortraitDebug(ShapeRenderer shapeRenderer, BitmapFont font, SpriteBatch batch) {
        if (!debugPortrait || !rootTable.isVisible()) return;

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip Y

        // Vẽ khung debug
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (hasPortrait1) {
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(portraitRect1[0], portraitRect1[1], portraitRect1[2], portraitRect1[3]);
        }
        if (hasPortrait2) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(portraitRect2[0], portraitRect2[1], portraitRect2[2], portraitRect2[3]);
        }
        // Crosshair chuột
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.line(mouseX - 10, mouseY, mouseX + 10, mouseY);
        shapeRenderer.line(mouseX, mouseY - 10, mouseX, mouseY + 10);
        shapeRenderer.end();

        // Vẽ text thông tin toạ độ
        batch.begin();
        if (hasPortrait1) {
            font.setColor(Color.GREEN);
            font.draw(batch, String.format("P1: x=%.0f y=%.0f w=%.0f h=%.0f",
                portraitRect1[0], portraitRect1[1], portraitRect1[2], portraitRect1[3]),
                portraitRect1[0], portraitRect1[1] + portraitRect1[3] + 15);
        }
        if (hasPortrait2) {
            font.setColor(Color.YELLOW);
            font.draw(batch, String.format("P2: x=%.0f y=%.0f w=%.0f h=%.0f",
                portraitRect2[0], portraitRect2[1], portraitRect2[2], portraitRect2[3]),
                portraitRect2[0], portraitRect2[1] + portraitRect2[3] + 15);
        }
        font.setColor(Color.WHITE);
        font.draw(batch, "F3: Toggle portrait debug | Drag: move | Shift+Drag: resize | F4: Export", 10, Gdx.graphics.getHeight() - 10);
        batch.end();
    }

    /** Xử lý click debug portrait — return true nếu bắt đầu drag */
    public boolean handlePortraitDebugClick(float screenX, float screenY) {
        if (!debugPortrait || !rootTable.isVisible()) return false;

        float worldX = screenX;
        float worldY = Gdx.graphics.getHeight() - screenY;
        draggingResize = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.SHIFT_LEFT)
                      || Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.SHIFT_RIGHT);

        // Kiểm tra portrait1 trước (main, ở trên)
        if (hasPortrait1 && worldX >= portraitRect1[0] && worldX <= portraitRect1[0] + portraitRect1[2]
            && worldY >= portraitRect1[1] && worldY <= portraitRect1[1] + portraitRect1[3]) {
            draggingPortrait = 1;
            dragOffsetX = worldX - portraitRect1[0];
            dragOffsetY = worldY - portraitRect1[1];
            return true;
        }
        // Kiểm tra portrait2
        if (hasPortrait2 && worldX >= portraitRect2[0] && worldX <= portraitRect2[0] + portraitRect2[2]
            && worldY >= portraitRect2[1] && worldY <= portraitRect2[1] + portraitRect2[3]) {
            draggingPortrait = 2;
            dragOffsetX = worldX - portraitRect2[0];
            dragOffsetY = worldY - portraitRect2[1];
            return true;
        }
        return false;
    }

    /** Xử lý kéo portrait debug */
    public void handlePortraitDebugDrag(float screenX, float screenY) {
        if (draggingPortrait == 0) return;

        float worldX = screenX;
        float worldY = Gdx.graphics.getHeight() - screenY;
        float[] rect = (draggingPortrait == 1) ? portraitRect1 : portraitRect2;

        if (draggingResize) {
            rect[2] = Math.max(50, worldX - rect[0]);
            rect[3] = Math.max(50, worldY - rect[1]);
        } else {
            rect[0] = worldX - dragOffsetX;
            rect[1] = worldY - dragOffsetY;
        }
    }

    /** Kết thúc drag */
    public void finishPortraitDebugDrag() {
        if (draggingPortrait != 0) {
            float[] rect = (draggingPortrait == 1) ? portraitRect1 : portraitRect2;
            Gdx.app.log("DialogueUI", String.format("Portrait%d => x=%.0f, y=%.0f, w=%.0f, h=%.0f",
                draggingPortrait, rect[0], rect[1], rect[2], rect[3]));
            draggingPortrait = 0;
        }
    }

    /** Export toạ độ portrait dưới dạng JSON — bấm F4 */
    public void exportPortraitCoordinates() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PORTRAIT COORDINATES (copy vào dialogues.json) ===\n");
        if (hasPortrait1) {
            sb.append(String.format("\"portraitX\": %.0f, \"portraitY\": %.0f, \"portraitW\": %.0f, \"portraitH\": %.0f",
                portraitRect1[0], portraitRect1[1], portraitRect1[2], portraitRect1[3]));
            sb.append("\n");
        }
        if (hasPortrait2) {
            sb.append(String.format("\"portrait2X\": %.0f, \"portrait2Y\": %.0f, \"portrait2W\": %.0f, \"portrait2H\": %.0f",
                portraitRect2[0], portraitRect2[1], portraitRect2[2], portraitRect2[3]));
            sb.append("\n");
        }
        sb.append("=====================================================");
        Gdx.app.log("DialogueUI", sb.toString());
    }

    public boolean isDebugPortrait() { return debugPortrait; }
    public void toggleDebugPortrait() { debugPortrait = !debugPortrait; }
    public boolean isDraggingPortrait() { return draggingPortrait != 0; }

    public void update(float delta) {
        if (!rootTable.isVisible()) return;
        if (clickDelayTimer < 2.0f) {
            clickDelayTimer += delta;
        }
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
                    showChoices();
                    autoAdvanceTimer = 0f;// Gõ xong mới ném nút A/B ra
                }
                updateContentLabel();
            }
        } else {
            float currentRS = (rsManager != null) ? rsManager.getRS() : 50f;
            if (currentRS <= 0f || currentRS >= 100f) {
                DialogueNode node = engine.getCurrentNode();
                if (node != null && !node.hasChoice()) {
                    autoAdvanceTimer += delta;
                    if (autoAdvanceTimer >= 3.0f) {
                        autoAdvanceTimer = 0f;
                        engine.advance();
                        displayNode(engine.getCurrentNode());
                    }
                }
            }
        }
        // 2. LOGIC ĐỒNG HỒ 1 GIÂY (RS < 35 hoặc node textEffects = liên tục)
        if (activeTypingLabel != null) {
            float currentRS = (rsManager != null) ? rsManager.getRS() : 50f;
            boolean forceMax = (currentNodeRef != null && currentNodeRef.textEffects);

            if (forceMax) {
                // Force: glitch state ON liên tục, refresh label mỗi frame để text đổi nát liên tục
                isGlitchedState = true;
                updateContentLabel();
            } else if (currentRS < 35f) {
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

            boolean forceMax = (currentNodeRef != null && currentNodeRef.textEffects);
            // --- 1. QUYẾT ĐỊNH HIỆN CHỮ GÌ ---
            if (forceMax || (currentRS < 35f && isGlitchedState)) {
                // Force-glitch hoặc RS thấp đang trong frame lỗi -> Băm nát chữ với intensity max
                float glitchRs = forceMax ? 0f : currentRS;
                activeTypingLabel.setText(GlitchTextRenderer.applyGlitch(currentText, glitchRs));
            } else {
                // Bình thường -> Chữ rõ ràng
                activeTypingLabel.setText(currentText);
            }

            // --- 2. QUYẾT ĐỊNH MÀU SẮC ---
            if (forceMax || currentRS > 65f) {
                activeTypingLabel.setColor(1f, 0.4f, 0.4f, 1f); // Force/RS > 65: Màu đỏ
            } else if (currentRS < 35f && isGlitchedState) {
                activeTypingLabel.setColor(1f, 0.4f, 0.4f, 1f);
            } else {
                activeTypingLabel.setColor(Color.WHITE); // RS 35-65 hoặc 1 giây bình thường: Màu Trắng
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

                String playerName = "Player";
                if (game != null && game.getGameState() != null) {
                    String savedName = game.getGameState().getPlayerName();
                    if (savedName != null && !savedName.trim().isEmpty()) {
                        playerName = savedName;
                    }
                }

                String choiceText = choice.content;
                if (game != null && game.getGameState() != null) {
                    choiceText = choiceText.replace("{player}", game.getGameState().getPlayerName());
                }

                TextButton btn = new TextButton(choiceText, btnStyle);
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
    public boolean isTyping() {
        return isTyping;
    }
    public void finishTyping() {
        if (isTyping) {
            typeIndex = fullContentText.length();
            isTyping = false;
            updateContentLabel();
            showChoices();
        }
    }
    public boolean canClick() {
        // Block click khi đang hiển thị overlay choice A/B (Thành's fix — PR #84)
        boolean choiceNotVisible = (choiceOverlayTable == null || !choiceOverlayTable.isVisible());
        // Default 2s gate (chống skip nhanh). Cheat F2 → 0s.
        float requiredDelay = CHEAT_INSTANT_DIALOGUE ? 0f : 1.0f;
        boolean timeOk = clickDelayTimer >= requiredDelay;
        return timeOk && choiceNotVisible;
    }
}
