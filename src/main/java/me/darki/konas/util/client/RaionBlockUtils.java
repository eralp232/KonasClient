package me.darki.konas.util.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RaionBlockUtils {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean place(BlockPos pos) {
        return place(pos, true, true, true);
    }

    public static boolean place(BlockPos pos, boolean sneak, boolean swing, boolean rotate) {
        boolean sneaking = mc.player.isSneaking();
        try {
            Vec3d eyesPos = new Vec3d(
                    mc.player.posX,
                    mc.player.posY + mc.player.getEyeHeight(),
                    mc.player.posZ
            );

            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbor = pos.offset(side);
                EnumFacing side2 = side.getOpposite();

                // check if side is visible (facing away from player)
            /*if(eyesPos.squareDistanceTo(
                new Vec3d(pos).addVector(0.5, 0.5, 0.5)) >= eyesPos
                .squareDistanceTo(
                    new Vec3d(neighbor).addVector(0.5, 0.5, 0.5)))
                continue;*/

                // check if neighbor can be right clicked
                if (mc.world.getBlockState(neighbor).getBlock() == Blocks.AIR || (mc.world.getBlockState(neighbor)
                        .getBlock() instanceof BlockLiquid))
                    continue;

                Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5)
                        .add(new Vec3d(side2.getDirectionVec()).scale(0.5));

                // check if hitVec is within range (4.25 blocks)
                if (eyesPos.squareDistanceTo(hitVec) > 18.0625)
                    continue;

                // place block
                if (rotate) {
                    double diffX = hitVec.x - eyesPos.x;
                    double diffY = hitVec.y - eyesPos.y;
                    double diffZ = hitVec.z - eyesPos.z;

                    double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

                    float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
                    float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

                    float[] rotations = {
                            mc.player.rotationYaw
                                    + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
                            mc.player.rotationPitch +
                                    MathHelper
                                            .wrapDegrees(pitch - mc.player.rotationPitch)
                    };

                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], rotations[1], mc.player.onGround));
                }
                if (sneak) {
                    mc.player.setSneaking(true);
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }
                boolean success = false;
                mc.playerController.updateController();
                if (mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND) != EnumActionResult.FAIL) {
                    if (swing) {
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                    }
                    success = true;
                }
                if (sneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }

                if (success) {
                    return true;
                }
            }
            return false;
        }
        finally {
            mc.player.setSneaking(sneaking);
        }
    }

    public static void place(BlockPos pos, EnumFacing facing) {
        Vec3d eyesPos = new Vec3d(
                mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight(),
                mc.player.posZ
        );

        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();

            // check if side is visible (facing away from player)
            /*if(eyesPos.squareDistanceTo(
                new Vec3d(pos).addVector(0.5, 0.5, 0.5)) >= eyesPos
                .squareDistanceTo(
                    new Vec3d(neighbor).addVector(0.5, 0.5, 0.5)))
                continue;*/

            // check if neighbor can be right clicked
            if (mc.world.getBlockState(neighbor).getBlock() == Blocks.AIR || (mc.world.getBlockState(neighbor)
                    .getBlock() instanceof BlockLiquid))
                continue;

            Vec3d hitVec = new Vec3d(neighbor).add(0.9, 0.9, 0.9)
                    .add(new Vec3d(side2.getDirectionVec()).scale(0.5));

            // check if hitVec is within range (4.25 blocks)
            if (eyesPos.squareDistanceTo(hitVec) > 18.0625)
                continue;

            // place block
            double diffX = hitVec.x - eyesPos.x;
            double diffY = hitVec.y - eyesPos.y;
            double diffZ = hitVec.z - eyesPos.z;

            double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

            float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
            float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

            float[] rotations = {
                    mc.player.rotationYaw
                            + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
                    mc.player.rotationPitch +
                            MathHelper
                                    .wrapDegrees(pitch - mc.player.rotationPitch)
            };

            //final boolean activated = mc.world.getBlockState(neighbor).getBlock().onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);

            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], rotations[1], mc.player.onGround));
            mc.player.setSneaking(true);
            //mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            if (mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, facing, hitVec, EnumHand.MAIN_HAND) != EnumActionResult.FAIL)
                mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.player.setSneaking(false);
            //mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

            return;
        }
    }

    public static boolean canPlace(BlockPos pos, Block block, boolean checkEntity) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos blockPos = pos.offset(facing);
            Block posBlock = mc.world.getBlockState(blockPos).getBlock();
            if (posBlock != Blocks.AIR && !(posBlock instanceof BlockLiquid) && block.canPlaceBlockAt(mc.world, pos)) {
                if (checkEntity) {
                    if (mc.world.checkNoEntityCollision(new AxisAlignedBB(pos))) {
                        return true;
                    }
                }
                else return true;
            }
        }
        return false;
    }
}