package com.gnivol.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic overlay for opening containers (fridge, wardrobe, etc).
 * Loads configuration from a JSON file.
 */
public class InventoryOverlay {

    // --- Model classes ---

    public static class OverlayItem {
        public String itemId;
        public float x, y, w, h;
    }

    public static class OverlayData {
        public String id;
        public String background;
        public ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
    }

    // --- Listener ---

    public interface OverlayListener {
        void onItemCollected(String overlayId, String itemId);
        void onOverlayClosed(String overlayId);
    }

    // --- Fields ---

    private final Map<String, OverlayData> overlays = new HashMap<String, OverlayData>();
    private OverlayData currentOverlay;
    private Texture currentBackground;
    private boolean open;
    private OverlayListener listener;

    // --- API ---

    public void loadOverlays(String jsonPath) {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal(jsonPath));

            for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                OverlayData data = new OverlayData();
                data.id = entry.getString("id");
                data.background = entry.getString("background", "");

                JsonValue itemsArray = entry.get("items");
                if (itemsArray != null) {
                    for (JsonValue itemVal = itemsArray.child; itemVal != null; itemVal = itemVal.next) {
                        OverlayItem item = new OverlayItem();
                        item.itemId = itemVal.getString("itemId", "");
                        item.x = itemVal.getFloat("x", 0f);
                        item.y = itemVal.getFloat("y", 0f);
                        item.w = itemVal.getFloat("w", 0f);
                        item.h = itemVal.getFloat("h", 0f);
                        data.items.add(item);
                    }
                }

                overlays.put(data.id, data);
            }
            Gdx.app.log("InventoryOverlay", "Loaded " + overlays.size() + " overlays");
        } catch (Exception e) {
            Gdx.app.error("InventoryOverlay", "Failed to load overlays from: " + jsonPath, e);
        }
    }

    public void open(String overlayId) {
        OverlayData data = overlays.get(overlayId);
        if (data == null) {
            Gdx.app.error("InventoryOverlay", "Overlay not found: " + overlayId);
            return;
        }
        currentOverlay = data;
        open = true;

        if (data.background != null && !data.background.isEmpty()) {
            try {
                currentBackground = new Texture(Gdx.files.internal(data.background));
            } catch (Exception e) {
                Gdx.app.error("InventoryOverlay", "Failed to load background: " + data.background, e);
                currentBackground = null;
            }
        }
    }

    public void close() {
        String overlayId = (currentOverlay != null) ? currentOverlay.id : null;
        open = false;
        if (currentBackground != null) {
            currentBackground.dispose();
            currentBackground = null;
        }
        currentOverlay = null;
        if (listener != null && overlayId != null) {
            listener.onOverlayClosed(overlayId);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void render(SpriteBatch batch) {
        if (!open || currentOverlay == null) return;

        if (currentBackground != null) {
            batch.draw(currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    public boolean handleClick(float worldX, float worldY) {
        if (!open || currentOverlay == null) return false;

        for (OverlayItem item : currentOverlay.items) {
            if (worldX >= item.x && worldX <= item.x + item.w
                && worldY >= item.y && worldY <= item.y + item.h) {
                if (listener != null) {
                    listener.onItemCollected(currentOverlay.id, item.itemId);
                }
                return true;
            }
        }
        return false;
    }

    public void setListener(OverlayListener listener) {
        this.listener = listener;
    }

    public OverlayData getOverlayData(String overlayId) {
        return overlays.get(overlayId);
    }

    public OverlayData findByObjectId(String objectId) {
        for (OverlayData data : overlays.values()) {
            if (data.id.startsWith(objectId + "_")) return data;
        }
        return null;
    }

    public void dispose() {
        if (currentBackground != null) {
            currentBackground.dispose();
            currentBackground = null;
        }
    }
}
