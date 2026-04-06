package com.gnivol.game.component;

import com.badlogic.ashley.core.Component;

public class GlitchComponent implements Component {
    public boolean active = false;
    public float intensity = 0f;

    public void activate() {
        active = true;
        // thêm asset khi bật
        System.out.println("GLITCH ON");
    }

    public void deactivate() {
        active = false;
        System.out.println("GLITCH OFF");
    }

}
