package com.gnivol.game.system.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.component.BoundsComponent;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.screen.GameScreen;
import com.gnivol.game.system.FontManager;
import com.gnivol.game.system.scene.Scene;

import java.util.List;

public class DebugRenderer {
    private final GameScreen screen;
    private final GnivolGame game;
    private final ShapeRenderer debugShapeRenderer;

    private boolean debugMode = false;
    private RoomData.RoomObject dragTarget;
    private boolean dragResizing;
    private float dragOffsetX, dragOffsetY;

    public DebugRenderer(GameScreen screen) {
        this.screen = screen;
        this.game = screen.getGnivolGame();
        this.debugShapeRenderer = new ShapeRenderer();
    }

    public boolean isDebugMode() { return debugMode; }
    public void toggleDebugMode() { debugMode = !debugMode; }
    public boolean hasDragTarget() { return dragTarget != null; }
    public void clearDrag() { dragTarget = null; }

    public void handleDebugClick(int screenX, int screenY, OrthographicCamera camera, Viewport viewport) {
        Vector3 world = new Vector3(screenX, screenY, 0);
        camera.unproject(world, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        Scene scene = screen.getSceneManager().getCurrentScene();
        if (scene == null || scene.getRoomData() == null) return;

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

    public void handleDebugDrag(int screenX, int screenY, OrthographicCamera camera, Viewport viewport) {
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

        Scene scene = screen.getSceneManager().getCurrentScene();
        if (scene != null) {
            GameObject go = scene.findObjectById(dragTarget.id);
            if (go != null && go.getComponent(BoundsComponent.class) != null) {
                go.getComponent(BoundsComponent.class).hitbox.set(dragTarget.x, dragTarget.y, dragTarget.w, dragTarget.h);
            }
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, Viewport viewport) {
        if (!debugMode) return;

        Scene currentScene = screen.getSceneManager().getCurrentScene();
        if (currentScene == null || currentScene.getRoomData() == null) return;

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
                + " → x:" + (int) dragTarget.x + " y:" + (int) dragTarget.y
                + " w:" + (int) dragTarget.w + " h:" + (int) dragTarget.h;
            fm.fontDebug.draw(batch, dragInfo, 10, Constants.WORLD_HEIGHT - 64);
        }
        batch.end();
    }

    public void exportDebugCoordinates() {
        Scene scene = screen.getSceneManager().getCurrentScene();
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
