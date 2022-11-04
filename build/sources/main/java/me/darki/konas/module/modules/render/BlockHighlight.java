package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.DrawBlockOutlineEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.TessellatorUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class BlockHighlight extends Module {

    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.FULL);
    private Setting<Boolean> facing = new Setting<>("Facing", false);
    public static Setting<Boolean> notWhenTraj = new Setting<>("NotWhenTrajectories", true);
    private Setting<Boolean> depth = new Setting<>("Depth", false);
    private Setting<Boolean> vanilla = new Setting<>("Vanilla", false);

    private static final Setting<Float> radius = new Setting<>("Radius", 0.1F, 1F, 0.1F, 0.1F).withVisibility(() -> mode.getValue() == Mode.VECTOR);
    private static final Setting<Integer> slices = new Setting<>("Slices", 8, 24, 3, 1).withVisibility(() -> mode.getValue() == Mode.VECTOR);

    private static Setting<Float> scale = new Setting<>("Scale", 0.6F, 4.0F, 0.1F, 0.1F).withVisibility(() -> mode.getValue() == Mode.BASED);
    private static Setting<Double> speed = new Setting<>("SpinSpeed", 15D, 30D, 0D, 0.1D).withVisibility(() -> mode.getValue() == Mode.BASED);

    private static final Setting<Float> drawW = new Setting<>("Width", 1.5F, 10F, 0.1F, 0.1F);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x2250b4b4));

    private enum Mode {
        FULL, OUTLINE, VECTOR, BASED
    }

    private RayTraceResult result;

    public static Timer timer = new Timer();

    public BlockHighlight() {
        super("BlockHighlight", Category.RENDER);
    }

    @Subscriber
    public void onDrawBlockOutline(DrawBlockOutlineEvent event) {
        if (!vanilla.getValue()) event.setCancelled(true);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {

        //Retard check
        if (mc.world == null || mc.player == null) return;

        // we call this every client tick instead of every render tick
        result = mc.objectMouseOver;

    }

    @Subscriber
    public void onRender(Render3DEvent event) {

        if (mc.world == null || mc.player == null) return;

        //If not hovering over anything return
        if (result == null) return;

        if (mode.getValue() == Mode.VECTOR || mode.getValue() == Mode.BASED) return;

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = result.getBlockPos();
            IBlockState blockState = mc.world.getBlockState(pos);
            AxisAlignedBB box = blockState.getSelectedBoundingBox(mc.world, pos);
            if (facing.getValue()) {
                switch (result.sideHit) {
                    case DOWN:
                        box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
                        break;
                    case UP:
                        box = new AxisAlignedBB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
                        break;
                    case NORTH:
                        box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
                        break;
                    case SOUTH:
                        box = new AxisAlignedBB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
                        break;
                    case EAST:
                        box = new AxisAlignedBB(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
                        break;
                    case WEST:
                        box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
                        break;
                }
            }
            render(box);
        }
    }

    @Subscriber
    public void onRenderWorld(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (mode.getValue() != Mode.VECTOR && mode.getValue() != Mode.BASED) return;

        if (notWhenTraj.getValue() && !timer.hasPassed(75)) return;

        RayTraceResult result = mc.objectMouseOver;

        if (result == null) return;

        if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
            if(mode.getValue() == Mode.VECTOR) {
                GlStateManager.pushMatrix();
                TessellatorUtil.prepare();
                GlStateManager.glLineWidth(drawW.getValue());
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                if (!depth.getValue()) {
                    GlStateManager.disableDepth();
                }

                GL11.glLineWidth(drawW.getValue());
                GL11.glColor4f(color.getValue().getRed() / 255F, color.getValue().getGreen() / 255F, color.getValue().getBlue() / 255F, color.getValue().getAlpha() / 255F);

                GlStateManager.translate(result.hitVec.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), result.hitVec.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), result.hitVec.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                EnumFacing side = result.sideHit;

                switch (side) {
                    case NORTH:
                    case SOUTH:
                        GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
                        break;
                    case WEST:
                    case EAST:
                        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
                        break;
                }

                Cylinder c = new Cylinder();
                GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
                c.setDrawStyle(GLU.GLU_LINE);

                c.draw(radius.getValue() * 2F, radius.getValue(), 0.0f, slices.getValue(), 1);

                GlStateManager.color(1F, 1F, 1F, 1F);
                if (!depth.getValue()) {
                    GlStateManager.enableDepth();
                }
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                TessellatorUtil.release();
                GlStateManager.popMatrix();
            } else if(mode.getValue() == Mode.BASED) {
                renderSwastika(result, scale.getValue(), speed.getValue(), color.getValue().getColorObject());
            }
        }
    }

    private void render(@NotNull AxisAlignedBB bb) {

        if (mc.world == null || mc.player == null) return;

        if (notWhenTraj.getValue() && !timer.hasPassed(75)) return;

        GlStateManager.pushMatrix();
        TessellatorUtil.prepare();
        if (depth.getValue()) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }

        if (mode.getValue() == Mode.FULL) {
            TessellatorUtil.drawBox(bb, color.getValue());
        }

        TessellatorUtil.drawBoundingBox(bb, drawW.getValue(), color.getValue().withAlpha(255));
        GlStateManager.color(1F, 1F, 1F, 1F);
        TessellatorUtil.release();
        GlStateManager.popMatrix();

    }

    private void renderSwastika(RayTraceResult result, float scale, double speed, Color color) {

        // Push (enable) all Bit Buffers (Depth, Color, Alpha, Lighting, Stencil, Scissor, etc...)
        // https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glPushAttrib.xml
        glPushAttrib(GL_ALL_ATTRIB_BITS);

        // Push Matrix, so we dont mess up main transformation matrix (rotation, scale, translation)
        glPushMatrix();

        // Disable fragment discarding
        glDisable(GL_ALPHA_TEST);

        // Make non opaque fragments blend with surroundings (without this you wouldn't be able to see behind transparent fragments correctly)
        glEnable(GL_BLEND);

        // Use Alpha Blending (blends alphas instead of color)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Disable Depth Mask
        glDepthMask(false);

        // Disable Depth Test, self explanatory, disables depth testing :troll:
        glDisable(GL_DEPTH_TEST);

        // Disable culling (we only want this on if we are drawing fully enclosed shapes)
        glDisable(GL_CULL_FACE);

        // Smooths out the line drawn, by line drawing interpolation
        glEnable(GL_LINE_SMOOTH);

        // Set Hint for GL_LINE_SMOOTH to GL_DONT_CARE (Options are: GL_DONT_CARE, GL_NICEST, GL_FASTEST)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);

        // Disable textured vertices (on by default because of Minecraft)
        glDisable(GL_TEXTURE_2D);

        // We disable lighting, since we want our shape to be unlit
        glDisable(GL_LIGHTING);

        // Set line width of our lines (self explanatory)
        glLineWidth(2.0F);

        // Set color
        glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);

        // Make camera (render pos) translate towards Hit Vec
        glTranslated(result.hitVec.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(),
                result.hitVec.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(),
                result.hitVec.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());

        // Rotate so x is horizontal and y is vertical on our blockface
        switch (result.sideHit) {
            case WEST:
                glRotatef(90F, 0F, 0F, 1F);
                glRotatef(-90F, 1F, 0F, 0F);
                break;
            case EAST:
                glRotatef(-90F, 0F, 0F, 1F);
                glRotatef(-90F, 1F, 0F, 0F);
                break;
            case NORTH:
                glRotatef(180F, 0F, 1F, 0F);
                break;
            case UP:
                glRotatef(-90F, 1F, 0F, 0F);
                break;
            case DOWN:
                glRotatef(90F, 1F, 0F, 0F);
                break;
        }


        double rainbowState = Math.ceil(System.currentTimeMillis() / Math.abs(speed - 30.1D));
        rainbowState %= 360;

        if (speed == 0) {
            rainbowState = 0;
        }

        glRotated(rainbowState, 0D, 0D, 1D);

        glScalef(scale * 0.5F, scale * 0.5F, scale * 0.5F);

        // Specify swastika vertices
        glBegin(GL_LINE_STRIP);
        glVertex3d(0, 0, 0);
        glVertex3d(0, 1, 0);
        glVertex3d(1, 1, 0);
        glEnd();

        glBegin(GL_LINE_STRIP);
        glVertex3d(0, 0, 0);
        glVertex3d(1, 0, 0);
        glVertex3d(1, -1, 0);
        glEnd();

        glBegin(GL_LINE_STRIP);
        glVertex3d(0, 0, 0);
        glVertex3d(0, -1, 0);
        glVertex3d(-1, -1, 0);
        glEnd();

        glBegin(GL_LINE_STRIP);
        glVertex3d(0, 0, 0);
        glVertex3d(-1, 0, 0);
        glVertex3d(-1, 1, 0);
        glEnd();

        // Re-enable lighting
        glEnable(GL_LIGHTING);

        // Re-enable textured vertices
        glEnable(GL_TEXTURE_2D);

        // Disable line smoothing
        glDisable(GL_LINE_SMOOTH);

        // Re-enable culling
        glEnable(GL_CULL_FACE);

        // Re-enable depth test
        glEnable(GL_DEPTH_TEST);

        // Re-enable depth mask
        glDepthMask(true);

        // Disable blending
        glDisable(GL_BLEND);

        // Re-enable alpha test
        glEnable(GL_ALPHA_TEST);

        // Pop matrix, to revert transformations
        glPopMatrix();

        // Pop (disable) Bit Buffers that we enabled earlier
        glPopAttrib();


    }

}
