package com.gnivol.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class RSUI {
    private Label rsLabel;
    private Table table;

    public RSUI(Stage stage, Label.LabelStyle labelStyle) {
        // 1. Tạo Table bao phủ toàn màn hình
        table = new Table();
        table.setFillParent(true);

        // 2. Ép nội dung trong table lên Top và Right
        table.top().right();

        // 3. Khởi tạo nhãn với giá trị mặc định
        rsLabel = new Label("35", labelStyle);

        // 4. Thêm nhãn vào table với một chút khoảng cách (padding) từ góc
        // pad(trên, trái, dưới, phải)
        table.add(rsLabel).padTop(20).padRight(20);

        // 5. Thêm table vào stage
        stage.addActor(table);
    }

    // Hàm cập nhật số RS từ RSManager
    public void updateRS(float value) {
        rsLabel.setText(String.valueOf((int) value));

        // Bonus: Đổi màu nếu RS quá thấp (nguy hiểm)
        if (value < 35) {
            rsLabel.setColor(Color.RED);
        } else {
            rsLabel.setColor(Color.WHITE);
        }
    }
}
