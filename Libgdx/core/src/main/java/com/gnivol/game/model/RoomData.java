package com.gnivol.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model chứa dữ liệu một phòng, được parse từ room JSON.
 *
 * Cấu trúc JSON tương ứng:
 * {
 *   "roomId": "room_bedroom",
 *   "roomName": "Phòng ngủ",
 *   "background": "textures/bg_bedroom.png",
 *   "objects": [
 *     { "id": "bed", "type": "furniture", "x": 100, "y": 200, "w": 300, "h": 150 }
 *   ]
 * }
 */
public class RoomData {

    // --- Fields khớp tên với JSON ---
    public String roomId;
    public String roomName;
    public String background;  // đường dẫn tới ảnh background
    public List<RoomObject> objects;

    /** Constructor mặc định — BẮT BUỘC cho JSON parser */
    public RoomData() {
        this.objects = new ArrayList<>();
    }

    // --- Getter/Setter ---

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public List<RoomObject> getObjects() {
        return objects;
    }

    public void setObjects(List<RoomObject> objects) {
        this.objects = objects;
    }

    /**
     * Đại diện cho 1 vật thể trong phòng.
     * Static inner class — JSON parser cần class có constructor mặc định.
     */
    public static class RoomObject {
        public String id;         // ID duy nhất, VD: "bed", "phone", "door_bathroom"
        public String type;       // Loại object: "furniture", "item", "door", "interactable"
        public String texture;    // Đường dẫn texture, VD: "textures/objects/bed.png" (null nếu không có sprite riêng)
        public float x;           // Vị trí X hitbox (pixel, góc dưới-trái)
        public float y;           // Vị trí Y hitbox (pixel, góc dưới-trái)
        public float w;           // Chiều rộng hitbox
        public float h;           // Chiều cao hitbox

        // Render parameters — điều chỉnh vẽ sprite full-canvas
        // scale < 1.0 = thu nhỏ, offsetX/Y = dịch chuyển (pixel)
        public float scale;       // Scale sprite (default 0 → dùng 1.0)
        public float offsetX;     // Dịch ngang khi render
        public float offsetY;     // Dịch dọc khi render

        public Properties properties;  // Thuộc tính riêng tùy loại object

        /** Constructor mặc định — BẮT BUỘC cho JSON parser */
        public RoomObject() {}

        /** Lấy scale thực tế (0 trong JSON = chưa set = 1.0) */
        public float getEffectiveScale() {
            return scale > 0 ? scale : 1.0f;
        }
    }

    /**
     * Thuộc tính mở rộng cho từng object.
     * Tùy loại object sẽ dùng field nào.
     */
    public static class Properties {
        public String itemId;         // Nếu type="item": ID trong items.json
        public String targetScene;    // Nếu type="door": scene đích
        public String inspectText;    // Text khi inspect
        public boolean collectible;   // Có nhặt được không
        public boolean hasGlitch;     // Có hiệu ứng glitch không
        public int rsChange;          // RS thay đổi khi tương tác

        // Texture thay thế theo trạng thái: state → đường dẫn texture
        // VD: {"open": "textures/objects/wardrobe_open.png", "taken": "textures/objects/wardrobe_open_taken.png"}
        public HashMap<String, String> altTextures;

        /** Constructor mặc định — BẮT BUỘC cho JSON parser */
        public Properties() {}
    }
}