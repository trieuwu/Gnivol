package com.gnivol.game.system.save;

import java.util.HashSet;
import java.util.Set;

public class DirtyTracker {
    private final Set<String> dirtyKeys = new HashSet<>();

    public void markDirty(String key) {
        dirtyKeys.add(key);
    }

    public boolean isDirty(String key) {
        return dirtyKeys.contains(key);
    }

    public boolean hasAnyDirty() {
        return !dirtyKeys.isEmpty();
    }

    public void clearAll() {
        dirtyKeys.clear();
    }
}
