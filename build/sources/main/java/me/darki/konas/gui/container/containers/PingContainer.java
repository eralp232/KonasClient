package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;

import java.awt.*;

public class PingContainer extends Container {
    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));

    public PingContainer() {
        super("Ping", 100, 200, 5, 10);
    }

    @Override
    public void onRender() {
        super.onRender();

        String pingString = getPing() + " ms";

        float currentWidth = Math.max(5, FontRendererWrapper.getStringWidth(pingString));
        setWidth(currentWidth + 1);
        setHeight(FontRendererWrapper.getStringHeight(pingString) + 1);

        FontRendererWrapper.drawStringWithShadow(pingString, (int) getPosX(), (int) getPosY(), textColor.getValue().getColor());
    }

    private int getPing() {
        if (mc.getConnection() == null) {
            return 1;
        } else if (mc.player == null) {
            return -1;
        } else {
            try {
                return mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime();
            } catch (NullPointerException ignored) {
            }
            return -1;
        }
    }
}
