package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.system.minigame.LaserLogic;

public class LaserScreen extends BaseScreen {
    private final LaserLogic logic;
    private final SpriteBatch batch;
    private final BaseScreen previousScreen;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private int playerX = 0;
    private int playerY = 0;
    private int currentTime = 0;

    private Texture texFloor, texWall, texLaserGen, texDanger, texPlayer, texGoal;
    private float cellSize;
    private float offsetX, offsetY;

    public LaserScreen(GnivolGame game, BaseScreen previousScreen) {
        super(game);
        this.logic = new LaserLogic();
        this.batch = new SpriteBatch();
        this.previousScreen = previousScreen;

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);

        createTextures();
        logic.generateValidMap(10, 15, 8);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                int dx = 0, dy = 0;
                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) dy = 1;
                else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) dy = -1;
                else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) dx = -1;
                else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) dx = 1;

                if (dx != 0 || dy != 0) {
                    handleMove(dx, dy);
                    return true;
                }
                return false;
            }
        });
    }

    private void handleMove(int dx, int dy) {
        int nx = playerX + dx;
        int ny = playerY + dy;

        if (nx >= 0 && nx < logic.N && ny >= 0 && ny < logic.N) {
            if (logic.grid[nx][ny] == 0) {
                playerX = nx;
                playerY = ny;
                currentTime = (currentTime + 1) % 4;

                if (logic.isTileDangerous(playerX, playerY, currentTime)) {
                    resetLevel();
                } else if (playerX == logic.N - 1 && playerY == logic.N - 1) {
                    onWin();
                }
            }
        }
    }

    private void onWin() {
        game.getPuzzleManager().markSolved("puzzle_laser");
        game.getInventoryManager().addItem("chuoi_chia_khoa");
        game.getInventoryManager().addItem("keo_502_final");

        game.setScreen(previousScreen);

        if (previousScreen instanceof GameScreen) {
            ((GameScreen) previousScreen).showNotification(
                "Minigame Solved! Nhận được Chuỗi chìa khóa & Keo 502.",
                Color.GREEN
            );
        }

        if (game.getAutoSaveManager() != null) {
            game.getAutoSaveManager().onSaveTrigger("puzzle_laser");
        }
    }

    private void resetLevel() {
        playerX = 0; playerY = 0; currentTime = 0;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        cellSize = Math.min(viewport.getWorldWidth(), viewport.getWorldHeight()) / (logic.N + 2f);
        offsetX = (viewport.getWorldWidth() - (cellSize * logic.N)) / 2f;
        offsetY = (viewport.getWorldHeight() - (cellSize * logic.N)) / 2f;

        batch.begin();
        for (int x = 0; x < logic.N; x++) {
            for (int y = 0; y < logic.N; y++) {
                float drawX = offsetX + x * cellSize;
                float drawY = offsetY + y * cellSize;

                Texture currentTex = texFloor;
                if (logic.grid[x][y] == 1) currentTex = texWall;
                else if (logic.grid[x][y] >= 2) currentTex = texLaserGen;
                batch.draw(currentTex, drawX, drawY, cellSize, cellSize);

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
        batch.end();
    }

    private void createTextures() {
        texFloor = createColorTexture(new Color(0.2f, 0.2f, 0.2f, 1f));
        texWall = createColorTexture(Color.GRAY);
        texLaserGen = createColorTexture(Color.GOLD);
        texDanger = createColorTexture(new Color(1, 0, 0, 0.5f));
        texPlayer = createColorTexture(Color.LIME);
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

    @Override
    public void dispose() {
        batch.dispose();
        texFloor.dispose(); texWall.dispose(); texLaserGen.dispose();
        texDanger.dispose(); texPlayer.dispose(); texGoal.dispose();
    }
}
