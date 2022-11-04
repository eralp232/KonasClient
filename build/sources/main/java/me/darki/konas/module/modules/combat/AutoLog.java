package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.misc.AutoReconnect;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.combat.CrystalUtils;
import me.darki.konas.util.friends.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class AutoLog extends Module {

    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.HEALTH);
    private static final Setting<Float> health = new Setting<>("Health", 10f, 22f, 0f, 0.1f).withVisibility(() -> mode.getValue() == Mode.HEALTH);
    public Setting<Float> crystalRange = new Setting<>("CrystalRange", 10f, 15f, 1f, 1f).withVisibility(() -> mode.getValue() == Mode.CRYSTALDMG);
    private static final Setting<Boolean> totem = new Setting<>("IgnoreTotem", true).withVisibility(() -> mode.getValue() != Mode.PLAYER);
    private Setting<Boolean> notify = new Setting<>("Notify", false).withVisibility(this::isNotif);

    public AutoLog() {
        super("AutoLog", Category.COMBAT, "AutoDisconnect");
    }

    private enum Mode {
        HEALTH, PLAYER, CRYSTALDMG
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (mode.getValue() == Mode.HEALTH) {
            if (mc.player.getHealth() <= health.getValue()) {
                if (totem.getValue()) disconnect();
                else if (!hasTotems()) disconnect();
            }
        } else if (mode.getValue() == Mode.PLAYER) {
            for (EntityPlayer e : mc.world.playerEntities) {
                if (e != mc.player && !Friends.isFriend(e.getName()) && !FakePlayerManager.isFake(e)) {
                    disconnect();
                    break;
                }
            }
        } else {
            if (!totem.getValue() && hasTotems()) return;
            float dmg = 0.0f;

            List<Entity> crystalsInRange = mc.world.loadedEntityList.stream()
                    .filter(e -> e instanceof EntityEnderCrystal)
                    .filter(e -> mc.player.getDistance(e) <= crystalRange.getValue())
                    .collect(Collectors.toList());

            for (Entity entity : crystalsInRange) {
                dmg += CrystalUtils.calculateDamage((EntityEnderCrystal) entity, mc.player);
            }

            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= dmg) disconnect();
        }
    }

    private boolean hasTotems() {
        for (int slot = 0; slot < 36; slot++) {
            if (mc.player.inventory.getStackInSlot(slot).getItem() == Items.TOTEM_OF_UNDYING) return true;
        }
        return false;
    }

    private void disconnect() {
        AutoReconnect module = (AutoReconnect) ModuleManager.getModuleByClass(AutoReconnect.class);
        if (module != null && module.isEnabled()) module.toggle();
        this.toggle();
        if (notify.getValue()) {
            doNotification("You have AutoLogged!", TrayIcon.MessageType.ERROR);
        }
        mc.player.inventory.currentItem = 1000;
    }

}
