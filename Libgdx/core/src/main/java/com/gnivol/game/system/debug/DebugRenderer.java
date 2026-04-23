package com.gnivol.game.system.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.component.BoundsComponent;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.system.FontManager;
import com.gnivol.game.system.scene.Scene;
import com.gnivol.game.ui.InventoryOverlay;

import java.util.List;

public class DebugRenderer {
    private final GnivolGame game;
    private final ShapeRenderer debugShapeRenderer;

    private boolean debugMode = false;

    // Room object drag
    private RoomData.RoomObject dragTarget;
    private boolean dragResizing;
    private float dragOffsetX, dragOffsetY;

    // Overlay item drag
    private boolean draggingOverlayItem = false;
    private InventoryOverlay.OverlayItem draggingItem = null;

    // Cutscene sprite drag
    private boolean draggingCutsceneSprite = false;
    private float[] cutsceneSpriteRect; // reference to {x, y, w, h} in GameScreen

    public DebugRenderer(GnivolGame game) {
        this.game = game;
        this.debugShapeRenderer = new ShapeRenderer();
    }

    public boolean isDebugMode() { return debugMode; }
    public void toggleDebugMode() { debugMode = !debugMode; }
    public boolean hasDragTarget() { return dragTarget != null; }
    public boolean isDraggingOverlayItem() { return draggingOverlayItem; }
    public InventoryOverlay.OverlayItem getDraggingItem() { return draggingItem; }
    public boolean isDraggingCutsceneSprite() { return draggingCutsceneSprite; }
    public void clearDrag() { dragTarget = null; draggingOverlayItem = false; draggingItem = null; draggingCutsceneSprite = false; cutsceneSpriteRect = null; }

    // --- Room object debug ---

