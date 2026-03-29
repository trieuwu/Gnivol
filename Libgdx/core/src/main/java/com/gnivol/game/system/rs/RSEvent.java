package com.gnivol.game.system.rs;

public class RSEvent {
    private final RSEventType eventType;
    private final float rsAmount;
    private final String source;

    public RSEvent(RSEventType eventType, float rsAmount, String source) {
        this.eventType = eventType;
        this.rsAmount = rsAmount;
        this.source = source;
    }

    public RSEventType getEventType() { return eventType; }
    public float getRSAmount() { return rsAmount; }
    public String getSource() { return source; }

    @Override
    public String toString() {
        return "RSEvent{" + eventType + ", +" + rsAmount + ", src=" + source + "}";
    }
}
