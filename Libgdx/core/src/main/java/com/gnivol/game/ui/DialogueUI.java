package com.gnivol.game.ui;

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
import com.gnivol.game.model.dialogue.Choice;
import com.gnivol.game.model.dialogue.DialogueNode;
import com.gnivol.game.system.dialogue.DialogueEngine;

public class DialogueUI {
    private Table rootTable;
    private Label speakerLabel;
    private Label contentLabel;
    private Table choicesTable;

    private DialogueEngine engine;
    private Label.LabelStyle labelStyle;
    private TextButton.TextButtonStyle btnStyle;
    private TextButton.TextButtonStyle btnHoverStyle;

    // Callback khi dialogue kết thúc — GameScreen dùng để chain dialogue tiếp
    private Runnable onFinished;

    public DialogueUI(Stage stage, BitmapFont font, DialogueEngine engine) {
        this.engine = engine;

        // 1. Tạo style chữ từ font tiếng Việt của bạn
        labelStyle = new Label.LabelStyle(font, Color.WHITE);

        // Tạo nút bấm (có nền xám mờ để phân biệt)
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
        Pixmap bgPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPix.setColor(new Color(0f, 0f, 0f, 0.85f));
        bgPix.fill();
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
                // Chỉ cho phép click next nếu KHÔNG có lựa chọn (bắt buộc phải bấm nút A/B)
                if (engine.getCurrentNode() != null && !engine.getCurrentNode().hasChoice()) {
                    engine.advance();
                    displayNode(engine.getCurrentNode());
                }
            }
        });

        speakerLabel = new Label("", labelStyle);
        speakerLabel.setColor(1f, 0.9f, 0.5f, 1f); // Tên màu vàng nhạt

        contentLabel = new Label("", labelStyle);
        contentLabel.setWrap(true);
        contentLabel.setAlignment(Align.topLeft);

        choicesTable = new Table();

        dialogBox.add(speakerLabel).align(Align.left).padBottom(10).row();
        dialogBox.add(contentLabel).width(800).height(100).align(Align.topLeft).row();

        rootTable.add(choicesTable).padBottom(10).row();
        rootTable.add(dialogBox).width(900);

        rootTable.setVisible(false);
        stage.addActor(rootTable);
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public void displayNode(DialogueNode node) {
        if (node == null) {
            rootTable.setVisible(false);
            if (onFinished != null) {
                Runnable callback = onFinished;
                onFinished = null;  // chỉ chạy 1 lần
                callback.run();
            }
            return;
        }

        rootTable.setVisible(true);
        speakerLabel.setText(node.speaker != null ? node.speaker : "");
        contentLabel.setText(node.content);
        choicesTable.clearChildren();

        if (node.hasChoice()) {
            for (int i = 0; i < node.choices.size(); i++) {
                final int index = i;
                Choice choice = node.choices.get(i);

                TextButton btn = new TextButton(choice.content, btnStyle);
                if (choice.rsChange < 0) {
                    btn.setColor(1f, 0.5f, 0.5f, 1f); // Lựa chọn nguy hiểm -> Nút hơi đỏ
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
            // Gợi ý nhấp nháy cho người chơi biết click để tiếp tục
            Label hintLabel = new Label("▼ Click để tiếp tục", labelStyle);
            hintLabel.setColor(0.6f, 0.6f, 0.6f, 1f);
            choicesTable.add(hintLabel).align(Align.right).padRight(20);
        }
    }

    public boolean isVisible() {
        return rootTable.isVisible();
    }
}
