package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.PlayerUtils;
import net.minecraft.client.Minecraft;

public class PlayerContainer extends Container {

    private final Setting<Boolean> yaw = new Setting<>("Yaw", true);
    private final Setting<Boolean> pitch = new Setting<>("Pitch", true);

    public PlayerContainer() {
        super("Player", 3, 80, 100, 115);
    }

    @Override
    public void onRender() {
        super.onRender();
        if(mc.player == null || mc.world == null) return;
        PlayerUtils.drawPlayerOnScreen((int) (getPosX() + (getWidth() / 2)), (int) (getPosY() + getHeight()), 50, -30, 0, Minecraft.getMinecraft().player, yaw.getValue(), pitch.getValue());
    }

}
