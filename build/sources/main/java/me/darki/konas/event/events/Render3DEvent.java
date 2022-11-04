package me.darki.konas.event.events;


/**
 * @author cats
 */
public class Render3DEvent {
    private static Render3DEvent INSTANCE = new Render3DEvent();

    private Float partialTicks;

    public static Render3DEvent get(float partialTicks) {
        INSTANCE.partialTicks = partialTicks;
        return INSTANCE;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
