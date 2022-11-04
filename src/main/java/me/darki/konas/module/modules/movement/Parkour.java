package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class Parkour extends Module {
    public Parkour() {
        super("Parkour", Category.MOVEMENT);
    }

    @Subscriber
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent.Pre event) {
        if(!mc.player.onGround || mc.gameSettings.keyBindJump.isPressed()) return;
        if(mc.player.isSneaking() || mc.gameSettings.keyBindSneak.isPressed()) return;

        AxisAlignedBB entityBoundingBox = mc.player.getEntityBoundingBox();
        AxisAlignedBB offsetBox = entityBoundingBox.offset(0, -0.5, 0).expand(-0.001, 0, -0.001);

        List<AxisAlignedBB> collisionBoxes = mc.world.getCollisionBoxes(mc.player, offsetBox);

        if(!collisionBoxes.isEmpty()) return;

        mc.player.jump();
    }
}
