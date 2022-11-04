package me.darki.konas.util.client;

import me.darki.konas.mixin.mixins.IEntity;
import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import me.darki.konas.module.modules.render.ESP;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.text.DecimalFormat;

public class PlayerUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static double[] lastLookAt = null;

    public static boolean isPlayerMoving() {
        return mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown();
    }

    public static boolean isPlayerMovingLegit() {
        return mc.gameSettings.keyBindForward.isKeyDown() && !mc.player.isSneaking();
    }

    public static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();

        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));


        float[] rotations = new float[]{
                mc.player.rotationYaw
                        + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
                mc.player.rotationPitch + MathHelper
                        .wrapDegrees(pitch - mc.player.rotationPitch)};

        return rotations;
    }

    private static Vec3d getEyesPos() {
        return new Vec3d(mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight(),
                mc.player.posZ);
    }

    public static Vec3d roundPos(Vec3d vec3d) {
        DecimalFormat f = new DecimalFormat(".##");
        double x = Double.parseDouble(f.format(vec3d.x));
        double y = Double.parseDouble(f.format(vec3d.y));
        double z = Double.parseDouble(f.format(vec3d.z));
        return new Vec3d(x, y, z);
    }

    public static double degreeToRadian(final float degree) { return degree * (Math.PI / 180); } // 1Deg × π/180 = 0.01745Rad

    public static double[] getDirectionFromYaw(final float yaw) {
        final double radian = degreeToRadian(yaw);
        final double x = Math.sin(radian);
        final double z = Math.cos(radian);
        return new double[] {x, z};
    }

    public static double[] directionSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static double[] directionSpeed(final double speed, EntityPlayerSP player) {
        final Minecraft mc = Minecraft.getMinecraft();
        float forward = player.movementInput.moveForward;
        float side = player.movementInput.moveStrafe;
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static boolean isPlayerAboveVoid() {
        boolean aboveVoid = false;
        if (mc.player.posY <= 0.0D) return true;
        for (int i = 1; i < mc.player.posY; i++) {
            BlockPos pos = new BlockPos(mc.player.posX, i, mc.player.posZ);
            if (mc.world.getBlockState(pos).getBlock() instanceof BlockAir) {
                aboveVoid = true;
            } else {
                aboveVoid = false;
                break;
            }

        }
        return aboveVoid;
    }

    public static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = getLegitRotations(vec);

        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], (float) MathHelper.normalizeAngle(((int) rotations[1]), 360), mc.player.onGround));
        ((IEntityPlayerSP) mc.player).setLastReportedYaw(rotations[0]);
        ((IEntityPlayerSP) mc.player).setLastReportedPitch((float) MathHelper.normalizeAngle(((int) rotations[1]), 360));
    }

    public static void setElytraFlying(boolean value) {

        ((IEntity) mc.player).invokeSetFlag(7, value);

    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0D;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt(difX * difX + difZ * difZ);
        return new float[] { (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0D), (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))) };
    }

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;

        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        //to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]{yaw, pitch};
    }

    public static void moveToBlockCenter() {
        double centerX = Math.floor(mc.player.posX) + 0.5;
        double centerZ = Math.floor(mc.player.posZ) + 0.5;
        mc.player.setPosition(centerX, mc.player.posY, centerZ);
        mc.player.connection.sendPacket(new CPacketPlayer.Position(centerX, mc.player.posY, centerZ, mc.player.onGround));
    }

    public static void drawPlayerOnScreen(int x, int y, int scale, float mouseX, float mouseY, EntityPlayer player, boolean yaw, boolean pitch) {
        ESP.hackyFix = true;
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.color(1f, 1f, 1f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, 50.0F);
        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = player.renderYawOffset;
        float f1 = player.rotationYaw;
        float f2 = player.rotationPitch;
        float f3 = player.prevRotationYawHead;
        float f4 = player.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        mouseX = yaw ? player.rotationYaw * -1 : mouseX;
        mouseY = pitch ? player.rotationPitch * -1 : mouseY;
        GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        if (!yaw) {
            player.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
            player.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
            player.rotationYawHead = player.rotationYaw;
            player.prevRotationYawHead = player.rotationYaw;
        }
        if (!pitch) {
            player.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        }
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(player, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        if (!yaw) {
            player.renderYawOffset = f;
            player.rotationYaw = f1;
            player.prevRotationYawHead = f3;
            player.rotationYawHead = f4;
        }
        if (!pitch) {
            player.rotationPitch = f2;
        }
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
        ESP.hackyFix = false;
    }

    public static boolean isKeyDown(int i) {
        if (i != 0 && i < 256)
        {
            return i < 0 ? Mouse.isButtonDown(i + 100) : Keyboard.isKeyDown(i);
        }
        else
        {
            return false;
        }
    }

}
