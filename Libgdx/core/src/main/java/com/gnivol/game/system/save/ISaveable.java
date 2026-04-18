package com.gnivol.game.system.save;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public interface ISaveable {
    void save(Json json);

    void load(JsonValue jsonValue);

}
