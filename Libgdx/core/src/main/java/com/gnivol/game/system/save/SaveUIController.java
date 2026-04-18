package com.gnivol.game.system.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

public class SaveUIController {
    private final Image saveIcon;

    public SaveUIController(Stage stage) {
        Texture tex;
        try {
            tex = new Texture(Gdx.files.internal("ui/save_icon.png"));
        } catch (Exception e) {
            Gdx.app.error("SaveUI", "Lỗi: Not found assets/ui/save_icon.png");
            // Tạo ảnh rỗng tạm thời nếu thiếu file để tránh crash
            tex = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        }

        saveIcon = new Image(tex);
        saveIcon.setSize(40, 40);
        saveIcon.setOrigin(Align.center);
        saveIcon.setPosition(1230, 670);
        saveIcon.setVisible(false);

        stage.addActor(saveIcon);
    }

    public void showSavingIcon() {
        saveIcon.setVisible(true);
        saveIcon.getColor().a = 1f;
        saveIcon.clearActions();

        saveIcon.addAction(Actions.forever(Actions.rotateBy(-360f, 1.5f)));
    }

    public void hideSavingIcon() {
        saveIcon.clearActions();
        saveIcon.addAction(Actions.sequence(
            Actions.fadeOut(0.3f),
            Actions.visible(false)
        ));
    }
}