    public void handleDebugClick(int screenX, int screenY, OrthographicCamera camera, Viewport viewport, Scene scene) {
        if (scene == null || scene.getRoomData() == null) return;
        Vector3 world = new Vector3(screenX, screenY, 0);
        camera.unproject(world, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        List<RoomData.RoomObject> objs = scene.getRoomData().getObjects();
        for (int i = objs.size() - 1; i >= 0; i--) {
            RoomData.RoomObject obj = objs.get(i);
            if (world.x >= obj.x && world.x <= obj.x + obj.w && world.y >= obj.y && world.y <= obj.y + obj.h) {
                dragTarget = obj;
                dragResizing = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                dragOffsetX = world.x - obj.x;
                dragOffsetY = world.y - obj.y;
                break;
            }
        }
    }

    public void handleDebugDrag(int screenX, int screenY, OrthographicCamera camera, Viewport viewport, Scene scene) {
        if (dragTarget == null) return;
        Vector3 world = new Vector3(screenX, screenY, 0);
        camera.unproject(world, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        if (dragResizing) {
            dragTarget.w = Math.max(10, world.x - dragTarget.x);
            dragTarget.h = Math.max(10, world.y - dragTarget.y);
        } else {
            dragTarget.x = world.x - dragOffsetX;
            dragTarget.y = world.y - dragOffsetY;
        }

        if (scene != null) {
            GameObject go = scene.findObjectById(dragTarget.id);
            if (go != null && go.getComponent(BoundsComponent.class) != null) {
                go.getComponent(BoundsComponent.class).hitbox.set(dragTarget.x, dragTarget.y, dragTarget.w, dragTarget.h);
            }
        }
    }

    // --- Overlay item debug ---

    public boolean handleOverlayItemClick(float relX, float relY, InventoryOverlay.OverlayData overlayData) {
        if (overlayData == null) return false;
        for (InventoryOverlay.OverlayItem item : overlayData.items) {
            if (relX >= item.x && relX <= item.x + item.w && relY >= item.y && relY <= item.y + item.h) {
                draggingOverlayItem = true;
                draggingItem = item;
                dragResizing = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
                dragOffsetX = relX - item.x;
                dragOffsetY = relY - item.y;
                return true;
            }
        }
        return false;
    }

    public void handleOverlayItemDrag(float relX, float relY) {
        if (draggingItem == null) return;
        if (dragResizing) {
            draggingItem.w = Math.max(8f, relX - draggingItem.x);
            draggingItem.h = Math.max(8f, relY - draggingItem.y);
        } else {
            draggingItem.x = relX - dragOffsetX;
            draggingItem.y = relY - dragOffsetY;
        }
    }

    public void finishOverlayItemDrag() {
        if (draggingOverlayItem && draggingItem != null) {
            Gdx.app.log("OverlayDebug",
                    draggingItem.itemId + " → x=" + (int) draggingItem.x + " y=" + (int) draggingItem.y
                    + " w=" + (int) draggingItem.w + " h=" + (int) draggingItem.h);
        }
        draggingOverlayItem = false;
        draggingItem = null;
    }

    // --- Cutscene sprite debug ---

    public boolean handleCutsceneSpriteClick(float worldX, float worldY, float[] rect) {
        if (rect == null) return false;
        if (worldX >= rect[0] && worldX <= rect[0] + rect[2]
                && worldY >= rect[1] && worldY <= rect[1] + rect[3]) {
            draggingCutsceneSprite = true;
            cutsceneSpriteRect = rect;
            dragResizing = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
            dragOffsetX = worldX - rect[0];
            dragOffsetY = worldY - rect[1];
            return true;
        }
        return false;
    }

    public void handleCutsceneSpriteDrag(float worldX, float worldY) {
        if (cutsceneSpriteRect == null) return;
        if (dragResizing) {
            cutsceneSpriteRect[2] = Math.max(10f, worldX - cutsceneSpriteRect[0]);
            cutsceneSpriteRect[3] = Math.max(10f, worldY - cutsceneSpriteRect[1]);
        } else {
            cutsceneSpriteRect[0] = worldX - dragOffsetX;
            cutsceneSpriteRect[1] = worldY - dragOffsetY;
        }
    }

    public void finishCutsceneSpriteDrag() {
        if (draggingCutsceneSprite && cutsceneSpriteRect != null) {
            Gdx.app.log("CutsceneDebug",
                    "Sprite → x:" + (int) cutsceneSpriteRect[0] + " y:" + (int) cutsceneSpriteRect[1]
                    + " w:" + (int) cutsceneSpriteRect[2] + " h:" + (int) cutsceneSpriteRect[3]);
        }
        draggingCutsceneSprite = false;
        cutsceneSpriteRect = null;
    }

    public void renderCutsceneSprite(SpriteBatch batch, OrthographicCamera camera, Viewport viewport, float[] rect) {
        if (!debugMode || rect == null) return;

        FontManager fm = game.getFontManager();
        Vector3 mouseW = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseW, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        boolean hovered = mouseW.x >= rect[0] && mouseW.x <= rect[0] + rect[2]
                && mouseW.y >= rect[1] && mouseW.y <= rect[1] + rect[3];

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        debugShapeRenderer.setProjectionMatrix(camera.combined);
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);
        debugShapeRenderer.setColor(hovered ? Color.YELLOW : Color.CYAN);
        debugShapeRenderer.rect(rect[0], rect[1], rect[2], rect[3]);
        debugShapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        fm.fontDebug.setColor(hovered ? Color.YELLOW : Color.CYAN);
        String info = "sprite [" + (int) rect[0] + "," + (int) rect[1]
                + " " + (int) rect[2] + "x" + (int) rect[3] + "]";
        fm.fontDebug.draw(batch, info, rect[0], rect[1] + rect[3] + 16);

        if (draggingCutsceneSprite && cutsceneSpriteRect != null) {
            fm.fontDebug.setColor(Color.CYAN);
            String dragInfo = (dragResizing ? "RESIZE" : "MOVE")
                    + " sprite \u2192 x:" + (int) cutsceneSpriteRect[0] + " y:" + (int) cutsceneSpriteRect[1]
                    + " w:" + (int) cutsceneSpriteRect[2] + " h:" + (int) cutsceneSpriteRect[3];
            fm.fontDebug.draw(batch, dragInfo, rect[0], rect[1] + rect[3] + 34);
        }
        batch.end();
    }

    // --- Render room objects ---

    public void render(SpriteBatch batch, OrthographicCamera camera, Viewport viewport, Scene currentScene) {
        if (!debugMode || currentScene == null || currentScene.getRoomData() == null) return;

        FontManager fm = game.getFontManager();
        Vector3 mouseWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        debugShapeRenderer.setProjectionMatrix(camera.combined);
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);

        for (RoomData.RoomObject obj : currentScene.getRoomData().objects) {
            boolean hovered = mouseWorld.x >= obj.x && mouseWorld.x <= obj.x + obj.w && mouseWorld.y >= obj.y && mouseWorld.y <= obj.y + obj.h;
            debugShapeRenderer.setColor(hovered ? Color.YELLOW : Color.RED);
            debugShapeRenderer.rect(obj.x, obj.y, obj.w, obj.h);
        }

        debugShapeRenderer.setColor(Color.GREEN);
        debugShapeRenderer.line(mouseWorld.x - 10, mouseWorld.y, mouseWorld.x + 10, mouseWorld.y);
        debugShapeRenderer.line(mouseWorld.x, mouseWorld.y - 10, mouseWorld.x, mouseWorld.y + 10);
        debugShapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.begin();
        for (RoomData.RoomObject obj : currentScene.getRoomData().objects) {
            boolean hovered = mouseWorld.x >= obj.x && mouseWorld.x <= obj.x + obj.w && mouseWorld.y >= obj.y && mouseWorld.y <= obj.y + obj.h;
            fm.fontDebug.setColor(hovered ? Color.YELLOW : Color.RED);
            String info = obj.id + " [" + (int) obj.x + "," + (int) obj.y + " " + (int) obj.w + "x" + (int) obj.h + "]";
            fm.fontDebug.draw(batch, info, obj.x, obj.y + obj.h + 16);
        }

        fm.fontDebug.setColor(Color.GREEN);
        fm.fontDebug.draw(batch, "Mouse: " + (int) mouseWorld.x + ", " + (int) mouseWorld.y, 10, Constants.WORLD_HEIGHT - 10);
        fm.fontDebug.draw(batch, "[F1] Toggle | [F2] Export | Drag=Move | Shift+Drag=Resize", 10, Constants.WORLD_HEIGHT - 28);
        fm.fontDebug.draw(batch, "Room: " + currentScene.getSceneId(), 10, Constants.WORLD_HEIGHT - 46);

        if (dragTarget != null) {
            fm.fontDebug.setColor(Color.CYAN);
            String dragInfo = (dragResizing ? "RESIZE " : "MOVE ") + dragTarget.id
                + " \u2192 x:" + (int) dragTarget.x + " y:" + (int) dragTarget.y
                + " w:" + (int) dragTarget.w + " h:" + (int) dragTarget.h;
            fm.fontDebug.draw(batch, dragInfo, 10, Constants.WORLD_HEIGHT - 64);
        }
        batch.end();
    }

