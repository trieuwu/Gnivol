package com.gnivol.game.system.interaction;

import com.gnivol.game.entity.GameObject;

/**
 * Callback để GameScreen xử lý phần visual sau khi PlayerInteractionSystem dispatch.
 * Tách logic (interaction) khỏi visual (render/UI).
 */
public interface InteractionCallback {

    /** Hiện inspect text cho object */
    void onShowInspectText(String text);

    /** Click vào vùng trống — ẩn UI */
    void onEmptyClick();

    /** Item đã được nhặt — cập nhật visual (ẩn sprite, play sound...) */
    void onItemCollected(GameObject obj, String itemId);

    /** Door được click — chuyển scene với fade */
    void onDoorInteracted(GameObject obj);

    /** Object khác được tương tác (mở overlay, alt texture...) */
    void onObjectInteracted(GameObject obj);
}
