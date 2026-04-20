package com.gnivol.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gnivol.game.system.rs.RSManager;

/**
 * Phone UI with 2 modes: CALL and MAP.
 * CALL mode shows contact buttons, MAP mode delegates to PhoneMapOverlay.
 */
public class PhoneOverlay {

    public interface CallListener {
        void onCallContact(String contactId);
    }

    private enum Mode {
        CALL, MAP
    }

    private final Stage stage;
    private final RSManager rsManager;
    private final BitmapFont font;
    private final PhoneMapOverlay mapOverlay;

    private Table rootTable;
    private Table callTable;
    private Table tabTable;
    private Mode currentMode = Mode.CALL;
    private boolean open;
    private CallListener callListener;

    private static final String[] CONTACTS = {"Linh", "M\u1EB9", "B\u1EA1n"};

    public PhoneOverlay(Stage stage, RSManager rsManager, BitmapFont font) {
        this.stage = stage;
        this.rsManager = rsManager;
        this.font = font;
        this.mapOverlay = new PhoneMapOverlay(stage, rsManager, font);
        buildUI();
    }

    private void buildUI() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle tabActiveStyle = new TextButton.TextButtonStyle();
        tabActiveStyle.font = font;
        tabActiveStyle.fontColor = Color.YELLOW;

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();

        // Tab bar
        tabTable = new Table();
        final TextButton callTab = new TextButton("G\u1ECDi", tabActiveStyle);
        final TextButton mapTab = new TextButton("B\u1EA3n \u0111\u1ED3", btnStyle);

        callTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchMode(Mode.CALL);
            }
        });

        mapTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchMode(Mode.MAP);
            }
        });

        tabTable.add(callTab).padRight(20f);
        tabTable.add(mapTab);
        rootTable.add(tabTable).padBottom(20f).row();

        // Call content
        callTable = new Table();
        for (final String contact : CONTACTS) {
            TextButton contactBtn = new TextButton(contact, btnStyle);
            contactBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (callListener != null) {
                        callListener.onCallContact(contact);
                    }
                }
            });
            callTable.add(contactBtn).padBottom(8f).row();
        }
        rootTable.add(callTable).row();

        rootTable.setVisible(false);
    }

    public void open() {
        open = true;
        rootTable.setVisible(true);
        stage.addActor(rootTable);
        switchMode(Mode.CALL);
    }

    public void close() {
        open = false;
        rootTable.setVisible(false);
        rootTable.remove();
        if (mapOverlay.isVisible()) {
            mapOverlay.hide();
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setCallListener(CallListener listener) {
        this.callListener = listener;
    }

    public PhoneMapOverlay getMapOverlay() {
        return mapOverlay;
    }

    private void switchMode(Mode mode) {
        currentMode = mode;
        if (mode == Mode.CALL) {
            callTable.setVisible(true);
            if (mapOverlay.isVisible()) {
                mapOverlay.hide();
            }
        } else {
            callTable.setVisible(false);
            mapOverlay.show();
        }
    }
}
