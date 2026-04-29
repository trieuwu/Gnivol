package com.gnivol.game.model.dialogue;

// Lớp đại diện cho 1 lựa chọn
public class Choice {
    public String id;
    public String content;           // Nội dung text hiển thị cho người chơi chọn
    public String nextNodeId;        // ID của node tiếp theo nếu chọn cái này
    public boolean textEffects;      // Có hiệu ứng text (rung, đỏ...) không
    public int rsChange = 0;
    public String cutsceneId;
}
