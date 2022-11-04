package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import io.netty.util.internal.ConcurrentSet;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.RenderUtil;
import me.darki.konas.util.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Set;

public class NewChunks extends Module {

    private ICamera frustum = new Frustum();

    private Set<ChunkPos> chunks = new ConcurrentSet<>();

    private Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(214f / 255f, 86f / 255f, 147f / 255f, 100f / 255f).hashCode(), false));

    public NewChunks() {
        super("NewChunks", Category.RENDER);
    }

    @Subscriber
    public void onReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketChunkData) {
            final SPacketChunkData packet = (SPacketChunkData) event.getPacket();

            //TODO make it find the opposite array of chunks, because packet.isFullChunk() somehow is flagged for everything
            if (packet.isFullChunk()) return;

            final ChunkPos newChunk = new ChunkPos(packet.getChunkX(), packet.getChunkZ());
            this.chunks.add(newChunk);
        }
    }

    @Subscriber
    public void onRender(Render3DEvent event) {
        if (mc.getRenderViewEntity() == null) return;
        this.frustum.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        GlStateManager.pushMatrix();
        RenderUtil.beginRender();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.glLineWidth(2f);

        for (ChunkPos chunk : this.chunks) {
            final AxisAlignedBB chunkBox = new AxisAlignedBB(chunk.getXStart(), 0, chunk.getZStart(), chunk.getXEnd(), 0, chunk.getZEnd());


            GlStateManager.pushMatrix();
            if (this.frustum.isBoundingBoxInFrustum(chunkBox)) {
                double x = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * (double) event.getPartialTicks();
                double y = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double) event.getPartialTicks();
                double z = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * (double) event.getPartialTicks();
                RenderUtils.drawBox(chunkBox.offset(-x, -y, -z), GL11.GL_LINE_STRIP, color.getValue().getColor());
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.glLineWidth(1f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        RenderUtil.endRender();
        GlStateManager.popMatrix();
    }

    @Override
    public void onEnable() {
        chunks.clear();
    }

}
