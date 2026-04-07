package com.gnivol.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Quản lý input cho game bằng InputMultiplexer.
 *
 * Thứ tự ưu tiên (processor đầu tiên xử lý trước):
 * 1. Stage (UI) — button, slider, dialogue box
 * 2. GameInput — click vật thể, di chuyển
 *
 * Nếu Stage xử lý event (VD: click vào button) → GameInput KHÔNG nhận event đó.
 * → Tránh lỗi: click button "Save" nhưng đồng thời click vào vật dưới nền.
 */
public class InputHandler {

    private final InputMultiplexer multiplexer;

    public InputHandler() {
        this.multiplexer = new InputMultiplexer();
    }

    /**
     * Kích hoạt InputHandler — set làm input processor chính của game.
     * Gọi 1 lần trong GameScreen.show().
     */
    public void activate() {
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Thêm Stage vào đầu danh sách (ưu tiên cao nhất).
     * Stage xử lý UI event: button click, text field, slider...
     */
    public void addStage(Stage stage) {
        // Stage đã implement InputProcessor
        multiplexer.addProcessor(0, stage);  // index 0 = ưu tiên cao nhất
    }

    /**
     * Thêm InputProcessor vào cuối danh sách (ưu tiên thấp hơn Stage).
     * Dùng cho game input: click vật thể, phím tắt...
     */
    public void addProcessor(InputProcessor processor) {
        multiplexer.addProcessor(processor);
    }

    /**
     * Xóa 1 processor.
     */
    public void removeProcessor(InputProcessor processor) {
        multiplexer.removeProcessor(processor);
    }

    /**
     * Xóa tất cả processor.
     */
    public void clear() {
        multiplexer.clear();
    }

    /**
     * Trả về InputMultiplexer (nếu cần truy cập trực tiếp).
     */
    public InputMultiplexer getMultiplexer() {
        return multiplexer;
    }
}