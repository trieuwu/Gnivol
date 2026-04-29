package com.gnivol.game.data;

import java.util.HashMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.gnivol.game.model.ItemData;
import com.gnivol.game.Constants;

public class ItemDatabase {
    private static ItemDatabase instance;

    public static ItemDatabase getInstance() {
        if (instance == null) {
            instance = new ItemDatabase();
        }
        return instance;
    }

    private final HashMap<String, ItemData> database;

    public static class ItemDataWrapper {
        public Array<ItemData> items;
    }

    private ItemDatabase() {
        database = new HashMap<>();
        loadDataFromJson();
    }

    public ItemData getItemData(String itemID) {
        return database.get(itemID);
    }

    /** Trả về toàn bộ itemID đã load — phục vụ debug/cheat. */
    public java.util.Set<String> getAllItemIds() {
        return database.keySet();
    }

    private void loadDataFromJson() {
        Json json = new Json();

        FileHandle file = Gdx.files.internal(Constants.DATA_ITEMS);

        if (!file.exists()) {
            Gdx.app.error("ItemDatabase", "Items file not found: " + Constants.DATA_ITEMS);
            return;
        }

        ItemDataWrapper wrapper = json.fromJson(ItemDataWrapper.class, file);

        if (wrapper != null && wrapper.items != null) {
            for (ItemData item : wrapper.items) {
                database.put(item.itemID, item);
            }
            Gdx.app.log("ItemDatabase", "Loaded " + database.size() + " items into database.");
        }
    }

}
