package com.gnivol.game.system.interaction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.entity.GameObject;
import com.gnivol.game.model.RoomData;
import com.gnivol.game.model.dialogue.DialogueTree;
import com.gnivol.game.screen.GameScreen;
import com.gnivol.game.system.dialogue.ThoughtManager;
import com.gnivol.game.system.scene.RoomScene;

import java.util.Map;

public class RoomInteractionHandler implements InteractionCallback {
    private final GnivolGame game;
    private final GameScreen screen;
    private final Map<String, DialogueTree> dialogueDatabase;

    public RoomInteractionHandler(GameScreen screen, Map<String, DialogueTree> dialogueDatabase) {
        this.screen = screen;
        this.game = screen.getGnivolGame(); // Đảm bảo GameScreen có hàm getter này
        this.dialogueDatabase = dialogueDatabase;
    }

    @Override
    public void onShowInspectText(String text) {
        screen.showInspectText(text);
    }

    @Override
    public void onEmptyClick() {
        screen.hideInspectText();
    }

    @Override
    public void onInventoryFull() {
        screen.showNotification("MAX INVENTORY!", Color.RED);
    }

    @Override
    public void onItemCollected(GameObject obj, String itemId) {
        com.gnivol.game.model.ItemData itemData = com.gnivol.game.data.ItemDatabase.getInstance().getItemData(itemId);
        if (itemData != null && itemData.pickupSoundID != null) {
            game.getAudioManager().playSFX(itemData.pickupSoundID);
        }
        screen.getInventoryUI().refreshUI();
        screen.hideInspectText();
        screen.showItemNotification(itemId);

        if ("keo_502_final".equals(itemId)) {
            if (screen.getSceneManager().getCurrentScene() instanceof RoomScene) {
                ((RoomScene) screen.getSceneManager().getCurrentScene()).changeBackground("images/back_ground/bathroom/bathroom_no_bottle.png");
            }
        }

        if (game.getAutoSaveManager() != null) {
            game.getAutoSaveManager().onSaveTrigger("pickup_" + itemId);
        }
    }

    @Override
    public void onDoorInteracted(GameObject obj) {
        // Logic cửa chính hành lang
        if ("door_left_hallway".equals(obj.getId())) {
            handleMainDoorInteraction();
            return;
        }

        // Logic chuyển cảnh thông thường qua targetScene trong JSON
        RoomData roomData = screen.getSceneManager().getCurrentScene().getRoomData();
        for (RoomData.RoomObject roomObj : roomData.getObjects()) {
            if (roomObj.id.equals(obj.getId()) && roomObj.properties != null && roomObj.properties.targetScene != null) {
                screen.changeSceneWithFade(roomObj.properties.targetScene);
                return;
            }
        }
    }

