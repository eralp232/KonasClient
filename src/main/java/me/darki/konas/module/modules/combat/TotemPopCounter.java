package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.TotemPopEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.event.listener.TargetManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.ChatUtil;
import me.darki.konas.util.client.FakePlayerManager;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

public class TotemPopCounter extends Module {
    private Setting<Boolean> notSelf = new Setting<>("NotSelf", false);
    private Setting<Boolean> notify = new Setting<>("Notify", false).withVisibility(this::isNotif);

    public TotemPopCounter() {
        super("TotemPopCounter", Category.COMBAT, "PopCounter", "TotemCounter", "TotemPops");
        setProtocolRange(315, 1000);
    }

    @Subscriber
    public void onTotemPop(TotemPopEvent event) {
        if (notSelf.getValue() && event.getPlayer() == mc.player) return;

        ChatUtil.info(event.getPlayer().getEntityId(),"(h)%s(r) popped (h)%s(r) totem%s!", event.getPlayer().getName(), event.getPops(), (event.getPops() > 1 ? "s" : ""));

        if (notify.getValue()) {
            doNotification(event.getPlayer().getName() + " popped " + event.getPops() + " totem" + (event.getPops() > 1 ? "s" : "") + "!", TrayIcon.MessageType.NONE);
        }
    }

    @Subscriber(priority = 20)
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        for (EntityPlayer player : mc.world.playerEntities) {
            if ((notSelf.getValue() && player == mc.player) || FakePlayerManager.isFake(player) || player.getHealth() > 0 || !TargetManager.popList.containsKey(player.getName())) continue;

            ChatUtil.info(player.getEntityId(),"(h)%s(r) died after popping (h)%s(r) totem%s!", player.getName(), TargetManager.popList.get(player.getName()), (TargetManager.popList.get(player.getName()) > 1 ? "s" : ""));
            if (notify.getValue()) {
                doNotification(player.getName() + " died after popping " + TargetManager.popList.get(player.getName()) + " totems!", TrayIcon.MessageType.INFO);
            }
        }
    }

}
