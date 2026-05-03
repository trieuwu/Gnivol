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

    private int stepCount = 0;
    private boolean isJumpscareActive = false;
    private float jumpscareTimer = 0f;
    private Texture texJumpscare;

    private Texture texBackground, texFloor, texWall, texGoal;
    private Texture texPlayerUp, texPlayerDown, texPlayerLeft, texPlayerRight;
    private Texture texTurretUp, texTurretDown, texTurretLeft, texTurretRight;
    private Texture texLaserH, texLaserV;

    private boolean isMapReady = false;

    private com.badlogic.gdx.scenes.scene2d.Stage uiStage;
    private boolean showInstructions = true;
    private Texture texDim;
    private float instructionDelayTimer = 0f;

    /** BGM id của room trước khi vào minigame, để restore khi exit. */
    private String previousBgmId;

    private void createInstructionUI(String titleText, String contentText) {
        uiStage = new com.badlogic.gdx.scenes.scene2d.Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));

        // Tạo kính mờ đen 85%
        com.badlogic.gdx.graphics.Pixmap pix = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pix.setColor(0, 0, 0, 0.9f);
        pix.fill();
        texDim = new Texture(pix);
        pix.dispose();

        com.badlogic.gdx.scenes.scene2d.ui.Image dimImg = new com.badlogic.gdx.scenes.scene2d.ui.Image(texDim);
        dimImg.setFillParent(true);
        uiStage.addActor(dimImg);

        com.badlogic.gdx.scenes.scene2d.ui.Table table = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        table.setFillParent(true);

        // Lấy font từ game
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle titleStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(game.getFontManager().fontTitle, Color.RED);
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle textStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(game.getFontManager().fontVietnamese, Color.WHITE);

        com.badlogic.gdx.scenes.scene2d.ui.Label title = new com.badlogic.gdx.scenes.scene2d.ui.Label(titleText, titleStyle);
        com.badlogic.gdx.scenes.scene2d.ui.Label content = new com.badlogic.gdx.scenes.scene2d.ui.Label(contentText, textStyle);
        content.setAlignment(com.badlogic.gdx.utils.Align.center);

        com.badlogic.gdx.scenes.scene2d.ui.Label hint = new com.badlogic.gdx.scenes.scene2d.ui.Label("(Click or press any key to begin)", textStyle);
        hint.setColor(Color.LIGHT_GRAY);

        table.add(title).padBottom(30).row();
        table.add(content).padBottom(50).row();
        table.add(hint);

        uiStage.addActor(table);
    }

    public LaserScreen(GnivolGame game, BaseScreen previousScreen) {
        super(game);
        this.logic = new LaserLogic();
        this.batch = new SpriteBatch();
        this.previousScreen = previousScreen;

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        //createTextures();
        //logic.generateValidMap(10, 15, 8);
    }
    public void initAsync(final Runnable onDone) {
        new Thread(() -> {
            logic.generateValidMap(10, 15, 8);
            Gdx.app.postRunnable(() -> {
                createTextures();
                isMapReady = true;
                if (onDone != null) onDone.run();
            });
        }).start();
    }
    @Override
    public void show() {
        game.getScreenFader().startFadeIn();

        // Lưu BGM hiện tại của room rồi cross sang minigame_music
        if (game.getAudioManager() != null) {
            previousBgmId = game.getAudioManager().getCurrentBGMId();
            game.getAudioManager().crossfadeBGM("minigame_music", 0.5f);
        }

        createInstructionUI("THE LABYRINTH",
            "Use W-A-S-D or Arrow keys to navigate the dark.\nEvade the crimson lasers and find the exit, or be burned alive.\n\nRemember... with every step you take, the turrets shift their gaze.\nThey are watching your every move.");

        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (showInstructions) {
                    if (instructionDelayTimer > 1.0f) showInstructions = false;
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (showInstructions) {
                    if (instructionDelayTimer > 1.0f) showInstructions = false;
                    return true;
                }


                if (isMoving || isJumpscareActive) return false;

                if (keycode == Input.Keys.ESCAPE) {
                    if (game.getScreenFader().isFading()) return false;
                    if (game.getAudioManager() != null && previousBgmId != null) {
                        game.getAudioManager().crossfadeBGM(previousBgmId, 0.5f);
                    }
                    game.getScreenFader().startFade(() -> {
                        game.setScreen(previousScreen);
                        game.getScreenFader().startFadeIn();
                        LaserScreen.this.dispose();
                    });
                    return true;
                }
                // Cheat F4: skip + win minigame ngay
                if (keycode == Input.Keys.F4) {
                    Gdx.app.log("LaserGame", "CHEAT F4: Force win");
                    onWin();
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
        if (game.getAutoSaveManager() != null) {
            game.getAutoSaveManager().onSaveTrigger("puzzle_laser_solved");
        }
        // --
        game.getInventoryManager().addItem("ca_vat_final");
        if (game.getAudioManager() != null) game.getAudioManager().playSFX("verification");
        if (previousScreen instanceof GameScreen) {
            ((GameScreen) previousScreen).showNotification("Bạn đã nhận được 1 cà vạt", com.badlogic.gdx.graphics.Color.YELLOW);
        }
        if (game.getAudioManager() != null && previousBgmId != null) {
            game.getAudioManager().crossfadeBGM(previousBgmId, 0.5f);
        }
        game.setScreen(previousScreen);

        game.getScreenFader().startFade(() -> {
            game.setScreen(previousScreen);
            game.getScreenFader().startFadeIn();
            LaserScreen.this.dispose();

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
        if (uiStage != null) uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        // BGM crossfade cần update mỗi frame (GameScreen không render khi minigame active)
        if (game.getAudioManager() != null) game.getAudioManager().update(delta);

        if (!isMapReady) {
            ScreenUtils.clear(0, 0, 0, 1);
            return;
        }
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
                stepCount++;

                if (stepCount > 0 && stepCount % 18 == 0) {
                    isJumpscareActive = true;
                    jumpscareTimer = 0f;
                    game.getRsManager().addRS(0f); // Tăng/giảm RS
                }

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
                    boolean hitWall = false;
                    while (cx >= 0 && cx < logic.N && cy >= 0 && cy < logic.N) {
                        dist++;
                        if (logic.grid[cx][cy] != 0) {
                            hitWall = true;
                            break;
                        }
                        cx += dx; cy += dy;
                    }

                    float targetLength = dist * cellSize;
                    if (!hitWall) {
                        targetLength += cellSize / 2f;
                    }

                    if (targetLength > 0) {
                        float startWorldX = offsetX + x * cellSize + cellSize / 2f;
                        float startWorldY = offsetY + y * cellSize + cellSize / 2f;

                        float length = targetLength * progress;

                        float thickness = cellSize * 0.4f;
                        float halfThick = thickness / 2f;

                        if (dir == 2) batch.draw(texLaserV, startWorldX - halfThick, startWorldY, thickness, length);
                        else if (dir == 4) batch.draw(texLaserV, startWorldX - halfThick, startWorldY - length, thickness, length);
                        else if (dir == 3) batch.draw(texLaserH, startWorldX, startWorldY - halfThick, length, thickness);
                        else if (dir == 5) batch.draw(texLaserH, startWorldX - length, startWorldY - halfThick, length, thickness);
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

        com.badlogic.gdx.graphics.g2d.BitmapFont font = game.getFontManager().fontTitle;
        font.setColor(Color.RED);
        font.draw(batch, "Count steps: " + stepCount, 40, Constants.WORLD_HEIGHT - 20);

        if (isJumpscareActive && texJumpscare != null) {
            jumpscareTimer += delta;
            if (jumpscareTimer > 2f) {
                isJumpscareActive = false;
            } else {
                batch.setColor(1, 1, 1, 1);
                batch.draw(texJumpscare, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            }
        }

        batch.end();
        if (showInstructions) {
            instructionDelayTimer += delta;
            if (uiStage != null) {
                uiStage.act(delta);
                uiStage.draw();
            }
        }
        game.getScreenFader().update(delta);
        game.getScreenFader().render();

    }

    private int getTurretDir(int baseType, int time) {
        int offset = baseType - 2;
        int currentOffset = (offset + time) % 4;
        return 2 + currentOffset; // 2=UP, 3=RIGHT, 4=DOWN, 5=LEFT
    }

    private void createTextures() {
        texBackground = new Texture(Gdx.files.internal("images/mini_games/mng1/anhnenminigame.jpg"));
        texFloor = new Texture(Gdx.files.internal("images/mini_games/mng1/background.png"));
        texWall = new Texture(Gdx.files.internal("images/mini_games/mng1/walls.png"));
        texGoal = new Texture(Gdx.files.internal("images/mini_games/mng1/door.png"));

        texPlayerUp = new Texture(Gdx.files.internal("images/mini_games/mng1/player_bw.png"));
        texPlayerDown = new Texture(Gdx.files.internal("images/mini_games/mng1/player_fw.png"));
        texPlayerLeft = new Texture(Gdx.files.internal("images/mini_games/mng1/player_facing_left.png"));
        texPlayerRight = new Texture(Gdx.files.internal("images/mini_games/mng1/player_facing_right.png"));

        texLaserH = new Texture(Gdx.files.internal("images/mini_games/mng1/laser_horizontal.png"));
        texLaserV = new Texture(Gdx.files.internal("images/mini_games/mng1/laser_vertical.png"));

        texTurretUp = new Texture(Gdx.files.internal("images/mini_games/mng1/turet.png"));
        texTurretDown = new Texture(Gdx.files.internal("images/mini_games/mng1/turet.png"));
        texTurretLeft = new Texture(Gdx.files.internal("images/mini_games/mng1/turet.png"));
        texTurretRight = new Texture(Gdx.files.internal("images/mini_games/mng1/turet.png"));

        try {
            texJumpscare = new Texture(Gdx.files.internal("images/horror/jumpscare.png"));
        } catch (Exception e) {
            Gdx.app.error("LaserScreen", "Not found jumpscare", e);
        }
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
        if (uiStage != null) uiStage.dispose();
        if (texDim != null) texDim.dispose();

    }
}
