package me.darki.konas.module.modules.render;

import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;

public class CustomEnchants extends Module {
    public static Setting<ColorSetting> enchantColor = new Setting<>("EnchantColor", new ColorSetting(0x77FF00FF, true));

    public CustomEnchants() {
        super("CustomEnchants", "Makes your enchant glint change color", Category.RENDER, "RainbowEnchants");
    }
}
