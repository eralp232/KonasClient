package me.darki.konas.module.modules.client;

import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;

public class Macros extends Module {
    public static final Setting<Boolean> oneLine = new Setting<>("OneLine", false);

    public Macros() {
        super("Macros", Category.CLIENT);
        this.toggle();
    }
}
