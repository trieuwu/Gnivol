package com.gnivol.game.system.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.system.interaction.InteractionCallback;
import com.gnivol.game.system.interaction.PlayerInteractionSystem;

/**
 * Bộ điều phối logic trong scene - cầu nối giữa GameScreen (render)
 * và các system (interaction, RS, dialogue...).
 *
 * Trách nhiệm (chuyển từ GameScreen sang):
 * 1. Nhận callback từ PlayerInteractionSystem, quyết định chuyện gì xảy ra
 * 2. Điều khiển overlay (hiện ảnh phóng to khi xem object)
 * 3. Điều khiển inspect text (hiện/ẩn mô tả)
 * 4. Chuyển cảnh có hiệu ứng fade
 *
 * Luồng: PlayerInteractionSystem → SceneController → GameScreen
 *         (phát hiện click)        (quyết định)       (vẽ lên màn hình)
 */
public class SceneController implements InteractionCallback {

    // SceneManager: quản lý phòng hiện tại, chuyển phòng
    private final SceneManager sceneManager;

    // ScreenFader: hiệu ứng fade đen khi chuyển phòng
    private final ScreenFader screenFader;

    // PlayerInteractionSystem: phát hiện click trúng object nào
    private final PlayerInteractionSystem interactionSystem;

    // overlayActive: đánh dấu overlay có đang mở không (VD: xem tủ phóng to)
    private boolean overlayActive;

    // overlayAlpha: độ trong suốt hiện tại của overlay (0 = ẩn, 1 = hiện rõ)
    private float overlayAlpha;

    // OVERLAY_FADE_SPEED: tốc độ fade overlay (4f = từ 0→1 trong 0.25 giây)
    private static final float OVERLAY_FADE_SPEED = 4f;

    // viewListener: GameScreen đăng ký vào đây để nhận thông báo render
    private ViewListener viewListener;

    /**
     * Constructor - nhận 3 dependency và tự đăng ký làm callback.
     *
     * @param sceneManager       quản lý phòng, chuyển phòng
     * @param screenFader        hiệu ứng fade đen
     * @param interactionSystem  phát hiện click trúng object
     */
    public SceneController(SceneManager sceneManager, ScreenFader screenFader,
                           PlayerInteractionSystem interactionSystem) {
        this.sceneManager = sceneManager;
        this.screenFader = screenFader;
        this.interactionSystem = interactionSystem;
        this.interactionSystem.setCallback(this);
    }

    // =========================================================================
    // UPDATE - GameScreen gọi mỗi frame (60 lần/giây)
    // =========================================================================

    /**
     * Tạo hiệu ứng fade khi overlay hiện lên
     * @param delta
     */
    public void update(float delta) {
        // Nếu overlay đang mở VÀ chưa hiện rõ hoàn toàn (alpha < 1)
        if (overlayActive && overlayAlpha < 1f) {
            // Tăng alpha lên, nhưng không vượt quá 1.0
            // VD: delta=0.016, speed=4 → mỗi frame tăng 0.064 → khoảng 16 frame = 0.25 giây
            overlayAlpha = Math.min(overlayAlpha + delta * OVERLAY_FADE_SPEED, 1f);
        }
    }

    // =========================================================================
    // INTERACTION CALLBACK - 5 method nhận từ PlayerInteractionSystem
    // Đây là phần chuyển từ anonymous class trong GameScreen.show() sang đây
    // =========================================================================

    /**
     * Gọi khi click vào object có inspectText.
     * VD: click giường → "Chiếc giường cũ kỹ. Ga trải giường nhàu nát..."
     *
     * Chuyển tiếp cho GameScreen để hiện text với animation.
     */
    @Override
    public void onShowInspectText(String text) {
        // Kiểm tra GameScreen đã đăng ký chưa
        if (viewListener != null) {
            // Bảo GameScreen hiện text lên màn hình
            viewListener.onShowInspectText(text);
        }
    }

    /**
     * Gọi khi click vào chỗ trống (không trúng object nào).
     *
     * 2 trường hợp:
     * - Overlay đang mở → đóng overlay (ưu tiên cao hơn)
     * - Overlay không mở → ẩn inspect text
     */
    @Override
    public void onEmptyClick() {
        if (overlayActive) {
            // Đang xem overlay (VD: ảnh tủ mở) → đóng nó lại
            closeOverlay();
        } else if (viewListener != null) {
            // Không có overlay → ẩn dòng text mô tả đang hiện
            viewListener.onHideInspectText();
        }
    }

