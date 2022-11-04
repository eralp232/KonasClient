package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.misc.AutoMend;
import me.darki.konas.module.modules.movement.ElytraFly;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.inventory.InvStack;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AutoArmor extends Module {

    private static final Setting<Boolean> elytraPrio = new Setting<>("ElytraPrio", false);
    private static final Setting<Boolean> smart = new Setting<>("Smart", false).withVisibility(elytraPrio::getValue);
    private static final Setting<Integer> delay = new Setting<>("Delay", 1, 10, 1, 1);
    private static final Setting<Boolean> strict = new Setting<>("Strict", false);
    private static final Setting<Boolean> armorSaver = new Setting<>("ArmorSaver", false);
    private static final Setting<Boolean> pauseWhenSafe = new Setting<>("PauseWhenSafe", false);
    private static final Setting<Float> depletion = new Setting<>("Depletion", 0.75F, 0.95F, 0.5F, 0.05F).withVisibility(armorSaver::getValue);
    private static final Setting<Boolean> allowMend = new Setting<>("AllowMend", false);

    private Timer rightClickTimer = new Timer();

    private boolean sleep;

    public AutoArmor() {
        super("AutoArmor", "Automatically equips armor", Category.PLAYER);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {

        if (event.getPhase() == TickEvent.Phase.END) return;

        if (mc.world == null || mc.player == null) return;

        if (!HotbarRefill.moveTimer.hasPassed(350)) return;

        if (mc.player.ticksExisted % delay.getValue() != 0) {
            return;
        }

        if (strict.getValue()) {
            if (mc.player.motionX != 0D || mc.player.motionZ != 0D) return;
        }

        if (pauseWhenSafe.getValue()) {
            List<Entity> proximity =  mc.world.loadedEntityList.stream().filter(e -> (e instanceof EntityPlayer && !(e.equals(mc.player)) && mc.player.getDistance(e) <= 6) || (e instanceof EntityEnderCrystal && mc.player.getDistance(e) <= 12)).collect(Collectors.toList());
            if(proximity.isEmpty()) return;
        }

        if(AutoMend.isMending && ModuleManager.getModuleByName("AutoMend").isEnabled()) return;

        if(allowMend.getValue()) {
            if(!rightClickTimer.hasPassed(500)) {
                for(int i = 0; i < mc.player.inventory.armorInventory.size(); i++) {
                    ItemStack armorPiece = mc.player.inventory.armorInventory.get(i);
                    if(armorPiece.getEnchantmentTagList() != null) {
                        boolean mending = false;
                        for(Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(armorPiece).entrySet()) {
                            if(entry.getKey().getName().contains("mending")) {
                                mending = true;
                                break;
                            }
                        }
                        if(!mending) continue;
                    }
                    if(armorPiece.isEmpty()) continue;
                    long freeSlots = mc.player.inventory.mainInventory
                            .stream()
                            .filter(is -> is.isEmpty() || is.getItem() == Items.AIR)
                            .map(is -> mc.player.inventory.getSlotFor(is))
                            .count();
                    if(freeSlots <= 0) return;
                    if(armorPiece.getItemDamage() != 0) {
                        shiftClickSpot(8 - i);
                        return;
                    }
                }
                return;
            }
        }

        if (mc.currentScreen instanceof GuiContainer) return;

        AtomicBoolean hasSwapped = new AtomicBoolean(false);

        if (sleep) {
            sleep = false;
            return;
        }

        boolean ep = elytraPrio.getValue();
        if (smart.getValue() && !ModuleManager.getModuleByClass(ElytraFly.class).isEnabled()) {
            ep = false;
        }

        final Set<InvStack> replacements = new HashSet<>();

        for (int slot = 0; slot < 36; slot++) {

            InvStack invStack = new InvStack(slot, mc.player.inventory.getStackInSlot(slot));
            if (invStack.stack.getItem() instanceof ItemArmor || invStack.stack.getItem() instanceof ItemElytra) {
                replacements.add(invStack);
            }

        }

        List<InvStack> armors = replacements.stream()
                .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                .filter(invStack -> !armorSaver.getValue() || invStack.stack.getItem().getDurabilityForDisplay(invStack.stack) < depletion.getValue())
                .sorted(Comparator.comparingInt(invStack -> invStack.slot))
                .sorted(Comparator.comparingInt(invStack -> ((ItemArmor) invStack.stack.getItem()).damageReduceAmount))
                .collect(Collectors.toList());

        boolean wasEmpty = armors.isEmpty();

        if (wasEmpty) {
            armors = replacements.stream()
                    .filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                    .sorted(Comparator.comparingInt(invStack -> invStack.slot))
                    .sorted(Comparator.comparingInt(invStack -> ((ItemArmor) invStack.stack.getItem()).damageReduceAmount))
                    .collect(Collectors.toList());
        }

        List<InvStack> elytras = replacements.stream()
                .filter(invStack -> invStack.stack.getItem() instanceof ItemElytra)
                .sorted(Comparator.comparingInt(invStack -> invStack.slot))
                .collect(Collectors.toList());


        Item currentHeadItem = mc.player.inventory.getStackInSlot(39).getItem();
        Item currentChestItem = mc.player.inventory.getStackInSlot(38).getItem();
        Item currentLegsItem = mc.player.inventory.getStackInSlot(37).getItem();
        Item currentFeetItem = mc.player.inventory.getStackInSlot(36).getItem();

        boolean replaceHead = currentHeadItem.equals(Items.AIR) || (!wasEmpty && armorSaver.getValue() && mc.player.inventory.getStackInSlot(39).getItem().getDurabilityForDisplay(mc.player.inventory.getStackInSlot(39)) >= depletion.getValue());
        boolean replaceChest = currentChestItem.equals(Items.AIR) || (!wasEmpty && armorSaver.getValue() && mc.player.inventory.getStackInSlot(38).getItem().getDurabilityForDisplay(mc.player.inventory.getStackInSlot(38)) >= depletion.getValue());
        boolean replaceLegs = currentLegsItem.equals(Items.AIR) || (!wasEmpty && armorSaver.getValue() && mc.player.inventory.getStackInSlot(37).getItem().getDurabilityForDisplay(mc.player.inventory.getStackInSlot(37)) >= depletion.getValue());
        boolean replaceFeet = currentFeetItem.equals(Items.AIR) || (!wasEmpty && armorSaver.getValue() && mc.player.inventory.getStackInSlot(36).getItem().getDurabilityForDisplay(mc.player.inventory.getStackInSlot(36)) >= depletion.getValue());



        if (replaceHead && !hasSwapped.get()) {
            armors.stream().filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                    .filter(invStack -> ((ItemArmor) invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.HEAD)
            ).findFirst().ifPresent(invStack -> {
                swapSlot(invStack.slot, 5);
                hasSwapped.set(true);
            });
        }

        if (ep && !(currentChestItem instanceof ItemElytra) && elytras.size() > 0 && !hasSwapped.get()) {
            elytras.stream().findFirst().ifPresent(invStack -> {
                swapSlot(invStack.slot, 6);
                hasSwapped.set(true);
            });
        }

        if (replaceChest || (!ep && currentChestItem.equals(Items.ELYTRA)) && !hasSwapped.get()) {
            armors.stream().filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                    .filter(invStack -> ((ItemArmor) invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.CHEST)
            ).findFirst().ifPresent(invStack -> {
                swapSlot(invStack.slot, 6);
                hasSwapped.set(true);
            });
        }

        if (replaceLegs && !hasSwapped.get()) {
            armors.stream().filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                    .filter(invStack -> ((ItemArmor) invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.LEGS)
            ).findFirst().ifPresent(invStack -> {
                swapSlot(invStack.slot, 7);
                hasSwapped.set(true);
            });
        }

        if (replaceFeet && !hasSwapped.get()) {
            armors.stream().filter(invStack -> invStack.stack.getItem() instanceof ItemArmor)
                    .filter(invStack -> ((ItemArmor) invStack.stack.getItem()).armorType.equals(EntityEquipmentSlot.FEET)
            ).findFirst().ifPresent(invStack -> {
                swapSlot(invStack.slot, 8);
                hasSwapped.set(true);
            });
        }

    }

    @Subscriber
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {

        if(event.getEntityPlayer() != mc.player) return;

        if(event.getItemStack().getItem() != Items.EXPERIENCE_BOTTLE) return;

        rightClickTimer.reset();
    }

    private void swapSlot(int source, int target) {

        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, source < 9 ? source + 36 : source, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, target, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, source < 9 ? source + 36 : source, 0, ClickType.PICKUP, mc.player);

        sleep = true;

    }

    private void shiftClickSpot(int source) {
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, source, 0, ClickType.QUICK_MOVE, mc.player);
    }

}
