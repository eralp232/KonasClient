package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.FogEvent;
import me.darki.konas.event.events.GetWorldTimeEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.ColorUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

public class Sky extends Module {

    private final Setting<TimeMode> timeMode = new Setting<>("TimeMode", TimeMode.STATIC);
    private final Setting<Integer> time = new Setting<>("Time", 6000, 24000, 0, 1).withVisibility(() -> timeMode.getValue() == TimeMode.STATIC);
    private static Setting<Boolean> customColor = new Setting<>("Colorize", false);
    private static Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFF0000FF)).withVisibility(customColor::getValue);

    public enum TimeMode {
        NONE, STATIC, IRL
    }

    public Sky() {
        super("Sky", Category.RENDER);
    }

    @Subscriber
    public void onGetWorldTime(GetWorldTimeEvent event) {
        if (timeMode.getValue() != TimeMode.NONE) {
            if (timeMode.getValue() == TimeMode.STATIC) {
                event.setWorldTime(time.getValue());
            } else {
                ZonedDateTime nowZoned = ZonedDateTime.now();
                Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
                Duration duration = Duration.between(midnight, Instant.now());
                long seconds = duration.getSeconds();
                event.setWorldTime((int) (seconds / 86400F));
            }
            event.cancel();
        }
    }

    @Subscriber
    public void onFogDensity(FogEvent.Density event) {
        if (customColor.getValue()) {
            event.setDensity(0);
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onFogColor(FogEvent.Color event) {
        if (customColor.getValue()) {
            float[] rgb = ColorUtils.intToRGB(color.getValue().getColor());
            event.setR(rgb[0]);
            event.setG(rgb[1]);
            event.setB(rgb[2]);
        }
    }

}
