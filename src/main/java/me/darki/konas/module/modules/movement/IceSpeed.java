package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import net.minecraft.init.Blocks;

public class IceSpeed extends Module {
    public IceSpeed() {
        super("IceSpeed", Category.MOVEMENT);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        Blocks.ICE.slipperiness = 0.4F;
        Blocks.FROSTED_ICE.slipperiness = 0.4F;
        Blocks.PACKED_ICE.slipperiness = 0.4F;
    }

    public void onDisable() {
        // 0.98F is default slipperiness from BlockIce
        Blocks.ICE.slipperiness = 0.98F;
        Blocks.FROSTED_ICE.slipperiness = 0.98F;
        Blocks.PACKED_ICE.slipperiness = 0.98F;
    }
}
