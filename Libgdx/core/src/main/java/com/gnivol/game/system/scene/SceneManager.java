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
    private com.gnivol.game.audio.AudioManager audioManager;
    private final Map<String, String> sceneBgmMap = new HashMap<String, String>();

    public SceneManager(PuzzleManager puzzleManager) {
        this.puzzleManager = puzzleManager;
        this.sceneStack = new Stack<>();
        this.roomDataMap = new HashMap<>();
    }

    public void setAudioManager(com.gnivol.game.audio.AudioManager am) {
        this.audioManager = am;
    }

    public void setSceneBGM(String sceneId, String bgmId) {
        sceneBgmMap.put(sceneId, bgmId);
    }

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

        if (audioManager != null) {
            String bgmId = sceneBgmMap.get(sceneId);
            if (bgmId != null) {
                audioManager.crossfadeBGM(bgmId, 1.5f);
            }
        }

        Gdx.app.log("SceneManager", "Changed to scene: " + sceneId);
    }

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

    public void update(float delta) {
        if (currentScene != null) {
            currentScene.update(delta);
        }
    }

    public void render(SpriteBatch batch) {
        if (currentScene != null) {
            currentScene.render(batch);
        }
    }


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

    public void reset() {
        if (currentScene != null) {
            currentScene.exit();
            currentScene.dispose();
            currentScene = null;
        }
        sceneStack.clear();
        roomDataMap.clear();
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public interface SceneChangeListener {
        void onSceneChanged(String newSceneId);
    }

    public void setChangeListener(SceneChangeListener listener) {
        this.changeListener = listener;
    }
}
