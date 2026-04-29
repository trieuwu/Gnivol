package com.gnivol.game.system.interaction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gnivol.game.component.BoundsComponent;
import com.gnivol.game.component.CollectibleComponent;
import com.gnivol.game.component.ItemInfoComponent;
import com.gnivol.game.component.RSModifierComponent;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.system.inventory.InventoryManager;
import com.gnivol.game.system.rs.RSEvent;
import com.gnivol.game.system.rs.RSEventType;
import com.gnivol.game.system.rs.RSManager;
import com.gnivol.game.system.scene.Scene;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.data.ItemDatabase;
import com.gnivol.game.model.ItemData;
import java.util.List;
import com.gnivol.game.system.puzzle.PuzzleManager;

public class PlayerInteractionSystem {

    private final SceneManager sceneManager;
    private final InventoryManager inventoryManager;
    private final RSManager rsManager;
    private final Vector3 touchPoint = new Vector3();

    // Callback để GameScreen xử lý phần visual (inspect text, overlay, scene change)
    private InteractionCallback callback;

    private final PuzzleManager puzzleManager;

    public PlayerInteractionSystem(SceneManager sceneManager, InventoryManager inventoryManager, RSManager rsManager, PuzzleManager puzzleManager) {
        this.sceneManager = sceneManager;
        this.inventoryManager = inventoryManager;
        this.rsManager = rsManager;
        this.puzzleManager = puzzleManager;
    }

    public void setCallback(InteractionCallback callback) {
        this.callback = callback;
    }

    /**
     * Xử lý click từ screen coordinates.
     * Unproject → loop objects → check bounds → dispatch.
     *
     * @return true nếu click trúng object
     */
    public boolean handleClick(int screenX, int screenY, Viewport viewport) {
        touchPoint.set(screenX, screenY, 0);
        viewport.unproject(touchPoint);
        float wx = touchPoint.x;
        float wy = touchPoint.y;

        Scene currentScene = sceneManager.getCurrentScene();
        if (currentScene == null) return false;

        List<GameObject> objects = currentScene.getGameObjects();
        if (objects == null || objects.isEmpty()) return false;

        // Duyệt ngược để object trên cùng (vẽ sau) được check trước
        for (int i = objects.size() - 1; i >= 0; i--) {
            GameObject obj = objects.get(i);

            if (!obj.hasComponent(BoundsComponent.class)) continue;

            BoundsComponent bounds = obj.getComponent(BoundsComponent.class);
            if (!bounds.hitbox.contains(wx, wy)) continue;

            Gdx.app.log("Interact", "Hit: " + obj.getId() + " at (" + (int) wx + ", " + (int) wy + ")");

            dispatch(obj);
            return true;
        }

        // Click trống — ẩn inspect text
        if (callback != null) {
            callback.onEmptyClick();
        }
        return false;
    }

    /**
     * Dispatch hành động dựa trên component của object.
     */
    private void dispatch(GameObject obj) {
        if (obj.hasComponent(CollectibleComponent.class)) {
            CollectibleComponent collectible = obj.getComponent(CollectibleComponent.class);
            if (!collectible.isCollected) {
                collectItem(obj, collectible);
                return;
            }
        }

        if (obj.hasComponent(ItemInfoComponent.class)) {
            ItemInfoComponent info = obj.getComponent(ItemInfoComponent.class);

            ItemData data = ItemDatabase.getInstance().getItemData(info.itemID);
            if (data != null && data.description != null) {
                Gdx.app.log("Examine", "Description: " + data.description);
            }

            if (info.inspectText != null && callback != null) {
                callback.onShowInspectText(info.inspectText);
            }
        }

        if ("door".equals(obj.getType())) {
            handleDoor(obj);
            return;
        }

        if (callback != null) {
            callback.onObjectInteracted(obj);
        }
    }

    /**
     * Flow nhặt item theo plan:
     * click collectible → addItem → setCollected → fire RSEvent
     */
    private void collectItem(GameObject obj, CollectibleComponent collectible) {
        ItemInfoComponent info = obj.getComponent(ItemInfoComponent.class);
        if (info == null || info.itemID == null) return;

        if (inventoryManager.getItems().size() >= 25) {
            if (callback != null) {
                callback.onInventoryFull();
            }
            return;
        }

        boolean isAdded = inventoryManager.addItem(info.itemID);

        if (isAdded) {
            collectible.isCollected = true;
            info.isPickedUp = true;

            puzzleManager.markItemCollected(info.itemID);

            if (obj.hasComponent(RSModifierComponent.class)) {
                RSModifierComponent rsMod = obj.getComponent(RSModifierComponent.class);
                if (rsMod.rsChangeValue != 0) {
                    rsManager.processEvent(new RSEvent(RSEventType.ITEM_INTERACTION, rsMod.rsChangeValue, info.itemID));
                }
            }

            if (obj.getEntity() != null) {
                obj.getEntity().removeAll();
            }

            sceneManager.getCurrentScene().getEngine().removeEntity(obj.getEntity());
            sceneManager.getCurrentScene().getGameObjects().remove(obj);

            if (callback != null) {
                callback.onItemCollected(obj, info.itemID);
            }
        }
    }

    /**
     * Xử lý door: lấy targetScene từ RoomData rồi callback chuyển scene.
     */
    private void handleDoor(GameObject obj) {
        if (callback == null) return;
        callback.onDoorInteracted(obj);
    }

}
