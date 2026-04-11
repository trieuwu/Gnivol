package com.gnivol.game.system.dialogue;
import com.gnivol.game.model.dialogue.Choice;
import com.gnivol.game.model.dialogue.DialogueNode;
import com.gnivol.game.model.dialogue.DialogueTree;
import com.gnivol.game.system.rs.RSEvent;
import com.gnivol.game.system.rs.RSEventType;
import com.gnivol.game.system.rs.RSManager;
import java.util.HashMap;
import java.util.Map;

public class DialogueEngine {
    private Map<String, DialogueNode> nodeMap;
    private DialogueNode currentNode;
    private RSManager rsManager;

    public DialogueEngine() {
        nodeMap = new HashMap<>();
    }

    // Load dữ liệu từ DialogueTree (được parse từ dialogues.json)
    public void loadDialogue(DialogueTree tree) {
        nodeMap.clear();
        for (DialogueNode node : tree.nodes) {
            nodeMap.put(node.id, node);
        }
        currentNode = nodeMap.get(tree.startNodeId);
    }

    public DialogueNode getCurrentNode() {
        return currentNode;
    }

    // Chuyển sang node tiếp theo (Dành cho Node KHÔNG có lựa chọn)
    public void advance() {
        if (currentNode != null && !currentNode.hasChoice() && currentNode.nextNodeId != null) {
            currentNode = nodeMap.get(currentNode.nextNodeId);
        } else {
            // Kết thúc treeDialogue
            currentNode = null;
        }
    }

    // Xử lý khi người chơi bấm vào một lựa chọn
    public void selectChoice(int choiceIndex) {
        if (currentNode != null && currentNode.hasChoice()) {
            if (choiceIndex >= 0 && choiceIndex < currentNode.choices.size()) {
                Choice selectedChoice = currentNode.choices.get(choiceIndex);
                // Thay đổi Reality Stability
                if (selectedChoice.rsChange != 0 && rsManager != null) {
                    // Khởi tạo RSEvent và quăng cho RSManager xử lý
                    RSEvent event = new RSEvent(
                        RSEventType.DIALOGUE_CHOICE,
                        selectedChoice.rsChange,
                        "Dialogue Node: " + currentNode.id
                    );
                    rsManager.processEvent(event);
                }
                // Chuyển tới node tiếp theo
                currentNode = nodeMap.get(selectedChoice.nextNodeId);
            }
        }
    }

    public boolean isFinished() {
        return currentNode == null;
    }
}
