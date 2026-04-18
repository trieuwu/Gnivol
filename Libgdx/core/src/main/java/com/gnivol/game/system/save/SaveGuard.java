package com.gnivol.game.system.save;

public class SaveGuard {
    private boolean isJumpscareActive = false;
    private boolean isEventPlaying = false;

    public void setJumpscareActive(boolean active) { this.isJumpscareActive = active; }
    public void setEventPlaying(boolean playing) { this.isEventPlaying = playing; }

    public boolean canSave() {
        return !isJumpscareActive && !isEventPlaying;
    }
}
