package com.gnivol.game.system.save;

import com.badlogic.gdx.Gdx;

public class AutoSaveManager {
    private final DirtyTracker dirtyTracker;
    private final SaveGuard saveGuard;
    private final SaveLock saveLock;
    private final AsyncFileWriter fileWriter;

    private final GameSnapshot gameSnapshot;
    private final SaveUIController saveUIController;

    public AutoSaveManager(GameSnapshot snapshot, SaveUIController uiController) {
        this.dirtyTracker = new DirtyTracker();
        this.saveGuard = new SaveGuard();
        this.saveLock = new SaveLock();
        this.fileWriter = new AsyncFileWriter();
        this.gameSnapshot = snapshot;
        this.saveUIController = uiController;
    }

    public void onSaveTrigger(String triggerKey) {
        dirtyTracker.markDirty(triggerKey);

        if (!dirtyTracker.hasAnyDirty()) return;
        if (!saveGuard.canSave()) {
            Gdx.app.log("AutoSave", "Be blocked by SaveGuard. Currently event/jumpscare");
            return;
        }
        if (!saveLock.tryLock()) {
            Gdx.app.log("AutoSave", "Another saving, ignore.");
            return;
        }

        if (saveUIController != null) {
            saveUIController.showSavingIcon();
        }
        String jsonData = gameSnapshot.captureAndToJson();

        fileWriter.writeAsync(jsonData, "save_slot_1.json", new AsyncFileWriter.SaveCallback() {
            @Override
            public void onSuccess() {
                Gdx.app.log("AutoSave", "Save success.");
                dirtyTracker.clearAll();
                saveLock.unlock();

                if (saveUIController != null) {
                    saveUIController.hideSavingIcon();
                }
            }

            @Override
            public void onError(Exception e) {
                Gdx.app.error("AutoSave", "Error while saving: ", e);
                saveLock.unlock();

                if (saveUIController != null) {
                    saveUIController.hideSavingIcon();
                }
            }
        });
    }

    // Getters
    public DirtyTracker getDirtyTracker() { return dirtyTracker; }
    public SaveGuard getSaveGuard() { return saveGuard; }
    public GameSnapshot getGameSnapshot() { return gameSnapshot; }

    public void dispose() {
        fileWriter.dispose();
    }
}
