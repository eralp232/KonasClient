package me.darki.konas.util.interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
    private Minecraft mc = Minecraft.getMinecraft();

    private float yaw;
    private float pitch;

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    private boolean rotationsSet = false;

    public boolean isRotationsSet() {
        return rotationsSet;
    }

    public void reset() {
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
        rotationsSet = false;
    }

    public void setRotations(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        rotationsSet = true;
    }

    public boolean safeSetRotations(float yaw, float pitch) {
        if (rotationsSet) {
            return false;
        }

        setRotations(yaw, pitch);
        return true;
    }

    public void lookAtPos(BlockPos pos) {
        float[] angle = calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((double)((float)pos.getX() + 0.5f), (double)((float)pos.getY() + 0.5f), (double)((float)pos.getZ() + 0.5f)));
        setRotations(angle[0], angle[1]);
    }

    public void lookAtVec3d(Vec3d vec3d) {
        float[] angle = calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(vec3d.x, vec3d.y, vec3d.z));
        setRotations(angle[0], angle[1]);
    }

    public void lookAtXYZ(double x, double y, double z) {
        Vec3d vec3d = new Vec3d(x, y, z);
        lookAtVec3d(vec3d);
    }

    public static float[] calculateAngle(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((double)(difX * difX + difZ * difZ));
        float yD = (float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0));
        float pD = (float)MathHelper.wrapDegrees((double)Math.toDegrees(Math.atan2(difY, dist)));
        if (pD > 90F) {
            pD = 90F;
        } else if (pD < -90F) {
            pD = -90F;
        }
        return new float[]{yD, pD};
    }
}
