package me.darki.konas.event.events;

import net.minecraftforge.client.event.RenderBlockOverlayEvent;

public class RenderOverlayEvent extends CancellableEvent {

    private final RenderBlockOverlayEvent.OverlayType overlayType;

    public RenderOverlayEvent(RenderBlockOverlayEvent.OverlayType overlayType) {
        this.overlayType = overlayType;
    }

    public RenderBlockOverlayEvent.OverlayType getOverlayType() {
        return overlayType;
    }
}