    // --- Render overlay items ---

    public void renderOverlayItems(SpriteBatch batch, OrthographicCamera camera, Viewport viewport,
                                    Texture overlayTexture, InventoryOverlay.OverlayData overlayData) {
        if (!debugMode || overlayData == null || overlayTexture == null) return;

        FontManager fm = game.getFontManager();
        float maxW = 700f, maxH = 550f;
        float imgW = overlayTexture.getWidth(), imgH = overlayTexture.getHeight();
        float scale = Math.min(maxW / imgW, maxH / imgH);
        float drawW = imgW * scale, drawH = imgH * scale;
        float drawX = (1280 - drawW) / 2f;
        float drawY = (720 - drawH) / 2f;

        Vector3 mouseW = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseW, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        float mouseRelX = (mouseW.x - drawX) / scale;
        float mouseRelY = (mouseW.y - drawY) / scale;

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        debugShapeRenderer.setProjectionMatrix(camera.combined);
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2f);

        for (InventoryOverlay.OverlayItem item : overlayData.items) {
            boolean collected = game.getInventoryManager().hasItem(item.itemId);
            boolean hovered = mouseRelX >= item.x && mouseRelX <= item.x + item.w
                    && mouseRelY >= item.y && mouseRelY <= item.y + item.h;
            debugShapeRenderer.setColor(collected ? Color.GRAY : (hovered ? Color.YELLOW : Color.RED));
            debugShapeRenderer.rect(drawX + item.x * scale, drawY + item.y * scale, item.w * scale, item.h * scale);
        }

        debugShapeRenderer.setColor(Color.GREEN);
        debugShapeRenderer.line(mouseW.x - 10, mouseW.y, mouseW.x + 10, mouseW.y);
        debugShapeRenderer.line(mouseW.x, mouseW.y - 10, mouseW.x, mouseW.y + 10);
        debugShapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (InventoryOverlay.OverlayItem item : overlayData.items) {
            boolean collected = game.getInventoryManager().hasItem(item.itemId);
            boolean hovered = mouseRelX >= item.x && mouseRelX <= item.x + item.w
                    && mouseRelY >= item.y && mouseRelY <= item.y + item.h;
            fm.fontDebug.setColor(collected ? Color.GRAY : (hovered ? Color.YELLOW : Color.RED));
            String info = item.itemId + " [" + (int) item.x + "," + (int) item.y
                    + " " + (int) item.w + "x" + (int) item.h + "]"
                    + (collected ? " (COLLECTED)" : "");
            fm.fontDebug.draw(batch, info, drawX + item.x * scale, drawY + (item.y + item.h) * scale + 16);
        }

        fm.fontDebug.setColor(Color.GREEN);
        fm.fontDebug.draw(batch, "Mouse: " + (int) mouseRelX + ", " + (int) mouseRelY, drawX, drawY + drawH + 16);
        fm.fontDebug.draw(batch, "[F1] Toggle | Drag=Move | Shift+Drag=Resize", drawX, drawY + drawH + 34);

        if (draggingOverlayItem && draggingItem != null) {
            fm.fontDebug.setColor(Color.CYAN);
            String dragInfo = (dragResizing ? "RESIZE " : "MOVE ") + draggingItem.itemId
                    + " \u2192 x:" + (int) draggingItem.x + " y:" + (int) draggingItem.y
                    + " w:" + (int) draggingItem.w + " h:" + (int) draggingItem.h;
            fm.fontDebug.draw(batch, dragInfo, drawX, drawY + drawH + 52);
        }
        batch.end();
    }

    // --- Export ---

    public void exportDebugCoordinates(Scene scene) {
        if (scene == null || scene.getRoomData() == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== ").append(scene.getSceneId()).append(" ==========\n");
        for (RoomData.RoomObject obj : scene.getRoomData().getObjects()) {
            sb.append(String.format("  \"%s\": { \"x\": %d, \"y\": %d, \"w\": %d, \"h\": %d }%n",
                obj.id, (int) obj.x, (int) obj.y, (int) obj.w, (int) obj.h));
        }
        sb.append("==========================================");
        Gdx.app.log("Debug-Export", sb.toString());
    }

    public void dispose() {
        if (debugShapeRenderer != null) debugShapeRenderer.dispose();
    }
}
