package me.darki.konas.mixin.mixins;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface IEntityRenderer {

    @Invoker(value = "setupCameraTransform")
    void iSetupCameraTransform(float partialTicks, int pass);

    @Invoker(value = "orientCamera")
    void iOrientCamera(float partialTicks);

    @Invoker(value = "getFOVModifier")
    float iGetFOVModifier(float partialTicks, boolean useFOVSetting);

    @Invoker(value = "updateFogColor")
    void iUpdateFogColor(float partialTicks);

    @Accessor(value = "lightmapColors")
    int[] getLightmapColors();

    @Accessor(value = "lightmapTexture")
    DynamicTexture getLightmapTexture();

    @Accessor(value = "torchFlickerX")
    float getTorchFlickerX();

    @Accessor(value = "renderEndNanoTime")
    long getRenderEndNanoTime();

    @Accessor(value = "cameraZoom")
    double getCameraZoom();

    @Accessor(value = "cameraZoom")
    void setCameraZoom(double cameraZoom);
}
