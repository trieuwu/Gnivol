package com.gnivol.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.Constants;

public class DesktopLauncher {
    public static void main(String[] args) {
        //Tạo object cấu hình cho LWJGL3 — thư viện mà LibGDX dùng để tạo cửa sổ, xử lý đồ hoạ OpenGL, input trên Desktop.
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        //Set tiêu đề dụa theo tên config ở file Constants và set cấu hình game
        config.setTitle(Constants.GAME_TITLE + " v" + Constants.GAME_VERSION);
        config.setWindowedMode((int) Constants.WORLD_WIDTH, (int) Constants.WORLD_HEIGHT);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);

        /*
         làm 3 việc cùng lúc: tạo cửa sổ theo config, tạo instance GnivolGame, rồi bắt đầu game loop
         Từ đây LibGDX sẽ gọi GnivolGame.create() → rồi lặp render() 60 lần/giây cho đến khi tắt game.
         */
        new Lwjgl3Application(new GnivolGame(), config);
    }
}