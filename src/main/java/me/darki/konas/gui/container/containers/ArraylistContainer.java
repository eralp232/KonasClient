package me.darki.konas.gui.container.containers;

import me.darki.konas.command.Command;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.gui.container.Container;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.ColorUtils;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ArraylistContainer extends Container {

    private Setting<Boolean> lines = new Setting<>("Lines", true);
    private Setting<Boolean> cute = new Setting<>("Cute", false);
    private Setting<ColorSetting> color = new Setting<>("LineColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), true)).withVisibility(() -> !cute.getValue());
    private Setting<Boolean> pulse = new Setting<>("Pulse", true);
    private Setting<Float> range = new Setting<>("Range", 1f, 1f, 0.1f, 0.1f).withVisibility(() -> pulse.getValue() && !color.getValue().isCycle());
    private Setting<Float> spread = new Setting<>("Spread", 1f, 2f, 0.1f, 0.1f).withVisibility(() -> pulse.getValue() && !color.getValue().isCycle());
    private Setting<Float> speed = new Setting<>("Speed", 1f, 10f, 1f, 1f).withVisibility(() -> pulse.getValue() && !color.getValue().isCycle());

    public ArraylistContainer() {
        super("ArrayList", 100, 100, 100, 100);
    }

    float maxWidth = 0;

    public Anchor anchor = Anchor.TOP_RIGHT;

    @Override
    public void onRender() {
        super.onRender();

        setAnchor();

        final int[] yDist = {0};
        final int[] counter = {1};

        boolean isTop = anchor == Anchor.TOP_LEFT || anchor == Anchor.TOP_RIGHT;
        boolean isRight = anchor == Anchor.BOTTOM_RIGHT || anchor == Anchor.TOP_RIGHT;

        ArrayList<Module> modules = ModuleManager.getEnabledVisibleModules();

        maxWidth = (float) modules
                .stream()
                .mapToDouble(m -> FontRendererWrapper.getStringWidth(m.getExtraInfo() != null ? m.getName() + " " + m.getExtraInfo() : m.getName()))
                .max().orElse(0);

        setWidth(maxWidth);

        modules.stream()
                .sorted(Comparator.comparingInt(module -> isTop ? -(int) FontRendererWrapper.getStringWidth(module.getExtraInfo() != null ? module.getName() + " " + module.getExtraInfo() : module.getName()) : (int) FontRendererWrapper.getStringWidth(module.getExtraInfo() != null ? module.getName() + " " + module.getExtraInfo() : module.getName())))
                .forEach(module -> {
                    float stringWidth = FontRendererWrapper.getStringWidth(module.getExtraInfo() != null ? module.getName() + " " + module.getExtraInfo() : module.getName());
                    String moduleDisplay = getModuleDisplay(module);

                    // Background
                    GuiRenderHelper.drawRect(getPosX() + (isRight ? getWidth() - stringWidth - 2 : 0), getPosY() + yDist[0], stringWidth + 2, (int) (FontRendererWrapper.getStringHeight(moduleDisplay) + 1.5F), new Color(20, 20, 20, 60).hashCode());


                    int color = getColor(counter[0]);

                    // Add optional eye candy line
                    if (lines.getValue()) {
                        GuiRenderHelper.drawRect(getPosX() + (isRight ? getWidth() - stringWidth - 2 : stringWidth + 2), getPosY() + yDist[0], 1F, (int) (FontRendererWrapper.getStringHeight(moduleDisplay) + 1.5F), color);
                    }

                    FontRendererWrapper.drawStringWithShadow(moduleDisplay, (int) ((int) getPosX() + (isRight ? getWidth() - stringWidth : 0)), (int) (getPosY() + yDist[0] + 0.5F), module.isVisible() ? color : Color.GRAY.getRGB());
                    yDist[0] += (int) (FontRendererWrapper.getStringHeight(moduleDisplay) + 1.5F);
                    counter[0]++;
                });

        setHeight(yDist[0]);
    }

    private int getColor(int index) {
        float[] hsb = Color.RGBtoHSB((color.getValue().getRawColor() >> 16) & 0xFF, (color.getValue().getRawColor() >> 8) & 0xFF, color.getValue().getRawColor() & 0xFF, null);
        if (cute.getValue()) {
            return getCuteColor(index - 1);
        } else if (pulse.getValue()) {
            if (this.color.getValue().isCycle()) {
                return ColorUtils.rainbow(300 * index, hsb);
            } else {
                return ColorUtils.pulse(index, hsb, this.spread.getValue(), this.speed.getValue(), range.getValue());
            }
        } else {
            return color.getValue().getColor();
        }
    }

    private int getCuteColor(int index) {

        int size = ModuleManager.getEnabledModules().size();

        int light_blue = new Color(91, 206, 250).getRGB();
        int white = Color.WHITE.getRGB();
        int pink = new Color(245, 169, 184).getRGB();

        int chunkSize = size / 5;

        if (index < chunkSize) {
            return light_blue;
        } else if (index < chunkSize * 2) {
            return pink;
        } else if (index < chunkSize * 3) {
            return white;
        } else if (index < chunkSize * 4) {
            return pink;
        } else if (index < chunkSize * 5) {
            return light_blue;
        }

        return light_blue;
    }

    private void setAnchor() {
        float x = getPosX() + getWidth() / 2;
        float y = getPosY() + getHeight() / 2;
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (y >= sr.getScaledHeight() / 2F && x >= sr.getScaledWidth() / 2F) {
            anchor = Anchor.BOTTOM_RIGHT;
        } else if (y >= sr.getScaledHeight() / 2F && x <= sr.getScaledWidth() / 2F) {
            anchor = Anchor.BOTTOM_LEFT;
        } else if (y <= sr.getScaledHeight() / 2F && x >= sr.getScaledWidth() / 2F) {
            anchor = Anchor.TOP_RIGHT;
        } else if (y <= sr.getScaledHeight() / 2F && x <= sr.getScaledWidth() / 2F) {
            anchor = Anchor.TOP_LEFT;
        }
    }

    private String getModuleDisplay(Module module) {
        if (module.getExtraInfo() != null) {
            return module.getName() + Command.SECTIONSIGN + "7 " + module.getExtraInfo();
        } else {
            return module.getName();
        }
    }

    public enum Anchor {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

}
