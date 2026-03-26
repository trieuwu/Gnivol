package com.gnivol.game;

public final class Constants {
    // Virtual Resolution (Kích thước resolution cố định của game dùng FitViewPort nên dù phóng to thu nhỏ cũng không sao)
    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    // Game info
    public static final String GAME_TITLE = "Gnivol";
    public static final String GAME_VERSION = "1.0.3";

    // Save
    public static final String SAVE_DIR = ".gnivol/"; // Địa chỉ folder lưu file 
    public static final int MAX_SAVE_SLOTS = 3; // Số lượng slot người chơi có thể lưu ở màn hình Load Game

    
    public static final float RS_MAX = 100f; // Giá trị tối đã có RS
    public static final float RS_THRESHOLD = 0.5f; // Ngưỡng thay đổi của RS <50% thế giới bình thường và >50% thì thế giới có biến dị

    // Timing
    public static final float FADE_DURATION = 0.5f; // Thời gian có hiệu ứng fade khi người chơi chuyển phòng
    public static final float TYPEWRITER_SPEED = 0.03f; // Tốc độ xuất hiện chữ của dialogue 

    private Constants() {} // Hằng số nên không được phép tạo thêm
}