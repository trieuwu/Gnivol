package com.gnivol.game.system.inventory.data;

public class ItemData {
    public String itemID;
    public String itemName;
    public int rsChangeValue;
    public boolean isCursed;

    public ItemData() {
    }

    public ItemData(String itemID, String itemName, int rsChangeValue, boolean isCursed) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.rsChangeValue = rsChangeValue;
        this.isCursed = isCursed;
    }

}
