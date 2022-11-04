package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.mixin.mixins.IEntityRenderer;
import me.darki.konas.mixin.mixins.IPlayerControllerMP;
import me.darki.konas.mixin.mixins.IRenderGlobal;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.BlockRenderUtil;
import me.darki.konas.util.render.TessellatorUtil;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Interactions extends Module {
    public static final Setting<Parent> breaking = new Setting<>("Breaking", new Parent(false));
    private final Setting<BreakRenderMode> bRenderMode = new Setting<>("BRenderMove", BreakRenderMode.GROW).withParent(breaking);

    private final Setting<Float> bRange = new Setting<>("BRange", 15f, 255f, 5f, 5f).withParent(breaking);

    private final Setting<Boolean> bOutline = new Setting<>("BOutline", true).withParent(breaking);
    private final Setting<Boolean> bWireframe = new Setting<>("BWireframe", false).withParent(breaking);
    private final Setting<Float> bWidth = new Setting<>("BWidth", 1.5f, 10f, 1f, 1f).withParent(breaking);
    private final Setting<ColorSetting> bOutlineColor = new Setting<>("BOutlineColor", new ColorSetting(0xFFFF0000)).withParent(breaking);
    private final Setting<ColorSetting> bCrossOutlineColor = new Setting<>("BCrossOutlineColor", new ColorSetting(0xFFFF0000)).withParent(breaking).withVisibility(() -> bRenderMode.getValue() == BreakRenderMode.CROSS);

    private final Setting<Boolean> bFill = new Setting<>("BFill", true).withParent(breaking);
    private final Setting<ColorSetting> bFillColor = new Setting<>("BFillColor", new ColorSetting(0x66FF0000)).withParent(breaking);
    private final Setting<ColorSetting> bCrossFillColor = new Setting<>("BCrossFillColor", new ColorSetting(0x66FF0000)).withParent(breaking).withVisibility(() -> bRenderMode.getValue() == BreakRenderMode.CROSS);

    private final Setting<Boolean> bTracer = new Setting<>("BTracer", false).withParent(breaking);
    private final Setting<ColorSetting> bTracerColor = new Setting<>("BTracerColor", new ColorSetting(0xFFFF0000)).withParent(breaking);

    public static final Setting<Parent> placing = new Setting<>("Placing", new Parent(false));

    private final Setting<Boolean> pOutline = new Setting<>("POutline", true).withParent(placing);
    private final Setting<Boolean> pWireframe = new Setting<>("PWireframe", false).withParent(placing);
    private final Setting<Float> pWidth = new Setting<>("PWidth", 1.5f, 10f, 1f, 1f).withParent(placing);
    private final Setting<ColorSetting> pOutlineColor = new Setting<>("POutlineColor", new ColorSetting(0xFF0000FF)).withParent(placing);

    private final Setting<Boolean> pFill = new Setting<>("PFill", true).withParent(placing);
    private final Setting<ColorSetting> pFillColor = new Setting<>("PFillColor", new ColorSetting(0x660000FF)).withParent(placing);

    private enum BreakRenderMode {
        GROW, SHRINK, CROSS, STATIC
    }

    public Interactions() {
        super("Interactions", "Render interactions with the world", Category.RENDER);
    }

    @Subscriber
    public void onWorldRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.playerController.getIsHittingBlock()) {
            float progress = ((IPlayerControllerMP) mc.playerController).getCurBlockDamageMP();

            BlockPos pos = ((IPlayerControllerMP) mc.playerController).getCurrentBlock();
            AxisAlignedBB bb = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);

            switch (bRenderMode.getValue()) {
                case GROW: {
                    renderBreakingBB(bb.shrink(0.5 - progress * 0.5), bFillColor.getValue(), bOutlineColor.getValue());
                    break;
                } case SHRINK: {
                    renderBreakingBB(bb.shrink(progress * 0.5), bFillColor.getValue(), bOutlineColor.getValue());
                    break;
                } case CROSS: {
                    renderBreakingBB(bb.shrink(0.5 - progress * 0.5), bFillColor.getValue(), bOutlineColor.getValue());
                    renderBreakingBB(bb.shrink(progress * 0.5), bCrossFillColor.getValue(), bCrossOutlineColor.getValue());
                    break;
                } default: {
                    renderBreakingBB(bb, bFillColor.getValue(), bOutlineColor.getValue());
                    break;
                }
            }

            if (bTracer.getValue()) {
                Vec3d eyes = new Vec3d(0, 0, 1)
                        .rotatePitch(-(float) Math
                                .toRadians(mc.player.rotationPitch))
                        .rotateYaw(-(float) Math
                                .toRadians(mc.player.rotationYaw));

                renderTracer(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z,
                        pos.getX() - ((IRenderManager) mc.getRenderManager()).getRenderPosX() + 0.5,
                        pos.getY() - ((IRenderManager) mc.getRenderManager()).getRenderPosY() + 0.5,
                        pos.getZ() - ((IRenderManager) mc.getRenderManager()).getRenderPosZ() + 0.5,
                        bTracerColor.getValue().getColor());
            }
        }

        ((IRenderGlobal) mc.renderGlobal).getDamagedBlocks().forEach(((integer, destroyBlockProgress) -> {
            renderGlobalBreakage(destroyBlockProgress);
        }));
    }

    private void renderGlobalBreakage(DestroyBlockProgress destroyBlockProgress) {
        if (destroyBlockProgress != null) {
            BlockPos pos = destroyBlockProgress.getPosition();
            if (mc.playerController.getIsHittingBlock()) {
                if (((IPlayerControllerMP) mc.playerController).getCurrentBlock().equals(pos)) return;
            }
            if (mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > bRange.getValue()) return;
            float progress = Math.min(1F, (float) destroyBlockProgress.getPartialBlockDamage() / 8F);

            AxisAlignedBB bb = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);
            
            switch (bRenderMode.getValue()) {
                case GROW: {
                    renderBreakingBB(bb.shrink(0.5 - progress * 0.5), bFillColor.getValue(), bOutlineColor.getValue());
                    break;
                } case SHRINK: {
                    renderBreakingBB(bb.shrink(progress * 0.5), bFillColor.getValue(), bOutlineColor.getValue());
                    break;
                } case CROSS: {
                    renderBreakingBB(bb.shrink(0.5 - progress * 0.5), bFillColor.getValue(), bOutlineColor.getValue());
                    renderBreakingBB(bb.shrink(progress * 0.5), bCrossFillColor.getValue(), bCrossOutlineColor.getValue());
                    break;
                } default: {
                    renderBreakingBB(bb, bFillColor.getValue(), bOutlineColor.getValue());
                    break;
                }
            }

            if (bTracer.getValue()) {
                Vec3d eyes = new Vec3d(0, 0, 1)
                        .rotatePitch(-(float) Math
                                .toRadians(mc.player.rotationPitch))
                        .rotateYaw(-(float) Math
                                .toRadians(mc.player.rotationYaw));

                renderTracer(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z,
                        pos.getX() - ((IRenderManager) mc.getRenderManager()).getRenderPosX() + 0.5,
                        pos.getY() - ((IRenderManager) mc.getRenderManager()).getRenderPosY() + 0.5,
                        pos.getZ() - ((IRenderManager) mc.getRenderManager()).getRenderPosZ() + 0.5,
                        bTracerColor.getValue().getColor());
            }
        }
    }

    public void renderPlacingBB(AxisAlignedBB bb) {
        if (pFill.getValue()) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(bb, pFillColor.getValue());
            TessellatorUtil.release();
        }

        if (pOutline.getValue()) {
            TessellatorUtil.prepare();
            if (pWireframe.getValue()) {
                BlockRenderUtil.drawWireframe(bb.offset(-((IRenderManager) Module.mc.getRenderManager()).getRenderPosX(), -((IRenderManager) Module.mc.getRenderManager()).getRenderPosY(), -((IRenderManager) Module.mc.getRenderManager()).getRenderPosZ()), pOutlineColor.getValue().getColor(), pWidth.getValue());
            } else {
                TessellatorUtil.drawBoundingBox(bb, pWidth.getValue(), pOutlineColor.getValue());
            }
            TessellatorUtil.release();
        }
    }

    private void renderBreakingBB(AxisAlignedBB bb, ColorSetting fill, ColorSetting outline) {
        if (bFill.getValue()) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(bb, fill);
            TessellatorUtil.release();
        }

        if (bOutline.getValue()) {
            TessellatorUtil.prepare();
            if (bWireframe.getValue()) {
                BlockRenderUtil.drawWireframe(bb.offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ()), outline.getColor(), bWidth.getValue());
            } else {
                TessellatorUtil.drawBoundingBox(bb, bWidth.getValue(), outline);
            }
            TessellatorUtil.release();
        }
    }

    private void renderTracer(double x, double y, double z, double x2, double y2, double z2, int color){
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(1.5f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glColor4f(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, ((color) & 0xFF) / 255F, ((color >> 24) & 0xFF) / 255F);
        GlStateManager.disableLighting();
        GL11.glLoadIdentity();

        ((IEntityRenderer) mc.entityRenderer).iOrientCamera(mc.getRenderPartialTicks());

        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3d(1d,1d,1d);
        GlStateManager.enableLighting();
    }
}
