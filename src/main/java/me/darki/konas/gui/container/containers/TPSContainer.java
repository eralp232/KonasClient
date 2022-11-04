package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.TickCalculation;
import me.darki.konas.util.render.font.FontRendererWrapper;

import java.awt.*;
import java.text.DecimalFormat;

public class TPSContainer extends Container {
    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));

    public TPSContainer() {
        super("TPS", 0, 200, 5, 10);
    }

    @Override
    public void onRender() {
        super.onRender();

        try {
            DecimalFormat df = new DecimalFormat("#.##");
            String tpsString = "TPS: " + df.format(TickCalculation.INSTANCE.calculateTPS());

            float currentWidth = Math.max(5, FontRendererWrapper.getStringWidth(tpsString));
            setWidth(currentWidth + 1);
            setHeight(FontRendererWrapper.getStringHeight(tpsString) + 1);

            FontRendererWrapper.drawStringWithShadow(tpsString, (int) getPosX(), (int) getPosY(), textColor.getValue().getColor());
        } catch (NullPointerException ignored) {

        }
    }
}
