package me.darki.konas.gui.container.containers;

import me.darki.konas.command.Command;
import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.util.EnumFacing;

import java.awt.*;
import java.text.DecimalFormat;

public class CoordinatesContainer extends Container {
    private final Setting<Boolean> freecam = new Setting<>("FreecamCoords", true);
    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));

    public CoordinatesContainer() {
        super("Coords", 0, 350, 5, 10);
    }

    @Override
    public void onRender() {
        super.onRender();

        String facing = mc.player.getHorizontalFacing().getName().substring(0, 1).toUpperCase() + mc.player.getHorizontalFacing().getName().substring(1) + Command.SECTIONSIGN + "7 [" + Command.SECTIONSIGN + "r" + getAxis(mc.player.getHorizontalFacing()) + Command.SECTIONSIGN + "7]";
        DecimalFormat df = new DecimalFormat("#.#");
        double x = Double.parseDouble(df.format(freecam.getValue() ? mc.getRenderViewEntity().posX : mc.player.posX));
        double y = Double.parseDouble(df.format(freecam.getValue() ? mc.getRenderViewEntity().posY : mc.player.posY));
        double z = Double.parseDouble(df.format(freecam.getValue() ? mc.getRenderViewEntity().posZ : mc.player.posZ));
        double convertedX = Double.parseDouble(df.format(convertCoords(mc.player.posX)));
        double convertedZ = Double.parseDouble(df.format(convertCoords(mc.player.posZ)));
        String coords = Command.SECTIONSIGN + "7XYZ" + Command.SECTIONSIGN + "r " + x + ", " + y + ", " + z + Command.SECTIONSIGN + "7 [" + Command.SECTIONSIGN + "r" + convertedX + ", " + convertedZ + Command.SECTIONSIGN + "7]";

        float currentWidth = Math.max(FontRendererWrapper.getStringWidth(coords), FontRendererWrapper.getStringWidth(facing));
        setWidth(currentWidth + 1);
        setHeight(FontRendererWrapper.getStringHeight(facing) + FontRendererWrapper.getStringHeight(coords) + 1);

        FontRendererWrapper.drawStringWithShadow(facing, (int) getPosX(), (int) getPosY(), textColor.getValue().getColor());
        FontRendererWrapper.drawStringWithShadow(coords, (int) getPosX(), (int) getPosY() + FontRendererWrapper.getStringHeight(facing), textColor.getValue().getColor());
    }

    private double convertCoords(double coord) {
        boolean inHell = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell"));

        return inHell ? coord * 8 : coord / 8;

    }

    private String getAxis(EnumFacing facing) {
        if (facing == EnumFacing.SOUTH) {
            return "+Z";
        } else if (facing == EnumFacing.WEST) {
            return "-X";
        } else if (facing == EnumFacing.NORTH) {
            return "-Z";
        } else if (facing == EnumFacing.EAST) {
            return "+X";
        }

        return null;

    }
}
