package com.gnivol.game.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.gnivol.game.system.save.ISaveable;
import java.util.HashSet;
import java.util.Set;

public class GameState implements ISaveable {
    private String playerName;
    private float currentRS = 35f;
    private String currentRoom = "room_bedroom";
    private final Set<String> finishedDialogues = new HashSet<>();

    public GameState() {
        this.playerName = "Guest";
        this.currentRS = 35;
    }

    @Override
    public void save(Json json) {
        json.writeObjectStart("gameState");

        json.writeValue("playerName", this.playerName);
        json.writeValue("currentRS", this.currentRS);
        json.writeValue("currentRoom", currentRoom);
        json.writeValue("finishedDialogues", finishedDialogues.toArray());

        json.writeObjectEnd();
    }

    @Override
    public void load(JsonValue jsonValue) {
        JsonValue stateJson = jsonValue.get("gameState");

        if (stateJson != null) {
            this.currentRS = stateJson.getFloat("currentRS", 35f);
            this.currentRoom = stateJson.getString("currentRoom", "room_bedroom");

            finishedDialogues.clear();
            if (stateJson.has("finishedDialogues")) {
                for (JsonValue val : stateJson.get("finishedDialogues")) {
                    if (val.isString()) finishedDialogues.add(val.asString());
                    else if (val.isObject() && val.has("value")) finishedDialogues.add(val.getString("value"));
                }
            }
        }
    }

    public boolean isDialogueFinished(String id) { return finishedDialogues.contains(id); }
    public void markDialogueFinished(String id) { finishedDialogues.add(id); }

    public String getPlayerName() { return playerName; }

    public float getCurrentRS() { return currentRS; }
    public void setCurrentRS(float rs) { this.currentRS = rs; }

    public String getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(String room) { this.currentRoom = room; }

    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setCurrentRS(int currentRS) { this.currentRS = currentRS; }
}
