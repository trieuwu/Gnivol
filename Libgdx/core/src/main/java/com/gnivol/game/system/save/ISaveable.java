package com.gnivol.game.system.save;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public interface ISaveable {
    // Hàm gọi khi muốn lưu game
    void save(Json json);

    // Hàm gọi khi muốn tải game
    void load(JsonValue jsonValue);
}
