package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;

public abstract class BaseScreen implements Screen {
    protected final GnivolGame game;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected SpriteBatch batch;
    protected Stage stage;

    public BaseScreen(GnivolGame game) {
        this.game = game;

        // Camera 2D — "mắt" nhìn vào game world. Orthographic nghĩa là không có phối cảnh (vật xa/gần cùng kích thước), phù hợp cho game 2D.
        this.camera = new OrthographicCamera();


        /* FitViewport giữ tỉ lệ 1280×720 cố định. Khi người chơi resize cửa sổ khác với tỉ lệ 16:9 , nó tự thêm viền đen (letterbox) thay vì kéo giãn méo hình.
        Camera được gắn vào viewport này. */
        this.viewport = new FitViewport(
                Constants.WORLD_WIDTH,
                Constants.WORLD_HEIGHT,
                camera
        );


        /* Stage cũng cần FitViewport riêng để UI (nút bấm, text, label...) giữ nguyên tỉ lệ và vị trí khi resize cửa sổ. */
        this.batch = new SpriteBatch();
        this.stage = new Stage(new FitViewport(
                Constants.WORLD_WIDTH,
                Constants.WORLD_HEIGHT
        ));
    }

    @Override
    // Đăng ký stage làm nơi nhận input (click chuột, bàn phím). Nếu không có dòng này, nhấn nút UI sẽ không phản hồi.
    public void show() {
        Gdx.input.setInputProcessor(stage); // gọi khi screen được hiển thị
    }

    @Override
    public void render(float delta) { // delta là thời gian giữa 2 frame (thường ~0.016s ở 60FPS).
        // Xoá toàn bộ màn hình bằng màu đen (RGBA: 0, 0, 0, 1). Nếu không clear, frame trước sẽ bị vẽ chồng lên.
        ScreenUtils.clear(0, 0, 0, 1);

        // Áp dụng viewport cho camera, rồi đồng bộ batch với camera. Nhờ vậy khi vẽ toạ độ (640, 360) sẽ luôn ở giữa màn hình bất kể cửa sổ to/nhỏ.
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        // Gọi method abstract — screen con override chỗ này để vẽ nội dung game (phòng, nhân vật, vật phẩm...).
        renderWorld(delta);

        // Update logic UI (animation nút, hiệu ứng...) rồi vẽ UI lên màn hình. Vẽ UI sau renderWorld nên UI luôn nằm trên game world.
        stage.act(delta);
        stage.draw();
    }

    /** Override method này để vẽ game world tất cả các screen con sẽ được override từ screen này*/
    protected abstract void renderWorld(float delta);

    @Override
    /* Cập nhật cả 2 viewport (game world + UI) theo kích thước mới. true nghĩa là đặt camera về giữa.
    Nhờ FitViewport nên chỉ thêm letterbox, không méo hình. Libgdx sẽ tự gọi hàm này không cần phải thủ công*/
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    /* Giải phóng tài nguyên GPU. SpriteBatch và Stage đều chiếm bộ nhớ GPU — nếu không dispose sẽ memory leak, game chạy càng lâu càng lag. */
    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }

    /* tự được libgdx gọi không cần quan tâm */
    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

}