package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.RootEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.player.HotbarRefill;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.combat.CrystalUtils;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class Offhand extends Module {

    public Setting<Boolean> totem = new Setting<>("Totem", true).withProtocolRange(315, 1000);
    public Setting<Boolean> gapple = new Setting<>("Gapple", true);
    public Setting<Boolean> crystal = new Setting<>("Crystal", true);

    public Setting<Float> delay = new Setting<>("Delay", 0F, 5F, 0F, 0.05F);

    public Setting<Boolean> hotbarTotem = new Setting<>("HotbarTotem", false);

    public Setting<Float> totemHealthThreshold = new Setting<>("TotemHealth", 5f, 36f, 0f, 0.5f);
    public Setting<Boolean> rightClick = new Setting<>("RightClickGap", true).withVisibility(() -> gapple.getValue());
    public Setting<CrystalCheck> crystalCheck = new Setting<>("CrystalCheck", CrystalCheck.DAMAGE);
    public Setting<Float> crystalRange = new Setting<>("CrystalRange", 10f, 15f, 1f, 1f).withVisibility(() -> crystalCheck.getValue() != CrystalCheck.NONE);
    public Setting<Boolean> fallCheck = new Setting<>("FallCheck", true);
    public Setting<Float> fallDist = new Setting<>("FallDist", 15f, 50f, 0f, 1f).withVisibility(() -> fallCheck.getValue());
    public Setting<Boolean> totemOnElytra = new Setting<>("TotemOnElytra", true);
    public Setting<Boolean> extraSafe = new Setting<>("ExtraSafe", false);
    
    public Setting<Boolean> clearAfter = new Setting<>("ClearAfter", true);
    public Setting<Boolean> hard = new Setting<>("Hard", false);
    public Setting<Boolean> notFromHotbar = new Setting<>("NotFromHotbar", true);
    public Setting<Default> defaultItem = new Setting<>("DefaultItem", Default.TOTEM);

    private final Queue<Integer> clickQueue = new LinkedList<>();

    public Offhand() {
        super("Offhand", Category.COMBAT, "OffhandCrystal", "AutoTotem", "OffhandGapple", "AutoOffhand", "SmartOffhand");
    }

    private Timer timer = new Timer();

    private enum CrystalCheck {
        NONE,
        DAMAGE,
        RANGE
    }

    private enum Default {
        TOTEM(Items.TOTEM_OF_UNDYING),
        CRYSTAL(Items.END_CRYSTAL),
        GAPPLE(Items.GOLDEN_APPLE),
        AIR(Items.AIR);

        public Item item;

        Default(Item item) {
            this.item = item;
        }
    }

    @Subscriber
    public void onUpdate(RootEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (!(mc.currentScreen instanceof GuiContainer) && !(mc.currentScreen instanceof GuiInventory)) {
            if (!clickQueue.isEmpty()) {
                if (!timer.hasPassed(delay.getValue() * 100F)) return;
                int slot = clickQueue.poll();
                try {
                    HotbarRefill.moveTimer.reset();
                    timer.reset();
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (!mc.player.inventory.getItemStack().isEmpty()) {
                    int index = 44;
                    while (index >= 9) {
                        if (mc.player.inventoryContainer.getSlot(index).getStack().isEmpty()) {
                            mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player);
                            return;
                        }
                        index--;
                    }
                }

                if (totem.getValue()) {
                    if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= totemHealthThreshold.getValue() || (totemOnElytra.getValue() && mc.player.isElytraFlying()) || (fallCheck.getValue() && mc.player.fallDistance >= fallDist.getValue() && !mc.player.isElytraFlying())) {
                        putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                        return;
                    } else if (crystalCheck.getValue() == CrystalCheck.RANGE) {
                        EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream()
                                .filter(e -> (e instanceof EntityEnderCrystal && mc.player.getDistance(e) <= crystalRange.getValue()))
                                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                                .orElse(null);

                        if (crystal != null) {
                            putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                            return;
                        }
                    } else if (crystalCheck.getValue() == CrystalCheck.DAMAGE) {
                        float damage = 0.0f;

                        List<Entity> crystalsInRange = mc.world.loadedEntityList.stream()
                                .filter(e -> e instanceof EntityEnderCrystal)
                                .filter(e -> mc.player.getDistance(e) <= crystalRange.getValue())
                                .collect(Collectors.toList());

                        for (Entity entity : crystalsInRange) {
                            damage += CrystalUtils.calculateDamage((EntityEnderCrystal) entity, mc.player);
                        }

                        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() - damage <= totemHealthThreshold.getValue()) {
                            putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                            return;
                        }
                    }

                    if (extraSafe.getValue()) {
                        if (crystalCheck()) {
                            putItemIntoOffhand(Items.TOTEM_OF_UNDYING);
                            return;
                        }
                    }
                }

                if (gapple.getValue()
                        && isSword(mc.player.getHeldItemMainhand().getItem())) {

                    if (rightClick.getValue()
                            && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
                        if (clearAfter.getValue()) {
                            putItemIntoOffhand(defaultItem.getValue().item);
                        }
                        return;
                    }

                    putItemIntoOffhand(Items.GOLDEN_APPLE);
                    return;
                }

                if (crystal.getValue()) {
                    if (ModuleManager.getModuleByClass(AutoCrystal.class).isEnabled()) {
                        putItemIntoOffhand(Items.END_CRYSTAL);
                        return;
                    } else if (clearAfter.getValue()) {
                        putItemIntoOffhand(defaultItem.getValue().item);
                        return;
                    }
                }

                if (hard.getValue()) {
                    putItemIntoOffhand(defaultItem.getValue().item);
                }
            }
        }
    }

    private boolean isSword(Item item) {
        return item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD || item == Items.GOLDEN_SWORD || item == Items.STONE_SWORD || item == Items.WOODEN_SWORD;
    }

    private int findItemSlot(Item item) {
        int itemSlot = -1;
        for (int i = notFromHotbar.getValue() ? 9 : 0; i < 36; i++) {

            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack != null && stack.getItem() == item) {
                itemSlot = i;
                break;
            }

        }
        return itemSlot;
    }

    private void putItemIntoOffhand(Item item) {
        if (mc.player.getHeldItemOffhand().getItem() == item) return;
        int slot = findItemSlot(item);
        if (hotbarTotem.getValue() && item == Items.TOTEM_OF_UNDYING) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.mainInventory.get(i);
                if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    if (mc.player.inventory.currentItem != i) {
                        mc.player.inventory.currentItem = i;
                    }
                    return;
                }
            }
        }
        if (slot != -1) {
            if (delay.getValue() > 0F) {
                if (timer.hasPassed(delay.getValue() * 100F)) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
                    timer.reset();
                } else {
                    clickQueue.add(slot < 9 ? slot + 36 : slot);
                }

                clickQueue.add(45);
                clickQueue.add(slot < 9 ? slot + 36 : slot);
            } else {
                timer.reset();
                HotbarRefill.moveTimer.reset();
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
                try {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
            }
        }
    }

    private boolean crystalCheck() {
        float cumDmg = 0;
        ArrayList<Float> damageValues = new ArrayList<>();
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(1, 0, 0)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(-1, 0, 0)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(0, 0, 1)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition().add(0, 0, -1)));
        damageValues.add(calculateDamageAABB(mc.player.getPosition()));
        for (float damage : damageValues) {
            cumDmg += damage;
            if ((((mc.player.getHealth() + mc.player.getAbsorptionAmount())) - damage) <= totemHealthThreshold.getValue()) {
                return true;
            }
        }

        if ((((mc.player.getHealth() + mc.player.getAbsorptionAmount())) - cumDmg) <= totemHealthThreshold.getValue()) {
            return true;
        }

        return false;
    }

    private float calculateDamageAABB(BlockPos pos){
        List<Entity> crystalsInAABB =  mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream()
                .filter(e -> e instanceof EntityEnderCrystal)
                .collect(Collectors.toList());
        float totalDamage = 0;
        for (Entity crystal : crystalsInAABB) {
            totalDamage += CrystalUtils.calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player);
        }
        return totalDamage;
    }
}
