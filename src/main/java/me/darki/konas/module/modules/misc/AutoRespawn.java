package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.gui.GuiGameOver;

public class AutoRespawn extends Module {

    public Setting<Boolean> safe = new Setting<>("Safe", false);

    public AutoRespawn() {
        super("AutoRespawn", Category.MISC);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if(mc.player == null || mc.world == null) return;
        boolean shouldSafe = !safe.getValue() || (mc.player.getHealth() < 0 || mc.player.isDead);
        if(mc.currentScreen instanceof GuiGameOver && shouldSafe) {
            mc.player.respawnPlayer();
        }
    }



}