    @Override
    public void onObjectInteracted(GameObject obj) {
        String id = obj.getId();

        // 1. Cửa hàng xóm: có rìu → vào phòng đối diện; không có → dialogue mùi xác chết
        // Cutscene jumpscare đã chuyển sang trigger lần đầu vào hành lang (xem GameScreen.changeSceneWithFade)
        if ("door_neighbor".equals(id)) {
            if ("axe".equals(screen.getInventoryUI().getSelectedItem())) {
                screen.changeSceneWithFade("room_opposite");
            } else {
                onDialogueTriggered("neighbor_door_smell");
            }
            return;
        }

        // 2. Cầu thang
        if ("stairs".equals(id)) {
            if (game.getFlagManager().get("fingerprint_ok")) {
                screen.changeSceneWithFade("room_downstairs_placeholder");
            } else {
                screen.hideInspectText();
                onDialogueTriggered("stairs_need_fingerprint");
            }
            return;
        }

        // 3. Giường (chỉ hiện inspect text)
        if ("bed".equals(id)) {
            return;
        }

        // 4. Ngăn kéo (Puzzle)
        if ("drawer".equals(id)) {
            if (screen.getPuzzleManager().isPuzzleSolved("puzzle_drawer")) {
                screen.showNotification("Ngăn kéo đã trống rỗng.", Color.LIGHT_GRAY);
            } else {
                screen.getPuzzleManager().openPuzzle("puzzle_drawer");
            }
            return;
        }

        // 5. Bồn cầu (Sự kiện tắc bồn cầu)
        if ("toilet".equals(id)) {
            handleToiletInteraction();
            return;
        }

        if ("mirror".equals(id)) {
            if (!game.getFlagManager().get("mirror_video_seen")) {
                game.getFlagManager().set("mirror_video_seen");
                screen.hideInspectText();
                screen.getCutsceneManager().play("mirror_video_jumpscare");
                return;
            }
//            if (screen.getPuzzleManager().isPuzzleSolved("puzzle_sliding_marble")) {
//                screen.showNotification("Bạn đã giải mã xong bí mật của gấu bông.", Color.LIGHT_GRAY);
//            } else {
//                screen.getPuzzleManager().openPuzzle("puzzle_sliding_marble");
//            }
            return;
        }

        // 6. Bồn rửa mặt (Minigame Laser)
        if ("sink".equals(id)) {
            if (screen.getPuzzleManager().isPuzzleSolved("puzzle_laser")) {
                screen.showNotification("Mạch điện đã nối xong. Nước chảy bình thường.", Color.LIGHT_GRAY);
            } else {
                screen.getPuzzleManager().openPuzzle("puzzle_laser");
            }
            return;
        }

        if ("plush_toy".equals(id)) {
            if (!game.getFlagManager().get("plush_toy_scare")) {
                game.getFlagManager().set("plush_toy_scare");
                screen.hideInspectText();
                game.getAudioManager().playSFX("scream2");
                screen.getCutsceneManager().play("hand_under_bed");
            }
            return;
        }
        // Tranh creepy: chỉ hiện inspect text khi click (cutscene đã move sang sau door_neighbor)
        if ("creepy_painting".equals(id)) {
            return;
        }

        // Bác chủ trọ: counter visit → angry_1 → angry_2 → angry_3 (lần 3 chết bằng xiên)
        // Mỗi click trigger lại từ intro Y/N. Yes → angry tương ứng visit count. B → exit.
        // TODO: chỉ tăng counter khi player thực sự reach angry node (hiện tại tăng cả khi pick "Không")
        if ("chu_tro_npc".equals(id)) {
            int visitCount;
            if (game.getFlagManager().get("chu_tro_visited_2")) visitCount = 2;
            else if (game.getFlagManager().get("chu_tro_visited_1")) visitCount = 1;
            else visitCount = 0;

            String dialogueId;
            if (visitCount >= 2) {
                dialogueId = "chu_tro_visit_3";
            } else if (visitCount == 1) {
                dialogueId = "chu_tro_visit_2";
                game.getFlagManager().set("chu_tro_visited_2");
            } else {
                dialogueId = "chu_tro_visit_1";
                game.getFlagManager().set("chu_tro_visited_1");
            }
            onDialogueTriggered(dialogueId);
            return;
        }

        // 7. Xử lý chung cho Dialogue, Overlay và Thought
        handleGenericInteractions(obj);
    }

    @Override
    public void onDialogueTriggered(String dialogueId) {
        screen.triggerDialogue(dialogueId);
    }

    // --- CÁC HÀM TRỢ GIÚP LOGIC ---

