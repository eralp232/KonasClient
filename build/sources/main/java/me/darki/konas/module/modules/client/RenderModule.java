package me.darki.konas.module.modules.client;

import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;

public class RenderModule extends Module {
    public static Setting<Boolean> slowRender = new Setting<>("SlowRender", false).withDescription("Prevents culling");

    public RenderModule() {
        super("Render", "Customize global rendering options", Category.CLIENT);
    }

    public void onEnable() {
        toggle();
    }
}
