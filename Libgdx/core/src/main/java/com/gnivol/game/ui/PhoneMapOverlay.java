package com.gnivol.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.gnivol.game.system.rs.RSManager;

/**
 * Shows 3 map locations. RS < 65 = greyed out with a warning tooltip.
 */
public class PhoneMapOverlay {

    public interface LocationListener {
        void onLocationSelected(String locationId);
    }

    private static final String[][] LOCATIONS = {
        {"ptit", "PTIT"},
        {"benh_vien", "B\u1EC7nh vi\u1EC7n"},
        {"phung_khoang", "Ph\u00F9ng Khoang"}
    };

    private final Stage stage;
    private final RSManager rsManager;
    private final BitmapFont font;
    private Table rootTable;
    private Label tooltipLabel;
    private boolean visible;
    private LocationListener listener;

    public PhoneMapOverlay(Stage stage, RSManager rsManager, BitmapFont font) {
        this.stage = stage;
        this.rsManager = rsManager;
        this.font = font;
        buildUI();
    }

    private void buildUI() {
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle tooltipStyle = new Label.LabelStyle(font, Color.RED);

        TextButton.TextButtonStyle enabledStyle = new TextButton.TextButtonStyle();
        enabledStyle.font = font;
        enabledStyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle disabledStyle = new TextButton.TextButtonStyle();
        disabledStyle.font = font;
        disabledStyle.fontColor = Color.DARK_GRAY;

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.center();

        Label title = new Label("B\u1EA3n \u0111\u1ED3", labelStyle);
        rootTable.add(title).padBottom(20f).row();

        for (final String[] loc : LOCATIONS) {
            final String locId = loc[0];
            final String locName = loc[1];

            boolean unlocked = rsManager.isMapUnlocked();
            TextButton.TextButtonStyle style = unlocked ? enabledStyle : disabledStyle;
            final TextButton btn = new TextButton(locName, style);

            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (rsManager.isMapUnlocked()) {
                        if (listener != null) {
                            listener.onLocationSelected(locId);
                        }
                    } else {
                        showTooltip();
                    }
                }
            });

            rootTable.add(btn).padBottom(10f).row();
        }

        tooltipLabel = new Label("Ch\u01B0a \u0111\u1EE7 nh\u1EADn th\u1EE9c...", tooltipStyle);
        tooltipLabel.setVisible(false);
        rootTable.add(tooltipLabel).padTop(15f).row();

        Label escHint = new Label("[ESC] \u0111\u00F3ng", labelStyle);
        rootTable.add(escHint).padTop(20f).row();

        rootTable.setVisible(false);
    }

    public void show() {
        visible = true;
        rootTable.setVisible(true);
        tooltipLabel.setVisible(false);
        stage.addActor(rootTable);
    }

    public void hide() {
        visible = false;
        rootTable.setVisible(false);
        rootTable.remove();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setListener(LocationListener listener) {
        this.listener = listener;
    }

    private void showTooltip() {
        tooltipLabel.setVisible(true);
    }
}