    private void handleMainDoorInteraction() {
        if (screen.getPuzzleManager().isPuzzleSolved("main_door_unlocked")) {
            screen.changeSceneWithFade("room_hallway");
        } else if (screen.getPuzzleManager().isPuzzleSolved("key_broke_on_door")) {
            if ("chia_khoa_fixed_final".equals(screen.getInventoryUI().getSelectedItem())) {
                game.getInventoryManager().removeItem("chia_khoa_fixed_final");
                screen.getInventoryUI().clearSelection();
                screen.getPuzzleManager().markSolved("main_door_unlocked");
                screen.showNotification("Cạch! Cửa đã được mở khóa.", Color.GREEN);
                screen.getInventoryUI().refreshUI();
                if (game.getAutoSaveManager() != null) game.getAutoSaveManager().onSaveTrigger("unlock_main_door");
            } else {
                onDialogueTriggered("door_need_fixed_key");
            }
        } else {
            if ("chia_khoa_final".equals(screen.getInventoryUI().getSelectedItem())) {
                game.getInventoryManager().removeItem("chia_khoa_final");
                screen.getInventoryUI().clearSelection();
                game.getInventoryManager().addItem("chuoi_chia_khoa");
                screen.getPuzzleManager().markSolved("key_broke_on_door");
                screen.getInventoryUI().refreshUI();
                game.getAudioManager().playSFX("key_break2");
                onDialogueTriggered("key_broke");
                if (game.getAutoSaveManager() != null) game.getAutoSaveManager().onSaveTrigger("key_broke");
            } else {
                onDialogueTriggered("door_locked_no_key");
            }
        }
    }

    private void handleToiletInteraction() {
        if (game.getFlagManager().get("toilet_clogged")) {
            screen.showNotification("Bồn cầu đã bị tắc cứng.", Color.LIGHT_GRAY);
            return;
        }
        if ("ca_vat_final".equals(screen.getInventoryUI().getSelectedItem())) {
            screen.showToiletConfirmDialog(); // Chúng ta sẽ tạo hàm này trong GameScreen
        } else {
            screen.showInspectText("Hệ thống xả nước có vẻ vẫn hoạt động bình thường.");
        }
    }

    private void handleGenericInteractions(GameObject obj) {
        RoomData.RoomObject roomObj = screen.getRoomObjectData(obj.getId());
        if (roomObj == null || roomObj.properties == null) return;

        // Dialogue cố định từ JSON — repeatable=true bypass check finished (trigger được nhiều lần)
        if (roomObj.properties.dialogueId != null
                && (roomObj.properties.repeatable || !game.getGameState().isDialogueFinished(roomObj.properties.dialogueId))) {
            onDialogueTriggered(roomObj.properties.dialogueId);
            return;
        }

        // Mở Overlay (tủ quần áo, tủ lạnh) — đọc altTextures + check overlays.json cho taken state
        if (roomObj.properties.altTextures != null && !roomObj.properties.altTextures.isEmpty()) {
            String openPath;
            boolean allCollected = false;
            com.gnivol.game.ui.InventoryOverlay.OverlayData od = screen.getInventoryOverlayData(obj.getId());
            if (od != null && !od.items.isEmpty()) {
                allCollected = true;
                for (com.gnivol.game.ui.InventoryOverlay.OverlayItem oi : od.items) {
                    if (!game.getInventoryManager().hasItem(oi.itemId)) {
                        allCollected = false;
                        break;
                    }
                }
            }
            if (allCollected && roomObj.properties.altTextures.containsKey("taken")) {
                openPath = roomObj.properties.altTextures.get("taken");
            } else {
                openPath = roomObj.properties.altTextures.get("open");
            }
            if (openPath == null) {
                openPath = roomObj.properties.altTextures.values().iterator().next();
            }
            screen.openOverlay(openPath, obj.getId());
            return;
        }

        // Inner Thought dựa trên RS
        DialogueTree thoughtTree = new ThoughtManager().getThoughtTree(obj.getId(), game.getRsManager().getRS());
        if (thoughtTree != null) {
            screen.hideInspectText();
            screen.getDialogueEngine().loadDialogue(thoughtTree);
            screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
        }
    }

    @Override public void onOpenPuzzleOverlay(String puzzleId) {}
    @Override public void onPuzzleFailed(String puzzleId) {}
}
