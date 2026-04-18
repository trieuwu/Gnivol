package com.gnivol.game.system.save;

public class SaveLock {
    private volatile boolean isSaving = false;

    public synchronized boolean tryLock() {
        if (isSaving) return false;
        isSaving = true;
        return true;
    }

    public void unlock() {
        isSaving = false;
    }
}
