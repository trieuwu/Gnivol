package com.gnivol.game.system.dialogue;

import com.badlogic.gdx.math.MathUtils;

public class GlitchTextRenderer {
    private static final char[] GLITCH_CHARS = {'̷', '̵', '̶', '█', '▓', '░', '?', '!', '@', '#'};

     // Biến đổi chuỗi văn bản gốc thành chuỗi bị glitch nếu RS < 35.
     // originalText Chuỗi văn bản gốc.
     // currentRS Giá trị RS hiện tại.
    public static String applyGlitch(String originalText, float currentRS) {
        if (originalText == null || originalText.isEmpty()) return "";

        if (currentRS >= 35f) {
            return originalText;
        }
        // Nhiễu 30%
        float glitchChance = 0.3f;

        StringBuilder glitched = new StringBuilder(originalText.length());
        for (int i = 0; i < originalText.length(); i++) {
            char c = originalText.charAt(i);
            // Bỏ qua khoảng trắng và các ký tự điều khiển
            if (Character.isWhitespace(c)) {
                glitched.append(c);
                continue;
            }
            // Có xác suất glitch ký tự này
            if (MathUtils.randomBoolean(glitchChance)) {
                glitched.append(GLITCH_CHARS[MathUtils.random(GLITCH_CHARS.length - 1)]);
            } else {
                glitched.append(c);
            }
        }
        return glitched.toString();
    }
}
