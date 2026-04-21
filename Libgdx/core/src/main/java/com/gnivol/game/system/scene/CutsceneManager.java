package com.gnivol.game.system.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.gnivol.game.audio.AudioManager;
import com.gnivol.game.system.rs.RSManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-driven cutscene system. Reads cutscene definitions and plays them
 * step by step with delta timing.
 */
public class CutsceneManager {

    // --- Model classes ---

    public static class CutsceneStep {
        public String type;
        public String color;
        public float duration;
        public String sprite;
        public String id;
        public float intensity;
        public float value;
    }

    public static class CutsceneData {
        public String id;
        public List<CutsceneStep> steps = new ArrayList<CutsceneStep>();
    }

    // --- Listener interface ---

    public interface CutsceneListener {
        void onFlash(String color, float duration);
        void onShowSprite(String sprite, float duration);
        void onShake(float intensity, float duration);
        void onFadeOut(float duration);
        void onFadeIn(float duration);
        void onSwapSprite(String target, String newSprite);
        void onDialogue(String dialogueId);
        void onCutsceneFinished(String cutsceneId);
    }

    // --- Fields ---

    private final Map<String, CutsceneData> cutscenes = new HashMap<String, CutsceneData>();
    private CutsceneData currentCutscene;
    private int currentStepIndex;
    private float stepTimer;
    private float stepDuration;
    private boolean playing;
    private boolean waitingForDialogue;

    private RSManager rsManager;
    private AudioManager audioManager;
    private CutsceneListener listener;

    // --- API ---

    public void loadCutscenes(String jsonPath) {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal(jsonPath));

            for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                CutsceneData data = new CutsceneData();
                data.id = entry.getString("id");

                JsonValue stepsArray = entry.get("steps");
                if (stepsArray != null) {
                    for (JsonValue stepVal = stepsArray.child; stepVal != null; stepVal = stepVal.next) {
                        CutsceneStep step = new CutsceneStep();
                        step.type = stepVal.getString("type", "");
                        step.color = stepVal.getString("color", null);
                        step.duration = stepVal.getFloat("duration", 0f);
                        step.sprite = stepVal.getString("sprite", null);
                        step.id = stepVal.getString("id", null);
                        step.intensity = stepVal.getFloat("intensity", 0f);
                        step.value = stepVal.getFloat("value", 0f);
                        data.steps.add(step);
                    }
                }

                cutscenes.put(data.id, data);
            }
            Gdx.app.log("CutsceneManager", "Loaded " + cutscenes.size() + " cutscenes");
        } catch (Exception e) {
            Gdx.app.error("CutsceneManager", "Failed to load cutscenes from: " + jsonPath, e);
        }
    }

    public void play(String cutsceneId) {
        CutsceneData data = cutscenes.get(cutsceneId);
        if (data == null) {
            Gdx.app.error("CutsceneManager", "Cutscene not found: " + cutsceneId);
            return;
        }
        currentCutscene = data;
        currentStepIndex = 0;
        stepTimer = 0f;
        stepDuration = 0f;
        playing = true;
        waitingForDialogue = false;
        executeCurrentStep();
    }

    public void update(float delta) {
        if (!playing || currentCutscene == null) return;
        if (waitingForDialogue) return;

        stepTimer += delta;
        if (stepTimer >= stepDuration) {
            advanceStep();
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setRSManager(RSManager rm) {
        this.rsManager = rm;
    }

    public void setAudioManager(AudioManager am) {
        this.audioManager = am;
    }

    public void setListener(CutsceneListener listener) {
        this.listener = listener;
    }

    /**
     * Call this when a dialogue triggered by cutscene finishes.
     */
    public void onDialogueFinished() {
        if (waitingForDialogue) {
            waitingForDialogue = false;
            advanceStep();
        }
    }

    // --- Internal ---

    private void advanceStep() {
        currentStepIndex++;
        if (currentStepIndex >= currentCutscene.steps.size()) {
            finishCutscene();
            return;
        }
        stepTimer = 0f;
        stepDuration = 0f;
        executeCurrentStep();
    }

    private void executeCurrentStep() {
        if (currentStepIndex >= currentCutscene.steps.size()) {
            finishCutscene();
            return;
        }

        CutsceneStep step = currentCutscene.steps.get(currentStepIndex);
        String type = step.type;

        if ("flash".equals(type)) {
            stepDuration = step.duration;
            if (listener != null) {
                listener.onFlash(step.color, step.duration);
            }
        } else if ("show_sprite".equals(type)) {
            stepDuration = step.duration;
            if (listener != null) {
                listener.onShowSprite(step.sprite, step.duration);
            }
        } else if ("sfx".equals(type)) {
            stepDuration = 0f;
            if (audioManager != null) {
                audioManager.playSFX(step.id);
            }
            advanceStep();
            return;
        } else if ("shake".equals(type)) {
            stepDuration = step.duration;
            if (listener != null) {
                listener.onShake(step.intensity, step.duration);
            }
        } else if ("fade_out".equals(type)) {
            stepDuration = step.duration;
            if (listener != null) {
                listener.onFadeOut(step.duration);
            }
        } else if ("fade_in".equals(type)) {
            stepDuration = step.duration;
            if (listener != null) {
                listener.onFadeIn(step.duration);
            }
        } else if ("wait".equals(type)) {
            stepDuration = step.duration;
        } else if ("rs_change".equals(type)) {
            stepDuration = 0f;
            if (rsManager != null) {
                rsManager.addRS(step.value);
            }
            advanceStep();
            return;
        } else if ("dialogue".equals(type)) {
            waitingForDialogue = true;
            if (listener != null) {
                listener.onDialogue(step.id);
            }
        } else if ("swap_sprite".equals(type)) {
            stepDuration = 0f;
            if (listener != null) {
                listener.onSwapSprite(step.id, step.sprite);
            }
            advanceStep();
            return;
        } else {
            Gdx.app.error("CutsceneManager", "Unknown step type: " + type);
            stepDuration = 0f;
            advanceStep();
            return;
        }
    }

    private void finishCutscene() {
        String finishedId = currentCutscene.id;
        playing = false;
        currentCutscene = null;
        currentStepIndex = 0;
        if (listener != null) {
            listener.onCutsceneFinished(finishedId);
        }
    }
}
