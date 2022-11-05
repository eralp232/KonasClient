package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.OrientCameraEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;

public class CameraClip extends Module {
    public static Setting<Boolean> offset = new Setting<>("Offset", false);
    public static Setting<Float> distance = new Setting<>("Distance", 0F, 10F, -10F, 0.5F).withVisibility(offset::getValue);

    public CameraClip() {
        super("CameraClip", Category.RENDER);
    }

    @Subscriber
    public void onOrientCameraPre(OrientCameraEvent.Pre event) {
        if (offset.getValue()) {
            event.setDistance(distance.getValue());
            event.cancel();
        }
    }

    @Subscriber
    public void onOrientCameraPost(OrientCameraEvent.Post event) {
        event.cancel();
        if (offset.getValue()) {
            event.setDistance(distance.getValue());
        } else {
            event.setDistance(4.0F);
        }
    }
}
