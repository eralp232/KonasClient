package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;

import java.awt.*;

public class ServerBrandContainer extends Container {
    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));

    public ServerBrandContainer() {
        super("ServerBrand", 200, 1000, 50, 10);
    }

    @Override
    public void onRender() {
        super.onRender();
        if(mc.player != null) {
            String brand = (mc.isIntegratedServerRunning() ? "Singleplayer: " : "Server: ") + (mc.player.getServerBrand() == null ? "Unknown" : mc.player.getServerBrand());
            setHeight(FontRendererWrapper.getStringHeight(brand) + 1);
            setWidth(FontRendererWrapper.getStringWidth(brand) + 1);
            FontRendererWrapper.drawStringWithShadow(brand, getPosX(), getPosY(), textColor.getValue().getColor());
        }

    }

}
