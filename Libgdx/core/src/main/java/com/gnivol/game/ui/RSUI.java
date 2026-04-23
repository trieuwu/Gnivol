package com.gnivol.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class RSUI {
    private Label rsLabel;
    private Table table;
    private Texture frameTex;

    public RSUI(Stage stage, Label.LabelStyle labelStyle) {
        // 1. Tạo Table bao phủ toàn màn hình
        table = new Table();
        table.setFillParent(true);
        // Ép chặt nội dung vào góc trên cùng bên phải
        table.align(Align.topRight);

        // 2. Tạo khung lót nền bằng item_frame.png
        frameTex = new Texture(Gdx.files.internal("images/UI/item_frame.png"));
        Table frameTable = new Table();
        frameTable.setBackground(new TextureRegionDrawable(new TextureRegion(frameTex)));

        // 3. Khởi tạo nhãn RS
        rsLabel = new Label("35", labelStyle);
        rsLabel.setAlignment(Align.center);

        // 4. Nhét chữ RS vào giữa khung lót
        frameTable.add(rsLabel).expand().center().padTop(-10f);

        // 5. Thêm khung vào table gốc: Thu nhỏ size còn 55x55 và đẩy cách lề 20px
        table.add(frameTable).size(50, 50).padTop(20f).padRight(20f);

        stage.addActor(table);
    }

    public void updateRS(float value) {
        rsLabel.setText(String.valueOf((int) value));

        // SỬA LỖI LOGIC: Dùng || (HOẶC) thay vì && (VÀ)
        if (value < 35 || value > 65) {
            rsLabel.setColor(Color.RED);
        } else {
            rsLabel.setColor(Color.WHITE);
        }
    }
}
