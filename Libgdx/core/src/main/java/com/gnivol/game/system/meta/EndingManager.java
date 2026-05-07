package com.gnivol.game.system.meta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Theo dõi các ending mà người chơi đã đạt được.
 * Lưu vào Preferences riêng (không thuộc save slot) → sống sót qua New Game / xóa save.
 */
public class EndingManager {

    public static final String ENDING_SUICIDE   = "suicide";
    public static final String ENDING_TRUE      = "true_ending";
    public static final String ENDING_RS_0      = "rs_0";
    public static final String ENDING_RS_100    = "rs_100";

    private static final String PREFS_NAME = "GnivolEndings";
    private static final String KEY_PREFIX = "ending_";

    private final Preferences prefs;

    public EndingManager() {
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public void markAchieved(String endingId) {
        if (endingId == null || endingId.isEmpty()) return;
        prefs.putBoolean(KEY_PREFIX + endingId, true);
        if (ENDING_SUICIDE.equals(endingId)) {
            prefs.putBoolean("suicided", true);
        }
        prefs.flush();
        Gdx.app.log("EndingManager", "Achieved: " + endingId);
    }

    public boolean hasAchieved(String endingId) {
        if (endingId == null || endingId.isEmpty()) return false;
        return prefs.getBoolean(KEY_PREFIX + endingId, false);
    }

    public boolean isSuicided() {
        return prefs.getBoolean("suicided", false);
    }

    public void resetAll() {
        prefs.clear();
        prefs.flush();
        Gdx.app.log("EndingManager", "Reset all endings.");
    }
}
