package me.darki.konas.event.events;

public class GetRainStrengthEvent extends CancellableEvent {
    private static GetRainStrengthEvent INSTANCE = new GetRainStrengthEvent();

    public static GetRainStrengthEvent get() {
        INSTANCE.setCancelled(false);
        return INSTANCE;
    }
}
