package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.TurnEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;

public class FreeLook extends Module {
    private float dYaw = 0F;
    private float dPitch = 0F;

    private static Setting<Boolean> autoThirdPerson = new Setting<>("AutoThirdPerson", true);

    public FreeLook() {
        super("FreeLook", "Look around in 3rd person", Category.RENDER);
    }

    public void onEnable() {
        dYaw = 0;
        dPitch = 0;

        if (autoThirdPerson.getValue()) {
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    public void onDisable() {
        if (autoThirdPerson.getValue()) {
            mc.gameSettings.thirdPersonView = 0;
        }
    }

    @Subscriber
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (mc.gameSettings.thirdPersonView > 0) {
            event.setYaw(event.getYaw() + dYaw);
            event.setPitch(event.getPitch() + dPitch);
        }
    }

    @Subscriber
    public void onTurnEvent(TurnEvent event) {
        if (mc.gameSettings.thirdPersonView > 0) {
            dYaw = (float) ((double) dYaw + (double) event.getYaw() * 0.15D);
            dPitch = (float) ((double) dPitch - (double) event.getPitch() * 0.15D);
            dPitch = MathHelper.clamp(dPitch, -90.0F, 90.0F);
            event.setCancelled(true);
        }
    }
}
