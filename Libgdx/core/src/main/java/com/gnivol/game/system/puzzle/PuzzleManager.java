package com.gnivol.game.system.puzzle;

import com.badlogic.gdx.Gdx;
import com.gnivol.game.system.interaction.InteractionCallback;
import com.gnivol.game.system.rs.RSEvent;
import com.gnivol.game.system.rs.RSEventType;
import com.gnivol.game.system.rs.RSManager;
import java.util.HashSet;
import java.util.Set;

public class PuzzleManager {
    private Set<String> solvedPuzzles;
    private RSManager rsManager;
    private InteractionCallback callback;

    public PuzzleManager(RSManager rsManager) {
        this.rsManager = rsManager;
        this.solvedPuzzles = new HashSet<>();
    }

    public void setCallback(InteractionCallback callback) {
        this.callback = callback;
    }

    public void openPuzzle(String puzzleId) {
        if (isPuzzleSolved(puzzleId)) {
            Gdx.app.log("Puzzle", "Have done!");
            return;
        }
        Gdx.app.log("Puzzle", "Showcase: " + puzzleId);
        if (callback != null) {
            callback.onOpenPuzzleOverlay(puzzleId);
        }
    }

    public boolean submitAnswer(String puzzleId, String answer) {
        if (puzzleId.equals("puzzle_drawer")) {
            if ("314".equals(answer)) {
                solvedPuzzles.add(puzzleId);
                rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_SOLVED, 10, puzzleId));
                Gdx.app.log("Puzzle", "Correct!");
                return true;
            } else {
                rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_FAILED, -5, puzzleId));
                Gdx.app.log("Puzzle", "Wrong!");
                if (callback != null) callback.onPuzzleFailed(puzzleId);
                return false;
            }
        }
        return false;
    }
    public boolean isPuzzleSolved(String puzzleId) {
        return solvedPuzzles.contains(puzzleId);
    }

}
