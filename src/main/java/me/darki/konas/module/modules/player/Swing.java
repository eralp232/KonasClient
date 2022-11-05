package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.mixin.mixins.ICPacketAnimation;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;

public class Swing extends Module {
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.CANCEL);
    private final Setting<Boolean> strict = new Setting<>("Strict", false);
    private final Setting<Boolean> render = new Setting<>("Render", false);
    
    private enum Mode {
        CANCEL, OFFHAND, MAINHAND, OPPOSITE, NONE
    }

    public Swing() {
        super("Swing", "Modifies swinging behavior", Category.PLAYER);
    }

    @Subscriber
    public void onPacketSent(PacketEvent.Send event) {
        if(mc.player == null || mc.world == null) return;
        if(event.getPacket() instanceof CPacketAnimation) {
            if (mode.getValue() == Mode.CANCEL) {
                if (!strict.getValue() || mc.playerController.getIsHittingBlock()) {
                    event.cancel();
                }
            } else if (mode.getValue() == Mode.OFFHAND) {
                CPacketAnimation packet = (CPacketAnimation) event.getPacket();
                ((ICPacketAnimation) packet).setHand(EnumHand.OFF_HAND);
            } else if (mode.getValue() == Mode.MAINHAND) {
                CPacketAnimation packet = (CPacketAnimation) event.getPacket();
                ((ICPacketAnimation) packet).setHand(EnumHand.MAIN_HAND);
            } else if (mode.getValue() == Mode.OPPOSITE) {
                CPacketAnimation packet = (CPacketAnimation) event.getPacket();
                ((ICPacketAnimation) packet).setHand(packet.getHand() == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            }

            if (render.getValue()) {
                EnumHand hand = ((CPacketAnimation) event.getPacket()).getHand();
                try {
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= getArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = hand;
                    }
                } catch (Exception ignored) {
                    
                }
            }
        }
    }

    private int getArmSwingAnimationEnd()
    {
        if (mc.player.isPotionActive(MobEffects.HASTE))
        {
            return 6 - (1 + mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier());
        }
        else
        {
            return mc.player.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
        }
    }

}
