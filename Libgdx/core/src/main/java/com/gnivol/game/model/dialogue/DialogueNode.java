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

    public String onEnterCutscene;  // cutscene ID to trigger when entering this node
    public String onEnterSfx;        // SFX id to play when entering this node (VD: "sike", "scream2")
    public String portrait;          // đường dẫn ảnh nhân vật (VD: "images/characters/Linh.char.png")
    public String portraitSide;      // "left" hoặc "right" (mặc định "left")
    public float portraitX = -1;     // toạ độ X (-1 = dùng mặc định theo side)
    public float portraitY = -1;     // toạ độ Y (-1 = dùng mặc định)
    public float portraitW = -1;     // chiều rộng (-1 = dùng mặc định)
    public float portraitH = -1;     // chiều cao (-1 = dùng mặc định)

    public String portrait2;         // đường dẫn ảnh nhân vật thứ 2
    public String portraitSide2;     // "left" hoặc "right" cho nhân vật thứ 2
    public float portrait2X = -1;    // toạ độ X nhân vật 2
    public float portrait2Y = -1;    // toạ độ Y nhân vật 2
    public float portrait2W = -1;    // chiều rộng nhân vật 2
    public float portrait2H = -1;    // chiều cao nhân vật 2

    // Danh sách các lựa chọn. Nếu list rỗng -> NodeNoChoice.
    public ArrayList<Choice> choices = new ArrayList<>();

    public boolean hasChoice() {
        return choices != null && !choices.isEmpty();
    }
}
