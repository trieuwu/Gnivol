package com.gnivol.game.component;

import com.badlogic.ashley.core.Component;

public class ItemInfoComponent implements Component {
    public String itemID;
    public String inspectText;
    public String pickupSoundID;

    public boolean isPickedUp = false;
}
