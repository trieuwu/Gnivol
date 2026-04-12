package com.gnivol.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.gnivol.game.Constants;
import com.gnivol.game.model.RoomData;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton quản lý việc load và cache dữ liệu game từ JSON.
 *
 * Sử dụng:
 *   RoomData data = DataManager.getInstance().loadRoomData("room_bedroom");
 *
 * Cache: Mỗi room chỉ load 1 lần từ file. Lần sau lấy từ cache.
 */
public class DataManager {

    // --- Singleton ---
    private static DataManager instance;

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    // --- Fields ---

    // LibGDX JSON parser — tái sử dụng, không tạo mới mỗi lần
    private final Json json;

    // Cache: roomId → RoomData (tránh load lại file mỗi lần chuyển phòng)
    private final Map<String, RoomData> roomCache;

    private DataManager() {
        this.json = new Json();
        // Bỏ qua các field trong JSON mà class không có — tránh crash
        this.json.setIgnoreUnknownFields(true);
        this.roomCache = new HashMap<>();
    }

    /**
     * Load dữ liệu phòng từ file JSON.
     *
     * Đường dẫn file: "data/rooms/{roomId}.json"
     * VD: roomId = "room_bedroom" → load "data/rooms/room_bedroom.json"
     *
     * @param roomId ID phòng (khớp với Constants.SCENE_BEDROOM, SCENE_BATHROOM...)
     * @return RoomData chứa danh sách object, hoặc null nếu file không tồn tại
     */
    public RoomData loadRoomData(String roomId) {
        // Kiểm tra cache trước
        if (roomCache.containsKey(roomId)) {
            Gdx.app.log("DataManager", "Loaded from cache: " + roomId);
            return roomCache.get(roomId);
        }

        // Tạo đường dẫn file
        String path = Constants.ASSETS_ROOMS + roomId + ".json";
        FileHandle file = Gdx.files.internal(path);

        // Kiểm tra file có tồn tại không
        if (!file.exists()) {
            Gdx.app.error("DataManager", "Room file not found: " + path);
            return null;
        }

        // Parse JSON → RoomData
        RoomData roomData = json.fromJson(RoomData.class, file.readString());
        Gdx.app.log("DataManager", "Loaded room: " + roomId
                + " (" + roomData.getObjects().size() + " objects)");

        // Lưu vào cache
        roomCache.put(roomId, roomData);

        return roomData;
    }

    /**
     * Xóa cache của 1 phòng (khi cần reload sau khi sửa JSON).
     */
    public void clearRoomCache(String roomId) {
        roomCache.remove(roomId);
    }

    /**
     * Xóa toàn bộ cache.
     */
    public void clearAllCache() {
        roomCache.clear();
    }
}