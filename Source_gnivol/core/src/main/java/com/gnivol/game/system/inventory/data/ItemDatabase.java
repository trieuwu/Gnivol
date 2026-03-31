package com.gnivol.game.system.inventory.data;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class ItemDatabase {
    private HashMap<String, ItemData> database;

    public ItemDatabase() {
        database = new HashMap<>();
        loadDataFromJson();
    }

    public ItemData getItemData(String itemID) {
        return database.get(itemID);
    }

    private void loadDataFromJson() {
        Json json = new Json();

        Array<ItemData> itemArray = json.fromJson(Array.class, ItemData.class, Gdx.files.internal("data/items.json"));

        for(ItemData item : itemArray) {
            database.put(item.itemID, item);
        }
        System.out.println("Loaded " + database.size() + " items into the database.");
        System.out.println("--- Check ---");
        for(ItemData item : database.values()) {
            System.out.println("Mã ID: " + item.itemID);
            System.out.println("Tên hiển thị: " + item.itemName);
            System.out.println("Điểm RS thay đổi: " + item.rsChangeValue);
            System.out.println("Có bị nguyền rủa không?: " + item.isCursed);
            System.out.println("----------------------------");
        }

    }
}
