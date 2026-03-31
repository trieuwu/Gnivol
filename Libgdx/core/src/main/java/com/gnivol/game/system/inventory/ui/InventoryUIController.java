package com.gnivol.game.system.inventory.ui;

import com.gnivol.game.system.inventory.data.ItemData;
import com.gnivol.game.system.inventory.data.ItemDatabase;
import com.gnivol.game.system.inventory.model.CraftingManager;
import com.gnivol.game.system.inventory.model.InventoryManager;

public class InventoryUIController {
    private String selectedItemID = null;

    private CraftingManager craftingManager;
    private InventoryManager inventoryManager;
    private ItemDatabase itemDatabase;
    private GameUI gameUI;
    //private GameUI gameUI

    public InventoryUIController(CraftingManager craftMgr, InventoryManager invMgr, ItemDatabase itemDB, GameUI ui) {
        this.craftingManager = craftMgr;
        this.inventoryManager = invMgr;
        this.itemDatabase = itemDB;
        this.gameUI = ui;
    }

    public void onItemClicked(String clickedItemID){
        if (selectedItemID == null){
            selectedItemID = clickedItemID;
            gameUI.highlightSlot(clickedItemID);
        } else if (selectedItemID.equals(clickedItemID)){
            resetSelection();
        } else {
            processMerge(selectedItemID, clickedItemID);
        }
    }

    private void processMerge(String itemA,  String itemB) {
        String resultItem = craftingManager.getMergeResult(itemA, itemB);

        if (resultItem != null) {
            inventoryManager.removeItem(itemA);
            inventoryManager.removeItem(itemB);
            inventoryManager.addItem(resultItem);

            ItemData resultData = itemDatabase.getItemData(resultItem);
            if (resultData != null) {
                gameUI.showNotification("Bạn đã tạo ra " + resultData.itemName);

            }
        } else {
            gameUI.showNotification("Không thể ghép 2 món này!");
        }
        resetSelection();
    }

    private void resetSelection() {
        if (selectedItemID != null) {
            gameUI.removeHighlight(selectedItemID); // (Giả lập View)

            selectedItemID = null;
        }
    }
}
