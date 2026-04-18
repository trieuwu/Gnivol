package com.gnivol.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.ashley.core.Engine;
import com.gnivol.game.audio.AudioManager;
import com.gnivol.game.input.InputHandler;
import com.gnivol.game.screen.MainMenuScreen;
import com.gnivol.game.system.interaction.PlayerInteractionSystem;
import com.gnivol.game.system.inventory.CraftingManager;
import com.gnivol.game.system.inventory.InventoryManager;
import com.gnivol.game.system.rs.RSManager;
import com.gnivol.game.system.scene.SceneManager;
import com.gnivol.game.system.scene.ScreenFader;

/**
 * Lớp chính của game Gnivol.
 * Quản lý lifecycle, giữ các manager dùng chung.
 *
 * Tất cả manager được khởi tạo 1 lần ở create() và dùng chung
 * cho mọi Screen (MainMenu, GameScreen, PauseScreen, v.v.)
 */
public class GnivolGame extends Game {

    // Stage: hệ thống UI Scene2D (chứa Actor, xử lý layout)
    private Stage stage;

    // Ashley ECS Engine: quản lý Entity + System (chưa dùng triệt để)
    private Engine ashleyEngine;

    // --- Các manager dùng chung ---

    // SceneManager: quản lý phòng (load, chuyển, push/pop)
    private SceneManager sceneManager;

    // ScreenFader: hiệu ứng fade đen khi chuyển phòng
    private ScreenFader screenFader;

    // InputHandler: quản lý input multiplexer (gom nhiều processor)
    private InputHandler inputHandler;

    // RSManager: quản lý chỉ số Reality Stability
    private RSManager rsManager;

    // InventoryManager: quản lý kho đồ người chơi (max 25 slot)
    private InventoryManager inventoryManager;
    private CraftingManager craftingManager;
    // PlayerInteractionSystem: phát hiện click trúng object + dispatch hành động
    private PlayerInteractionSystem playerInteractionSystem;

    // AudioManager: lưu volume nhạc nền + hiệu ứng
    private AudioManager audioManager;

    private com.gnivol.game.system.puzzle.PuzzleManager puzzleManager;
    /**
     * Gọi 1 lần khi game khởi động.
     * Khởi tạo TẤT CẢ manager theo đúng thứ tự dependency.
     */
    @Override
    public void create() {
        // Tạo Stage với viewport giữ tỉ lệ 1280×720 (letterbox nếu cửa sổ khác tỉ lệ)
        stage = new Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));

        // Tạo Ashley ECS Engine (quản lý Entity + System)
        ashleyEngine = new Engine();

        puzzleManager = new com.gnivol.game.system.puzzle.PuzzleManager();

        sceneManager = new SceneManager(puzzleManager);
        // 2. ScreenFader: không phụ thuộc ai, tốc độ fade 2.5 (fade trong ~0.4 giây)
        screenFader = new ScreenFader(2.5f, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        // 3. InputHandler: không phụ thuộc ai
        inputHandler = new InputHandler();

        // 4. RSManager: không phụ thuộc ai
        rsManager = new RSManager();

        // 5. InventoryManager: không phụ thuộc ai
        inventoryManager = new InventoryManager();
        craftingManager = new CraftingManager();
        // 6. PlayerInteractionSystem: phụ thuộc SceneManager + InventoryManager + RSManager
        //    Vì khi click object, nó cần: tìm object (SceneManager), nhặt đồ (InventoryManager),
        //    đổi RS (RSManager)
        playerInteractionSystem = new PlayerInteractionSystem(sceneManager, inventoryManager, rsManager, puzzleManager);

        audioManager = new AudioManager();

        // Bắt đầu từ màn hình MainMenu
        setScreen(new MainMenuScreen(this));
    }

    /**
     * Gọi mỗi frame. Xóa màn hình + gọi render() của Screen hiện tại.
     */
    @Override
    public void render() {
        // Xóa màn hình bằng màu đen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Gọi render() của Screen hiện tại (MainMenuScreen hoặc GameScreen, v.v.)
        super.render();
    }

    /**
     * Gọi khi thay đổi kích thước cửa sổ.
     */
    @Override
    public void resize(int width, int height) {
        // Cập nhật viewport của Stage (UI)
        stage.getViewport().update(width, height, true);

        // Chuyển tiếp cho Screen hiện tại
        super.resize(width, height);
    }

    /**
     * Gọi khi thoát game. Giải phóng TẤT CẢ tài nguyên.
     */
    @Override
    public void dispose() {
        if (stage != null) stage.dispose();             // giải phóng Stage (UI)
        if (sceneManager != null) sceneManager.dispose(); // giải phóng textures các phòng
        if (screenFader != null) screenFader.dispose();   // giải phóng ShapeRenderer
        if (getScreen() != null) getScreen().dispose();   // giải phóng Screen hiện tại
    }

    // =========================================================================
    // GETTER — các Screen dùng để lấy manager cần thiết
    // =========================================================================

    /** Stage: hệ thống UI Scene2D */
    public Stage getStage() {
        return stage;
    }

    /** Ashley ECS Engine */
    public Engine getAshleyEngine() {
        return ashleyEngine;
    }

    /** SceneManager: quản lý phòng */
    public SceneManager getSceneManager() {
        return sceneManager;
    }

    /** ScreenFader: hiệu ứng fade đen */
    public ScreenFader getScreenFader() {
        return screenFader;
    }

    /** InputHandler: quản lý input */
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    /** RSManager: quản lý Reality Stability */
    public RSManager getRsManager() {
        return rsManager;
    }

    /** InventoryManager: quản lý kho đồ */
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public CraftingManager getCraftingManager() {
        return craftingManager;
    }
    /** PlayerInteractionSystem: phát hiện click trúng object */
    public PlayerInteractionSystem getPlayerInteractionSystem() {
        return playerInteractionSystem;
    }

    /** AudioManager: lưu volume */
    public AudioManager getAudioManager() {
        return audioManager;
    }

    public com.gnivol.game.system.puzzle.PuzzleManager getPuzzleManager() {
        return puzzleManager;
    }
}
