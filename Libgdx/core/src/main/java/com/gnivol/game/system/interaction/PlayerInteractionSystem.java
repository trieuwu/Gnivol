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

import java.util.List;

/**
 * Xử lý tương tác của người chơi với object trong scene.
 *
 * Flow: unproject mouse → loop objects → bounds.contains → dispatch hành động.
 * Theo plan tuần 3 (Tùng): nhặt item → addItem → setCollected → fire RSEvent.
 */
public class PlayerInteractionSystem {

    private final SceneManager sceneManager;
    private final InventoryManager inventoryManager;
    private final RSManager rsManager;

    private final Vector3 touchPoint = new Vector3();

    // Callback để GameScreen xử lý phần visual (inspect text, overlay, scene change)
    private InteractionCallback callback;

    public PlayerInteractionSystem(SceneManager sceneManager, InventoryManager inventoryManager, RSManager rsManager) {
        this.sceneManager = sceneManager;
        this.inventoryManager = inventoryManager;
        this.rsManager = rsManager;
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
        // 1. Inspect text
        if (obj.hasComponent(ItemInfoComponent.class)) {
            ItemInfoComponent info = obj.getComponent(ItemInfoComponent.class);
            if (info.inspectText != null && callback != null) {
                callback.onShowInspectText(info.inspectText);
            }
        }

        // 2. Nhặt item (collectible chưa bị nhặt)
        if (obj.hasComponent(CollectibleComponent.class)) {
            CollectibleComponent collectible = obj.getComponent(CollectibleComponent.class);
            if (!collectible.isCollected) {
                collectItem(obj, collectible);
                return; // Nhặt xong thì không dispatch thêm
            }
        }

        // 3. Door → chuyển scene
        if ("door".equals(obj.getType())) {
            handleDoor(obj);
            return;
        }

        // 4. Object có alt textures → mở overlay
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
        if (info == null) return;

        String itemId = info.itemID;
        if (itemId == null) return;

        // addItem vào inventory
        inventoryManager.addItem(itemId);
        Gdx.app.log("Interact", "Collected: " + itemId);

        // setCollected
        collectible.isCollected = true;
        info.isPickedUp = true;

        // Fire RSEvent nếu object có RSModifier
        if (obj.hasComponent(RSModifierComponent.class)) {
            RSModifierComponent rsMod = obj.getComponent(RSModifierComponent.class);
            if (rsMod.rsChangeValue != 0) {
                RSEvent event = new RSEvent(
                    RSEventType.ITEM_INTERACTION,
                    rsMod.rsChangeValue,
                    itemId
                );
                rsManager.processEvent(event);
            }
        }

        // Callback để GameScreen cập nhật visual (ẩn object, animation, sound...)
        if (callback != null) {
            callback.onItemCollected(obj, itemId);
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
