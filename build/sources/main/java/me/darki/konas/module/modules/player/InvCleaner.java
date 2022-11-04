package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerUpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ItemListSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public class InvCleaner extends Module {
    public static Setting<ItemListSetting> items = new Setting<>("Items", new ItemListSetting());
    public static Setting<Boolean> hotbar = new Setting<>("Hotbar", true);

    public static Setting<Integer> delay = new Setting<>("Delay", 500, 1, 2000, 1);

    private Timer timer = new Timer();

    public InvCleaner() {
        super("InvCleaner", "Removes junk from your inventory", Category.PLAYER);
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (!timer.hasPassed(delay.getValue())) {
            return;
        }

        timer.reset();

        int i = 9;
        while (i < (hotbar.getValue() ? 45 : 36)) {
            ItemStack itemStack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (!itemStack.isEmpty() && items.getValue().getItems().contains(itemStack.getDisplayName().toLowerCase(Locale.ENGLISH))) {
                mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, -999, 0, ClickType.PICKUP, mc.player);
            }
            ++i;
        }
    }
}
