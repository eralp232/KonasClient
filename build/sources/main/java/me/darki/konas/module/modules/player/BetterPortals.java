package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.GetPortalBoundingBoxEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IEntity;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraftforge.client.GuiIngameForge;

public class BetterPortals extends Module {
    public static Setting<Boolean> allowGuis = new Setting<>("AllowGuis", true);
    public static Setting<Boolean> noRender = new Setting<>("NoRender", true);
    public static Setting<Boolean> noHitbox = new Setting<>("NoHitbox", false);

    public BetterPortals() {
        super("BetterPortals", "Remove unwanted portal functionality", Category.PLAYER, "PortalChat");
    }

    private boolean renderPortal = false;

    @Override
    public void onEnable() {
        renderPortal = GuiIngameForge.renderPortal;
    }

    @Override
    public void onDisable() {
        GuiIngameForge.renderPortal = renderPortal;
    }

    @Subscriber
    private void onUpdate(UpdateEvent event) {
        if(mc.player == null || mc.world == null) return;
        if (allowGuis.getValue()) {
            ((IEntity) mc.player).setInPortal(false);
        }
        if (noRender.getValue()) {
            GuiIngameForge.renderPortal = false;
        }
    }

    @Subscriber
    public void onGetPortalBoundingBox(GetPortalBoundingBoxEvent event) {
        if (noHitbox.getValue()) {
            event.cancel();
        }
    }
}