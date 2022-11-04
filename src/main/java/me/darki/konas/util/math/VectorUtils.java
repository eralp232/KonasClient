package me.darki.konas.util.math;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

// Credits to ForgeHax [MIT] inspiring this

public class VectorUtils {
    private Minecraft mc = Minecraft.getMinecraft();
    private Matrix4f modelMatrix = new Matrix4f();
    private Matrix4f projectionMatrix = new Matrix4f();
    private ScaledResolution resolution = new ScaledResolution(mc);
    Vec3d camPos = new Vec3d(0.0, 0.0, 0.0);

    @Subscriber
    public void updateMatrix(Render3DEvent event) {
        if (mc.getRenderViewEntity() == null) return;

        Vec3d viewerPos = ActiveRenderInfo.projectViewFromEntity(mc.getRenderViewEntity(), mc.getRenderPartialTicks());
        Vec3d relativeCamPos = ActiveRenderInfo.getCameraPosition();

        loadMatrix(modelMatrix, GL_MODELVIEW_MATRIX);
        loadMatrix(projectionMatrix, GL_PROJECTION_MATRIX);
        camPos = viewerPos.add(relativeCamPos);
        resolution = new ScaledResolution(mc);
    }

    public void loadMatrix(Matrix4f matrix, int glBit) {
        FloatBuffer floatBuffer = GLAllocation.createDirectFloatBuffer(16);
        glGetFloat(glBit, floatBuffer);
        matrix.load(floatBuffer);
    }

    public Vec3d toScaledScreenPos(Vec3d posIn) {
        Vector4f vector4f = getTransformedMatrix(posIn);

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        vector4f.x = width / 2f + (0.5f * vector4f.x * width + 0.5f);
        vector4f.y = height / 2f - (0.5f * vector4f.y * height + 0.5f);
        double posZ = (isVisible(vector4f, width, height)) ? 0.0 : -1.0;

        return new Vec3d(vector4f.x, vector4f.y, posZ);
    }

    public Vec3d toScreenPos(Vec3d posIn) {
        Vector4f vector4f = getTransformedMatrix(posIn);

        int width = mc.displayWidth;
        int height = mc.displayHeight;

        vector4f.x = width / 2f + (0.5f * vector4f.x * width + 0.5f);
        vector4f.y = height / 2f - (0.5f * vector4f.y * height + 0.5f);
        double posZ = (isVisible(vector4f, width, height)) ? 0.0 : -1.0;

        return new Vec3d(vector4f.x, vector4f.y, posZ);
    }

    public Vector4f getTransformedMatrix(Vec3d posIn) {
        Vec3d relativePos = camPos.subtract(posIn);
        Vector4f vector4f = new Vector4f((float) relativePos.x, (float) relativePos.y, (float) relativePos.z, 1f);

        transform(vector4f, modelMatrix);
        transform(vector4f, projectionMatrix);

        if (vector4f.w > 0.0f) {
            vector4f.x *= -100000;
            vector4f.y *= -100000;
        } else {
            float invert = 1f / vector4f.w;
            vector4f.x *= invert;
            vector4f.y *= invert;
        }

        return vector4f;
    }

    public void transform(Vector4f vec, Matrix4f matrix) {
        float x = vec.x;
        float y = vec.y;
        float z = vec.z;
        vec.x = x * matrix.m00 + y * matrix.m10 + z * matrix.m20 + matrix.m30;
        vec.y = x * matrix.m01 + y * matrix.m11 + z * matrix.m21 + matrix.m31;
        vec.z = x * matrix.m02 + y * matrix.m12 + z * matrix.m22 + matrix.m32;
        vec.w = x * matrix.m03 + y * matrix.m13 + z * matrix.m23 + matrix.m33;
    }

    public boolean isVisible(Vector4f pos, int width, int height) {
        return 0.0 <= pos.x && pos.x <= (double) width && 0.0 <= pos.y && pos.y <= (double) height;
    }
}
