package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.RootEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.entity.player.EnumPlayerModelParts;

public class SkinBlinker extends Module {
    private static final Setting<Float> delay = new Setting<>("Delay", 0.0F, 20.0F, 0.0F, 0.1F);
    private static final Setting<Boolean> random = new Setting<>("Random", true);

    private Timer timer = new Timer();

    public SkinBlinker() {
        super("SkinBlinker", "Flashes your skin parts", Category.PLAYER);
    }

    @Subscriber
    public void onRoot(RootEvent event) {
        if (timer.hasPassed(delay.getValue() * 1000.0f)) {
            EnumPlayerModelParts[] parts = EnumPlayerModelParts.values();
            int i = 0;
            while (i < parts.length) {
                EnumPlayerModelParts enumPlayerModelParts = parts[i];
                mc.gameSettings.setModelPartEnabled(enumPlayerModelParts, random.getValue() ? Math.random() < 0.5 : !mc.gameSettings.getModelParts().contains(enumPlayerModelParts));
                ++i;
            }
            timer.reset();
        }
    }
}
