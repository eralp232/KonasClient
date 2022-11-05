package me.darki.konas.event.events;

// For drawing lines on top of screen without using projection, instead of block-related rendering
public class WorldRenderEvent {
    private Float partialTicks;

    public WorldRenderEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
