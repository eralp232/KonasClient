package me.darki.konas.gui.container.containers;

import me.darki.konas.KonasMod;
import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class WatermarkContainer extends Container {

    private static Setting<WatermarkMode> mode = new Setting<>("Mode", WatermarkMode.IMAGE);
    public Setting<ColorSetting> color = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false)).withVisibility(() -> mode.getValue() == WatermarkMode.TEXT);

    public enum WatermarkMode {
        TEXT, IMAGE
    }

    public WatermarkContainer() {
        super("Watermark", 5, 5, 100, 60);
    }

    @Override
    public void onRender() {
        super.onRender();
        switch (mode.getValue()) {
            case TEXT:
                String s = KonasMod.NAME + " " + KonasMod.VERSION;
                setHeight(FontRendererWrapper.getStringHeight(s) + 1);
                setWidth(FontRendererWrapper.getStringWidth(s) + 1);
                FontRendererWrapper.drawStringWithShadow(s, (int) getPosX(), (int) getPosY(), color.getValue().getColor());
                break;
            case IMAGE:
                setHeight(60);
                setWidth(60);
                Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("konas/textures/konas.png"));
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableAlpha();
                Gui.drawModalRectWithCustomSizedTexture((int) getPosX(), (int) getPosY(), 0, 0, 60, 60, 60, 60);
                GlStateManager.disableAlpha();
                break;
        }
    }

}
