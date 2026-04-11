package com.gnivol.game.model.dialogue;

import java.util.ArrayList;
import java.util.List;

// Câu thoại
public class DialogueNode {
    public String id;
    public String speaker;           // Tên người nói (VD: "Hnil", "Bóng ma")
    public String content;           // Nội dung câu thoại
    public boolean textEffects;      // Hiệu ứng glitch/rung text
    public String nextNodeId;        // Dùng khi KHÔNG CÓ choice (chuyển thẳng tới node này)

    // Danh sách các lựa chọn. Nếu list rỗng -> NodeNoChoice.
    public List<Choice> choices = new ArrayList<>();

    public boolean hasChoice() {
        return choices != null && !choices.isEmpty();
    }
}
