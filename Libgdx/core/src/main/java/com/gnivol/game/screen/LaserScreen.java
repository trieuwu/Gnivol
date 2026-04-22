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

    // 0 = UP, 1 = DOWN, 2 = LEFT, 3 = RIGHT
    private int playerDirection = 1;

    private boolean isMoving = false;
    private float moveTimer = 0f;
    private final float MOVE_DURATION = 0.15f; // Thời gian di chuyển (0.15 giây/ô)
    private float visualPlayerX = 0f;
    private float visualPlayerY = 0f;
    private int startX = 0, startY = 0;
    private int targetX = 0, targetY = 0;

    private Texture texBackground, texFloor, texWall, texGoal;
    private Texture texPlayerUp, texPlayerDown, texPlayerLeft, texPlayerRight;
    private Texture texTurretUp, texTurretDown, texTurretLeft, texTurretRight;
    private Texture texLaserH, texLaserV;

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
        game.getScreenFader().startFadeIn();

        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (isMoving) return false;

                if (keycode == Input.Keys.ESCAPE) {
                    if (game.getScreenFader().isFading()) return false;
                    game.getScreenFader().startFade(() -> {
                        game.setScreen(previousScreen);
                        game.getScreenFader().startFadeIn();
                        LaserScreen.this.dispose();
                    });
                    return true;
                }

                int dx = 0, dy = 0;

                if (keycode == Input.Keys.W || keycode == Input.Keys.UP) { dy = 1; playerDirection = 0; }
                else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) { dy = -1; playerDirection = 1; }
                else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) { dx = -1; playerDirection = 2; }
                else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) { dx = 1; playerDirection = 3; }

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
                startX = playerX;
                startY = playerY;
                targetX = nx;
                targetY = ny;

                isMoving = true;
                moveTimer = 0f;

                currentTime = (currentTime + 1) % 4;
            }
        }
    }

    private void onWin() {
        game.getPuzzleManager().markSolved("puzzle_laser");
        game.getInventoryManager().addItem("ca_vat_final");
        game.setScreen(previousScreen);

        game.getScreenFader().startFade(() -> {
            game.setScreen(previousScreen);
            game.getScreenFader().startFadeIn();
            LaserScreen.this.dispose();

            if (previousScreen instanceof GameScreen) {
                ((GameScreen) previousScreen).showNotification(
                    "Minigame Solved! Nhận được Cà vạt.",
                    Color.GREEN
                );
            }
        });

        if (game.getAutoSaveManager() != null) {
            game.getAutoSaveManager().onSaveTrigger("puzzle_laser");
        }
    }

    private void resetLevel() {
        playerX = 0; playerY = 0; currentTime = 0; playerDirection = 1;
        isMoving = false;
        visualPlayerX = 0f; visualPlayerY = 0f;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        float cellSize = Math.min(viewport.getWorldWidth(), viewport.getWorldHeight()) / (logic.N + 2f);
        float offsetX = (viewport.getWorldWidth() - (cellSize * logic.N)) / 2f;
        float offsetY = (viewport.getWorldHeight() - (cellSize * logic.N)) / 2f;

        float progress = 1f;
        if (isMoving) {
            moveTimer += delta;
            progress = Math.min(1f, moveTimer / MOVE_DURATION);

            visualPlayerX = startX + (targetX - startX) * progress;
            visualPlayerY = startY + (targetY - startY) * progress;

            if (progress >= 1f) {
                isMoving = false;
                playerX = targetX;
                playerY = targetY;

                if (logic.isTileDangerous(playerX, playerY, currentTime)) {
                    resetLevel();
                } else if (playerX == logic.N - 1 && playerY == logic.N - 1) {
                    onWin();
                }
            }
        } else {
            visualPlayerX = playerX;
            visualPlayerY = playerY;
        }

        batch.begin();
        batch.draw(texBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (int x = 0; x < logic.N; x++) {
            for (int y = 0; y < logic.N; y++) {
                float drawX = offsetX + x * cellSize;
                float drawY = offsetY + y * cellSize;

                if (logic.grid[x][y] == 1) batch.draw(texWall, drawX, drawY, cellSize, cellSize);
                else batch.draw(texFloor, drawX, drawY, cellSize, cellSize);

                if (x == logic.N - 1 && y == logic.N - 1) batch.draw(texGoal, drawX, drawY, cellSize, cellSize);
            }
        }

        for (int x = 0; x < logic.N; x++) {
            for (int y = 0; y < logic.N; y++) {
                if (logic.grid[x][y] >= 2) {
                    int dir = getTurretDir(logic.grid[x][y], currentTime);
                    int dx = 0, dy = 0;
                    if (dir == 2) dy = 1;       // UP
                    else if (dir == 4) dy = -1; // DOWN
                    else if (dir == 3) dx = 1;  // RIGHT
                    else if (dir == 5) dx = -1; // LEFT

                    int dist = 0;
                    int cx = x + dx;
                    int cy = y + dy;
                    while (cx >= 0 && cx < logic.N && cy >= 0 && cy < logic.N) {
                        dist++;
                        if (logic.grid[cx][cy] != 0) break; // Đụng vật cản
                        cx += dx; cy += dy;
                    }

                    if (dist > 0) {
                        float startWorldX = offsetX + x * cellSize + cellSize / 2f;
                        float startWorldY = offsetY + y * cellSize + cellSize / 2f;

                        float length = dist * cellSize * progress;

                        if (dir == 2) batch.draw(texLaserV, startWorldX - cellSize / 2f, startWorldY, cellSize, length);
                        else if (dir == 4) batch.draw(texLaserV, startWorldX - cellSize / 2f, startWorldY - length, cellSize, length);
                        else if (dir == 3) batch.draw(texLaserH, startWorldX, startWorldY - cellSize / 2f, length, cellSize);
                        else if (dir == 5) batch.draw(texLaserH, startWorldX - length, startWorldY - cellSize / 2f, length, cellSize);
                    }
                }
            }
        }

        for (int x = 0; x < logic.N; x++) {
            for (int y = 0; y < logic.N; y++) {
                if (logic.grid[x][y] >= 2) {
                    float drawX = offsetX + x * cellSize;
                    float drawY = offsetY + y * cellSize;
                    int dir = getTurretDir(logic.grid[x][y], currentTime);

                    Texture turretTex = texTurretDown;
                    if (dir == 2) turretTex = texTurretUp;
                    else if (dir == 3) turretTex = texTurretRight;
                    else if (dir == 4) turretTex = texTurretDown;
                    else if (dir == 5) turretTex = texTurretLeft;

                    batch.draw(turretTex, drawX, drawY, cellSize, cellSize);
                }
            }
        }

        Texture pTex = texPlayerDown;
        if (playerDirection == 0) pTex = texPlayerUp;
        else if (playerDirection == 2) pTex = texPlayerLeft;
        else if (playerDirection == 3) pTex = texPlayerRight;

        float pDrawX = offsetX + visualPlayerX * cellSize;
        float pDrawY = offsetY + visualPlayerY * cellSize;
        batch.draw(pTex, pDrawX, pDrawY, cellSize, cellSize);

        batch.end();
        game.getScreenFader().update(delta);
        game.getScreenFader().render();

    }

    private int getTurretDir(int baseType, int time) {
        int offset = baseType - 2;
        int currentOffset = (offset + time) % 4;
        return 2 + currentOffset; // 2=UP, 3=RIGHT, 4=DOWN, 5=LEFT
    }

    private void createTextures() {
        texBackground = new Texture(Gdx.files.internal("images/bathroom_bottle.png"));
        texFloor = new Texture(Gdx.files.internal("images/item_frame.png"));
        texWall = new Texture(Gdx.files.internal("images/Merge_button_glitch.png"));
        texGoal = new Texture(Gdx.files.internal("images/NPC1.char.png"));

        texPlayerUp = createColorTexture(Color.OLIVE);
        texPlayerDown = createColorTexture(Color.GREEN);
        texPlayerLeft = createColorTexture(Color.FOREST);
        texPlayerRight = createColorTexture(Color.LIME);

        texTurretUp = createColorTexture(Color.ORANGE);
        texTurretDown = createColorTexture(Color.YELLOW);
        texTurretLeft = createColorTexture(Color.GOLD);
        texTurretRight = createColorTexture(Color.FIREBRICK);

        texLaserH = createColorTexture(new Color(1f, 0f, 0f, 0.4f));
        texLaserV = createColorTexture(new Color(1f, 0f, 0.5f, 0.4f));
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
        texBackground.dispose(); batch.dispose(); texFloor.dispose(); texWall.dispose(); texGoal.dispose();
        texPlayerUp.dispose(); texPlayerDown.dispose(); texPlayerLeft.dispose(); texPlayerRight.dispose();
        texTurretUp.dispose(); texTurretDown.dispose(); texTurretLeft.dispose(); texTurretRight.dispose();
        texLaserH.dispose(); texLaserV.dispose();
    }
}
