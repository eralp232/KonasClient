package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.XRayBlockEvent;
import me.darki.konas.event.events.XRayEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.BlockListSetting;
import me.darki.konas.setting.Setting;

public class XRay extends Module {
    public static Setting<BlockListSetting> blocks = new Setting<>("Blocks", new BlockListSetting());

    public XRay() {
        super("XRay", "Makes most blocks invisible so you can see ores", Category.RENDER);
    }

    public void onEnable() {
        mc.renderGlobal.loadRenderers();
    }

    public void onDisable() {
        mc.renderGlobal.loadRenderers();
    }

    @Subscriber
    public void onXray(XRayEvent event) {
        event.setCancelled(true);
    }

    @Subscriber
    public void onXrayBlock(XRayBlockEvent event) {
        if (!blocks.getValue().getBlocks().contains(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
