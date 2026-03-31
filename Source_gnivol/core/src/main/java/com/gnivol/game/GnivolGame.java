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
        // Nhặt 2 mảnh giấy mật mã mới
        invMgr.addItem("paper_fragment_2");
        invMgr.addItem("paper_fragment_1");

        String itemA = "paper_fragment_2";
        String itemB = "paper_fragment_1";

        // Ghép thử
        String resultItem = craftMgr.getMergeResult(itemA, itemB);

        if (resultItem != null) {
            System.out.println("Ghep thanh cong! Tao ra vat pham moi: " + resultItem);
            invMgr.removeItem(itemA);
            invMgr.removeItem(itemB);
            invMgr.addItem(resultItem);

            // Tìm data của đồ mới
            com.gnivol.game.system.inventory.data.ItemData newData = itemDB.getItemData(resultItem);

            // In ra text miêu tả để xem có chuẩn JSON không nhé
            System.out.println("Ban doc duoc: " + newData.description);

            if (newData.isCursed) {
                System.out.println("Ban dang cam mot vat pham bi nguyen rua!");
               // rsManager.addRS(newData.rsChangeValue);
            }
        } else {
            System.out.println("Khong the ghep 2 mon nay!");
        }
    }

}
