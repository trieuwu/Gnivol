package com.gnivol.game.audio;

/**
 * Luu tru gia tri volume cho nhac nen va hieu ung am thanh.
 * Gia tri tu 0.0 (tat) den 1.0 (max).
 */
public class AudioManager {

    // Volume nhac nen, mac dinh 70%
    private float musicVolume = 0.7f;

    // Volume hieu ung (nhat do, mo cua...), mac dinh 100%
    private float sfxVolume = 1.0f;

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        // Giu gia tri trong khoang 0 → 1
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }
}
