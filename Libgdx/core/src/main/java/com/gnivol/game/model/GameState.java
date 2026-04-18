package com.gnivol.game.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.gnivol.game.system.save.ISaveable;

public class GameState implements ISaveable {
    private String playerName;
    private int currentRS;

    public GameState() {
        this.playerName = "Guest";
        this.currentRS = 50;
    }

    @Override
    public void save(Json json) {
        json.writeObjectStart("gameState");

        json.writeValue("playerName", this.playerName);
        json.writeValue("currentRS", this.currentRS);

        json.writeObjectEnd();
    }

    @Override
    public void load(JsonValue jsonValue) {
        JsonValue stateJson = jsonValue.get("gameState");

        if (stateJson != null) {
            this.playerName = stateJson.getString("playerName", "Guest");
            this.currentRS = stateJson.getInt("currentRS", 50);
        }
    }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getCurrentRS() { return currentRS; }
    public void setCurrentRS(int currentRS) { this.currentRS = currentRS; }
}
