package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class FPSContainer extends Container {
    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));

    public FPSContainer() {
        super("FPS", 100, 250, 5, 10);
    }

    @Override
    public void onRender() {
        super.onRender();

        String fpsString = Minecraft.getDebugFPS() + " FPS";

        float currentWidth = Math.max(5, FontRendererWrapper.getStringWidth(fpsString));
        setWidth(currentWidth + 1);
        setHeight(FontRendererWrapper.getStringHeight(fpsString) + 1);

        FontRendererWrapper.drawStringWithShadow(fpsString, (int) getPosX() + (int) getWidth() - FontRendererWrapper.getStringWidth(fpsString), (int) getPosY(), textColor.getValue().getColor());
    }
}
