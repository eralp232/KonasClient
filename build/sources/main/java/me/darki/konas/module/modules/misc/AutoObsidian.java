package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.combat.AutoCrystal;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class AutoObsidian extends Module {
    private static final Setting<Integer> range = new Setting<>("Range", 2, 3, 1, 1);

    private Timer timer = new Timer();
    private Timer breakTimer = new Timer();
    private InteractionUtil.Placement placement = null;

    public AutoObsidian() {
        super("AutoObsidian", "Automatically placed EChests and mines them", Category.MISC);
    }

    public void onEnable() {
        placement = null;
        breakTimer.setTime(0);
    }

    @Subscriber(priority = 5)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        placement = null;
        if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;

        BlockPos closestEChest = AutoCrystal.getSphere(new BlockPos(mc.player), range.getValue(), range.getValue(), false, true, 0).stream()
                .filter(pos -> mc.world.getBlockState(pos).getBlock() instanceof BlockEnderChest)
                .min(Comparator.comparing(pos -> mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)))
                .orElse(null);

        if (closestEChest != null) {
            if (breakTimer.hasPassed(4000)) {
                boolean holdingPickaxe = mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_PICKAXE;

                if (!holdingPickaxe) {
                    for (int i = 0; i < 9; ++i) {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);

                        if (stack.isEmpty()) {
                            continue;
                        }

                        if (stack.getItem() == Items.DIAMOND_PICKAXE) {
                            holdingPickaxe = true;
                            mc.player.inventory.currentItem = i;
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(i));
                            break;
                        }
                    }
                }

                if (!holdingPickaxe) {
                    return;
                }

                EnumFacing facing = mc.player.getHorizontalFacing().getOpposite();

                KonasGlobals.INSTANCE.rotationManager.lookAtVec3d(new Vec3d(closestEChest.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5,
                        closestEChest.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5,
                        closestEChest.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5));

                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, closestEChest, facing));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, closestEChest, facing));
                breakTimer.reset();
            }
        } else if (timer.hasPassed(350)) {
            timer.reset();
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
                final ItemBlock block = (ItemBlock) mc.player.getHeldItemMainhand().getItem();
                if (block.getBlock() != Blocks.ENDER_CHEST) {
                    if (!changeToEChest()) return;
                }
            } else {
                if (!changeToEChest()) return;
            }

            for (BlockPos pos : AutoCrystal.getSphere(new BlockPos(mc.player), range.getValue(), range.getValue(), false, true, 0)) {
                InteractionUtil.Placement cPlacement = InteractionUtil.preparePlacement(pos, true);
                if (cPlacement != null) {
                    placement = cPlacement;
                }
            }
        }
    }

    @Subscriber(priority = 5)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (placement != null) {
            InteractionUtil.placeBlockSafely(placement, EnumHand.MAIN_HAND, false);
            breakTimer.setTime(0);
        }
    }

    private boolean changeToEChest() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            final ItemBlock block = (ItemBlock) stack.getItem();
            if (block.getBlock() == Blocks.ENDER_CHEST) {
                mc.player.inventory.currentItem = i;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(i));
                return true;
            }
        }

        return false;
    }

}
