package com.gnivol.game.system.rs;

import com.gnivol.game.entity.GameObject;
import com.gnivol.game.component.GlitchComponent;

import java.util.List;

public class RSMonitorSystem implements RSListener {

    private final List<GameObject> objects;

    public RSMonitorSystem(List<GameObject> objects) {
        this.objects = objects;
    }

    @Override
    public void onRSChanged(float oldValue, float newValue) {

    }

    // ktra từng object có chứa glitch compoment nếu đã vượt ngưỡng thì bật glitch compoment lên
    @Override
    public void onThresholdCrossed(boolean isAbove) {
        for (GameObject obj : objects) {
            if (obj.hasComponent(GlitchComponent.class)) {
                GlitchComponent glitch = obj.getComponent(GlitchComponent.class);

                if (isAbove) {
                    glitch.activate();
                } else {
                    glitch.deactivate();
                }
            }
        }
    }
}
