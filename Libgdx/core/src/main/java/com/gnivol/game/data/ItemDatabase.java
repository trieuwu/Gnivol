package com.gnivol.game.data;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.gnivol.game.model.ItemData;

public class ItemDatabase {

    private HashMap<String, ItemData> database;

    public static class ItemDataWrapper {
        public Array<ItemData> items;
    }

    public ItemDatabase() {
        database = new HashMap<>();
        loadDataFromJson();
    }

    public ItemData getItemData(String itemID) {
        return database.get(itemID);
    }

    private void loadDataFromJson() {
        Json json = new Json();
        ItemDataWrapper wrapper = json.fromJson(ItemDataWrapper.class, Gdx.files.internal("data/items.json"));

        if (wrapper != null && wrapper.items != null) {
            for (ItemData item : wrapper.items) {
                database.put(item.itemID, item);
            }
        }
    }
}
