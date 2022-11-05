package me.darki.konas.util.render.shader;

import me.darki.konas.mixin.mixins.IEntityRenderer;
import me.darki.konas.module.modules.render.ESP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.EXTFramebufferObject;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class ESPShader extends ShaderProgram {

    private int diffuseSamperUniformID, texelSizeUniformID, colorUniformID, fillUniformID, sampleRadiusUniformID, renderOutlineID, outlineFadeID;

    public ESPShader(Framebuffer fbo) {
        super(fbo);

        diffuseSamperUniformID = getUniformLocation("DiffuseSamper");
        texelSizeUniformID = getUniformLocation("TexelSize");
        colorUniformID = getUniformLocation("Color");
        fillUniformID = getUniformLocation("Fill");
        sampleRadiusUniformID = getUniformLocation("SampleRadius");
        renderOutlineID = getUniformLocation("RenderOutline");
        outlineFadeID = getUniformLocation("OutlineFade");
    }

    @Override
    public ShaderProgram update() {
        if (this.fboID == -1 || this.renderBufferID == -1 || shaderProgramID == -1) {
            throw new RuntimeException("Invalid IDs!");
        }
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, this.fboID);

        int var9 = Math.max(Minecraft.getDebugFPS(), 30);

        glClear(16640);

        ((IEntityRenderer) mc.entityRenderer).iUpdateFogColor((float) (((IEntityRenderer) mc.entityRenderer).getRenderEndNanoTime() + 1000000000 / var9));

        ARBShaderObjects.glUseProgramObjectARB(shaderProgramID);
        ARBShaderObjects.glUniform1iARB(diffuseSamperUniformID, 0);

        glActiveTexture(GL_TEXTURE0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, this.targetTextureID);

        FloatBuffer texelSizeBuffer = BufferUtils.createFloatBuffer(2);
        texelSizeBuffer.position(0);
        texelSizeBuffer.put(1.0F / this.textureWidth * (ESP.width.getValue() / 5F));
        texelSizeBuffer.put(1.0F / this.textureHeight * (ESP.width.getValue() / 5F));
        texelSizeBuffer.flip();
        ARBShaderObjects.glUniform2ARB(texelSizeUniformID, texelSizeBuffer);

        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(4);
        colorBuffer.position(0);
        colorBuffer.put(ESP.shaderColor.getValue().getRed() / 255F);
        colorBuffer.put(ESP.shaderColor.getValue().getGreen() / 255F);
        colorBuffer.put(ESP.shaderColor.getValue().getBlue() / 255F);
        colorBuffer.put(ESP.shaderColor.getValue().getAlpha() / 255F);
        colorBuffer.flip();
        ARBShaderObjects.glUniform4ARB(colorUniformID, colorBuffer);

        FloatBuffer fillBuffer = BufferUtils.createFloatBuffer(4);
        fillBuffer.position(0);
        if (ESP.shaderFill.getValue()) {
            fillBuffer.put(ESP.shaderFillColor.getValue().getRed() / 255F);
            fillBuffer.put(ESP.shaderFillColor.getValue().getGreen() / 255F);
            fillBuffer.put(ESP.shaderFillColor.getValue().getBlue() / 255F);
            fillBuffer.put(ESP.shaderFillColor.getValue().getAlpha() / 255F);
        } else {
            fillBuffer.put(0F);
            fillBuffer.put(0F);
            fillBuffer.put(0F);
            fillBuffer.put(0F);
        }
        fillBuffer.flip();
        ARBShaderObjects.glUniform4ARB(fillUniformID, fillBuffer);

        IntBuffer sampleRadiusBuffer = BufferUtils.createIntBuffer(1);
        sampleRadiusBuffer.position(0);
        sampleRadiusBuffer.put(4);
        sampleRadiusBuffer.flip();
        ARBShaderObjects.glUniform1ARB(sampleRadiusUniformID, sampleRadiusBuffer);

        IntBuffer renderOutlineBuffer = BufferUtils.createIntBuffer(1);
        renderOutlineBuffer.position(0);
        renderOutlineBuffer.put(ESP.doShaderOutline.getValue() ? 1 : 0);
        renderOutlineBuffer.flip();
        ARBShaderObjects.glUniform1ARB(renderOutlineID, renderOutlineBuffer);

        IntBuffer fadeBuffer = BufferUtils.createIntBuffer(1);
        fadeBuffer.position(0);
        fadeBuffer.put(ESP.doShaderFade.getValue() ? 1 : 0);
        fadeBuffer.flip();
        ARBShaderObjects.glUniform1ARB(outlineFadeID, fadeBuffer);

        this.draw();

        ARBShaderObjects.glUseProgramObjectARB(0);
        return this;
    }
}

