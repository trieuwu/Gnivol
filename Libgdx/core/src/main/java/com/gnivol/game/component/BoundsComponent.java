package com.gnivol.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;

public class BoundsComponent implements Component {
    public Rectangle hitbox = new Rectangle();
}
