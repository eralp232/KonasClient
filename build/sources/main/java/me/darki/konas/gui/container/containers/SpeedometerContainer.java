package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.mixin.mixins.ITimer;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpeedometerContainer extends Container {
    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));

    private final Setting<Boolean> kilometers = new Setting<>("Kilometers", true);
    private final Setting<Integer> places = new Setting<>("Places", 2, 5, 0, 1);
    private final Setting<Boolean> vertical = new Setting<>("Vertical", true);

    public SpeedometerContainer() {
        super("Speed", 100, 200, 5, 10);
    }

    private final double[] speeds = new double[20];
    private int nextIndex = 0;

    @Override
    public void onRender() {
        super.onRender();

        String speedString = (kilometers.getValue() ? "KPH: " : "BPS: ") + getSpeed();

        float currentWidth = Math.max(5, FontRendererWrapper.getStringWidth(speedString));
        setWidth(currentWidth + 1);
        setHeight(FontRendererWrapper.getStringHeight(speedString) + 1);

        FontRendererWrapper.drawStringWithShadow(speedString, (int) getPosX(), (int) getPosY(), textColor.getValue().getColor());
    }

    private String getSpeed() {
        float currentTps = ((ITimer) ((IMinecraft) mc).getTimer()).getTickLength() / 1000.0f;
        double multipier = 1.0;
        if (kilometers.getValue()) {
            multipier = 3.6;
        }
        double currentSpeed = ((MathHelper.sqrt(Math.pow(coordsDiff('x', mc), 2) + (vertical.getValue() ? Math.pow(coordsDiff('y', mc), 2) : 0) + Math.pow(coordsDiff('z', mc), 2)) / currentTps)) * multipier;
        speeds[(this.nextIndex % speeds.length)] = currentSpeed;
        nextIndex += 1;

        int numSpeeds = 1;
        double sumSpeeds = 0.0F;

        for (double s : speeds) {
            sumSpeeds += s;
            numSpeeds += 1;
        }

        return "" + round(sumSpeeds / numSpeeds, places.getValue());
    }

    private static double coordsDiff(char s, Minecraft mc) {
        switch (s) {
            case 'x': {
                return mc.player.posX - mc.player.prevPosX;
            }
            case 'y': {
                return mc.player.posY - mc.player.prevPosY;
            }
            case 'z': {
                return mc.player.posZ - mc.player.prevPosZ;
            }
            default: {
                return 0.0;
            }
        }
    }

    private double round(double value, int dp) {
        if (dp < 0) {
            return value;
        }
        return (new BigDecimal(value)).setScale(dp, RoundingMode.HALF_UP).doubleValue();
    }
}
