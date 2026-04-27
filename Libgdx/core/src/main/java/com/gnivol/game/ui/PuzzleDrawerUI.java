package com.gnivol.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gnivol.game.system.puzzle.PuzzleManager;
import com.gnivol.game.system.rs.RSEvent;
import com.gnivol.game.system.rs.RSEventType;
import com.gnivol.game.system.rs.RSManager;
import com.badlogic.gdx.math.Interpolation;

public class PuzzleDrawerUI {
    private Window window;
    private TextButton[] digitButtons;
    private int[] digits = {0, 0, 0}; // 3 ô số

    private final PuzzleManager puzzleManager;
    private final RSManager rsManager;

    private int failCount = 0;

    public interface PuzzleResultListener {
        void onPuzzleSolved(String puzzleId);
    }
    private PuzzleResultListener listener;

    public PuzzleDrawerUI(Skin skin, Stage stage, PuzzleManager puzzleManager, RSManager rsManager) {
        this.puzzleManager = puzzleManager;
        this.rsManager = rsManager;

        window = new Window("Khoa Ngan Keo", skin);
        window.setSize(400, 250);
        window.setPosition((1280 - 400) / 2f, (720 - 250) / 2f); // Căn giữa
        window.setModal(true);
        window.setVisible(false);

        digitButtons = new TextButton[3];
        window.add(new Label("PROPTIT'S BIRTHDAY", skin)).colspan(3).padBottom(20).row();

        for (int i = 0; i < 3; i++) {
            final int index = i;
            digitButtons[i] = new TextButton("0", skin);
            digitButtons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    digits[index] = (digits[index] + 1) % 10;
                    digitButtons[index].setText(String.valueOf(digits[index]));
                }
            });
            window.add(digitButtons[i]).width(80).height(80).pad(10);
        }

        window.row();

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

        window.add(submitBtn).colspan(2).padTop(20);
        window.add(closeBtn).padTop(20);

        stage.addActor(window);
    }

    public void setListener(PuzzleResultListener listener) {
        this.listener = listener;
    }

    public void show() {
        for (int i = 0; i < 3; i++) {
            digits[i] = 0;
            digitButtons[i].setText("0");
        }
        window.getColor().a = 1f;
        window.setVisible(true);
    }

    public void hide() {
        window.setVisible(false);
        if (window.getStage() != null) {
            window.getStage().setKeyboardFocus(null);
            window.getStage().setScrollFocus(null);
        }
    }

    private void checkAnswer() {
        String answer = digits[0] + "" + digits[1] + "" + digits[2];

        if (puzzleManager.submitAnswer("puzzle_drawer", answer)) {
            rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_SOLVED, 10, "puzzle_drawer"));
            hide();
            if (listener != null) {
                listener.onPuzzleSolved("puzzle_drawer");
            }
        } else {
            if(failCount < 2) {
                rsManager.processEvent(new RSEvent(RSEventType.PUZZLE_FAILED, -5, "puzzle_drawer"));
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
