package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerMoveEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;

public class HighJump extends Module {
    private static Setting<Double> height = new Setting<>("Height", 2D, 10D, 0.1D, 0.1D);
    private static Setting<Boolean> ground = new Setting<>("Ground", true);

    public HighJump() {
        super("HighJump", Category.MOVEMENT);
    }

    @Subscriber
    public void onMove(PlayerMoveEvent event) {
        if (mc.player.movementInput.jump && (!ground.getValue() || mc.player.onGround)) {
            mc.player.motionY = height.getValue();
            event.setY(mc.player.motionY);
        }
    }
}
