package com.gnivol.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages BGM, SFX, and ambient audio playback.
 * Loads on demand, caches in HashMaps.
 */
public class AudioManager {

    private float musicVolume = 0.7f;
    private float sfxVolume = 1.0f;

    private final Map<String, Music> bgmCache = new HashMap<String, Music>();
    private final Map<String, Sound> sfxCache = new HashMap<String, Sound>();
    private final Map<String, Music> ambientCache = new HashMap<String, Music>();

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
    }

    public void setBGMVolume(float v) {
        setMusicVolume(v);
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }

    public void setSFXVolume(float v) {
        setSfxVolume(v);
    }

    // --- Dispose ---

    public void dispose() {
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
        if (music == null) {
            try {
                music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/" + id + ".ogg"));
                bgmCache.put(id, music);
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load BGM: " + id, e);
            }
        }
        return music;
    }

    private Sound loadSFX(String id) {
        Sound sound = sfxCache.get(id);
        if (sound == null) {
            try {
                sound = Gdx.audio.newSound(Gdx.files.internal("audio/sfx/" + id + ".wav"));
                sfxCache.put(id, sound);
            } catch (Exception e) {
                Gdx.app.error("AudioManager", "Failed to load SFX: " + id, e);
            }
        }
        return sound;
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