    /**
     * Gọi SAU KHI PlayerInteractionSystem đã nhặt item xong
     * (đã thêm vào inventory, đã đổi RS).
     * SceneController chỉ cần bảo GameScreen phát hiệu ứng.
     *
     * @param obj    GameObject vừa được nhặt
     * @param itemId ID vật phẩm (VD: "phone", "paper_fragment_1")
     */
    @Override
    public void onItemCollected(GameObject obj, String itemId) {
        // Log ra console để debug
        Gdx.app.log("SceneController", "Item collected: " + itemId);
        // TODO: play pickup sound, ẩn sprite, animation
        if (viewListener != null) {
            // Bảo GameScreen phát sound/animation nhặt đồ
            viewListener.onPlayPickupEffect(obj, itemId);
        }
    }

    /**
     * Gọi khi click vào cửa (object có type="door").
     * Tìm targetScene trong RoomData rồi chuyển phòng với fade đen.
     *
     * VD: click "door_bathroom" → tìm targetScene="room_bathroom" → fade → chuyển phòng
     *
     * @param obj GameObject cửa vừa click
     */
    @Override
    public void onDoorInteracted(GameObject obj) {
        // Lấy RoomData của phòng hiện tại (chứa danh sách objects + properties)
        RoomData roomData = sceneManager.getCurrentScene().getRoomData();

        // Kiểm tra roomData có tồn tại không
        if (roomData == null || roomData.getObjects() == null) return;

        // Duyệt tất cả object trong phòng để tìm object trùng id với cửa vừa click
        for (RoomData.RoomObject roomObj : roomData.getObjects()) {
            // So sánh id VÀ kiểm tra có targetScene không
            if (roomObj.id.equals(obj.getId()) && roomObj.properties != null
                    && roomObj.properties.targetScene != null) {
                // Tìm thấy → chuyển phòng có fade đen
                changeSceneWithFade(roomObj.properties.targetScene);
                return; // Tìm thấy rồi, không cần duyệt tiếp
            }
        }
    }

    /**
     * Gọi khi click vào object tương tác (không phải item, không phải door).
     * VD: click tủ quần áo → mở overlay hiện ảnh tủ mở ra.
     *
     * Chỉ mở overlay nếu object có altTextures trong JSON.
     *
     * @param obj GameObject vừa click
     */
    @Override
    public void onObjectInteracted(GameObject obj) {
        // Lấy RoomData phòng hiện tại
        RoomData roomData = sceneManager.getCurrentScene().getRoomData();

        // Kiểm tra null
        if (roomData == null || roomData.getObjects() == null) return;

        // Duyệt tìm object trùng id
        for (RoomData.RoomObject roomObj : roomData.getObjects()) {
            // Kiểm tra: đúng id + có properties + có altTextures + altTextures không rỗng
            if (roomObj.id.equals(obj.getId()) && roomObj.properties != null
                    && roomObj.properties.altTextures != null
                    && !roomObj.properties.altTextures.isEmpty()) {
                // Lấy đường dẫn texture thay thế đầu tiên
                // VD: altTextures = {"open": "textures/objects/wardrobe_open.png"}
                //     → firstPath = "textures/objects/wardrobe_open.png"
                String firstPath = roomObj.properties.altTextures.values().iterator().next();

                // Mở overlay với texture đó
                openOverlay(firstPath);
                return; // Tìm thấy rồi, không cần duyệt tiếp
            }
        }
    }

    // =========================================================================
    // CLICK HANDLING - chuyển từ GameScreen touchDown
    // =========================================================================

    /**
     * Điểm vào duy nhất cho mọi click chuột từ GameScreen.
     *
     * Logic:
     * - Overlay đang mở? → đóng overlay, xong
     * - Overlay không mở? → chuyển cho PlayerInteractionSystem phát hiện click trúng gì
     *
     * @param screenX  tọa độ X click (pixel màn hình)
     * @param screenY  tọa độ Y click (pixel màn hình)
     * @param viewport viewport để chuyển đổi tọa độ màn hình → tọa độ game
     * @return true nếu click đã được xử lý
     */
    public boolean handleClick(int screenX, int screenY, Viewport viewport) {
        // Overlay đang mở → click bất kỳ đâu đều đóng overlay
        if (overlayActive) {
            closeOverlay();
            return true; // Đã xử lý, không cần truyền tiếp
        }

        // Overlay không mở → chuyển cho interaction system
        // Nó sẽ unproject tọa độ, tìm object, rồi gọi ngược callback ở trên
        return interactionSystem.handleClick(screenX, screenY, viewport);
    }

    // =========================================================================
    // OVERLAY - chuyển từ GameScreen openOverlay/closeOverlay
    // =========================================================================

