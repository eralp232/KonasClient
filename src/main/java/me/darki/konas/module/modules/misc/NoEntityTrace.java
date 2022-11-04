package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.TraceEntityEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.Setting;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;

public class NoEntityTrace extends Module {

    private Setting<Boolean> pickaxeOnly = new Setting<>("PickaxeOnly", false);
    private Setting<Boolean> swordOnly = new Setting<>("SwordOnly", false);

    public NoEntityTrace() {
        super("NoEntityTrace", Category.MISC, "NoEntityHit");
    }

    @Subscriber
    public void onTrace(TraceEntityEvent event) {
        if (mc.gameSettings.keyBindPickBlock.isKeyDown() && ModuleManager.getModuleByClass(MiddleClick.class).isEnabled()) return;
        if (pickaxeOnly.getValue() && !(mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemPickaxe)) {
            if (!swordOnly.getValue() || !(mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)) {
                return;
            }
        }

        event.setCancelled(true);
    }

}
