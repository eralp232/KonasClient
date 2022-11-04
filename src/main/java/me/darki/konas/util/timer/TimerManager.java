package me.darki.konas.util.timer;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.mixin.mixins.ITimer;
import me.darki.konas.module.Module;
import me.darki.konas.util.client.TickRateUtil;
import net.minecraft.client.Minecraft;

public class TimerManager {
    private Module currentModule;
    private int priority;
    private float timerSpeed;
    private boolean active = false;
    private boolean tpsSync = false;

    public void updateTimer(Module module, int priority, float timerSpeed) {
        if (module == currentModule) {
            this.priority = priority;
            this.timerSpeed = timerSpeed;
            this.active = true;
        } else if (priority > this.priority || !this.active) {
            this.currentModule = module;
            this.priority = priority;
            this.timerSpeed = timerSpeed;
            this.active = true;
        }
    }

    public void resetTimer(Module module) {
        if (this.currentModule == module) {
            active = false;
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) {
            ((ITimer) ((IMinecraft) Minecraft.getMinecraft()).getTimer()).setTickLength(50.0f);
            return;
        }
        if (tpsSync && TickRateUtil.INSTANCE.getLatestTickRate() > 0.125D) { // 0.125D check is nessasary to avoid 0tps when joining server
            ((ITimer)((IMinecraft) Minecraft.getMinecraft()).getTimer()).setTickLength(Math.min(500, 50F * (20F / TickRateUtil.INSTANCE.getLatestTickRate())));
        } else {
            ((ITimer) ((IMinecraft) Minecraft.getMinecraft()).getTimer()).setTickLength(active ? (50.0f / timerSpeed) : 50.0f);
        }
    }

    public boolean isTpsSync() {
        return tpsSync;
    }

    public void setTpsSync(boolean tpsSync) {
        this.tpsSync = tpsSync;
    }
}
