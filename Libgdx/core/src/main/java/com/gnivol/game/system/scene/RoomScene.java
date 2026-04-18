package com.gnivol.game.system.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gnivol.game.component.BoundsComponent;
import com.gnivol.game.component.CollectibleComponent;
import com.gnivol.game.component.GlitchComponent;
import com.gnivol.game.component.ItemInfoComponent;
import com.gnivol.game.component.RSModifierComponent;
import com.gnivol.game.component.TransformComponent;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.model.RoomData;

import java.util.HashMap;
import java.util.Map;

public class RoomScene extends Scene {

    private Texture backgroundTexture;
    private final Map<String, Texture> objectTextures;

    // Texture thay thế khi object đổi trạng thái (VD: tủ đóng → tủ mở)
    private final Map<String, Texture> altTextures;

    // Trạng thái object: objectId → state ("default", "open", "taken"...)
    private final Map<String, String> objectStates;

    private static final float BG_MARGIN = 40f;

    private final com.gnivol.game.system.puzzle.PuzzleManager puzzleManager;

    public RoomScene(String sceneId, RoomData roomData, com.gnivol.game.system.puzzle.PuzzleManager puzzleManager) {
        super(sceneId, roomData);
        this.puzzleManager = puzzleManager;
        this.objectTextures = new HashMap<>();
        this.altTextures = new HashMap<>();
        this.objectStates = new HashMap<>();
    }

    @Override
    public void enter() {
        if (roomData.getBackground() != null) {
            backgroundTexture = new Texture(Gdx.files.internal(roomData.getBackground()));
        }

        if (roomData != null && roomData.getObjects() != null) {
            for (RoomData.RoomObject objData : roomData.getObjects()) {
                if (objData.properties != null && objData.properties.itemId != null) {
                    if (puzzleManager.isItemCollected(objData.properties.itemId)) {
                        continue;
                    }
                }

                GameObject obj = new GameObject(objData.id, objData.type);

                TransformComponent transform = new TransformComponent();
                transform.position.set(objData.x, objData.y);
                obj.addComponent(transform);

                BoundsComponent bounds = new BoundsComponent();
                bounds.hitbox.set(objData.x, objData.y, objData.w, objData.h);
                obj.addComponent(bounds);

                // Gắn component dựa trên properties từ room JSON
                if (objData.properties != null) {
                    RoomData.Properties props = objData.properties;

                    // ItemInfoComponent — cho object có itemId hoặc inspectText
                    if (props.itemId != null || props.inspectText != null) {
                        ItemInfoComponent info = new ItemInfoComponent();
                        info.itemID = props.itemId;
                        info.inspectText = props.inspectText;
                        obj.addComponent(info);
                    }

                    // CollectibleComponent — object nhặt được
                    if (props.collectible) {
                        CollectibleComponent collectible = new CollectibleComponent();
                        obj.addComponent(collectible);
                    }

                    // RSModifierComponent — object thay đổi RS khi tương tác
                    if (props.rsChange != 0) {
                        RSModifierComponent rsMod = new RSModifierComponent();
                        rsMod.rsChangeValue = props.rsChange;
                        obj.addComponent(rsMod);
                    }

                    // GlitchComponent — object có hiệu ứng glitch
                    if (props.hasGlitch) {
                        GlitchComponent glitch = new GlitchComponent();
                        obj.addComponent(glitch);
                    }
                }

                gameObjects.add(obj);
                objectStates.put(objData.id, "default");

                // Load texture chính
                if (objData.texture != null) {
                    try {
                        objectTextures.put(objData.id,
                                new Texture(Gdx.files.internal(objData.texture)));
                    } catch (Exception e) {
                        Gdx.app.error("RoomScene", "Cannot load texture: " + objData.texture, e);
                    }
                }

                // Load alt textures nếu có
                if (objData.properties != null && objData.properties.altTextures != null) {
                    for (Map.Entry<String, String> entry : objData.properties.altTextures.entrySet()) {
                        try {
                            altTextures.put(objData.id + ":" + entry.getKey(),
                                    new Texture(Gdx.files.internal(entry.getValue())));
                        } catch (Exception e) {
                            Gdx.app.error("RoomScene", "Cannot load alt texture: " + entry.getValue(), e);
                        }
                    }
                }
            }
        }

        Gdx.app.log("RoomScene", "Entered scene: " + sceneId
                + " with " + gameObjects.size() + " objects, "
                + objectTextures.size() + " textures, "
                + altTextures.size() + " alt textures");
    }

    @Override
    public void update(float delta) {}

    @Override
    public void render(SpriteBatch batch) {
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture,
                    -BG_MARGIN, -BG_MARGIN,
                    1280 + BG_MARGIN * 2, 720 + BG_MARGIN * 2);
        }

        if (roomData == null || roomData.getObjects() == null) return;
        for (RoomData.RoomObject objData : roomData.getObjects()) {
            if (findObjectById(objData.id) == null) continue;

            String state = objectStates.getOrDefault(objData.id, "default");
            Texture tex;
            if ("default".equals(state)) {
                tex = objectTextures.get(objData.id);
            } else {
                tex = altTextures.get(objData.id + ":" + state);
                if (tex == null) tex = objectTextures.get(objData.id); // fallback
            }

            if (tex != null) {
                float s = objData.getEffectiveScale();
                float drawW = (1280 + BG_MARGIN * 2) * s;
                float drawH = (720 + BG_MARGIN * 2) * s;
                float drawX = (1280 - drawW) / 2f + objData.offsetX;
                float drawY = (720 - drawH) / 2f + objData.offsetY;
                batch.draw(tex, drawX, drawY, drawW, drawH);
            }
        }
    }

    /**
     * Đổi trạng thái visual của object.
     * VD: setObjectState("wardrobe", "open") → swap sang altTexture "open"
     */
    public void setObjectState(String objectId, String state) {
        objectStates.put(objectId, state);
        Gdx.app.log("RoomScene", objectId + " → state: " + state);
    }

    public String getObjectState(String objectId) {
        return objectStates.getOrDefault(objectId, "default");
    }

    @Override
    public void exit() {
        Gdx.app.log("RoomScene", "Exiting scene: " + sceneId);
    }

    @Override
    public void dispose() {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }
        for (Texture tex : objectTextures.values()) {
            tex.dispose();
        }
        for (Texture tex : altTextures.values()) {
            tex.dispose();
        }
        objectTextures.clear();
        altTextures.clear();
        objectStates.clear();
        gameObjects.clear();
        Gdx.app.log("RoomScene", "Disposed scene: " + sceneId);
    }
}
