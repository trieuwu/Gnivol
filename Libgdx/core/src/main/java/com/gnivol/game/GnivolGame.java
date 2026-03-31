package com.gnivol.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

// Import đầy đủ các class của hệ thống Inventory
import com.gnivol.game.system.inventory.data.ItemDatabase;
import com.gnivol.game.system.inventory.model.CraftingManager;
import com.gnivol.game.system.inventory.model.InventoryManager;
import com.gnivol.game.system.inventory.ui.GameUI;
import com.gnivol.game.system.inventory.ui.InventoryUIController;

public class GnivolGame extends ApplicationAdapter {

    // Sân khấu Scene2D để vẽ giao diện UI
    private Stage stage;
    private GameUI gameUI;

    @Override
    public void create() {
        System.out.println("Dang khoi dong game Gnivol...");

        // 1. TẠO SÂN KHẤU VÀ BẬT CẢM ỨNG CHUỘT
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // Lệnh cực kỳ quan trọng để game nhận diện click chuột

        // 2. KHỞI TẠO BỘ NÃO (MODEL & DATA)
        ItemDatabase itemDB = new ItemDatabase();
        CraftingManager craftMgr = new CraftingManager();
        InventoryManager invMgr = new InventoryManager();
      //  GameStateManager stateMgr = new GameStateManager();

        // 3. KHỞI TẠO GIAO DIỆN (VIEW)
        gameUI = new GameUI(stage);

        // 4. KHỞI TẠO QUẢN GIA (CONTROLLER)
        InventoryUIController uiController = new InventoryUIController(craftMgr, invMgr, itemDB, gameUI);

        // --- KỊCH BẢN TEST GAME BẰNG CONTROLLER ---
        System.out.println("\n--- KICH BAN TEST GAME MOI (MVC) ---");

        // Nhặt đồ vào túi
        invMgr.addItem("paper_fragment_2");
        invMgr.addItem("paper_fragment_1");

        // Giả lập người chơi bấm chuột vào mảnh 1
        uiController.onItemClicked("paper_fragment_1");

        // Giả lập người chơi bấm chuột vào mảnh 2 -> Sẽ kích hoạt ghép đồ!
        uiController.onItemClicked("paper_fragment_2");

        // Ghi lại lịch sử cốt truyện
       // stateMgr.setFlag("has_password_note", true);
    }

    @Override
    public void render() {
        // Xóa màn hình cũ và tô màu nền đen (Red=0, Green=0, Blue=0)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Lệnh cho GameUI vẽ đồ họa lên màn hình mỗi khung hình (60 FPS)
        if (gameUI != null) {
            gameUI.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        // Cập nhật lại kích thước sân khấu khi người chơi kéo dãn cửa sổ
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        // Dọn dẹp RAM khi tắt game
        if (stage != null) {
            stage.dispose();
        }
    }
}