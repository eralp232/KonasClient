package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.combat.ArmorUtils;
import me.darki.konas.util.interaction.InteractionUtil;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

public class AutoMend extends Module {

    private final Setting<Boolean> lookdown = new Setting<>("Lookdown", true);
    private final Setting<Boolean> xp = new Setting<>("AutoXP", false);
    private final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", false);
    private final Setting<Boolean> armor = new Setting<>("Armor", false);
    private final Setting<Integer> threshold = new Setting<>("Threshold", 60, 100, 0, 1);
    private final Setting<Boolean> attackCheck = new Setting<>("AttackCheck", true);
    private final Setting<Float> crystalRange = new Setting<>("CrystalRange", 6f, 10f, 0f, 0.1f).withVisibility(attackCheck::getValue);

    public AutoMend() {
        super("AutoMend", Category.MISC);
    }

    private boolean shouldMend = false;

    public static boolean isMending = false;

    private float prevHealth = 0.0F;

    @Subscriber(priority = 10)
    public void onUpdate(UpdateWalkingPlayerEvent event) {
        if (mc.player == null || mc.world == null) return;

        EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
                .filter(e -> (e instanceof EntityEnderCrystal && mc.player.getDistance(e) <= crystalRange.getValue()))
                .map(entity -> (EntityEnderCrystal) entity)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);

        if ((crystal != null || mc.player.getHealth() + mc.player.getAbsorptionAmount() < prevHealth) && attackCheck.getValue()) {
            isMending = false;
            shouldMend = false;
            toggle();
            return;
        }

        prevHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.EXPERIENCE_BOTTLE || (autoSwitch.getValue() && getXpSlot() != -1)) {
            if (event instanceof UpdateWalkingPlayerEvent.Pre) {
                if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;
                shouldMend = false;

                if (lookdown.getValue()) {
                    KonasGlobals.INSTANCE.rotationManager.setRotations(mc.player.rotationYaw, 90);
                }

                if (armor.getValue()) {

                    ItemStack[] armorStacks = new ItemStack[]{
                            mc.player.inventory.getStackInSlot(39),
                            mc.player.inventory.getStackInSlot(38),
                            mc.player.inventory.getStackInSlot(37),
                            mc.player.inventory.getStackInSlot(36)
                    };

                    for (int i = 0; i < 4; i++) {

                        ItemStack stack = armorStacks[i];

                        if (!(stack.getItem() instanceof ItemArmor)) continue;

                        if (ArmorUtils.calculatePercentage(stack) < threshold.getValue()) continue;

                        for (int s = 0; s < 36; s++) {

                            ItemStack emptyStack = mc.player.inventory.getStackInSlot(s);

                            if (!emptyStack.isEmpty() || !(emptyStack.getItem() == Items.AIR)) continue;

                            isMending = true;
                            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i + 5, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, s < 9 ? s + 36 : s, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i + 5, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.updateController();
                            return;
                        }

                    }

                    for (int i = 0; i < 4; i++) {
                        ItemStack stack = armorStacks[i];

                        if (!(stack.getItem() instanceof ItemArmor)) continue;

                        if (ArmorUtils.calculatePercentage(stack) >= threshold.getValue()) continue;

                        shouldMend = true;
                    }

                    if (!shouldMend) {
                        isMending = false;
                        toggle();
                    }

                }
            } else if (xp.getValue()) {
                if (!armor.getValue() || shouldMend) {
                    int itemSlot = getXpSlot();
                    boolean changeItem = autoSwitch.getValue() && mc.player.inventory.currentItem != itemSlot && itemSlot != -1;
                    int startingItem = mc.player.inventory.currentItem;

                    if (changeItem) {
                        mc.player.inventory.currentItem = itemSlot;
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(itemSlot));
                    }

                    if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemExpBottle) {
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    }

                    if (changeItem) {
                        mc.player.inventory.currentItem = startingItem;
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(startingItem));
                    }
                }
            }
        }
    }

    private int getXpSlot() {
        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!stack.isEmpty() && stack.getItem() instanceof ItemExpBottle) {
            return mc.player.inventory.currentItem;
        } else {
            for (int i = 0; i < 9; ++i) {
                stack = mc.player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemExpBottle) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        prevHealth = 0.0F;
    }

    @Override
    public void onDisable() {
        isMending = false;
    }

}
