package com.gnivol.game.system.rs;
import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.List;

public class RSManager {
    private float currentRS;
    private final float maxRS;
    private final float minThreshold;
    private final float maxThreshold;
    private final List<RSListener> listeners;

    public RSManager() {
        this.currentRS = 35f;
        this.maxRS = 100;
        this.minThreshold = 35f;
        this.maxThreshold = 65f;
        this.listeners = new ArrayList<>();
    }

    // Xử lý sự kiện RS chính
    public void processEvent(RSEvent event) {
        if (event == null || event.getRSAmount() == 0) return;
        Gdx.app.log("RS", "Processing: " + event);
        addRS(event.getRSAmount());
    }

    public void addRS(float amount) {
        float oldValue = currentRS;
        boolean wasAbove = isAboveThreshold();
        currentRS += amount;
        clampRS();
        float newValue = currentRS;
        boolean isAbove = isAboveThreshold();

        Gdx.app.log("RS", "Changed: " + oldValue + " -> " + newValue
            + " (" + amount + ")");

        // Cập nhật tất cả thay đổi theo từng Event
        notifyListeners(oldValue, newValue);
        // Chỉ gọi khi trạng thái bị đảo ngược
        if(isAbove != wasAbove){
            // Check vượt ngưỡng để bật tắt glitch, sound
            notifyThresholdCross(isAbove);
        }

        // Specific thresholds: 50 (glitch), 65 (map unlock)
        if (oldValue < 50f && newValue >= 50f) {
            notifySpecificThreshold(50f, true);
        } else if (oldValue >= 50f && newValue < 50f) {
            notifySpecificThreshold(50f, false);
        }
        if (oldValue < maxThreshold && newValue >= maxThreshold) {
            notifySpecificThreshold(maxThreshold, true);
        } else if (oldValue >= maxThreshold && newValue < maxThreshold) {
            notifySpecificThreshold(maxThreshold, false);
        }
    }
    /** Giữ RS trong khoảng [minThreshold, maxThreshold] */
    private void clampRS() {
        currentRS = Math.max(0, Math.min(currentRS, maxRS));
    }

    private void notifyListeners(float oldValue, float newValue) {
        for (RSListener listener : listeners) {
            try {
                listener.onRSChanged(oldValue, newValue);
            } catch (Exception e) {
                Gdx.app.error("RS", "Listener error", e);
            }
        }
    }

    private void notifyThresholdCross(boolean isAbove) {
        Gdx.app.log("RS", "Threshold crossed! Above 50: " + isAbove);
        for (RSListener listener : listeners) {
            try {
                listener.onThresholdCrossed(isAbove);
            } catch (Exception e) {
                Gdx.app.error("RS", "Threshold listener error", e);
            }
        }
    }
    public void reset() {
        float oldRS = this.currentRS;
        this.currentRS = 35f;

        notifyListeners(oldRS, this.currentRS);
    }

    // --- Getters ---
    public float getRS() { return currentRS; }
    public boolean isAboveThreshold() { return ((currentRS) < minThreshold) || ((currentRS) > maxThreshold); }
    public boolean isMapUnlocked() { return currentRS >= maxThreshold; }

    // --- Listener management ---
    public void addListener(RSListener listener) { listeners.add(listener); }
    public void removeListener(RSListener listener) { listeners.remove(listener); }

    // --- For save/load ---
    public void setRS(float value) {
        this.currentRS = Math.max(0, Math.min(value, maxRS));
    }

    public void setCurrentRS(float currentRS) {
        this.currentRS = currentRS;
    }

    private void notifySpecificThreshold(float threshold, boolean crossed) {
        for (RSListener listener : listeners) {
            try {
                listener.onSpecificThreshold(threshold, crossed);
            } catch (Exception e) {
                Gdx.app.error("RS", "Specific threshold listener error", e);
            }
        }
    }
}
