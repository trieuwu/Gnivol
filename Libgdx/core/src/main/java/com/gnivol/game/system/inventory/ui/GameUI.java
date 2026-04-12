package com.gnivol.game.system.inventory.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;

public class GameUI {
    private Stage stage;

    public GameUI(Stage stage) {
        this.stage = stage;
    }

    public void updateInventoryBar(ArrayList<String> items) {
        // TODO: Xóa rương đồ cũ, vẽ lại các ô vuông rương đồ và nhét hình ảnh item vào
        System.out.println("[View - UI] Dang ve lai rương do. Tong so mon: " + items.size());
    }

    public void showNotification(String message) {
        // TODO: Tạo một dòng text (Label) bay lên giữa màn hình rồi mờ dần
        System.out.println("[View - UI] HIEN THONG BAO: " + message);
    }

    public void highlightSlot(String itemID) {
        // TODO: Vẽ một cái khung viền màu vàng/đỏ đè lên ô đồ đang được chọn
        System.out.println("[View - UI] Bat vien sang cho o do: " + itemID);
    }

    public void removeHighlight(String itemID) {
        // TODO: Tắt cái khung viền đi
        System.out.println("[View - UI] Tat vien sang cua o do: " + itemID);
    }

    public void draw() {
        if (stage != null) {
            stage.act();
            stage.draw();
        }
    }
}
