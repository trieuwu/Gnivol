package com.gnivol.game.system.inventory;

import java.util.ArrayList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.gnivol.game.Constants;
import com.gnivol.game.system.save.ISaveable;

public class InventoryManager implements ISaveable {
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

    @Override
    public void save(Json json) {
        json.writeObjectStart("inventoryManager");

        json.writeValue("items", items);
        json.writeObjectEnd();
    }

    @Override
    public void load(JsonValue jsonValue) {
        JsonValue invJson = jsonValue.get("inventoryManager");
        if (invJson != null) {
            JsonValue itemsJson = invJson.get("items");
            items.clear();
            if (itemsJson != null && itemsJson.isArray()) {
                for (JsonValue item : itemsJson) {
                    items.add(item.asString());
                }
            }
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
