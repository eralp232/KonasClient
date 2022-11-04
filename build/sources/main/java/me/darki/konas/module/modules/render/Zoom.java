package me.darki.konas.module.modules.render;

import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.ListenableSettingDecorator;

public class Zoom extends Module {
    private static float preZoom;

    public static final ListenableSettingDecorator<Float> zoom = new ListenableSettingDecorator<>("Zoom", 1.1F, 1.5F, 0.5F, 0.05F, value -> {
        if (ModuleManager.getModuleByClass(Zoom.class).isEnabled()) {
            mc.gameSettings.fovSetting = preZoom * (1.6F - value);
        }
    });

    public Zoom() {
        super("Zoom", "Zoom in properly, not with perspective modification", Category.RENDER);
    }


    public void onEnable() {
        preZoom = mc.gameSettings.fovSetting;
        mc.gameSettings.fovSetting *= (1.6F - zoom.getValue());
    }

    public void onDisable() {
        mc.gameSettings.fovSetting = preZoom;
    }
}
