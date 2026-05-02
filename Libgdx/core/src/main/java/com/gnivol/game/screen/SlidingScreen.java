package com.gnivol.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gnivol.game.Constants;
import com.gnivol.game.GnivolGame;
import com.gnivol.game.system.minigame.SlidingLogic;

public class SlidingScreen extends BaseScreen {
    private final SlidingLogic logic;
    private final SpriteBatch batch;
    private final BaseScreen previousScreen;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private Texture texBackground, texWall, texMarble, texHole, texDoneBox;
    private Texture texBtnTop, texBtnBottom, texBtnLeft, texBtnRight;

    private float cellSize;
    private float boardOffsetX, boardOffsetY;
    private final float BUTTON_SIZE_RATIO = 0.8f;

    private float[] visX;
    private float[] visY;
    private boolean isAnimating = false;

    private int stepCount = 0;
    private boolean isJumpscareActive = false;
    private float jumpscareTimer = 0f;
    private Texture texJumpscare;
    private boolean isMapReady = false;

    private com.badlogic.gdx.scenes.scene2d.Stage uiStage;
    private boolean showInstructions = true;
    private Texture texDim;
    private float instructionDelayTimer = 0f;

    /** BGM id của room trước khi vào minigame, để restore khi exit. */
    private String previousBgmId;

    private void createInstructionUI(String titleText, String contentText) {
        uiStage = new com.badlogic.gdx.scenes.scene2d.Stage(new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT));

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

    public SlidingScreen(GnivolGame game, BaseScreen previousScreen) {
        super(game);
        this.logic = new SlidingLogic();
        this.previousScreen = previousScreen;

        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);

