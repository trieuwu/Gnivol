package com.gnivol.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gnivol.game.system.puzzle.PuzzleManager;

/**
 * Simple popup: shows a URL label + number input TextField + submit button.
 * On submit, delegates to PuzzleManager for answer validation.
 */
public class QRPopup {

    private final Stage stage;
    private final BitmapFont font;
    private final PuzzleManager puzzleManager;

    private Table rootTable;
    private Label urlLabel;
    private TextField inputField;
    private Label errorLabel;
    private String currentPuzzleId;
    private boolean open;

    public QRPopup(Stage stage, BitmapFont font, PuzzleManager puzzleManager) {
        this.stage = stage;
        this.font = font;
        this.puzzleManager = puzzleManager;
        buildUI();
    }

    private void buildUI() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle errorStyle = new Label.LabelStyle(font, Color.RED);

        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle();
        tfStyle.font = font;
        tfStyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.GREEN;

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();

        urlLabel = new Label("", labelStyle);
        rootTable.add(urlLabel).padBottom(15f).row();

        inputField = new TextField("", tfStyle);
        rootTable.add(inputField).width(200f).padBottom(10f).row();

        TextButton submitBtn = new TextButton("G\u1EEDi", btnStyle);
        submitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onSubmit();
            }
        });
        rootTable.add(submitBtn).padBottom(10f).row();

        errorLabel = new Label("", errorStyle);
        errorLabel.setVisible(false);
        rootTable.add(errorLabel).row();

        rootTable.setVisible(false);
    }

    public void open(String url, String puzzleId) {
        this.currentPuzzleId = puzzleId;
        urlLabel.setText(url);
        inputField.setText("");
        errorLabel.setVisible(false);
        open = true;
        rootTable.setVisible(true);
        stage.addActor(rootTable);
        stage.setKeyboardFocus(inputField);
    }

    public void close() {
        open = false;
        rootTable.setVisible(false);
        rootTable.remove();
        currentPuzzleId = null;
    }

    public boolean isOpen() {
        return open;
    }

    private void onSubmit() {
        String answer = inputField.getText().trim();
        if (answer.isEmpty()) return;

        boolean correct = puzzleManager.submitAnswer(currentPuzzleId, answer);
        if (correct) {
            close();
        } else {
            errorLabel.setText("Sai r\u1ED3i, th\u1EED l\u1EA1i!");
            errorLabel.setVisible(true);
        }
    }
}
