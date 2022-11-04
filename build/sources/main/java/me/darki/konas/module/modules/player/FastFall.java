package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.exploit.RubberFill;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.timer.TimerManager;

public class FastFall extends Module {
    public static Setting<Boolean> strict = new Setting<>("Strict", false);
    public static Setting<Boolean> timer = new Setting<>("Timer", false);

    public FastFall() {
        super("FastFall", Category.PLAYER, "Falls faster", "RStep", "DownStep", "ReverseStep");
    }

    private boolean valid = false;

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) {
            KonasGlobals.INSTANCE.timerManager.resetTimer(this);
            return;
        }
        if (ModuleManager.getModuleByClass(RubberFill.class).isEnabled()) return;
        if (timer.getValue()) {
            if (!mc.player.onGround) {
                if (mc.player.motionY < 0 && valid) {
                    KonasGlobals.INSTANCE.timerManager.updateTimer(this, 50, strict.getValue() ? 2.5F : 5F);
                    return;
                } else {
                    valid = false;
                }
            } else if (mc.player.onGround && !(mc.player.isInWater() || mc.player.isInLava())) {
                mc.player.motionY = -0.08;
                valid = true;
            }
        } else if (mc.player.onGround && !(mc.player.isInWater() || mc.player.isInLava())) {
            if (strict.getValue()) {
                mc.player.motionY = -1;
            } else {
                mc.player.motionY = -5;
            }
        }
        KonasGlobals.INSTANCE.timerManager.resetTimer(this);
    }

    public void onEnable() {
        valid = false;
    }

    public void onDisable() {
        KonasGlobals.INSTANCE.timerManager.resetTimer(this);
    }
}