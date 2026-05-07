package com.gnivol.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.gnivol.game.system.save.ISaveable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages BGM, SFX, and ambient audio playback.
 * Loads on demand, caches in HashMaps.
 */
public class AudioManager {

    private float musicVolume;
    private float sfxVolume;
    private Preferences prefs;

    public AudioManager() {
        prefs = Gdx.app.getPreferences("GnivolSettings");

        musicVolume = prefs.getFloat("musicVolume", 0.7f);
        sfxVolume = prefs.getFloat("sfxVolume", 1.0f);
    }

    private final Map<String, Music> bgmCache = new HashMap<String, Music>();
    private final Map<String, Sound> sfxCache = new HashMap<String, Sound>();
    private final Map<String, Music> ambientCache = new HashMap<String, Music>();

    /** Active streamIds cho SFX đang loop. Key = sfx id, Value = streamId của Sound.loop(). */
    private final Map<String, Long> loopingSfxStreams = new HashMap<String, Long>();

    private Music currentBGM;
    private String currentBGMId;
    private Music currentAmbient;
    private String currentAmbientId;

    // Crossfade state
    private boolean crossfading;
    private Music fadingOutMusic;
    private Music fadingInMusic;
    private float crossfadeDuration;
    private float crossfadeElapsed;

    // --- BGM ---

    public void playBGM(String id) {
        if (id == null) return;
        if (id.equals(currentBGMId) && currentBGM != null && currentBGM.isPlaying()) {
            return;
        }
        stopBGM();
        currentBGM = loadBGM(id);
        currentBGMId = id;
        if (currentBGM != null) {
            currentBGM.setVolume(musicVolume);
            currentBGM.setLooping(true);
            currentBGM.play();
        }
    }

    public void stopBGM() {
        if (currentBGM != null) {
            currentBGM.stop();
        }
        currentBGM = null;
        currentBGMId = null;
    }

    public void crossfadeBGM(String newId, float duration) {
        if (newId == null) return;
        if (newId.equals(currentBGMId)) return;

        Music newMusic = loadBGM(newId);
        if (newMusic == null) return;

        if (currentBGM != null && currentBGM.isPlaying()) {
            crossfading = true;
            fadingOutMusic = currentBGM;
            fadingInMusic = newMusic;
            crossfadeDuration = duration;
            crossfadeElapsed = 0f;

            fadingInMusic.setVolume(0f);
            fadingInMusic.setLooping(true);
            fadingInMusic.play();
        } else {
            currentBGM = newMusic;
            currentBGMId = newId;
            currentBGM.setVolume(musicVolume);
            currentBGM.setLooping(true);
            currentBGM.play();
        }

        currentBGM = newMusic;
        currentBGMId = newId;
    }

    /**
     * Call each frame to update crossfade progress.
     */
    public void update(float delta) {
        if (!crossfading) return;

        crossfadeElapsed += delta;
        float progress = Math.min(crossfadeElapsed / crossfadeDuration, 1f);

        if (fadingOutMusic != null) {
            fadingOutMusic.setVolume(musicVolume * (1f - progress));
        }
        if (fadingInMusic != null) {
            fadingInMusic.setVolume(musicVolume * progress);
        }

        if (progress >= 1f) {
            if (fadingOutMusic != null) {
                fadingOutMusic.stop();
            }
            fadingOutMusic = null;
            fadingInMusic = null;
            crossfading = false;
        }
    }

    // --- SFX ---

    public void playSFX(String id) {
        playSFX(id, sfxVolume);
    }

    public void playSFX(String id, float volume) {
        if (id == null) return;
        Sound sound = loadSFX(id);
        if (sound != null) {
            sound.play(volume);
        }
    }

    /** Phát SFX dạng loop. Idempotent: gọi lại với cùng id khi đang loop sẽ no-op. */
    public void playSFXLoop(String id) {
        playSFXLoop(id, sfxVolume);
    }

    public void playSFXLoop(String id, float volume) {
        if (id == null) return;
        if (loopingSfxStreams.containsKey(id)) return; // đang loop rồi, không restart
        Sound sound = loadSFX(id);
        if (sound != null) {
            long streamId = sound.loop(volume);
            loopingSfxStreams.put(id, streamId);
        }
    }

    /** Dừng instance loop của SFX này. No-op nếu chưa loop. */
    public void stopSFXLoop(String id) {
        if (id == null) return;
        Long streamId = loopingSfxStreams.remove(id);
        if (streamId == null) return;
        Sound sound = sfxCache.get(id);
        if (sound != null) {
            sound.stop(streamId);
        }
    }

    /** Dừng mọi SFX đang loop. */
    public void stopAllSFXLoops() {
        for (Map.Entry<String, Long> entry : loopingSfxStreams.entrySet()) {
            Sound sound = sfxCache.get(entry.getKey());
            if (sound != null) {
                sound.stop(entry.getValue());
            }
        }
        loopingSfxStreams.clear();
    }

    // --- Ambient ---

