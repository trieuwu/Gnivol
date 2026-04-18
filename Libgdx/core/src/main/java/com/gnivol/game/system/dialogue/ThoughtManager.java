package com.gnivol.game.system.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.gnivol.game.model.dialogue.DialogueNode;
import com.gnivol.game.model.dialogue.DialogueTree;

import java.util.HashMap;

public class ThoughtManager {
    // Map lưu cấu trúc: Object_ID -> (RS_Range -> Text)
    // Ví dụ: "fridge" -> {"LOW": "...", "MID": "...", "HIGH": "..."}
    private HashMap<String, HashMap<String, String>> thoughtDatabase;

    public ThoughtManager() {
        thoughtDatabase = new HashMap<>();
        loadThoughts();
    }

    private void loadThoughts() {
        try {
            Json json = new Json();
            // Đọc file json dạng Map lồng Map
            thoughtDatabase = json.fromJson(HashMap.class, HashMap.class, Gdx.files.internal("data/thoughts.json"));
            Gdx.app.log("ThoughtManager", "Đã load dữ liệu suy nghĩ nội tâm.");
        } catch (Exception e) {
            Gdx.app.error("ThoughtManager", "LỖI ĐỌC FILE THOUGHTS.JSON", e);
        }
    }

    // Trả về một DialogueNode ảo chứa suy nghĩ
    public DialogueTree getThoughtTree(String objectId, float currentRS) {
        if (!thoughtDatabase.containsKey(objectId)) {
            return null; // Object này không có data suy nghĩ
        }

        HashMap<String, String> objectThoughts = thoughtDatabase.get(objectId);
        String text = "";

        // Phân loại RS
        if (currentRS < 35f) {
            text = objectThoughts.get("LOW");
        } else if (currentRS <= 65f) {
            text = objectThoughts.get("MID");
        } else {
            text = objectThoughts.get("HIGH");
        }

        // Đóng gói thành DialogueNode
        DialogueNode thoughtNode = new DialogueNode();
        thoughtNode.id = "thought_" + objectId;
        thoughtNode.speaker = "Suy nghĩ"; // Label trống, UI tự đổi màu xám
        thoughtNode.content = text != null ? text : "...";
        thoughtNode.choices = new java.util.ArrayList<>(); // Không có lựa chọn
        thoughtNode.nextNodeId = null;
        // Bọc vào 1 DialogueTree để ném cho UI chạy Typewriter
        DialogueTree tree = new DialogueTree();
        tree.dialogueId = "thought_" + objectId;
        tree.startNodeId = thoughtNode.id;
        tree.nodes = new java.util.ArrayList<>();
        tree.nodes.add(thoughtNode);
        return tree;
    }
}
