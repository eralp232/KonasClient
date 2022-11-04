package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.combat.AutoCrystal;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.interaction.InteractionUtil;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

// original author hub

public class AutoWither extends Module {
    public Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public Setting<Boolean> triggerable = new Setting<>("Triggerable", true);
    private static final Setting<Integer> range = new Setting<>("Range", 4, 10, 2, 1);
    private static final Setting<Integer> actionShift = new Setting<>("ActionShift", 1, 5, 1, 1);
    private static final Setting<Integer> delay = new Setting<>("ActionInterval", 15, 30, 5, 1);

    private BlockPos placeTarget;
    private boolean rotationPlaceableX;
    private boolean rotationPlaceableZ;

    private int bodySlot;
    private int headSlot;

    private int buildStage;
    private int delayStep;

    private boolean shifting = false;

    public AutoWither() {
        super("AutoWither", "Automatically places and spawns wither", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            toggle();
            return;
        }

        buildStage = 1;
        delayStep = 1;
        shifting = false;
    }

    @Subscriber(priority = 40)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (buildStage == 1) {
            if (shifting) {
                shifting = false;
            } else {

                rotationPlaceableX = false;
                rotationPlaceableZ = false;

                if (!checkBlocksInHotbar()) {
                    toggle();
                    return;
                }

                List<BlockPos> blockPosList = AutoCrystal.getSphere(mc.player.getPosition().down(), range.getValue(), range.getValue(), false, true, 0);

                boolean noPositionInArea = true;

                for (BlockPos pos : blockPosList) {
                    placeTarget = pos.down();
                    if (testWitherStructure()) {
                        noPositionInArea = false;
                        break;
                    }
                }

                if (noPositionInArea) {
                    if (triggerable.getValue()) {
                        toggle();
                    }
                    return;
                }
            }

            if (mc.player.inventory.currentItem != bodySlot) {
                mc.player.inventory.currentItem = bodySlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(bodySlot));
            }

            int actions = 0;

