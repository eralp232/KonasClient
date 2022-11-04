package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerUpdateEvent;
import me.darki.konas.mixin.mixins.IGuiBrewingStand;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AutoBrew extends Module {
    private static Setting<PotionMode> potionType = new Setting<>("Type", PotionMode.StrengthII);
    private static Setting<Modifier> modifier = new Setting<>("Mod", Modifier.NONE);

    private static Setting<Boolean> autoOpen = new Setting<>("AutoOpen", true);
    private static Setting<Boolean> autoClose = new Setting<>("AutoClose", false);
    private static Setting<Boolean> autoPlace = new Setting<>("AutoPlace", false);

    private enum Modifier {
        NONE,
        SPLASH,
        LINGERING
    }

    private enum PotionMode {
        Swiftness(Items.NETHER_WART, Items.SUGAR),
        SwiftnessLong(Items.NETHER_WART, Items.SUGAR, Items.REDSTONE),
        SwiftnessII(Items.NETHER_WART, Items.SUGAR, Items.GLOWSTONE_DUST),

        Slowness(Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE),
        SlownessLong(Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE),
        SlownessII(Items.NETHER_WART, Items.SUGAR, Items.FERMENTED_SPIDER_EYE, Items.GLOWSTONE_DUST),

        JumpBoost(Items.NETHER_WART, Items.RABBIT_FOOT),
        JumpBoostLong(Items.NETHER_WART, Items.RABBIT_FOOT, Items.REDSTONE),
        JumpBoostII(Items.NETHER_WART, Items.RABBIT_FOOT, Items.GLOWSTONE_DUST),

        Strength(Items.NETHER_WART, Items.BLAZE_POWDER),
        StrengthLong(Items.NETHER_WART, Items.BLAZE_POWDER, Items.REDSTONE),
        StrengthII(Items.NETHER_WART, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST),

        Healing(Items.NETHER_WART, Items.SPECKLED_MELON),
        HealingII(Items.NETHER_WART, Items.SPECKLED_MELON, Items.GLOWSTONE_DUST),

        Harming(Items.NETHER_WART, Items.SPECKLED_MELON, Items.FERMENTED_SPIDER_EYE),
        HarmingII(Items.NETHER_WART, Items.SPECKLED_MELON, Items.FERMENTED_SPIDER_EYE, Items.GLOWSTONE_DUST),

        Poison(Items.NETHER_WART, Items.SPIDER_EYE),
        PoisonLong(Items.NETHER_WART, Items.SPIDER_EYE, Items.REDSTONE),
        PoisonII(Items.NETHER_WART, Items.SPIDER_EYE, Items.GLOWSTONE_DUST),

        Regeneration(Items.NETHER_WART, Items.GHAST_TEAR),
        RegenerationLong(Items.NETHER_WART, Items.GHAST_TEAR, Items.REDSTONE),
        RegenerationII(Items.NETHER_WART, Items.GHAST_TEAR, Items.GLOWSTONE_DUST),

        FireResistance(Items.NETHER_WART, Items.MAGMA_CREAM),
        FireResistanceLong(Items.NETHER_WART, Items.MAGMA_CREAM, Items.REDSTONE),

        NightVision(Items.NETHER_WART, Items.GOLDEN_CARROT),
        NightVisionLong(Items.NETHER_WART, Items.GOLDEN_CARROT, Items.REDSTONE),

        Invisibility(Items.NETHER_WART, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE),
        InvisibilityLong(Items.NETHER_WART, Items.GOLDEN_CARROT, Items.FERMENTED_SPIDER_EYE, Items.REDSTONE),

        Weakness(Items.FERMENTED_SPIDER_EYE),
        WeaknessLong(Items.FERMENTED_SPIDER_EYE, Items.REDSTONE);

        public final Item[] ingredients;

        PotionMode(Item... ingredients) {
            this.ingredients = ingredients;
        }
    }

    public AutoBrew() {
        super("AutoBrew", Category.MISC);
    }

    private int ingredientIndex;
    private boolean first;
    private int counter;

    private Timer openTimer = new Timer();
    private boolean opened = false;
    private boolean placed = false;

    public void onEnable() {
        first = false;
        opened = false;
        placed = false;
    }

    public void onDisable() {
        if (mc.player != null && mc.world != null) {
            if (autoClose.getValue() && mc.currentScreen instanceof GuiBrewingStand) {
                mc.player.closeScreen();
            }
        }
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player == null && mc.world == null) {
            toggle();
            return;
        }

        if (mc.currentScreen instanceof GuiBrewingStand && mc.player.openContainer instanceof ContainerBrewingStand) {
            counter++;

            if (!first) {
                first = true;

                ingredientIndex = -2;
                counter = 0;
            }

            GuiBrewingStand guiBrewingStand = (GuiBrewingStand) mc.currentScreen;

            if (((IGuiBrewingStand) guiBrewingStand).getTileBrewingStand().getField(0) != 0 || counter < 5) return;

            if (ingredientIndex == -2) {
                for (int i = 0; i < 3; i++) {
                    mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                    if (!((IGuiBrewingStand) guiBrewingStand).getTileBrewingStand().getStackInSlot(i).isEmpty()) {
                        toggle();
                        return;
                    }
                }

                ingredientIndex++;
                counter = 0;
            } else if (ingredientIndex == -1) {
                for (int i = 0; i < 3; i++) {
                    int slot = -1;

                    for (int slotIndex = 5; slotIndex < mc.player.openContainer.getInventory().size(); slotIndex++) {
                        if (mc.player.openContainer.getInventory().get(slotIndex).getItem() instanceof ItemPotion) {
                            if (PotionUtils.getPotionFromItem(mc.player.openContainer.getInventory().get(slotIndex)).getNamePrefixed("").equalsIgnoreCase("water")) {
                                slot = slotIndex;
                                break;
                            }
                        }
                    }

                    if (slot == -1) {
                        Logger.sendChatMessage("No water bottles found!");
                        toggle();
                        return;
                    }

                    mc.playerController.windowClick(mc.player.openContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, mc.player);
                }

                ingredientIndex++;
                counter = 0;
            } else if (ingredientIndex < potionType.getValue().ingredients.length) {
                if (mc.player.openContainer.getInventory().get(4).isEmpty()) {
                    int slot = -1;

                    for (int slotIndex = 5; slotIndex < mc.player.openContainer.getInventory().size(); slotIndex++) {
                        if (mc.player.openContainer.getInventory().get(slotIndex).getItem() == Items.BLAZE_POWDER) {
                            slot = slotIndex;
                            break;
                        }
                    }

                    if (slot == -1) {
                        Logger.sendChatMessage("No blaze powder found!");
                        toggle();
                        return;
                    }

                    mc.playerController.windowClick(mc.player.openContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.openContainer.windowId, 4, 0, ClickType.PICKUP, mc.player);

                    counter = 0;
                    return;
                }

                Item ingredient = potionType.getValue().ingredients[ingredientIndex];

                int slot = -69;

                for (int slotIndex = 5; slotIndex < mc.player.openContainer.getInventory().size(); slotIndex++) {
                    if (mc.player.openContainer.getInventory().get(slotIndex).getItem().equals(ingredient)) {
                        slot = slotIndex;
                        break;
                    }
                }

                if (slot == -69) {
                    Logger.sendChatMessage("You don't have ingredients left");
                    toggle();
                    return;
                }

                mc.playerController.windowClick(mc.player.openContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.openContainer.windowId, 3, 1, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.openContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);

                ingredientIndex++;
                counter = 0;
            } else if (ingredientIndex == potionType.getValue().ingredients.length) {
                if (modifier.getValue() != Modifier.NONE) {
                    Item item;

                    if (modifier.getValue() == Modifier.SPLASH) item = Items.GUNPOWDER;
                    else item = Items.DRAGON_BREATH;

                    int slot = -1;

                    for (int slotIndex = 5; slotIndex < mc.player.openContainer.getInventory().size(); slotIndex++) {
                        if (mc.player.openContainer.getInventory().get(slotIndex).getItem() == item) {
                            slot = slotIndex;
                            break;
                        }
                    }

                    if (slot == -1) {
                        Logger.sendChatMessage("You don't have your modifier");
                        toggle();
                        return;
                    }

                    mc.playerController.windowClick(mc.player.openContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.openContainer.windowId, 3, 1, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.openContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                }

                ingredientIndex++;
                counter = 0;
            } else {
                ingredientIndex = -1;
                counter = 0;
            }
        } else {
            first = false;

            if (autoOpen.getValue() && !opened) {
                if (openTimer.hasPassed(150)) {
                    openTimer.reset();

                    List<BlockPos> blocks = BlockUtils.getBlocksInSphere(mc.player.getPosition(), 4);

                    BlockPos brewingStandPos = blocks.stream().filter(pos -> mc.world.getBlockState(pos).getBlock() instanceof BlockBrewingStand)
                            .min(Comparator.comparing(pos -> mc.player.getDistanceSq(pos)))
                            .orElse(null);

                    if (brewingStandPos != null) {
                        PlayerUtils.faceVectorPacketInstant(new Vec3d(brewingStandPos.getX(), brewingStandPos.getY() + 0.5, brewingStandPos.getZ()));
                        mc.playerController.processRightClickBlock(mc.player, mc.world, brewingStandPos, EnumFacing.UP, new Vec3d(brewingStandPos.getX(), brewingStandPos.getY() + 0.5, brewingStandPos.getZ()), EnumHand.MAIN_HAND);
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        opened = true;
                    } else if (autoPlace.getValue() && !placed) {
                        int slot = getStandSlot();

                        if (slot == -1) {
                            Logger.sendChatMessage("You don't have a brewing stand");
                            toggle();
                            return;
                        }

                        for (BlockPos pos : blocks) {
                            Optional<BlockUtils.ClickLocation> posCL = BlockUtils.generateClickLocation(pos);

                            if (posCL.isPresent()) {
                                mc.player.inventory.currentItem = slot;
                                mc.playerController.updateController();

                                BlockPos currentPos = posCL.get().neighbour;
                                EnumFacing currentFace = posCL.get().opposite;

                                double[] yawPitch = BlockUtils.calculateLookAt(currentPos.getX(), currentPos.getY(), currentPos.getZ(), currentFace, mc.player);
                                mc.player.connection.sendPacket(new CPacketPlayer.Rotation((float) yawPitch[0],
                                        (float) yawPitch[1], mc.player.onGround));

                                boolean isSprinting = mc.player.isSprinting();
                                boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(currentPos);

                                if (isSprinting) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                                }

                                if (shouldSneak) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                                }

                                // Block utils has cringe right click method
                                mc.playerController.processRightClickBlock(mc.player, mc.world, currentPos, currentFace,
                                        new Vec3d(currentPos)
                                                .add(0.5, 0.5, 0.5)
                                                .add(new Vec3d(currentFace.getDirectionVec()).scale(0.5)),
                                        EnumHand.MAIN_HAND);
                                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                                if (shouldSneak) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                                }

                                if (isSprinting) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                                }

                                placed = true;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private int getStandSlot() {
        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!stack.isEmpty() && stack.getItem() == Items.BREWING_STAND) {
            return mc.player.inventory.currentItem;
        } else {
            for (int i = 0; i < 9; ++i) {
                stack = mc.player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() == Items.BREWING_STAND) {
                    return i;
                }
            }
        }
        return -1;
    }
}
