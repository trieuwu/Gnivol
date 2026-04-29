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

    private Texture jumpscareTexture;
    private float jumpscareTimer = 0f;
    private boolean isJumpscareActive = false;
    private float currentJumpscareInterval = 10f;
    private static final float JUMPSCARE_DURATION = 1f;

    private Texture ghostMirrorTex;
    private Texture brokenMirrorTex;
    private boolean isMirrorBreakingEffect = false;
    private boolean isMirrorAlreadyBroken = false;
    private float mirrorEffectTimer = 0f;
    private float mirrorFlashTimer = 0f;
    private boolean showGhostFlash = false;

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
        if (sceneId.equals("room_bedroom")) {
            jumpscareTexture = new Texture(Gdx.files.internal("images/back_ground/room/new_blank_room_1.png"));
            currentJumpscareInterval = com.badlogic.gdx.math.MathUtils.random(10f, 20f);
        }
        else if(sceneId.equals("room_bathroom")) {
            ghostMirrorTex = new Texture(Gdx.files.internal("images/back_ground/bathroom/bathroom_ghost_mirror.png"));
            brokenMirrorTex = new Texture(Gdx.files.internal("images/back_ground/bathroom/bathroom_break_mirror.png"));

            jumpscareTexture = ghostMirrorTex;

            com.gnivol.game.GnivolGame game = (com.gnivol.game.GnivolGame) Gdx.app.getApplicationListener();
            isMirrorAlreadyBroken = game.getInventoryManager().hasItem("glass_shard");

            currentJumpscareInterval = com.badlogic.gdx.math.MathUtils.random(3f, 10f);
        }
        //
        if (roomData != null && roomData.getObjects() != null) {
            for (RoomData.RoomObject objData : roomData.getObjects()) {
                if (objData.id == null || objData.id.trim().isEmpty()) {
                    Gdx.app.error("RoomScene", "🚨 LỖI JSON: Phát hiện một object bị thiếu 'id' ở phòng: " + sceneId);
                    continue;
                }

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
    public void update(float delta) {
        if (isMirrorBreakingEffect) {
            mirrorEffectTimer += delta;
            mirrorFlashTimer += delta;

            if (mirrorFlashTimer >= 0.5f) {
                mirrorFlashTimer = 0;
                showGhostFlash = !showGhostFlash;
            }

            if (mirrorEffectTimer >= 3.0f) {
                isMirrorBreakingEffect = false;
                isMirrorAlreadyBroken = true;
            }
            return;
        }

        if (jumpscareTexture != null) {
            jumpscareTimer += delta;
            if (!isJumpscareActive && jumpscareTimer >= currentJumpscareInterval) {
                isJumpscareActive = true;
                jumpscareTimer = 0f;
            } else if (isJumpscareActive && jumpscareTimer >= JUMPSCARE_DURATION) {
                isJumpscareActive = false;
                jumpscareTimer = 0f;

                if (sceneId.equals("room_bedroom")) {
                    currentJumpscareInterval = com.badlogic.gdx.math.MathUtils.random(10f, 20f);
                }
                else if (sceneId.equals("room_bathroom")) {
                    com.gnivol.game.GnivolGame game = (com.gnivol.game.GnivolGame) Gdx.app.getApplicationListener();
                    isMirrorAlreadyBroken = game.getInventoryManager().hasItem("glass_shard");
                    currentJumpscareInterval = com.badlogic.gdx.math.MathUtils.random(3f, 10f);
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Texture currentBg;
        if (isMirrorAlreadyBroken) {
            currentBg = brokenMirrorTex;
        } else if (isMirrorBreakingEffect) {
            currentBg = showGhostFlash ? ghostMirrorTex : backgroundTexture;
        } else {
            currentBg = (isJumpscareActive && jumpscareTexture != null) ? jumpscareTexture : backgroundTexture;
        }
        if (currentBg != null) {
            batch.draw(currentBg, -BG_MARGIN, -BG_MARGIN,
                com.gnivol.game.Constants.WORLD_WIDTH + BG_MARGIN * 2,
                com.gnivol.game.Constants.WORLD_HEIGHT + BG_MARGIN * 2);
        }

        if (roomData == null || roomData.getObjects() == null) return;

        for (RoomData.RoomObject objData : roomData.getObjects()) {
            if (objData.id == null) continue;
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

    public void changeBackground(String newBackgroundPath) {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        backgroundTexture = new Texture(Gdx.files.internal(newBackgroundPath));
        roomData.setBackground(newBackgroundPath);
    }

    public void startMirrorBreakEvent() {
        if (!isMirrorAlreadyBroken && !isMirrorBreakingEffect) {
            isMirrorBreakingEffect = true;
            mirrorEffectTimer = 0;
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
        if (jumpscareTexture != null) jumpscareTexture.dispose();
        if (ghostMirrorTex != null) ghostMirrorTex.dispose();
        if (brokenMirrorTex != null) brokenMirrorTex.dispose();

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
