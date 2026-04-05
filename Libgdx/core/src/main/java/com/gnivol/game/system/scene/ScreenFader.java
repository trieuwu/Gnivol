package com.gnivol.game.system.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Hiệu ứng fade đen khi chuyển cảnh.
 *
 * State machine:
 *   IDLE → startFade() → FADING_OUT (tối dần)
 *   → alpha = 1.0 → gọi callback (đổi scene) → FADING_IN (sáng dần)
 *   → alpha = 0.0 → IDLE
 *
 * Sử dụng:
 *   // Trong GameScreen:
 *   fader.startFade(() -> sceneManager.changeScene("room_bathroom"));
 *   // Mỗi frame:
 *   fader.update(delta);
 *   fader.render();
 */
public class ScreenFader {

    // --- Trạng thái ---
    public enum FadeState {
        IDLE,         // Không làm gì — scene hiện bình thường
        FADING_OUT,   // Đang tối dần (alpha 0 → 1)
        FADING_IN     // Đang sáng dần (alpha 1 → 0)
    }

    // State hiện tại
    private FadeState state;

    // Giá trị alpha hiện tại (0 = trong suốt, 1 = đen hoàn toàn)
    private float alpha;

    // Tốc độ fade (đơn vị: alpha/giây). 2.0 = fade hết trong 0.5 giây
    private float fadeSpeed;

    // Callback: hàm chạy khi fade out xong (thường là đổi scene)
    private Runnable onFadeOutComplete;

    // ShapeRenderer để vẽ hình chữ nhật đen phủ toàn màn hình
    private final ShapeRenderer shapeRenderer;

    // Kích thước màn hình
    private final float screenWidth;
    private final float screenHeight;

    /**
     * @param fadeSpeed tốc độ fade. Gợi ý: 2.0f (fade 0.5s), 3.0f (fade 0.33s)
     */
    public ScreenFader(float fadeSpeed, float screenWidth, float screenHeight) {
        this.state = FadeState.IDLE;
        this.alpha = 0f;
        this.fadeSpeed = fadeSpeed;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * Bắt đầu quá trình fade.
     * 1. Màn hình tối dần (FADING_OUT)
     * 2. Khi tối hẳn → chạy callback (đổi scene)
     * 3. Màn hình sáng dần (FADING_IN)
     * 4. Khi sáng hẳn → về IDLE
     *
     * @param onFadeOutComplete hàm chạy khi fade out hoàn tất (VD: đổi scene)
     */
    public void startFade(Runnable onFadeOutComplete) {
        if (state != FadeState.IDLE) return;  // Đang fade rồi, bỏ qua

        this.onFadeOutComplete = onFadeOutComplete;
        this.state = FadeState.FADING_OUT;
        this.alpha = 0f;
    }

    /**
     * Bắt đầu fade-in ngay lập tức (không cần fade-out trước).
     * Dùng khi vừa load xong scene mới và muốn sáng dần.
     */
    public void startFadeIn() {
        this.state = FadeState.FADING_IN;
        this.alpha = 1f;
    }

    /**
     * Update mỗi frame. Xử lý state machine.
     */
    public void update(float delta) {
        switch (state) {
            case FADING_OUT:
                // Tăng alpha (tối dần)
                alpha += fadeSpeed * delta;
                if (alpha >= 1f) {
                    alpha = 1f;
                    // Fade out xong → chạy callback
                    if (onFadeOutComplete != null) {
                        onFadeOutComplete.run();
                        onFadeOutComplete = null;
                    }
                    // Chuyển sang fade in
                    state = FadeState.FADING_IN;
                }
                break;

            case FADING_IN:
                // Giảm alpha (sáng dần)
                alpha -= fadeSpeed * delta;
                if (alpha <= 0f) {
                    alpha = 0f;
                    state = FadeState.IDLE;
                }
                break;

            case IDLE:
                // Không làm gì
                break;
        }
    }

    /**
     * Render overlay đen. Gọi SAU khi render scene (để phủ lên trên).
     *
     * QUAN TRỌNG: Method này tự begin/end ShapeRenderer.
     * Nếu đang có SpriteBatch.begin() bên ngoài → phải end() trước khi gọi.
     */
    public void render() {
        if (state == FadeState.IDLE || alpha <= 0f) return;  // Không cần vẽ

        // Bật blending để alpha hoạt động
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0f, 0f, 0f, alpha));  // Đen + alpha
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    /**
     * Giải phóng ShapeRenderer.
     */
    public void dispose() {
        shapeRenderer.dispose();
    }

    // --- Getter ---

    public FadeState getState() {
        return state;
    }

    public boolean isFading() {
        return state != FadeState.IDLE;
    }

    public float getAlpha() {
        return alpha;
    }
}