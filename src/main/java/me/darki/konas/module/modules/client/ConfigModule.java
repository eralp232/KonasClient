package me.darki.konas.module.modules.client;

import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;

public class ConfigModule extends Module {

    public static Setting<Boolean> overwriteFriends = new Setting<>("OverwriteFriends", false);

    public ConfigModule() {
        super("Config", "Settings for Config", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        this.toggle();
    }

}
