package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerUpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.util.MovementInput;

public class EntitySpeed extends Module {
    private static Setting<Float> speed = new Setting<>("Speed", 3F, 10F, 0.1F, 0.1F);
    private static Setting<Boolean> accelerate = new Setting<>("Accelerate", false);
    private static Setting<Float> acceleration = new Setting<>("Acceleration", 0.1F, 2F, 0.1F, 0.1F).withVisibility(accelerate::getValue);

    public EntitySpeed() {
        super("EntitySpeed", Category.MOVEMENT);
    }

    private long accelerationTime = 0L;

    public void onEnable() {
        accelerationTime = System.currentTimeMillis();
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player.getRidingEntity() != null) {
            MovementInput movementInput = mc.player.movementInput;
            double forward = movementInput.moveForward;
            double strafe = movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if ((forward == 0.0D) && (strafe == 0.0D)) {
                mc.player.getRidingEntity().motionX = 0.0D;
                mc.player.getRidingEntity().motionZ = 0.0D;
                accelerationTime = System.currentTimeMillis();
            } else {
                if (forward != 0.0D) {
                    if (strafe > 0.0D) {
                        yaw += (forward > 0.0D ? -45 : 45);
                    }else if (strafe < 0.0D) {
                        yaw += (forward > 0.0D ? 45 : -45);
                    }
                    strafe = 0.0D;
                    if (forward > 0.0D) {
                        forward = 1.0D;
                    }else if (forward < 0.0D) {
                        forward = -1.0D;
                    }
                }
                float spd = speed.getValue();
                if (accelerate.getValue()) {
                    spd *= Math.min(1F, (System.currentTimeMillis() - accelerationTime) / (1000F * acceleration.getValue()));
                }
                double sin = Math.sin(Math.toRadians(yaw + 90.0F));
                double cos = Math.cos(Math.toRadians(yaw + 90.0F));
                mc.player.getRidingEntity().motionX = (forward * spd * cos + strafe * spd * sin);
                mc.player.getRidingEntity().motionZ = (forward * spd * sin - strafe * spd * cos);
            }
        }
    }
}