    /**
     * Mở overlay: hiện ảnh phóng to ở giữa màn hình.
     * VD: click tủ → hiện ảnh tủ mở ra.
     *
     * SceneController quản lý STATE (mở/đóng, alpha).
     * GameScreen quản lý TEXTURE (load file ảnh, vẽ lên màn hình).
     *
     * @param texturePath đường dẫn file ảnh (VD: "textures/objects/wardrobe_open.png")
     */
    private void openOverlay(String texturePath) {
        // Đánh dấu overlay đang mở
        this.overlayActive = true;

        // Alpha = 0 → bắt đầu trong suốt, update() sẽ tăng dần lên 1
        this.overlayAlpha = 0f;

        // Bảo GameScreen load texture từ file
        if (viewListener != null) {
            viewListener.onOpenOverlay(texturePath);
        }

        // Log để debug
        Gdx.app.log("SceneController", "Overlay opened: " + texturePath);
    }

    /**
     * Đóng overlay: ẩn ảnh phóng to, quay lại scene bình thường.
     */
    private void closeOverlay() {
        // Đánh dấu overlay đã đóng
        this.overlayActive = false;

        // Reset alpha về 0
        this.overlayAlpha = 0f;

        if (viewListener != null) {
            // Ẩn inspect text đang hiện (nếu có)
            viewListener.onHideInspectText();

            // Bảo GameScreen dispose texture (giải phóng bộ nhớ GPU)
            viewListener.onCloseOverlay();
        }

        // Log để debug
        Gdx.app.log("SceneController", "Overlay closed");
    }

    // =========================================================================
    // SCENE TRANSITION - chuyển từ GameScreen changeSceneWithFade
    // =========================================================================

    /**
     * Chuyển phòng với hiệu ứng fade đen.
     *
     * Luồng do ScreenFader xử lý:
     * 1. Màn hình tối dần (FADING_OUT)
     * 2. Khi tối hẳn → chạy callback: sceneManager.changeScene() đổi phòng
     * 3. Màn hình sáng dần (FADING_IN)
     * 4. Sáng hẳn → về bình thường
     *
     * @param targetSceneId ID phòng đích (VD: "room_bathroom")
     */
    public void changeSceneWithFade(final String targetSceneId) {
        // Nếu đang fade rồi → bỏ qua (tránh spam click gây chuyển phòng nhiều lần)
        if (screenFader.isFading()) return;

        // Bắt đầu fade: truyền callback sẽ chạy khi fade out xong
        screenFader.startFade(new Runnable() {
            @Override
            public void run() {
                // Callback này chạy khi màn hình đã tối hẳn
                // Lúc này đổi phòng → người chơi không thấy quá trình load
                sceneManager.changeScene(targetSceneId);
            }
        });
    }

    // =========================================================================
    // GETTER - GameScreen đọc state để quyết định vẽ gì
    // =========================================================================

    /** Overlay có đang mở không? GameScreen dùng để quyết định có vẽ overlay không */
    public boolean isOverlayActive() {
        return overlayActive;
    }

    /** Alpha hiện tại của overlay (0→1). GameScreen dùng để vẽ overlay với đúng độ trong suốt */
    public float getOverlayAlpha() {
        return overlayAlpha;
    }

    // =========================================================================
    // VIEW LISTENER - cơ chế giao tiếp SceneController → GameScreen
    // =========================================================================

    /**
     * GameScreen gọi method này để đăng ký nhận thông báo.
     * Sau khi đăng ký, mỗi khi SceneController cần GameScreen làm gì
     * (hiện text, load texture, phát sound), nó sẽ gọi qua viewListener.
     *
     * @param listener GameScreen (implement ViewListener)
     */
    public void setViewListener(ViewListener listener) {
        this.viewListener = listener;
    }

    public interface ViewListener {

        /** Hiện inspect text với animation fade-in.
         *  @param text nội dung cần hiện (VD: "Chiếc giường cũ kỹ...") */
        void onShowInspectText(String text);

        /** Ẩn inspect text với animation fade-out */
        void onHideInspectText();

        /** Mở overlay — GameScreen cần load texture từ file và chuẩn bị vẽ.
         *  @param texturePath đường dẫn ảnh (VD: "textures/objects/wardrobe_open.png") */
        void onOpenOverlay(String texturePath);

        /** Đóng overlay — GameScreen cần dispose texture để giải phóng bộ nhớ */
        void onCloseOverlay();

        /** Play hiệu ứng nhặt đồ (sound, particle...).
         *  @param obj  GameObject vừa nhặt
         *  @param itemId ID vật phẩm (VD: "phone") */
        void onPlayPickupEffect(GameObject obj, String itemId);
    }
}
