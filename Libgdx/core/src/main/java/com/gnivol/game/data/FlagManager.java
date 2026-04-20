package com.gnivol.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.gnivol.game.system.save.ISaveable;

import java.util.HashMap;
import java.util.Map;

public class FlagManager implements ISaveable {

    private final Map<String, Boolean> flags;

    public FlagManager() {
        this.flags = new HashMap<>();
    }

    public void set(String key) {
        flags.put(key, true);
        Gdx.app.log("FlagManager", "Set: " + key);
    }

    public void set(String key, boolean value) {
        flags.put(key, value);
        Gdx.app.log("FlagManager", "Set: " + key + " = " + value);
    }

    public boolean get(String key) {
        return flags.getOrDefault(key, false);
    }

    public boolean has(String key) {
        return flags.containsKey(key);
    }

    public void clear(String key) {
        flags.remove(key);
    }

    public void reset() {
        flags.clear();
    }

    @Override
    public void save(Json json) {
        json.writeObjectStart("flagManager");
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            json.writeValue(entry.getKey(), entry.getValue());
        }
        json.writeObjectEnd();
    }

    @Override
    public void load(JsonValue jsonValue) {
        JsonValue flagJson = jsonValue.get("flagManager");
        flags.clear();
        if (flagJson != null) {
            for (JsonValue val = flagJson.child; val != null; val = val.next) {
                flags.put(val.name, val.asBoolean());
            }
        }
    }
}
