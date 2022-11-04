package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.mixin.mixins.ITimer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import org.lwjgl.input.Keyboard;

public class Timer extends Module {

    public Setting<Float> timerSpeed = new Setting<>("TimerSpeed", 4f, 50f, 0.2f, 0.5f);
    public Setting<Boolean> tpsSync = new Setting<>("TpsSync", false);
    public Setting<Boolean> Switch = new Setting<>("Switch", false).withVisibility(() -> !tpsSync.getValue());
    public Setting<Integer> activeTicks = new Setting<>("Active", 5, 20, 1, 1).withVisibility(Switch::getValue);
    public Setting<Integer> inactiveTicks = new Setting<>("Inactive", 5, 20, 1, 1).withVisibility(Switch::getValue);
    public Setting<Float> inactiveSpeed = new Setting<>("InactiveSpeed", 2f, 50f, 0.2f, 0.5f).withVisibility(Switch::getValue);

    public Timer() {
        super("Timer", "Changes game tick length", Keyboard.KEY_NONE, Category.PLAYER);
    }

    private int counter = 0;

    public void onEnable() {
        counter = 0;
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (tpsSync.getValue()) {
            KonasGlobals.INSTANCE.timerManager.setTpsSync(true);
            setExtraInfo(Math.round(50.0f / ((ITimer) ((IMinecraft) mc).getTimer()).getTickLength() * 100.0f) / 100.0f + "");
        } else {
            KonasGlobals.INSTANCE.timerManager.setTpsSync(false);
            float speed = timerSpeed.getValue();
            if (Switch.getValue()) {
                if (counter > activeTicks.getValue() + inactiveTicks.getValue()) {
                    counter = 0;
                } if (counter > activeTicks.getValue()) {
                    speed = inactiveSpeed.getValue();
                }
            }
            KonasGlobals.INSTANCE.timerManager.updateTimer(this, 5, speed);
            setExtraInfo(Math.round(50.0f / ((ITimer) ((IMinecraft) mc).getTimer()).getTickLength() * 100.0f) / 100.0f + "");
            counter++;
        }
    }

    @Override
    public void onDisable() {
        KonasGlobals.INSTANCE.timerManager.resetTimer(this);
        KonasGlobals.INSTANCE.timerManager.setTpsSync(false);
    }
}
