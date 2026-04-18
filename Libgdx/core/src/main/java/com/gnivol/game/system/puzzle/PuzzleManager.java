package com.gnivol.game.system.puzzle;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashSet;
import java.util.Set;
import com.gnivol.game.system.save.ISaveable;

public class PuzzleManager implements ISaveable {

    private final Set<String> solvedPuzzles;
    private final Set<String> collectedItems = new HashSet<>();

    public interface PuzzleCallback {
        void onShowPuzzleOverlay(String puzzleId);
    }

    private PuzzleCallback callback;

    public PuzzleManager() {
        this.solvedPuzzles = new HashSet<>();
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
            if ("912".equals(answer)) {
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

        json.writeObjectEnd();
    }

    @Override
    public void load(JsonValue jsonValue) {
        JsonValue pzJson = jsonValue.get("puzzleManager");
        solvedPuzzles.clear();
        if (pzJson != null) {
            solvedPuzzles.clear();
            if (pzJson.has("solvedPuzzles")) {
                for (JsonValue val : pzJson.get("solvedPuzzles")) {
                    solvedPuzzles.add(val.asString());
                }
            }

            collectedItems.clear();
            if (pzJson.has("collectedItems")) {
                for (JsonValue val : pzJson.get("collectedItems")) {
                    collectedItems.add(val.asString());
                }
            }
        }
    }

    public void reset() {
        solvedPuzzles.clear();
        collectedItems.clear();
    }
}
