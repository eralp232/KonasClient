package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.init.MobEffects;

public class AntiEffects extends Module {
    private final Setting<Boolean> levitation = new Setting<>("Levitation", true);
    private final Setting<Boolean> jumpBoost = new Setting<>("JumpBoost", true);

    public AntiEffects() {
        super("AntiEffects", "Removes unwanted effects from the player", Category.PLAYER);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player != null) {
            if (levitation.getValue() && mc.player.isPotionActive(MobEffects.LEVITATION)) {
                mc.player.removeActivePotionEffect(MobEffects.LEVITATION);
            }
            if (jumpBoost.getValue() && mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                mc.player.removeActivePotionEffect(MobEffects.JUMP_BOOST);
            }
        }
    }
}
