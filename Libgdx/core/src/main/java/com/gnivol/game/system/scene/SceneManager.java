package com.gnivol.game.system.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gnivol.game.data.DataManager;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.system.puzzle.PuzzleManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Quản lý lifecycle của các Scene (phòng) trong game.
 *
 * Hỗ trợ 2 cách chuyển cảnh:
 * 1. changeScene(id) — chuyển hẳn (exit scene cũ, enter scene mới)
 * 2. pushScene(id) / popScene() — chồng scene (scene cũ bị tạm dừng)
 *
 * Sử dụng:
 *   sceneManager.changeScene("room_bedroom");
 *   sceneManager.update(delta);
 *   sceneManager.render(batch);
 */
public class SceneManager {

    // Scene đang active (đang được update + render)
    private Scene currentScene;

    // Stack để push/pop scene (ví dụ: mở inventory overlay)
    private final Stack<Scene> sceneStack;

    // Cache: sceneId → RoomData (giữ trạng thái phòng đã thăm)
    private final Map<String, RoomData> roomDataMap;

    // Callback khi chuyển scene xong (dùng cho ScreenFader)
    private SceneChangeListener changeListener;

    private final PuzzleManager puzzleManager;

    public SceneManager(PuzzleManager puzzleManager) {
        this.puzzleManager = puzzleManager;
        this.sceneStack = new Stack<>();
        this.roomDataMap = new HashMap<>();
    }

    /**
     * Chuyển hẳn sang scene mới. Scene cũ bị exit() + dispose().
     *
     * Luồng: currentScene.exit() → load RoomData → tạo Scene mới → scene.enter()
     *
     * @param sceneId ID phòng (VD: "room_bedroom")
     */
    public void changeScene(String sceneId) {
        // 1. Exit scene cũ (nếu có)
        if (currentScene != null) {
            currentScene.exit();
            // Lưu RoomData đã thay đổi (vật đã nhặt, trạng thái...)
            roomDataMap.put(currentScene.getSceneId(), currentScene.getRoomData());
            currentScene.dispose();
        }

        // 2. Load RoomData (ưu tiên cache nội bộ, rồi mới từ DataManager)
        RoomData roomData = roomDataMap.get(sceneId);
        if (roomData == null) {
            roomData = DataManager.getInstance().loadRoomData(sceneId);
        }

        if (roomData == null) {
            Gdx.app.error("SceneManager", "Cannot load scene: " + sceneId);
            return;
        }

        // 3. Tạo scene mới và enter
        currentScene = new RoomScene(sceneId, roomData, puzzleManager);
        currentScene.enter();

        // 4. Thông báo listener (nếu có)
        if (changeListener != null) {
            changeListener.onSceneChanged(sceneId);
        }

        Gdx.app.log("SceneManager", "Changed to scene: " + sceneId);
    }

    /**
     * Push scene mới lên stack. Scene cũ bị TẠM DỪNG (không dispose).
     * Dùng khi muốn quay lại scene cũ (VD: mở puzzle overlay).
     */
    public void pushScene(String sceneId) {
        if (currentScene != null) {
            sceneStack.push(currentScene);
        }

        RoomData roomData = roomDataMap.get(sceneId);
        if (roomData == null) {
            roomData = DataManager.getInstance().loadRoomData(sceneId);
        }

        if (roomData != null) {
            currentScene = new RoomScene(sceneId, roomData, puzzleManager);
            currentScene.enter();
        }
    }

    /**
     * Pop scene trên cùng, quay lại scene trước đó.
     */
    public void popScene() {
        if (currentScene != null) {
            currentScene.exit();
            currentScene.dispose();
        }

        if (!sceneStack.isEmpty()) {
            currentScene = sceneStack.pop();
            Gdx.app.log("SceneManager", "Popped back to: " + currentScene.getSceneId());
        } else {
            currentScene = null;
        }
    }

    /**
     * Update scene hiện tại. Gọi mỗi frame từ GameScreen.
     */
    public void update(float delta) {
        if (currentScene != null) {
            currentScene.update(delta);
        }
    }

    /**
     * Render scene hiện tại. Gọi mỗi frame từ GameScreen.
     */
    public void render(SpriteBatch batch) {
        if (currentScene != null) {
            currentScene.render(batch);
        }
    }

    /**
     * Dispose tất cả. Gọi khi thoát game.
     */
    public void dispose() {
        if (currentScene != null) {
            currentScene.exit();
            currentScene.dispose();
        }
        for (Scene scene : sceneStack) {
            scene.dispose();
        }
        sceneStack.clear();
        roomDataMap.clear();
    }

    // --- Getter ---

    public Scene getCurrentScene() {
        return currentScene;
    }

    // --- Listener interface ---

    public interface SceneChangeListener {
        void onSceneChanged(String newSceneId);
    }

    public void setChangeListener(SceneChangeListener listener) {
        this.changeListener = listener;
    }
}
