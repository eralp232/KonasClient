package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.movement.ElytraFly;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;

import java.awt.*;

public class PitchContainer extends Container {
    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));

    public PitchContainer() {
        super("FlightPitch", 5, 100, 50, 100);
    }

    @Override
    public void onRender() {
        super.onRender();
        if(mc.player == null || mc.world == null) return;
        if (!ModuleManager.getModuleByClass(ElytraFly.class).isEnabled() || !ElytraFly.isHasElytra()) return;
        GuiRenderHelper.drawRect(getPosX(), getPosY(), getWidth(), getHeight() / 2F, mc.player.rotationPitch <= 0 ? 0xff1b83c2 : 0xff14608f);
        GuiRenderHelper.drawRect(getPosX(), getPosY() + getHeight() / 2F, getWidth(), getHeight() / 2F, mc.player.rotationPitch > 0 ? 0xff685834 : 0xff4f4328);
        float cursorPos = getPosY() + ((mc.player.rotationPitch + 90F) / 180F) * getHeight();
        GuiRenderHelper.drawRect(getPosX(), cursorPos, getWidth(), 2F, textColor.getValue().getColor());
        GuiRenderHelper.drawOutlineRect(getPosX(), getPosY(), getWidth(), getHeight(), 2F, textColor.getValue().getColor());
    }

}
