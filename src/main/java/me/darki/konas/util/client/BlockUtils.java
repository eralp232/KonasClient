package me.darki.konas.util.client;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    //Thanks to mr 3arth for this method
    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            float f = (float) (vec.x - (double) pos.getX());
            float f1 = (float) (vec.y - (double) pos.getY());
            float f2 = (float) (vec.z - (double) pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
            mc.player.connection.sendPacket(new CPacketAnimation(hand));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
            mc.player.swingArm(hand);
        }
        // ((IMinecraft) mc).setRightClickDelayTimer(4); //? // ?
    }

    public static Vec3d blockPosToVec(BlockPos pos) {
        return new Vec3d(pos);
    }

    public static BlockPos vecToBlockPos(Vec3d vec) {
        return new BlockPos(vec);
    }

    public static List<BlockPos> getBlocksInSphere(BlockPos center, int radius) {
        List<BlockPos> blocksInSphere = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int y = centerY - radius; y < centerY + radius; y++) {
                    double dist = (centerX - x) * (centerX - x) + (centerZ - z) * (centerZ - z) + (centerY - y) * (centerY - y);
                    if (dist < radius * radius) {
                        blocksInSphere.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return blocksInSphere;
    }

    // Checks if a blockpos has neighbours. If it has none, it's impossible to place a block there.
    public static boolean hasNeighbours(BlockPos blockPos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbour = blockPos.offset(facing);
            if (!mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) { // We have to make sure it's a block we can place against without replacing it
                return true;
            }
        }
        return false;
    }

    public static Optional<ClickLocation> generateClickLocation(BlockPos pos) {
        return generateClickLocation(pos, false, false);
    }

    public static Optional<ClickLocation> generateClickLocation(BlockPos pos, boolean ignoreEntities) {
        return generateClickLocation(pos, ignoreEntities, false);
    }

    public static Optional<ClickLocation> generateClickLocation(BlockPos pos, boolean ignoreEntities, boolean noPistons) {
        return generateClickLocation(pos, ignoreEntities, false, false);
    }

    public static Optional<ClickLocation> generateClickLocation(BlockPos pos, boolean ignoreEntities, boolean noPistons, boolean onlyCrystals) {
        // check if block is already placed
        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return Optional.empty();
        }

        // check if entity blocks placing
        if (!ignoreEntities) {
            for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
                if (onlyCrystals && entity instanceof EntityEnderCrystal) continue;
                if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityArrow)) {
                    return Optional.empty();
                }
            }
        }

        // check if we have a block adjacent to blockpos to click at
        EnumFacing side = null;
        for (EnumFacing blockSide : EnumFacing.values()) {
            BlockPos sidePos = pos.offset(blockSide);
            if (noPistons) {
                if (mc.world.getBlockState(sidePos).getBlock() == Blocks.PISTON) continue;
            }
            if (!mc.world.getBlockState(sidePos).getBlock().canCollideCheck(mc.world.getBlockState(sidePos), false)) {
                continue;
            }
            IBlockState blockState = mc.world.getBlockState(sidePos);
            if (!blockState.getMaterial().isReplaceable()) {
                side = blockSide;
                break;
            }
        }
        if (side == null) {
            return Optional.empty();
        }

        // check if neighbor can be right clicked
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
            return Optional.empty();
        }

        return Optional.of(new ClickLocation(neighbour, opposite));
    }

    public static class ClickLocation {
        public final BlockPos neighbour;
        public final EnumFacing opposite;

        public ClickLocation(BlockPos neighbour, EnumFacing opposite) {
            this.neighbour = neighbour;
            this.opposite = opposite;
        }
    }

    public static double[] calculateLookAt(BlockPos pos, EnumFacing facing, EntityPlayer me) {
        return PlayerUtils.calculateLookAt(pos.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5, pos.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5, pos.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5, me);
    }

    public static double[] calculateLookAt(double x, double y, double z, EnumFacing facing, EntityPlayer me) {
        return PlayerUtils.calculateLookAt(x + 0.5 + facing.getDirectionVec().getX() * 0.5, y + 0.5 + facing.getDirectionVec().getY() * 0.5, z + 0.5 + facing.getDirectionVec().getZ() * 0.5, me);
    }

    public static boolean shouldSneakWhileRightClicking(BlockPos blockPos) {
        Block block = mc.world.getBlockState(blockPos).getBlock();
        TileEntity tileEntity = null;
        for (TileEntity tE : mc.world.loadedTileEntityList) {
            if (!tE.getPos().equals(blockPos)) continue;
            tileEntity = tE;
            break;
        }
        return tileEntity != null || block instanceof BlockBed || block instanceof BlockContainer || block instanceof BlockDoor || block instanceof BlockTrapDoor || block instanceof BlockFenceGate || block instanceof BlockButton || block instanceof BlockAnvil || block instanceof BlockWorkbench || block instanceof BlockCake || block instanceof BlockRedstoneDiode;
    }

    public static boolean validObi(BlockPos pos) {
        return !validBedrock(pos)
                && (mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK)
                && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
    }

    public static boolean validBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
    }

    public static BlockPos validTwoBlockObiXZ(BlockPos pos) {
        if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.east()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null ? new BlockPos(1, 0, 0) : null;
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.south()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null ? new BlockPos(0, 0, 1) : null;
        }
        return null;
    }

    public static BlockPos validTwoBlockBedrockXZ(BlockPos pos) {
        if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.east()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up(2)).getMaterial() == Material.AIR
        ) {
            return new BlockPos(1, 0, 0);
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.south()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up(2)).getMaterial() == Material.AIR
        ) {
            return new BlockPos(0, 0, 1);
        }
        return null;
    }

    public static boolean isHole(BlockPos pos) {
        return validObi(pos) || validBedrock(pos);
    }

}