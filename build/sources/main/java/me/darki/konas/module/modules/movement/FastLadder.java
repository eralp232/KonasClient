package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.util.client.PlayerUtils;

public class FastLadder extends Module {

    public FastLadder() {
        super("FastLadder", Category.MOVEMENT);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if(mc.world == null || mc.player == null) return;

        if(mc.player.isOnLadder() && PlayerUtils.isPlayerMoving()) {
            mc.player.motionY = 0.169;
        }

    }

}
