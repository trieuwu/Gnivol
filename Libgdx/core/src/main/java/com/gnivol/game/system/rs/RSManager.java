package com.gnivol.game.system.rs;
import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.List;

public class RSManager {
    private float currentRS;
    private final float maxRS;
    private final float threshold;
    private final List<RSListener> listeners;

    public RSManager() {
        this.currentRS = 0f;
        this.maxRS = 100;
        this.threshold = 50;
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
        currentRS += amount;
        clampRS();
        float newValue = currentRS;
        boolean isAbove = isAboveThreshold();

        Gdx.app.log("RS", "Changed: " + oldValue + " -> " + newValue
            + " (" + amount + ")");

        // Cập nhật tất cả thay đổi theo từng Event
        notifyListeners(oldValue, newValue);
        // Check vượt ngưỡng để bật tắt glitch, sound
        notifyThresholdCross(isAbove);
    }
    /** Giữ RS trong khoảng [0, maxRS] */
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

    // --- Getters ---
    public float getRS() { return currentRS; }
    public boolean isAboveThreshold() { return (currentRS) >= threshold; }

    // --- Listener management ---
    public void addListener(RSListener listener) { listeners.add(listener); }
    public void removeListener(RSListener listener) { listeners.remove(listener); }

    // --- For save/load ---
    public void setRS(float value) {
        this.currentRS = Math.max(0, Math.min(value, maxRS));
    }
}