    public void playAmbient(String id) {
        if (id == null) return;
        if (id.equals(currentAmbientId) && currentAmbient != null && currentAmbient.isPlaying()) {
            return;
        }
        stopAmbient();
        currentAmbient = loadAmbient(id);
        currentAmbientId = id;
        if (currentAmbient != null) {
            currentAmbient.setVolume(musicVolume);
            currentAmbient.setLooping(true);
            currentAmbient.play();
        }
    }

    public void stopAmbient() {
        if (currentAmbient != null) {
            currentAmbient.stop();
        }
        currentAmbient = null;
        currentAmbientId = null;
    }

    /**
     * Phát SFX dưới dạng Music (streaming) để có thể bắt onCompletion → biết duration.
     * Không cache — caller chịu trách nhiệm dispose. Trả về null nếu file không tồn tại.
     */
    public Music playSfxOneShot(String id, float volume) {
        if (id == null) return null;
        String[] candidates = {
            "audio/sfx/" + id + ".wav",
            "audio/sfx/" + id + ".mp3",
            "sfx/" + id + ".mp3",
            "sfx/" + id + ".wav"
        };
        for (String path : candidates) {
            try {
                if (Gdx.files.internal(path).exists()) {
                    Music m = Gdx.audio.newMusic(Gdx.files.internal(path));
                    m.setVolume(volume);
                    m.play();
                    return m;
                }
            } catch (Exception ignored) {}
        }
        Gdx.app.error("AudioManager", "Failed to load SFX (one-shot): " + id);
        return null;
    }

    /** Tạm tắt nhạc nền (BGM + ambient) — không ghi prefs. Dùng cho jumpscare/SFX gây sốc. */
    public void duckMusic() {
        if (currentBGM != null) currentBGM.setVolume(0f);
        if (currentAmbient != null) currentAmbient.setVolume(0f);
    }

    /** Khôi phục nhạc nền về mức musicVolume hiện tại. Idempotent. */
    public void unduckMusic() {
        if (currentBGM != null) currentBGM.setVolume(musicVolume);
        if (currentAmbient != null) currentAmbient.setVolume(musicVolume);
    }

    // --- Volume controls ---

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentBGM != null) {
            currentBGM.setVolume(this.musicVolume);
        }
        if (currentAmbient != null) {
            currentAmbient.setVolume(this.musicVolume);
        }
        prefs.putFloat("musicVolume", this.musicVolume);
        prefs.flush();
    }

    public void setBGMVolume(float v) {
        setMusicVolume(v);
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float volume) {

        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
        prefs.putFloat("sfxVolume", this.sfxVolume);
        prefs.flush();
    }

    public void setSFXVolume(float v) {
        setSfxVolume(v);
    }

    // --- Dispose ---

    public void dispose() {
        stopAllSFXLoops();

        for (Music m : bgmCache.values()) {
            m.dispose();
        }
        bgmCache.clear();

        for (Sound s : sfxCache.values()) {
            s.dispose();
        }
        sfxCache.clear();

        for (Music m : ambientCache.values()) {
            m.dispose();
        }
        ambientCache.clear();

        currentBGM = null;
        currentBGMId = null;
        currentAmbient = null;
        currentAmbientId = null;
    }

    // --- Internal loading ---

    private Music loadBGM(String id) {
        Music music = bgmCache.get(id);
        if (music != null) return music;

        // Thử các đường dẫn quen thuộc — ogg/bgm trước, fallback sang mp3 trong sfx/
        String[] candidates = {
            "audio/bgm/" + id + ".ogg",
            "audio/bgm/" + id + ".mp3",
            "sfx/" + id + ".mp3",
            "sfx/" + id + ".ogg"
        };
        for (String path : candidates) {
            try {
                if (Gdx.files.internal(path).exists()) {
                    music = Gdx.audio.newMusic(Gdx.files.internal(path));
                    bgmCache.put(id, music);
                    return music;
                }
            } catch (Exception ignored) {}
        }
        Gdx.app.error("AudioManager", "Failed to load BGM: " + id);
        return null;
    }

    /** Trả về id của BGM đang phát (null nếu không có). */
    public String getCurrentBGMId() {
        return currentBGMId;
    }

    private Sound loadSFX(String id) {
        Sound sound = sfxCache.get(id);
        if (sound != null) return sound;

        String[] candidates = {
            "audio/sfx/" + id + ".wav",
            "audio/sfx/" + id + ".mp3",
            "sfx/" + id + ".mp3",
            "sfx/" + id + ".wav"
        };
        for (String path : candidates) {
            try {
                if (Gdx.files.internal(path).exists()) {
                    sound = Gdx.audio.newSound(Gdx.files.internal(path));
                    sfxCache.put(id, sound);
                    return sound;
                }
            } catch (Exception ignored) {}
        }
        Gdx.app.error("AudioManager", "Failed to load SFX: " + id);
        return null;
    }

    private Music loadAmbient(String id) {
        Music music = ambientCache.get(id);
        if (music == null) {
            try {
                music = Gdx.audio.newMusic(Gdx.files.internal("audio/ambient/" + id + ".ogg"));
                ambientCache.put(id, music);
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load ambient: " + id, e);
            }
        }
        return music;
    }
}
