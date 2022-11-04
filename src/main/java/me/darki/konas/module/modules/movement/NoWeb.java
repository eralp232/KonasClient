package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.event.events.WebEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockWeb;
import net.minecraft.util.math.BlockPos;

public class NoWeb extends Module {

    private static Setting<Mode> mode = new Setting<>("Mode", Mode.BYPASS);
    private final Setting<Float> factor = new Setting<>("Factor", 1F, 3F, 0.1F, 0.1F).withVisibility(() -> mode.getValue() == Mode.TIMER);

    private enum Mode {
        VANILLA, BYPASS, TIMER
    }

    private Timer timer = new Timer();

    public NoWeb() {
        super("NoWeb", Category.MOVEMENT);
    }

    public void onDisable() {
        KonasGlobals.INSTANCE.timerManager.resetTimer(this);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (timer.hasPassed(100)) {
            KonasGlobals.INSTANCE.timerManager.resetTimer(this);
        }
    }

    @Subscriber
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (mode.getValue() != Mode.TIMER) return;
        double prevHDist = Math.sqrt((mc.player.posX - mc.player.prevPosX) * (mc.player.posY - mc.player.prevPosY) + (mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ));
        if (mc.world.getBlockState(new BlockPos(mc.player)).getBlock() instanceof BlockWeb && prevHDist == 0.0D && mc.player.posY < mc.player.prevPosY) {
            float speed = (mc.player.ticksExisted % 3 == 0) ? 3F : 5F;
            KonasGlobals.INSTANCE.timerManager.updateTimer(this, 30, speed * factor.getValue());
            timer.reset();
        }
    }

    @Subscriber
    public void onWeb(WebEvent event) {
        if (mc.world == null || mc.player == null) return;

        switch (mode.getValue()) {
            case VANILLA:
                event.cancel();
                break;
            case BYPASS:
                mc.player.jumpMovementFactor = 0.59f;
                if (!mc.gameSettings.keyBindSneak.isPressed())
                    mc.player.motionY = 0.0;
                break;
        }

    }


}
