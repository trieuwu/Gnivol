package com.gnivol.game.system.puzzle;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.gnivol.game.system.save.ISaveable;

public class PuzzleManager implements ISaveable {

    private final Set<String> solvedPuzzles;
    private final Set<String> collectedItems = new HashSet<>();
    private final Map<String, Integer> puzzleFailCounts = new HashMap<>();

    public interface PuzzleCallback {
        void onShowPuzzleOverlay(String puzzleId);
    }

    private PuzzleCallback callback;

    public PuzzleManager() {
        this.solvedPuzzles = new HashSet<>();
    }

    public int getFailCount(String puzzleId) {
        return puzzleFailCounts.containsKey(puzzleId) ? puzzleFailCounts.get(puzzleId) : 0;
    }

    public void incrementFailCount(String puzzleId) {
        puzzleFailCounts.put(puzzleId, getFailCount(puzzleId) + 1);
    }

    public void markItemCollected(String itemId) {
        collectedItems.add(itemId);
    }

    public boolean isItemCollected(String itemId) {
        return collectedItems.contains(itemId);
    }

    public void setCallback(PuzzleCallback callback) {
        this.callback = callback;
    }

    public void openPuzzle(String puzzleId) {
        if (isPuzzleSolved(puzzleId)) {

            return;
        }

        if (callback != null) {
            callback.onShowPuzzleOverlay(puzzleId);
        }
    }

    public boolean submitAnswer(String puzzleId, String answer) {
        if ("puzzle_drawer".equals(puzzleId)) {
            if ("910".equals(answer)) {
                markSolved(puzzleId);
                return true;
            }
        }
        return false;
    }


    public boolean isPuzzleSolved(String puzzleId) {
        return solvedPuzzles.contains(puzzleId);
    }


    public void markSolved(String puzzleId) {
        solvedPuzzles.add(puzzleId);
    }



    @Override
    public void save(Json json) {
        json.writeObjectStart("puzzleManager");

        json.writeValue("solvedPuzzles", solvedPuzzles.toArray());
        json.writeValue("collectedItems", collectedItems.toArray());

        json.writeObjectStart("puzzleFailCounts");
        for (Map.Entry<String, Integer> entry : puzzleFailCounts.entrySet()) {
            json.writeValue(entry.getKey(), entry.getValue());
        }
        json.writeObjectEnd();

        json.writeObjectEnd();
    }

    @Override
    public void load(JsonValue jsonValue) {
        JsonValue pzJson = jsonValue.get("puzzleManager");
        solvedPuzzles.clear();
        collectedItems.clear();
        puzzleFailCounts.clear();

        if (pzJson != null) {
            if (pzJson.has("solvedPuzzles")) {
                for (JsonValue val : pzJson.get("solvedPuzzles")) {
                    if (val.isString()) {
                        solvedPuzzles.add(val.asString());
                    } else if (val.isObject() && val.has("value")) {
                        solvedPuzzles.add(val.getString("value"));
                    }
                }
            }

            if (pzJson.has("collectedItems")) {
                for (JsonValue val : pzJson.get("collectedItems")) {
                    if (val.isString()) {
                        collectedItems.add(val.asString());
                    } else if (val.isObject() && val.has("value")) {
                        collectedItems.add(val.getString("value"));
                    }
                }
            }

            if (pzJson.has("puzzleFailCounts")) {
                JsonValue failsJson = pzJson.get("puzzleFailCounts");
                for (JsonValue val = failsJson.child; val != null; val = val.next) {
                    puzzleFailCounts.put(val.name, val.asInt());
                }
            }

        }
    }

    public void reset() {
        solvedPuzzles.clear();
        collectedItems.clear();
        puzzleFailCounts.clear();
    }
}
