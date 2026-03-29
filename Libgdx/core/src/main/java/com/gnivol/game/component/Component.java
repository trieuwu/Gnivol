package com.gnivol.game.component;

public abstract class Component {
    protected boolean enabled = true; // đóng, mở chức năng

    // update khi FPS thay đổi sẽ cập nhật lại delta = 1/FPS để giữ màn hình mượt khi chơi
    public abstract void update(float dt);

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean val) { this.enabled = val; }
}
