package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerMoveEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.PlayerUtils;

public class FastSwim extends Module {
    private static Setting<Double> speed = new Setting<>("Speed", 2D, 10D, 0.1D, 0.1D);
    private static Setting<Boolean> antiKick = new Setting<>("AntiKick", true);

    public FastSwim() {
        super("FastSwim", Category.MOVEMENT);
    }

    @Subscriber
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!mc.player.isInWater() || !PlayerUtils.isPlayerMoving()) return;
        double[] dir;
        if (mc.player.ticksExisted % 4 == 0 && antiKick.getValue()) {
            dir = PlayerUtils.directionSpeed(speed.getValue()/40);
        } else {
            dir = PlayerUtils.directionSpeed(speed.getValue()/10);
        }
        event.setX(dir[0]);
        event.setZ(dir[1]);
    }
}
