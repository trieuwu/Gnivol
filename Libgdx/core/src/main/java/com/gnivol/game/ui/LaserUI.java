package com.gnivol.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.gnivol.game.system.minigame.LaserLogic;

public class LaserUI {
    private final Stage stage;
    private final Window window;
    private final LaserLogic logic;

    private int playerX = 0;
    private int playerY = 0;
    private int currentTime = 0;

    // Các texture màu cơ bản (Dùng tạm để test)
    private Texture texFloor, texWall, texLaserGen, texDanger, texPlayer, texGoal;

    public interface LaserResultListener {
        void onLaserSolved(String puzzleId);
    }

    private LaserResultListener listener;
    private final String puzzleId = "puzzle_laser";

    public void setListener(LaserResultListener listener) {
        this.listener = listener;
    }

    public LaserUI(Skin skin, Stage stage) {
        this.stage = stage;
        this.logic = new LaserLogic();

        createTextures();

        window = new Window("Laser Maze - W A S D to move", skin);
        window.setSize(600, 600);
        window.setPosition((1280 - 600) / 2f, (720 - 600) / 2f);
        window.setMovable(false);
        window.setVisible(false);

        LaserBoard board = new LaserBoard();
        window.add(board).expand().fill().pad(20f);

        window.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (!window.isVisible()) return false;

                int dx = 0; int dy = 0;
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) dy = 1;
                else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) dy = -1;
                else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) dx = -1;
                else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) dx = 1;

                if (dx != 0 || dy != 0) {
                    movePlayer(dx, dy);
                    return true;
                }

                if (keycode == Input.Keys.ESCAPE) {
                    hide();
                    return true;
                }
                return false;
            }
        });

        stage.addActor(window);
    }

    private void movePlayer(int dx, int dy) {
        int nx = playerX + dx;
        int ny = playerY + dy;

        if (nx >= 0 && nx < logic.N && ny >= 0 && ny < logic.N) {
            if (logic.grid[nx][ny] == 0) {
                playerX = nx;
                playerY = ny;
                currentTime = (currentTime + 1) % 4;

                if (logic.isTileDangerous(playerX, playerY, currentTime)) {
                    Gdx.app.log("LaserGame", "YOU DIED!");
                    resetLevel();
                }
                else if (playerX == logic.N - 1 && playerY == logic.N - 1) {
                    Gdx.app.log("LaserGame", "YOU WON!");
                    hide();

                    if (listener != null) {
                        listener.onLaserSolved(puzzleId);
                    }
                }
            }
        }
    }

    public void show() {
        // Sinh một map ngẫu nhiên 10x10, 15 tường, 8 cục Laze
        logic.generateValidMap(10, 15, 8);
        resetLevel();
        window.setVisible(true);
        stage.setKeyboardFocus(window);
    }

    public void hide() {
        window.setVisible(false);
        if (stage.getKeyboardFocus() == window) {
            stage.setKeyboardFocus(null);
        }
    }

    private void resetLevel() {
        playerX = 0;
        playerY = 0;
        currentTime = 0;
    }

    private class LaserBoard extends Actor {
        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (logic.grid == null) return;

            float cellSize = Math.min(getWidth(), getHeight()) / logic.N;
            float startX = getX();
            float startY = getY();

            for (int x = 0; x < logic.N; x++) {
                for (int y = 0; y < logic.N; y++) {
                    float drawX = startX + x * cellSize;
                    float drawY = startY + y * cellSize;

                    if (logic.grid[x][y] == 1) {
                        batch.draw(texWall, drawX, drawY, cellSize, cellSize);
                    } else if (logic.grid[x][y] >= 2) {
                        batch.draw(texLaserGen, drawX, drawY, cellSize, cellSize);
                    } else {
                        batch.draw(texFloor, drawX, drawY, cellSize, cellSize);
                    }

                    if (logic.grid[x][y] == 0 && logic.isTileDangerous(x, y, currentTime)) {
                        batch.draw(texDanger, drawX, drawY, cellSize, cellSize);
                    }

                    if (x == logic.N - 1 && y == logic.N - 1) {
                        batch.draw(texGoal, drawX, drawY, cellSize, cellSize);
                    }

                    if (x == playerX && y == playerY) {
                        batch.draw(texPlayer, drawX, drawY, cellSize, cellSize);
                    }
                }
            }
        }
    }

    // Tiện ích tạo cục màu Solid bằng code
    private void createTextures() {
        texFloor = createColorTexture(Color.DARK_GRAY);
        texWall = createColorTexture(Color.LIGHT_GRAY);
        texLaserGen = createColorTexture(Color.YELLOW);
        texDanger = createColorTexture(new Color(1f, 0f, 0f, 0.4f));
        texPlayer = createColorTexture(Color.GREEN);
        texGoal = createColorTexture(Color.CYAN);
    }

    private Texture createColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }
}
