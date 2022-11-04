package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.BlockReachDistanceEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;

public class Reach extends Module {
    private static Setting<Float> reach = new Setting<>("Reach", 4f, 10f, 0.5f, 0.5f);

    public Reach() {
        super("Reach", "Increaces your block reach range", Category.PLAYER);
    }

    @Subscriber
    public void onBlockReachDistance(BlockReachDistanceEvent event) {
        event.setReachDistance(reach.getValue());
    }
}
