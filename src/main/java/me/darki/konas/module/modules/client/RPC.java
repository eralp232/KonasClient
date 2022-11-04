package me.darki.konas.module.modules.client;

import me.darki.konas.module.Module;
import me.darki.konas.setting.IRunnable;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.Logger;
import me.darki.konas.util.timer.Timer;

public class RPC extends Module {

    private final Timer cooldown = new Timer();
    public static boolean firstRun = true;

    private final ListenableSettingDecorator<Boolean> trans = new ListenableSettingDecorator<>("Cute", false, new IRunnable<Boolean>() {
        @Override
        public void run(Boolean value) {
            if (KonasGlobals.INSTANCE.rpc == null) return;
            if(!firstRun && !cooldown.hasPassed(10000)) {
                Logger.sendChatMessage("Please wait another " + Math.abs(((System.currentTimeMillis() - cooldown.getTime()) / 1000) - 10) + " seconds, before you enable this setting again!");
                trans.cancel();
            } else {
                KonasGlobals.INSTANCE.rpc.setCute(value);
                cooldown.reset();
            }
        }
    });

    private final ListenableSettingDecorator<Boolean> invite = new ListenableSettingDecorator<>("Invite", true, new IRunnable<Boolean>() {
        @Override
        public void run(Boolean value) {
            if (KonasGlobals.INSTANCE.rpc == null) return;
            if(!firstRun && !cooldown.hasPassed(10000)) {
                Logger.sendChatMessage("Please wait another " + Math.abs(((System.currentTimeMillis() - cooldown.getTime()) / 1000) - 10) + " seconds, before you enable this setting again!");
                invite.cancel();
            } else {
                KonasGlobals.INSTANCE.rpc.setDiscordLink(value);
                cooldown.reset();
            }
        }
    });

    public RPC() {
        super("RPC", "Control your discord rich presence", Category.CLIENT, "Discord", "DiscordRPC");
        cooldown.setTime(10000);
    }

    @Override
    public void onEnable() {
        if (KonasGlobals.INSTANCE.rpc == null) return;

        if(!firstRun && !cooldown.hasPassed(10000)) {
            Logger.sendChatMessage("Please wait another " + Math.abs(((System.currentTimeMillis() - cooldown.getTime()) / 1000) - 10) + " seconds, before you enable this module again!");
            this.toggle();
        } else {
            KonasGlobals.INSTANCE.rpc.enable();
            cooldown.reset();
        }
    }

    @Override
    public void onDisable() {
        if (KonasGlobals.INSTANCE.rpc == null) return;
        KonasGlobals.INSTANCE.rpc.disable();
    }

}
