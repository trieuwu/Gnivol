package com.gnivol.game.system.scene;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.model.RoomData;

import java.util.ArrayList;
import java.util.List;

/**
 * Đại diện cho một cảnh/phòng trong game.
 *
 * Vòng đời (lifecycle):
 *   enter() → [update() + render()] lặp mỗi frame → exit() → dispose()
 *
 * - enter():   Gọi 1 lần khi bắt đầu vào cảnh — setup tài nguyên
 * - update():  Gọi mỗi frame — cập nhật logic (animation, AI, timer...)
 * - render():  Gọi mỗi frame SAU update — vẽ mọi thứ lên màn hình
 * - exit():    Gọi 1 lần khi rời cảnh — lưu trạng thái nếu cần
 * - dispose(): Gọi khi cảnh bị hủy hoàn toàn — giải phóng tài nguyên
 */
public abstract class Scene {

    // ID của scene (VD: "room_bedroom") — dùng để SceneManager tra cứu
    protected final String sceneId;

    // Dữ liệu phòng được load từ JSON
    protected final RoomData roomData;

    // Danh sách tất cả vật thể trong phòng
    protected final List<GameObject> gameObjects;

    // Đánh dấu scene là overlay (VD: inventory, pause menu) — vẽ đè lên scene bên dưới thay vì thay thế
    protected boolean isOverlay = false;

    public Scene(String sceneId, RoomData roomData) {
        this.sceneId = sceneId;
        this.roomData = roomData;
        this.gameObjects = new ArrayList<>();
    }

    /**
     * Gọi 1 lần khi scene được kích hoạt.
     * Tạo GameObject từ RoomData, load texture, setup input.
     */
    public abstract void enter();

    /**
     * Gọi mỗi frame. Cập nhật logic game (animation, timer, AI).
     * @param delta thời gian (giây) kể từ frame trước
     */
    public abstract void update(float delta);

    /**
     * Gọi mỗi frame SAU update(). Vẽ background + tất cả object.
     * @param batch SpriteBatch dùng chung — đã begin() từ bên ngoài
     */
    public abstract void render(SpriteBatch batch);

    /**
     * Gọi 1 lần khi rời scene (chuyển sang scene khác).
     * Lưu trạng thái object (đã nhặt chưa, vị trí...) nếu cần.
     */
    public abstract void exit();

    /**
     * Gọi khi scene bị hủy hoàn toàn. Giải phóng texture, sound...
     */
    public abstract void dispose();

    // --- Getter ---

    public String getSceneId() {
        return sceneId;
    }

    public RoomData getRoomData() {
        return roomData;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public boolean isOverlay() {
        return isOverlay;
    }

    /**
     * Tìm GameObject theo ID.
     * @return GameObject hoặc null nếu không tìm thấy
     */
    public GameObject findObjectById(String id) {
        for (GameObject obj : gameObjects) {
            if (obj.getId().equals(id)) {
                return obj;
            }
        }
        return null;
    }
}
