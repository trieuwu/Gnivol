package com.gnivol.game.system.inventory;

import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.gnivol.game.Constants;

public class InventoryManager {
    private ArrayList<String> items;

    public InventoryManager() {
        items = new ArrayList<>();
    }

    public boolean addItem(String itemID) {
        if (itemID == null || itemID.isEmpty()) return false;

        if (items.size() >= Constants.MAX_INVENTORY_SLOTS) {
            Gdx.app.log("Inventory", "Inventory is full.");
            return false;
        }

        if (!items.contains(itemID)) {
            items.add(itemID);
            Gdx.app.log("Inventory", "Picked up: " + itemID); // <-- Đúng yêu cầu log ra console
            return true;
        } else {
            return false;
        }
    }



    public void removeItem(String itemID) {
        if (items.contains(itemID)) {
            items.remove(itemID);
            Gdx.app.log("Inventory", "Remove item: " + itemID);
        }
    }

    public boolean hasItem(String itemID) {
        return items.contains(itemID);
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public int getItemCount() {
        return items.size();
    }

    public void clearInventory() {
        items.clear();
    }
}
