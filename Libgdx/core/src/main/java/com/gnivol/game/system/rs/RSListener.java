package com.gnivol.game.system.rs;

public interface RSListener {
        // Gọi khi giá trị RS thay đổi để update UI,... mới theo từng EventType khi được tương tác
        void onRSChanged(float oldValue, float newValue);

        // Nếu RS vượt 50 sẽ gọi glitch, sound đặc biệt (check RS liên tục để bật tắt glitch, sound)
        void onThresholdCrossed(boolean isAbove);
}