            for (BlockPos pos : BodyParts.bodyBase) {
                if (InteractionUtil.canPlaceBlock(placeTarget.add(pos), false, true)) {
                    InteractionUtil.Placement placement = InteractionUtil.preparePlacement(placeTarget.add(pos), rotate.getValue(), true);
                    if (placement != null) {
                        InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, false);
                        actions++;
                        if (actions >= actionShift.getValue()) {
                            shifting = true;
                            return;
                        }
                    }
                }
            }

            if (rotationPlaceableX) {
                for (BlockPos pos : BodyParts.ArmsX) {
                    if (InteractionUtil.canPlaceBlock(placeTarget.add(pos), false, true)) {
                        InteractionUtil.Placement placement = InteractionUtil.preparePlacement(placeTarget.add(pos), rotate.getValue(), true);
                        if (placement != null) {
                            InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, false);
                            actions++;
                            if (actions >= actionShift.getValue()) {
                                shifting = true;
                                return;
                            }
                        }
                    }
                }
            } else if (rotationPlaceableZ) {
                for (BlockPos pos : BodyParts.ArmsZ) {
                    if (InteractionUtil.canPlaceBlock(placeTarget.add(pos), false, true)) {
                        InteractionUtil.Placement placement = InteractionUtil.preparePlacement(placeTarget.add(pos), rotate.getValue(), true);
                        if (placement != null) {
                            InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, false);
                            actions++;
                            if (actions >= actionShift.getValue()) {
                                shifting = true;
                                return;
                            }
                        }
                    }
                }
            }

            buildStage = 2;

        } else if (buildStage == 2) {

            if (mc.player.inventory.currentItem != headSlot) {
                mc.player.inventory.currentItem = headSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(headSlot));
            }

            int actions = 0;

            if (rotationPlaceableX) {
                for (BlockPos pos : BodyParts.headsX) {
                    if (InteractionUtil.canPlaceBlock(placeTarget.add(pos), false, true)) {
                        InteractionUtil.Placement placement = InteractionUtil.preparePlacement(placeTarget.add(pos), rotate.getValue(), true);
                        if (placement != null) {
                            InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, false);
                            actions++;
                            if (actions >= actionShift.getValue()) return;
                        }
                    }
                }
            } else if (rotationPlaceableZ) {
                for (BlockPos pos : BodyParts.headsZ) {
                    if (InteractionUtil.canPlaceBlock(placeTarget.add(pos), false, true)) {
                        InteractionUtil.Placement placement = InteractionUtil.preparePlacement(placeTarget.add(pos), rotate.getValue(), true);
                        if (placement != null) {
                            InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, false);
                            actions++;
                            if (actions >= actionShift.getValue()) return;
                        }
                    }
                }
            }


            if (triggerable.getValue()) {
                toggle();
            }

            buildStage = 3;

        } else if (buildStage == 3) {

            if (delayStep < delay.getValue()) {
                delayStep++;
            } else {
                delayStep = 1;
                buildStage = 1;

            }
        }
    }

    private static EnumFacing getPlaceableSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {

            BlockPos neighbour = pos.offset(side);

            if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                continue;
            }

            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (!blockState.getMaterial().isReplaceable() && !(blockState.getBlock() instanceof BlockTallGrass) && !(blockState.getBlock() instanceof BlockDeadBush)) {
                return side;
            }

        }

        return null;

    }

    private boolean testWitherStructure() {

        boolean noRotationPlaceable = true;
        rotationPlaceableX = true;
        rotationPlaceableZ = true;
        boolean isShitGrass = false;

        if (mc.world.getBlockState(placeTarget) == null) {
            return false;
        }

        Block block = mc.world.getBlockState(placeTarget).getBlock();
        if ((block instanceof BlockTallGrass) || (block instanceof BlockDeadBush)) {
            isShitGrass = true;
        }

        if (getPlaceableSide(placeTarget.up()) == null) {
            return false;
        }

        for (BlockPos pos : BodyParts.bodyBase) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                noRotationPlaceable = false;
            }
        }

        for (BlockPos pos : BodyParts.ArmsX) {
            if (placingIsBlocked(placeTarget.add(pos)) || placingIsBlocked(placeTarget.add(pos.down()))) {
                rotationPlaceableX = false;
            }
        }

        for (BlockPos pos : BodyParts.ArmsZ) {
            if (placingIsBlocked(placeTarget.add(pos)) || placingIsBlocked(placeTarget.add(pos.down()))) {
                rotationPlaceableZ = false;
            }
        }

        for (BlockPos pos : BodyParts.headsX) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                rotationPlaceableX = false;
            }
        }

        for (BlockPos pos : BodyParts.headsZ) {
            if (placingIsBlocked(placeTarget.add(pos))) {
                rotationPlaceableZ = false;
            }
        }

        return !isShitGrass && noRotationPlaceable && (rotationPlaceableX || rotationPlaceableZ);

    }

    private boolean placingIsBlocked(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir)) {
            return true;
        }

        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                return true;
            }
        }

        return false;
    }


    private boolean checkBlocksInHotbar() {

        headSlot = -1;
        bodySlot = -1;

        for (int i = 0; i < 9; i++) {

            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY) {
                continue;
            }

            if (stack.getItem() == Items.SKULL && stack.getItemDamage() == 1) {
                if (mc.player.inventory.getStackInSlot(i).getCount() >= 3) {
                    headSlot = i;
                }
                continue;
            }

            if (!(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block instanceof BlockSoulSand) {
                if (mc.player.inventory.getStackInSlot(i).getCount() >= 4) {
                    bodySlot = i;
                }
            }
        }

        return (bodySlot != -1 && headSlot != -1);

    }

    private static class BodyParts {

        private static final BlockPos[] bodyBase = {
                new BlockPos(0, 1, 0),
                new BlockPos(0, 2, 0),
        };

        private static final BlockPos[] ArmsX = {
                new BlockPos(-1, 2, 0),
                new BlockPos(1, 2, 0)
        };

        private static final BlockPos[] ArmsZ = {
                new BlockPos(0, 2, -1),
                new BlockPos(0, 2, 1)
        };

        private static final BlockPos[] headsX = {
                new BlockPos(0, 3, 0),
                new BlockPos(-1, 3, 0),
                new BlockPos(1, 3, 0)
        };

        private static final BlockPos[] headsZ = {
                new BlockPos(0, 3, 0),
                new BlockPos(0, 3, -1),
                new BlockPos(0, 3, 1)
        };

        private static final BlockPos[] head = {
                new BlockPos(0, 3, 0)
        };

    }


}
