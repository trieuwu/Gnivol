package com.gnivol.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gnivol.game.system.puzzle.PuzzleManager;
import com.gnivol.game.system.rs.RSEvent;
import com.gnivol.game.system.rs.RSEventType;
import com.gnivol.game.system.rs.RSManager;

public class PuzzleQrProPtitUI {

    private static final String PUZZLE_ID = "qr_proptit";
    private static final float RS_ON_SOLVE = -10f;
    private static final float RS_ON_FAIL = 2f;
    private static final int MAX_RS_FAILS = 2;

    private Window window;
    private TextField inputField;

    private final PuzzleManager puzzleManager;
    private final RSManager rsManager;

    private int failCount = 0;

    public interface PuzzleResultListener {
        void onPuzzleSolved(String puzzleId);
    }
    private PuzzleResultListener listener;

    public PuzzleQrProPtitUI(Skin skin, Stage stage, PuzzleManager puzzleManager, RSManager rsManager) {
        this.puzzleManager = puzzleManager;
        this.rsManager = rsManager;

        window = new Window("QR Code", skin);
        window.setSize(420, 280);
        window.setPosition((1280 - 420) / 2f, (720 - 280) / 2f);
        window.setModal(true);
        window.setVisible(false);

        window.add(new Label("Sinh nhat CLB ProPTIT", skin)).colspan(2).padBottom(10).row();
        window.add(new Label("Nhap ma so:", skin)).colspan(2).padBottom(10).row();

        inputField = new TextField("", skin);
        window.add(inputField).width(200).height(40).colspan(2).padBottom(20).row();

        TextButton submitBtn = new TextButton("SUBMIT", skin);
        submitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                checkAnswer();
            }
        });

        TextButton closeBtn = new TextButton("CLOSE", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        window.add(submitBtn).padTop(10);
        window.add(closeBtn).padTop(10);

        stage.addActor(window);
    }

    public void setListener(PuzzleResultListener listener) {
        this.listener = listener;
    }

    public void show() {
        inputField.setText("");
        failCount = 0;
        window.getColor().a = 1f;
        window.setVisible(true);
        if (window.getStage() != null) {
            window.getStage().setKeyboardFocus(inputField);
        }
    }

    public void hide() {
        window.setVisible(false);
        if (window.getStage() != null) {
            window.getStage().setKeyboardFocus(null);
            window.getStage().setScrollFocus(null);
        }
    }

    private void checkAnswer() {
        String answer = inputField.getText().trim();
        if (answer.isEmpty()) return;

        if (puzzleManager.submitAnswer(PUZZLE_ID, answer)) {
            rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_SOLVED, RS_ON_SOLVE, PUZZLE_ID));
            hide();
            if (listener != null) {
                listener.onPuzzleSolved(PUZZLE_ID);
            }
        } else {
            if (failCount < MAX_RS_FAILS) {
                rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_FAILED, RS_ON_FAIL, PUZZLE_ID));
                failCount++;
            }
            window.addAction(Actions.sequence(
                Actions.color(Color.RED, 0.1f),
                Actions.moveBy(20, 0, 0.05f), Actions.moveBy(-40, 0, 0.05f), Actions.moveBy(20, 0, 0.05f),
                Actions.moveBy(20, 0, 0.05f), Actions.moveBy(-40, 0, 0.05f), Actions.moveBy(20, 0, 0.05f),
                Actions.color(Color.WHITE, 0.2f)
            ));
        }
    }
}
