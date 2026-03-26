package com.gnivol.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.gnivol.game.screen.MainMenuScreen;

public class GnivolGame extends Game {

    //Đây là "hàng chờ". Khi muốn chuyển screen, không switch ngay mà ghi vào đây trước, đợi đầu frame sau mới thực sự switch.
    private Screen pendingScreen = null;

    @Override
    // Chạy lần đầu khi game khởi động
    public void create() {
        setScreen(new MainMenuScreen(this));
    }

    //Method setScreen
    public void switchScreen(Screen newScreen) {
        this.pendingScreen = newScreen;
    }

    @Override
    /* Ở đây sẽ lấy screen hiiện tại và đổi screen mới vào hàng chờ, chờ sau khi xóa xong screen cũ
    mới render screen tiếp theo để tránh crash*/
    public void render() {
        if (pendingScreen != null) {
            Screen old = getScreen();       // 1. Lấy screen hiện tại
            setScreen(pendingScreen);       // 2. Đổi sang screen mới
            pendingScreen = null;           // 3. Xoá hàng chờ
            if (old != null) {
                old.dispose();              // 4. Giải phóng screen cũ
            }
        }
        super.render();                     // 5. Render screen hiện tại
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (current != null) { // Phải check null vì nếu dispose script null khả năng cao sẽ gây crash
            current.dispose();
        }
    }
}