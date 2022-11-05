package me.darki.konas.event.events;

import net.minecraftforge.fml.common.gameevent.TickEvent;

public class UpdateEvent {
    private static UpdateEvent INSTANCE = new UpdateEvent();

    private TickEvent.Phase phase;

    public static UpdateEvent get(TickEvent.Phase phase) {
        INSTANCE.phase = phase;
        return INSTANCE;
    }

    public TickEvent.Phase getPhase() {
        return phase;
    }
}
