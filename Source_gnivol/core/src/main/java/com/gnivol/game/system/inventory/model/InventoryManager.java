package com.gnivol.game.system.inventory.model;

import com.gnivol.game.system.inventory.data.RecipeData;

import java.util.ArrayList;

public class InventoryManager {
    private ArrayList<String> items;

    public InventoryManager() {
        items = new ArrayList<>();
    }

    public void addItem(String itemID) {
        if (itemID != null && !itemID.isEmpty()) {
            items.add(itemID);
        }
    }

    public void removeItem(String itemID) {
        if (items.contains(itemID)) {
            items.remove(itemID);
        }
    }

    public boolean hasItem(String itemID) {
        return items.contains(itemID);
    }

    public  ArrayList<String> getItems() {
        return items;
    }

    public void clearInventory() {
        items.clear();
    }

}
