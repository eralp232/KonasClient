package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerUpdateEvent;
import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;

public class AutoFish extends Module {
    private static Setting<Mode> mode = new Setting<>("Mode", Mode.BOUNCE);
    private static Setting<Boolean> cast = new Setting<>("Cast", true);

    private enum Mode {
        BOUNCE, SPLASH, BOTH
    }

    public AutoFish() {
        super("AutoFish", Category.MISC, "AutoCaster");
    }

    private boolean shouldCatch = false;
    private boolean shouldReCast = false;
    private Timer timer = new Timer();

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            if (((SPacketSoundEffect) event.getPacket()).getSound().getSoundName().toString().toLowerCase().contains("entity.bobber.splash")) {
                if (!shouldReCast && staticCheck() && mode.getValue() != Mode.BOUNCE) {
                    shouldCatch = true;
                    timer.reset();
                }
            }
        }
    }

    @Override
    public String getExtraInfo() {
        return mode.getValue().toString().charAt(0) + mode.getValue().toString().substring(1).toLowerCase();
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player.getHeldItemMainhand().getItem() != Items.FISHING_ROD) {
            timer.reset();
            shouldCatch = false;
            shouldReCast = false;
            return;
        }

        if (mc.player.fishEntity == null) {
            if (shouldReCast) {
                if (timer.hasPassed(450)) {
                    ((IMinecraft) mc).invokeRightClickMouse();
                    timer.reset();
                    shouldCatch = false;
                    shouldReCast = false;
                }
            } else if (cast.getValue() && timer.hasPassed(4500)) {
                ((IMinecraft) mc).invokeRightClickMouse();
                timer.reset();
                shouldCatch = false;
                shouldReCast = false;
            }
        } else if (staticCheck() && waterCheck()) {
            if (shouldCatch) {
                if (timer.hasPassed(350)) {
                    ((IMinecraft) mc).invokeRightClickMouse();
                    timer.reset();
                    shouldCatch = false;
                    shouldReCast = true;
                }
            } else {
                if (mode.getValue() != Mode.SPLASH && bounceCheck()) {
                    timer.reset();
                    shouldCatch = true;
                    shouldReCast = false;
                }
            }
        } else if (staticCheck()) {
            ((IMinecraft) mc).invokeRightClickMouse();
            timer.reset();
            shouldCatch = false;
            shouldReCast = false;
        }
    }

    private boolean bounceCheck() {
        if (mc.player.fishEntity == null || !waterCheck()) return false;
        return Math.abs(mc.player.fishEntity.motionY) > 0.05;
    }

    private boolean staticCheck() {
        if (mc.player.fishEntity == null || mc.player.fishEntity.isAirBorne || shouldReCast) return false;
        return Math.abs(mc.player.fishEntity.motionX) + Math.abs(mc.player.fishEntity.motionZ) < 0.01;
    }

    private boolean waterCheck() {
        if (mc.player.fishEntity == null || mc.player.fishEntity.isAirBorne) return false;
        BlockPos pos = mc.player.fishEntity.getPosition();
        return mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid || mc.world.getBlockState(pos.down()).getBlock() instanceof BlockLiquid;
    }
}