         loadAssets();
        // logic.generateValidMap(15, 4, 7);

//        visX = new float[logic.marbles.size()];
//        visY = new float[logic.marbles.size()];
//        snapVisuals();
    }


    private void loadAssets() {
        texBackground = new Texture(Gdx.files.internal("images/mini_games/mng1/anhnenminigame.jpg"));
        texWall = new Texture(Gdx.files.internal("images/mini_games/mng2/wall1.png"));
        texMarble = new Texture(Gdx.files.internal("images/mini_games/mng2/box.png"));
        texHole = new Texture(Gdx.files.internal("images/mini_games/mng2/x.png"));

        texDoneBox = new Texture(Gdx.files.internal("images/mini_games/mng2/done_box.png"));

        // --- LOAD ẢNH 4 NÚT ---
        texBtnBottom = new Texture(Gdx.files.internal("images/mini_games/mng2/pointer_down.png"));
        texBtnLeft = new Texture(Gdx.files.internal("images/mini_games/mng2/pointer_left.png"));
        texBtnTop = new Texture(Gdx.files.internal("images/mini_games/mng2/pointer_up.png"));
        texBtnRight = new Texture(Gdx.files.internal("images/mini_games/mng2/pointer_right.png"));

        try {
            texJumpscare = new Texture(Gdx.files.internal("images/horror/jumpscare.png"));
        } catch (Exception e) {
            Gdx.app.error("SlidingScreen", "Không tìm thấy ảnh jumpscare", e);
        }
    }

    public void generateMapAsync(final Runnable onDone) {
        new Thread(() -> {
            logic.generateValidMap(15, 4, 7);

            Gdx.app.postRunnable(() -> {
                visX = new float[logic.marbles.size()];
                visY = new float[logic.marbles.size()];
                snapVisuals();

                isMapReady = true;
                if (onDone != null) onDone.run();
            });
        }).start();
    }

    private void snapVisuals() {
        for (int i = 0; i < logic.marbles.size(); i++) {
            visX[i] = logic.marbles.get(i).x;
            visY[i] = logic.marbles.get(i).y;
        }
    }

    @Override
    public void show() {
        game.getScreenFader().startFadeIn();

        // Lưu BGM hiện tại của room rồi cross sang minigame_music
        if (game.getAudioManager() != null) {
            previousBgmId = game.getAudioManager().getCurrentBGMId();
            game.getAudioManager().crossfadeBGM("minigame_music", 0.5f);
        }

        createInstructionUI("THE SHIFTING GRAVES",
            "Click the border arrows to push the heavy wooden crates.\nYour task is to drag all of them to the bloody 'X' marks.\n\nBeware... the more steps you wander, the deeper the madness will consume your mind.");

        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (showInstructions) {
                    if (instructionDelayTimer > 1.0f) showInstructions = false;
                    return true;
                }

                if (!isMapReady || button != Input.Buttons.LEFT || isAnimating || isJumpscareActive) return false;

                Vector3 touch = new Vector3(screenX, screenY, 0);
                camera.unproject(touch, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

                handleInput(touch.x, touch.y);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (showInstructions) {
                    showInstructions = false;
                    return true;
                }

                if (isJumpscareActive) return false;

                if (keycode == Input.Keys.ESCAPE) {
                    exitMinigame();
                    return true;
                }
                if (keycode == Input.Keys.R) {
                    logic.resetBoard();
                    snapVisuals();
                    return true;
                }
                return false;
            }
        });
    }

    private void handleInput(float worldX, float worldY) {
        float btnS = cellSize * BUTTON_SIZE_RATIO;
        boolean moved = false;

        for (int i = 0; i < logic.N; i++) {
            if (isClicked(worldX, worldY, boardOffsetX - cellSize, boardOffsetY + i * cellSize, btnS)) {
                logic.pullRow(i, -1);
                moved = true;
            }
            else if (isClicked(worldX, worldY, boardOffsetX + logic.N * cellSize, boardOffsetY + i * cellSize, btnS)) {
                logic.pullRow(i, 1);
                moved = true;
            }
            else if (isClicked(worldX, worldY, boardOffsetX + i * cellSize, boardOffsetY - cellSize, btnS)) {
                logic.pullCol(i, -1);
                moved = true;
            }
            else if (isClicked(worldX, worldY, boardOffsetX + i * cellSize, boardOffsetY + logic.N * cellSize, btnS)) {
                logic.pullCol(i, 1);
                moved = true;
            }
        }

        if (moved) {
            stepCount++;
            if (stepCount > 0 && stepCount % 18 == 0) {
                isJumpscareActive = true;
                jumpscareTimer = 0f;
                game.getRsManager().addRS(0f);
            }
        }
    }

    private boolean isClicked(float tx, float ty, float bx, float by, float size) {
        return tx >= bx && tx <= bx + cellSize && ty >= by && ty <= by + cellSize;
    }

    private boolean isHoleOccupied(int x, int y) {
        for (int i = 0; i < logic.marbles.size(); i++) {
            com.gnivol.game.system.minigame.SlidingLogic.Marble m = logic.marbles.get(i);
            if (m.locked && m.x == x && m.y == y && visX[i] == x && visY[i] == y) {
                return true;
            }
        }
        return false;
    }

    private void checkWin() {
        if (logic.isWin()) {
            Gdx.app.log("SlidingGame", "Victory!");
            game.getPuzzleManager().markSolved("puzzle_sliding_marble");
            if (game.getAutoSaveManager() != null) {
                game.getAutoSaveManager().onSaveTrigger("puzzle_laser_solved");
            }

            exitMinigame();
        }
    }

    private void exitMinigame() {
        if (game.getScreenFader().isFading()) return;
        // Restore BGM của room trước khi rời minigame
        if (game.getAudioManager() != null && previousBgmId != null) {
            game.getAudioManager().crossfadeBGM(previousBgmId, 0.5f);
        }
        game.getScreenFader().startFade(() -> {
            game.setScreen(previousScreen);
            game.getScreenFader().startFadeIn();
            if (previousScreen instanceof com.gnivol.game.screen.GameScreen) {
                com.gnivol.game.screen.GameScreen gameScreen = (com.gnivol.game.screen.GameScreen) previousScreen;
                if (!gameScreen.getPuzzleManager().isPuzzleSolved("puzzle_sliding_marble")) {
                    gameScreen.triggerDialogue("confirm_resume_minigame");
                }
            }
            this.dispose();
        });
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

        cellSize = Math.min(viewport.getWorldWidth(), viewport.getWorldHeight()) / (logic.N + 3f);
        boardOffsetX = (viewport.getWorldWidth() - (cellSize * logic.N)) / 2f;
        boardOffsetY = (viewport.getWorldHeight() - (cellSize * logic.N)) / 2f;

        batch.begin();
        batch.draw(texBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (int x = 0; x < logic.N; x++) {
            for (int y = 0; y < logic.N; y++) {
                float dx = boardOffsetX + x * cellSize;
                float dy = boardOffsetY + y * cellSize;

                if (logic.grid[x][y] == 2) {
                    if (!isHoleOccupied(x, y)) {
                        batch.draw(texHole, dx, dy, cellSize, cellSize);
                    }
                }
                if (logic.grid[x][y] == 1) {
                    batch.draw(texWall, dx, dy, cellSize, cellSize);
                }
            }
        }

        boolean anyMoving = false;
        float speed = 25f * delta;

        for (int i = 0; i < logic.marbles.size(); i++) {
            SlidingLogic.Marble m = logic.marbles.get(i);

            if (visX[i] != m.x || visY[i] != m.y) {
                anyMoving = true;

                if (Math.abs(m.x - visX[i]) <= speed) visX[i] = m.x;
                else visX[i] += Math.signum(m.x - visX[i]) * speed;

                if (Math.abs(m.y - visY[i]) <= speed) visY[i] = m.y;
                else visY[i] += Math.signum(m.y - visY[i]) * speed;
            }

            float dx = boardOffsetX + visX[i] * cellSize;
            float dy = boardOffsetY + visY[i] * cellSize;

            if (m.locked && visX[i] == m.x && visY[i] == m.y) {
                batch.setColor(1, 1, 1, 0.6f);
                batch.draw(texDoneBox, dx, dy, cellSize, cellSize);
            } else {
                batch.setColor(Color.WHITE);
                batch.draw(texMarble, dx, dy, cellSize, cellSize);
            }
            batch.setColor(Color.WHITE);
        }

        if (isAnimating && !anyMoving) {
            isAnimating = false;
            checkWin();
        } else {
            isAnimating = anyMoving;
        }

        float btnS = cellSize * BUTTON_SIZE_RATIO;
        float btnPad = (cellSize - btnS) / 2f;


        for (int i = 0; i < logic.N; i++) {
            batch.draw(texBtnLeft, boardOffsetX - cellSize + btnPad, boardOffsetY + i * cellSize + btnPad, btnS, btnS);
            batch.draw(texBtnRight, boardOffsetX + logic.N * cellSize + btnPad, boardOffsetY + i * cellSize + btnPad, btnS, btnS);
            batch.draw(texBtnBottom, boardOffsetX + i * cellSize + btnPad, boardOffsetY - cellSize + btnPad, btnS, btnS);
            batch.draw(texBtnTop, boardOffsetX + i * cellSize + btnPad, boardOffsetY + logic.N * cellSize + btnPad, btnS, btnS);
        }

        com.badlogic.gdx.graphics.g2d.BitmapFont font = game.getFontManager().fontVietnamese;
        font.setColor(Color.WHITE);
        font.draw(batch, "Press [R] to restart", 20, 40);

        font.setColor(Color.RED);
        font.draw(batch, "Count steps: " + stepCount, 20, 70);

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

    @Override
    public void resize(int width, int height) {
        if (uiStage != null) uiStage.getViewport().update(width, height, true);
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        texBackground.dispose();
        texWall.dispose();
        texMarble.dispose();
        texHole.dispose();

        texBtnTop.dispose();
        texBtnBottom.dispose();
        texBtnLeft.dispose();
        texBtnRight.dispose();

        if (texDoneBox != null) texDoneBox.dispose();
        if (texJumpscare != null) texJumpscare.dispose();
        if (uiStage != null) uiStage.dispose();
        if (texDim != null) texDim.dispose();
    }
}
