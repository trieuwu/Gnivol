package com.gnivol.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gnivol.game.system.inventory.data.ItemDatabase;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GnivolGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    @Override
    public void create() {
        System.out.println("Dang khoi dong game Gnivol...");

        // 1. Khởi tạo các Manager
        com.gnivol.game.system.inventory.data.ItemDatabase itemDB = new com.gnivol.game.system.inventory.data.ItemDatabase();
        com.gnivol.game.system.inventory.model.CraftingManager craftMgr = new com.gnivol.game.system.inventory.model.CraftingManager();
        com.gnivol.game.system.inventory.model.InventoryManager invMgr = new com.gnivol.game.system.inventory.model.InventoryManager();

        System.out.println("\n--- KICH BAN TEST GAME ---");
        // 2. Kịch bản: Người chơi đi lòng vòng và nhặt được 2 mảnh giấy
        invMgr.addItem("paper_B");
        invMgr.addItem("paper_A");

        // 3. Người chơi mở túi, chọn mảnh B, chọn mảnh A và bấm "Ghép"
        String itemA = "paper_B"; // Cố tình để B trước xem nó có hiểu không
        String itemB = "paper_A";

        // Hỏi hệ thống xem ghép ra cái gì
        String resultItem = craftMgr.getMergeResult(itemA, itemB);

        if (resultItem != null) {
            System.out.println("Ghep thanh cong! Tao ra vat pham moi: " + resultItem);

            // 4. Xóa 2 mảnh cũ đi
            invMgr.removeItem(itemA);
            invMgr.removeItem(itemB);

            // 5. Thêm đồ mới vào túi
            invMgr.addItem(resultItem);

            // 6. In ra thông tin chi tiết của đồ mới để dọa người chơi
            com.gnivol.game.system.inventory.data.ItemData cursedData = itemDB.getItemData(resultItem);
            System.out.println("Canh bao! Ban vua tao ra [" + cursedData.itemName + "]. Diem thuc tai bi tru: " + cursedData.rsChangeValue);
        } else {
            System.out.println("Khong the ghep 2 mon nay!");
        }
    }

}
