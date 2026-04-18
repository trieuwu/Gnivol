package com.gnivol.game.system.save;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class GameSnapshot {
    private final List<ISaveable> saveables = new ArrayList<>();

    public void register(ISaveable saveable) {
        if (!saveables.contains(saveable)) {
            saveables.add(saveable);
        }
    }

    public String captureAndToJson() {
        StringWriter stringWriter = new StringWriter();
        Json json = new Json();
        json.setOutputType(OutputType.json);

        json.setWriter(new com.badlogic.gdx.utils.JsonWriter(stringWriter));

        json.writeObjectStart();

        for (ISaveable saveable : saveables) {
            saveable.save(json);
        }

        json.writeObjectEnd();

        return stringWriter.toString();
    }
}
