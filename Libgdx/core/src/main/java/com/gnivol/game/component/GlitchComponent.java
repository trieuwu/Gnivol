package com.gnivol.game.component;
import com.badlogic.gdx.graphics.Texture;

public class GlitchComponent extends Component {
    private Texture originalTexture; // lưu ảnh bình thường
    private Texture glitchTexture; // lưu ảnh glitch
    private boolean isGlitched = false;
    private float glitchIntensity = 0f; // độ mạnh yếu glitch
    private float flickerTimer = 0f; // thời gian glitch nhấp nháy

    // Mở glitch
    public void activate() {
        isGlitched = true;
        // TODO tuần 4: swapTexture() + applyColorDistortion()
    }

    //Đóng glitch
    public void deactivate() {
        isGlitched = false;
        // TODO tuần 4: restore original texture
    }

    @Override
    public void update(float dt) {
        if (!isGlitched || !enabled) return;
        flickerTimer += dt; // ổn định thời gian nhấp nháy khi FPS thay đổi
        // TODO tuần 4: flicker logic
    }

    // Getters/Setters
    public boolean isGlitched() { return isGlitched; }
    public float getGlitchIntensity() { return glitchIntensity; }
    public void setGlitchIntensity(float intensity) { this.glitchIntensity = intensity; }
    public void setOriginalTexture(Texture tex) { this.originalTexture = tex; }
    public void setGlitchTexture(Texture tex) { this.glitchTexture = tex; }
}
