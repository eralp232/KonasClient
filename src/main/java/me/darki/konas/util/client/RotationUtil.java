package me.darki.konas.util.client;

import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;

public class RotationUtil {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static void update(float yaw, float pitch) {
        boolean flag = mc.player.isSprinting();

        if (flag != ((IEntityPlayerSP) mc.player).getServerSprintState()) {
            if (flag) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            ((IEntityPlayerSP) mc.player).setServerSprintState(flag);
        }

        boolean flag1 = mc.player.isSneaking();

        if (flag1 != ((IEntityPlayerSP) mc.player).getServerSneakState()) {
            if (flag1) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            ((IEntityPlayerSP) mc.player).setServerSneakState(flag1);
        }

        if (mc.player == mc.getRenderViewEntity()) {
            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double dX = mc.player.posX - ((IEntityPlayerSP) mc.player).getLastReportedPosX();
            double dY = axisalignedbb.minY - ((IEntityPlayerSP) mc.player).getLastReportedPosY();
            double dZ = mc.player.posZ - ((IEntityPlayerSP) mc.player).getLastReportedPosZ();
            double dYaw = (yaw - ((IEntityPlayerSP) mc.player).getLastReportedYaw());
            double dPitch = (pitch - ((IEntityPlayerSP) mc.player).getLastReportedPitch());
            ((IEntityPlayerSP) mc.player).setPositionUpdateTicks(((IEntityPlayerSP) mc.player).getPositionUpdateTicks() + 1);
            boolean positionChanged = dX * dX + dY * dY + dZ * dZ > 9.0E-4D || ((IEntityPlayerSP) mc.player).getPositionUpdateTicks() >= 20;
            boolean rotationChanged = dYaw != 0.0D || dPitch != 0.0D;

            if (mc.player.isRiding()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, yaw, pitch, mc.player.onGround));
                positionChanged = false;
            } else if (positionChanged && rotationChanged) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, yaw, pitch, mc.player.onGround));
            } else if (positionChanged) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            } else if (rotationChanged) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
            } else if (((IEntityPlayerSP) mc.player).getPrevOnGround() != mc.player.onGround) {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (positionChanged) {
                ((IEntityPlayerSP) mc.player).setLastReportedPosX(mc.player.posX);
                ((IEntityPlayerSP) mc.player).setLastReportedPosY(axisalignedbb.minY);
                ((IEntityPlayerSP) mc.player).setLastReportedPosZ(mc.player.posZ);
                ((IEntityPlayerSP) mc.player).setPositionUpdateTicks(0);
            }

            if (rotationChanged) {
                ((IEntityPlayerSP) mc.player).setLastReportedYaw(yaw);
                ((IEntityPlayerSP) mc.player).setLastReportedPitch(pitch);
            }

            ((IEntityPlayerSP) mc.player).setPrevOnGround(mc.player.onGround);
            ((IEntityPlayerSP) mc.player).setAutoJumpEnabled(mc.gameSettings.autoJump);
        }
    }
}
