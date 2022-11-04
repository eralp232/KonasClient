package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.BlockResetEvent;
import me.darki.konas.module.Module;

public class BreakTweaks extends Module {

    public BreakTweaks() {
        super("BreakTweaks", "Lets you pause breaking blocks", Category.PLAYER, "StickyBreak");
    }

    @Subscriber
    public void onBlockReset(BlockResetEvent event) {
        event.cancel();
    }

}
