package com.gnivol.game.model.dialogue;

import java.util.ArrayList;
import java.util.List;

// Đoạn hội thoại lớn (chứa nhiều Node)
public class DialogueTree {
    public String dialogueId;
    public String startNodeId; // Node đầu tiên khi bắt đầu
    public ArrayList<DialogueNode> nodes = new ArrayList<>();
}
