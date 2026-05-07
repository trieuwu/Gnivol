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

        if ("fire_hose_box".equals(obj.getId())) {
            if (game.getFlagManager().get("hop_chua_chay_broken")) {
                screen.changeSceneWithFade("room_chua_chay_hong_chia_khoa5");
                return;
            }
            else if (game.getFlagManager().get("first_click_hop_chua_chay")) {
                // chưa đập vỡ hết sẽ reset lại
                game.getFlagManager().set("hop_chua_chay_hit_1", false);
                game.getFlagManager().set("hop_chua_chay_hit_2", false);
                game.getFlagManager().set("hop_chua_chay_hit_3", false);

                screen.changeSceneWithFade("room_chua_chay_hong_chia_khoa1");
                return;
            }
        }

        // Logic chuyển cảnh thông thường qua targetScene trong JSON
        RoomData roomData = screen.getSceneManager().getCurrentScene().getRoomData();
        for (RoomData.RoomObject roomObj : roomData.getObjects()) {
            if (roomObj.id.equals(obj.getId()) && roomObj.properties != null && roomObj.properties.targetScene != null) {
                screen.changeSceneWithFade(roomObj.properties.targetScene);

                boolean isFirstTimeHallway = obj.getId().equals("door_left_hallway")
                    && !game.getFlagManager().get("first_time_hallway");

                // --- THÊM DÒNG NÀY ĐỂ KIỂM TRA MINIGAME ---
                boolean isPendingMinigame = game.getFlagManager().get("started_minigame_2")
                    && !screen.getPuzzleManager().isPuzzleSolved("puzzle_sliding_marble");

                // --- SỬA LẠI ĐIỀU KIỆN HIỆN THOUGHT: Chỉ hiện nếu KHÔNG bị vướng sự kiện nào! ---
                if (!isFirstTimeHallway && !isPendingMinigame) {
                    DialogueTree thoughtTree = new ThoughtManager().getThoughtTree(obj.getId(), game.getRsManager().getRS());
                    if (thoughtTree != null) {
                        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                            @Override
                            public void run() {
                                Gdx.app.postRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        screen.hideInspectText();
                                        screen.getDialogueEngine().loadDialogue(thoughtTree);
                                        screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                                    }
                                });
                            }
                        }, 0.3f);
                    }
                }
                return;
            }
        }
    }

    @Override
    public void onObjectInteracted(GameObject obj) {
        String id = obj.getId();
        if ("mirror".equals(id)) {
            // 1. Nếu chưa xem video ma -> Chiếu video
            if (!game.getFlagManager().get("mirror_video_seen")) {
                game.getFlagManager().set("mirror_video_seen");
                screen.hideInspectText();
                screen.getCutsceneManager().play("mirror_video_jumpscare");
                return;
            }

            // 2. Nếu ĐÃ xem video rồi -> Cho phép đập gương
            if (screen.getSceneManager().getCurrentScene() instanceof RoomScene) {
                RoomScene rs = (RoomScene) screen.getSceneManager().getCurrentScene();

                if (!screen.getGnivolGame().getInventoryManager().hasItem("glass_shard")) {
                    rs.startMirrorBreakEvent();

                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            // Khi bathroom_break_mirror.png hiện ra (t=3s) → mới phát SFX gương vỡ + verification + nhận item
                            game.getAudioManager().playSFX("broken-glass-sound-effect-high-quality");
                            screen.getGnivolGame().getInventoryManager().addItem("glass_shard");
                            screen.getInventoryUI().refreshUI();
                            game.getAudioManager().playSFX("verification");

                            DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("nhan_manh_kinh", game.getRsManager().getRS());
                            if (thoughtTree != null) {
                                // --- THÊM TIMER ĐỂ TRÌ HOÃN HỘI THOẠI 0.7 GIÂY ---
                                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                    @Override
                                    public void run() {
                                        com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                screen.hideInspectText();
                                                screen.getDialogueEngine().loadDialogue(thoughtTree);
                                                screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                                            }
                                        });
                                    }
                                }, 0.001f); // Hẹn giờ 0.7s chờ chuyển cảnh gương vỡ xong
                                // --------------------------------------------------
                            }
                        }
                    }, 3.0f);
                } else {
                    DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("mirror_da_vo", game.getRsManager().getRS());
                    if (thoughtTree != null) {
                        screen.hideInspectText();
                        screen.getDialogueEngine().loadDialogue(thoughtTree);
                        screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                    }
                }
            }
            return;
        }
        // 1. Cửa hàng xóm: có rìu → vào phòng đối diện; không có → dialogue mùi xác chết
        // Cutscene jumpscare đã chuyển sang trigger lần đầu vào hành lang (xem GameScreen.changeSceneWithFade)
        if ("door_neighbor".equals(id)) {
            if ("axe".equals(screen.getInventoryUI().getSelectedItem()) && !game.getFlagManager().get("break_door_neighbor")) {
                game.getFlagManager().set("break_door_neighbor");
                screen.getSceneManager().changeScene("room_hallway_breaked_door");
                game.getGameState().setCurrentRoom("room_hallway_breaked_door");
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("pha_cua_bang_riu", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    screen.hideInspectText();
                                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                                }
                            });
                        }
                    }, 0.7f);
                }
                return;
            }
            else if (game.getFlagManager().get("break_door_neighbor") && !game.getFlagManager().get("first_click_break_door_neighbor")) {
                game.getFlagManager().set("first_click_break_door_neighbor");
                if (game.getAudioManager() != null) {
                    game.getAudioManager().stopBGM(); // Tắt nhạc nền hiện tại
                }
                screen.getCutsceneManager().play("room_opposite_video_jumpscare");
                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                    @Override
                    public void run() {
                        com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                screen.changeSceneWithFade("room_opposite");
                            }
                        });
                    }
                }, 26f);
            }
            else if(game.getFlagManager().get("break_door_neighbor")){
                screen.changeSceneWithFade("room_opposite");
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("phat_hien_xac_chet", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    screen.hideInspectText();
                                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                                }
                            });
                        }
                    }, 0.7f);
                }
            }
            else {
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

        if ("window".equals(id)) {
        }

        // 3. Giường (chỉ hiện inspect text)
        if ("bed".equals(id)) {
            handleBedInteraction();
            if ("glass_shard".equals(screen.getInventoryUI().getSelectedItem()) || game.getFlagManager().get("bed_cut")) {
                return;
            }
        }

        // 4. Ngăn kéo (Puzzle)
        if ("drawer".equals(id)) {
            if (screen.getPuzzleManager().isPuzzleSolved("puzzle_drawer")) {
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("drawer_da_mo", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    screen.hideInspectText();
                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                }
            } else {
                screen.hideInspectText();
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("hint_drawer", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                }
                screen.getPuzzleManager().openPuzzle("puzzle_drawer");
            }
            return;
        }

        // 5. Bồn cầu (Sự kiện tắc bồn cầu)
        if ("toilet".equals(id)) {
            handleToiletInteraction();
            if ("fabric_piece".equals(screen.getInventoryUI().getSelectedItem()) || game.getFlagManager().get("toilet_clogged")) {
                return;
            }
        }

        // 6. Bồn rửa mặt (Minigame Laser)
        if ("sink".equals(id)) {
            if (screen.getPuzzleManager().isPuzzleSolved("puzzle_laser")) {
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("sink_da_sua", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    screen.hideInspectText();
                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                }
            } else {
                screen.hideInspectText();
                screen.getPuzzleManager().openPuzzle("puzzle_laser");
            }
            return;
        }

        if ("plush_toy".equals(id)) {
            if (!game.getFlagManager().get("plush_toy_scare")) {
                game.getFlagManager().set("plush_toy_scare");
                screen.hideInspectText();
                // Gấp đôi volume scream2 cho hiệu ứng giật mình
                float vol = Math.min(1.0f, game.getAudioManager().getSfxVolume() * 2.0f);
                game.getAudioManager().playSFX("scream2", vol);
                screen.getCutsceneManager().play("hand_under_bed");
                return;
            }
        }
        // Tranh creepy: chỉ hiện inspect text khi click (cutscene đã move sang sau door_neighbor)
        if ("creepy_painting".equals(id)) {
        }

        // Bác chủ trọ: counter visit → angry_1 → angry_2 → angry_3 (lần 3 chết bằng xiên)
        // Mỗi click trigger lại từ intro Y/N. Yes → angry tương ứng visit count. B → exit.
        // TODO: chỉ tăng counter khi player thực sự reach angry node (hiện tại tăng cả khi pick "Không")
        if ("chu_tro_npc".equals(id)) {
            // Kiểm tra xem bồn cầu đã tắc chưa
            if (game.getFlagManager().get("toilet_clogged")) {
                if (!game.getFlagManager().get("chu_tro_fixed_toilet")) {
                    // Chưa gọi lên sửa -> Báo tắc
                    game.getFlagManager().set("chu_tro_fixed_toilet");
                    onDialogueTriggered("chu_tro_clogged_door");
                } else {
                    // Đã gọi rồi -> Bác chủ trọ đang đợi trong nhà tắm
                    DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("chu_tro_dang_sua_bon_cau", game.getRsManager().getRS());
                    if (thoughtTree != null) {
                        screen.hideInspectText();
                        screen.getDialogueEngine().loadDialogue(thoughtTree);
                        screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                    }
                }
                return;
            }
            // Nếu bồn cầu chưa tắc
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
        if ("chair".equals(id)) {
            if (game.getFlagManager().get("chair_on_bed")) {
                // Thêm rời lại ghế về vị trí cũ
                onDialogueTriggered("confirm_move_chair_back");
            } else {
                screen.hideInspectText();
                onDialogueTriggered("confirm_move_chair");
            }
            return;
        }

        if ("ceiling_fan".equals(id)) {
            if (game.getFlagManager().get("tie_hung")) {
                // Đã treo cà vạt -> Hỏi tự tử
                screen.hideInspectText();
                onDialogueTriggered("confirm_suicide");
                return;
            }
            // Nếu ghế đã trên giường VÀ tay đang cầm cà vạt
            else if (game.getFlagManager().get("chair_on_bed") && "ca_vat_final".equals(screen.getInventoryUI().getSelectedItem())) {
                screen.hideInspectText();
                onDialogueTriggered("confirm_hang_tie");
                return;
            }
            // Nếu có ghế nhưng ko cầm cà vạt
        }

        // Password scanner ở cổng chính (room_password_closeup): có vân tay → trigger final_ending
        if ("password_scanner".equals(id)) {
            if ("manh_kinh_van_tay".equals(screen.getInventoryUI().getSelectedItem())) {
                game.getInventoryManager().removeItem("manh_kinh_van_tay");
                screen.getInventoryUI().clearSelection();
                screen.getInventoryUI().refreshUI();
                screen.hideInspectText();
                screen.getCutsceneManager().play("final_ending");
            } else {
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("scanner_thieu_van_tay", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    screen.hideInspectText();
                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                }
            }
            return;
        }

        if("hop_chua_chay".equals(id)){
            if(!game.getFlagManager().get("first_click_hop_chua_chay")){
                game.getFlagManager().set("first_click_hop_chua_chay", true);
                screen.getSceneManager().changeScene("room_chua_chay_hong_chia_khoa1");
                game.getGameState().setCurrentRoom("room_chua_chay_hong_chia_khoa1");
                handleGenericInteractions(obj);
                return;
            }
            if (!game.getFlagManager().get("hop_chua_chay_broken")) {
                screen.hideInspectText();
                if ("bone".equals(screen.getInventoryUI().getSelectedItem())) {
                    // 1
                    if (!game.getFlagManager().get("hop_chua_chay_hit_1")) {
                        game.getFlagManager().set("hop_chua_chay_hit_1", true);
                        if (game.getAudioManager() != null) game.getAudioManager().playSFX("breaking_tuchuachay");
                        screen.getSceneManager().changeScene("room_chua_chay_hong_chia_khoa2");
                        game.getGameState().setCurrentRoom("room_chua_chay_hong_chia_khoa2");
                    }
                    // 2
                    else if (!game.getFlagManager().get("hop_chua_chay_hit_2")) {
                        game.getFlagManager().set("hop_chua_chay_hit_2", true);
                        if (game.getAudioManager() != null) game.getAudioManager().playSFX("breaking_tuchuachay");
                        screen.getSceneManager().changeScene("room_chua_chay_hong_chia_khoa3");
                        game.getGameState().setCurrentRoom("room_chua_chay_hong_chia_khoa3");
                    }
                    // 3
                    else if (!game.getFlagManager().get("hop_chua_chay_hit_3")) {
                        game.getFlagManager().set("hop_chua_chay_hit_3", true);
                        if (game.getAudioManager() != null) game.getAudioManager().playSFX("breaking_tuchuachay");
                        screen.getSceneManager().changeScene("room_chua_chay_hong_chia_khoa4");
                        game.getGameState().setCurrentRoom("room_chua_chay_hong_chia_khoa4");
                    }
                }
                else if(game.getFlagManager().get("hop_chua_chay_hit_3")){
                    screen.hideInspectText();
                    game.getFlagManager().set("hop_chua_chay_broken", true);
                    screen.getInventoryUI().clearSelection();
                    screen.getInventoryUI().refreshUI();

                    screen.getSceneManager().changeScene("room_chua_chay_hong_chia_khoa5");
                    game.getGameState().setCurrentRoom("room_chua_chay_hong_chia_khoa5");

                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override
                        public void run() {
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    game.getInventoryManager().addItem("axe");
                                    screen.getInventoryUI().refreshUI();
                                    if (game.getAudioManager() != null) {
                                        game.getAudioManager().playSFX("verification");
                                    }
                                    onDialogueTriggered("nhan_axe");
                                }
                            });
                        }
                    }, 0.7f);
                    return;
                }
                else {
                    onDialogueTriggered("thong_bao_hong_khoa");
                    return;
                }
            }
            else {
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("tu_chua_chay_da_vo", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    screen.hideInspectText();
                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                }
                return;
            }
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
            if (game.getFlagManager().get("started_minigame_2") && !screen.getPuzzleManager().isPuzzleSolved("puzzle_sliding_marble")) {
                screen.hideInspectText();
                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                    @Override
                    public void run() {
                        onDialogueTriggered("confirm_resume_minigame");
                    }
                }, 0.3f);
                return;
            }
            DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("door_left_hallway", game.getRsManager().getRS());
            if (thoughtTree != null) {
                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                    @Override
                    public void run() {
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                screen.hideInspectText();
                                screen.getDialogueEngine().loadDialogue(thoughtTree);
                                screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                            }
                        });
                    }
                }, 0.3f); // Hiện lên sau khi chuyển cảnh
            }
        } else if (screen.getPuzzleManager().isPuzzleSolved("key_broke_on_door")) {
            if ("chia_khoa_fixed_final".equals(screen.getInventoryUI().getSelectedItem())) {
                game.getInventoryManager().removeItem("chia_khoa_fixed_final");
                screen.getInventoryUI().clearSelection();
                screen.getPuzzleManager().markSolved("main_door_unlocked");
                game.getAudioManager().playSFX("open_door");
                DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("cua_chinh_da_mo_khoa", game.getRsManager().getRS());
                if (thoughtTree != null) {
                    screen.hideInspectText();
                    screen.getDialogueEngine().loadDialogue(thoughtTree);
                    screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
                }
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
            screen.hideInspectText();
            return;
        }
        if ("fabric_piece".equals(screen.getInventoryUI().getSelectedItem())) {
            screen.showToiletConfirmDialog();
            return;
        }
    }
    private void handleBedInteraction(){
        if (game.getFlagManager().get("bed_cut")) {
            DialogueTree thoughtTree = new ThoughtManager().getThoughtTree("bed_da_bi_cat", game.getRsManager().getRS());
            if (thoughtTree != null) {
                screen.hideInspectText();
                screen.getDialogueEngine().loadDialogue(thoughtTree);
                screen.getDialogueUI().displayNode(screen.getDialogueEngine().getCurrentNode());
            }
            return;
        }

        if ("glass_shard".equals(screen.getInventoryUI().getSelectedItem())) {
            screen.hideInspectText();
            screen.triggerDialogue("confirm_cut_bed");
            return;
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
