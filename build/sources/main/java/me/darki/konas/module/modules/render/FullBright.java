package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class FullBright extends Module {
    private float currentGamma;

    private ListenableSettingDecorator<Mode> mode = new ListenableSettingDecorator<>("Mode", Mode.NORMAL, (value) -> {
        if (mc.world != null && mc.player != null) {
            if (value != Mode.NORMAL) {
                if (mc.player.dimension == -1) {
                    generateHellLightBrightnessTable();
                } else {
                    generateLightBrightnessTable();
                }
            }
            if (value != Mode.GAMMA) {
                mc.gameSettings.gammaSetting = currentGamma;
            }
            if (value != Mode.POTION) {
                mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
            }
        }
    });

    private Setting<Boolean> sine = new Setting<>("Sine", false).withVisibility(() -> mode.getValue() == Mode.GAMMA);
    private Setting<Boolean> cancel = new Setting<>("Cancel", false).withVisibility(() -> mode.getValue() == Mode.POTION);

    private long time;

    private enum Mode {
        NORMAL, GAMMA, POTION
    }

    public FullBright() {
        super("FullBright", "Makes everything bright", Keyboard.KEY_NONE, Category.RENDER);
    }

    @Override
    public void onEnable() {
        currentGamma = mc.gameSettings.gammaSetting;
        time = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) return;
        if (mode.getValue() == Mode.GAMMA) {
            mc.gameSettings.gammaSetting = currentGamma;
        } else if (mode.getValue() == Mode.NORMAL) {
            if (mc.player.dimension == -1) {
                generateHellLightBrightnessTable();
            } else {
                generateLightBrightnessTable();
            }
        } else {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION);
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if(mc.world == null || mc.player == null) return;
        if (mode.getValue() == Mode.GAMMA) {
            mc.gameSettings.gammaSetting = sine.getValue() ? currentGamma + 20F * Math.min(1F, (System.currentTimeMillis() - time) / 1000F) : currentGamma + 20F;
        } else if (mode.getValue() == Mode.NORMAL) {
            Arrays.fill(mc.world.provider.getLightBrightnessTable(), 1f);
        } else {
            mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5210));
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketEntityEffect && cancel.getValue()) {
            SPacketEntityEffect packet = (SPacketEntityEffect) event.getPacket();
            if (mc.player != null && packet.getEntityId() == mc.player.getEntityId() && (packet.getEffectId() == 9 || packet.getEffectId() == 15)) {
                event.setCancelled(true);
            }
        }
    }

    private static void generateLightBrightnessTable()
    {
        float f = 0.0F;

        for (int i = 0; i <= 15; ++i)
        {
            float f1 = 1.0F - (float)i / 15.0F;
            mc.world.provider.getLightBrightnessTable()[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F + 0.0F;
        }
    }

    private static void generateHellLightBrightnessTable()
    {
        float f = 0.1F;

        for (int i = 0; i <= 15; ++i)
        {
            float f1 = 1.0F - (float)i / 15.0F;
            mc.world.provider.getLightBrightnessTable()[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * 0.9F + 0.1F;
        }
    }


}
