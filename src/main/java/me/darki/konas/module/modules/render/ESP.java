package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.mixin.mixins.IEntityRenderer;
import me.darki.konas.mixin.mixins.IRenderGlobal;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.mixin.mixins.IShaderGroup;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.math.Vec2i;
import me.darki.konas.util.render.BlockRenderUtil;
import me.darki.konas.util.render.RenderUtil;
import me.darki.konas.util.render.RenderUtils;
import me.darki.konas.util.render.shader.ESPShader;
import me.darki.konas.util.render.shader.ShaderHelper;
import me.darki.konas.util.render.shader.ShaderProgram;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author cats
 * Feb 10, 2020
 */
public class ESP extends Module {

    private enum Mode {
        NONE, GLOW, BOX, SHADER, OUTLINE
    }

    private enum VoidMode {
        LINE, OUTLINE, FULL
    }

    private static Setting<Parent> entities = new Setting<>("Entities", new Parent(true));
    private static Setting<Mode> mode = new Setting<>("Mode", Mode.SHADER).withParent(entities);
    private static Setting<Boolean> skeleton = new Setting<>("Skeleton", false).withParent(entities);
    private static Setting<Boolean> csgo = new Setting<>("Csgo", false).withParent(entities);
    public static Setting<Float> width = new Setting<>("Width", 5F, 10F, 0.1F, 0.1F).withParent(entities);
    public static Setting<Double> superScale = new Setting<>("Quality", 2D, 10D, 0.1D, 0.1D).withParent(entities).withVisibility(() -> mode.getValue() == Mode.SHADER);
    public static Setting<Boolean> doShaderOutline = new Setting<>("ShaderOutline", true).withParent(entities).withVisibility(() -> mode.getValue() == Mode.SHADER);
    public static Setting<Boolean> doShaderFade = new Setting<>("ShaderFade", true).withParent(entities).withVisibility(() ->  doShaderOutline.getValue() && mode.getValue() == Mode.SHADER);
    public static Setting<ColorSetting> shaderColor = new Setting<>("ShaderColor", new ColorSetting(0xFFFF00FF)).withParent(entities).withVisibility(() -> doShaderOutline.getValue() && mode.getValue() == Mode.SHADER);
    public static Setting<Boolean> shaderFill = new Setting<>("ShaderFill", false).withParent(entities).withVisibility(() -> mode.getValue() == Mode.SHADER);
    public static Setting<ColorSetting> shaderFillColor = new Setting<>("ShaderFillColor", new ColorSetting(0x88FF00FF)).withParent(entities).withVisibility(() -> shaderFill.getValue() && mode.getValue() == Mode.SHADER);
    private static Setting<Boolean> showTargets = new Setting<>("Show Targets", true).withParent(entities);
    private static Setting<Boolean> crystalsSetting = new Setting<>("Crystals", true).withParent(entities).withVisibility(() -> mode.getValue() == Mode.SHADER);
    private static Setting<Boolean> playerSetting = new Setting<>("Players", true).withParent(entities);
    private static Setting<ColorSetting> playerColor = new Setting<>("PlayerColor", new ColorSetting(0xFFff33f3)).withParent(entities).withVisibility(playerSetting::getValue);
    private static Setting<Boolean> animalSetting = new Setting<>("Animals", false).withParent(entities);
    private static Setting<ColorSetting> animalColor = new Setting<>("AnimalColor", new ColorSetting(0xFF3cff33)).withParent(entities).withVisibility(animalSetting::getValue);
    private static Setting<Boolean> mobSetting = new Setting<>("Mobs", true).withParent(entities);
    private static Setting<ColorSetting> mobColor = new Setting<>("MobColor", new ColorSetting(0xFFFF0000)).withParent(entities).withVisibility(mobSetting::getValue);
    private static Setting<Boolean> pearlSetting = new Setting<>("Pearls", true).withParent(entities);
    private static Setting<ColorSetting> pearlColor = new Setting<>("PearlColor", new ColorSetting(0xAA00FF33)).withParent(entities).withVisibility(pearlSetting::getValue);
    private static Setting<Boolean> itemSetting = new Setting<>("Items", false).withParent(entities);
    private static Setting<ColorSetting> itemColor = new Setting<>("ItemColor", new ColorSetting(0xFFfff633)).withParent(entities).withVisibility(itemSetting::getValue);

    private static Setting<Parent> storageParent = new Setting<>("Storage", new Parent(false));
    private static Setting<Boolean> storage = new Setting<>("Storages", true).withParent(storageParent);
    private static Setting<VoidMode> storageMode = new Setting<>("StorageMode", VoidMode.FULL).withParent(storageParent);
    private static Setting<Float> storageWidth = new Setting<>("StorageWidth", 1.5F, 10F, 0.1F, 1F).withParent(storageParent);
    private static Setting<Boolean> customColors = new Setting<>("CustomColors", false).withParent(storageParent);
    private static Setting<ColorSetting> chestColor = new Setting<>("ChestColor", new ColorSetting(Color.YELLOW.getRGB())).withParent(storageParent);
    private static Setting<ColorSetting> eChestColor = new Setting<>("EChestColor", new ColorSetting(Color.GREEN.darker().getRGB())).withParent(storageParent);
    private static Setting<ColorSetting> otherColor = new Setting<>("OtherColor", new ColorSetting(Color.GRAY.getRGB())).withParent(storageParent);

    private static Setting<Parent> voidParent = new Setting<>("Void", new Parent(false));
    private static Setting<Boolean> voidSetting = new Setting<>("Voids", true).withParent(voidParent);
    private static Setting<VoidMode> voidMode = new Setting<>("VoidMode", VoidMode.FULL).withParent(voidParent);
    private static Setting<ColorSetting> voidFillColor = new Setting<>("VoidFillColor", new ColorSetting(0x11FF6500)).withParent(voidParent);
    private static Setting<ColorSetting> voidColor = new Setting<>("VoidColor", new ColorSetting(0xFFFF6500)).withParent(voidParent);
    private static Setting<ColorSetting> openVoidFill = new Setting<>("OpenVoidFill", new ColorSetting(0x11FF0000)).withParent(voidParent);
    private static Setting<ColorSetting> openVoid = new Setting<>("OpenVoid", new ColorSetting(0xFFFF0000)).withParent(voidParent);
    private static Setting<Integer> vRange = new Setting<>("VRange", 25, 255, 5, 5).withParent(voidParent);
    private static Setting<Float> voidWidth = new Setting<>("VoidWidth", 1F, 10F, 0.1F, 1F).withParent(voidParent);
    private static Setting<Double> voidHeight = new Setting<>("VoidHeight", 0.5D, 3D, -3D, 0.1D).withParent(voidParent);

    private static Setting<Boolean> slimeChunks = new Setting<>("SlimeChunks", false);
    private static Setting<ColorSetting> slimeChunkColor = new Setting<>("SlimeChunkColor", new ColorSetting(0xFF00FF00, false)).withVisibility(slimeChunks::getValue);
    private static Setting<Long> seed = new Setting<>("Seed", 0L, 9223372036854775807L, -9223372036854775808L, 1L).withVisibility(() -> false);

    private static ICamera camera = new Frustum();

    private CopyOnWriteArrayList<BlockPos> voidHolesToRender = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<Vec2i> slimeChunkList = new CopyOnWriteArrayList<>();

    public static boolean hackyFix = false;

    public ESP() {
        super("ESP", Keyboard.KEY_NONE, Category.RENDER);
    }

    public void onEnable() {
        slimeChunkList.clear();
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketChunkData) {
            SPacketChunkData packet = (SPacketChunkData) event.getPacket();
            Vec2i chunk = new Vec2i(packet.getChunkX() * 16, packet.getChunkZ() * 16);

            if (!slimeChunkList.contains(chunk) && isSlimeChunk(seed.getValue(), packet.getChunkX(), packet.getChunkZ()) && mc.player.dimension == 0) {
                slimeChunkList.add(chunk);
            }
        }
    }

    private boolean isSlimeChunk(final long seed, int x, int z) {
        final Random rand = new Random(seed +
                (long) (x * x * 0x4c1906) +
                (long) (x * 0x5ac0db) +
                (long) (z * z) * 0x4307a7L +
                (long) (z * 0x5f24f) ^ 0x3ad8025f);
        return rand.nextInt(10) == 0;
    }

    @Subscriber
    public void onPRenderEvent(PostProccessRenderEvent event) {
        if (mode.getValue() == Mode.SHADER) {
            try {
                doShader();
            } catch (Exception e) {

            }
        }
    }

    /**
     * A function ran on the Render3D event
     * For each entity, it draws their bounding box
     *
     * @param event the event that runs the function
     */
    @Subscriber
    public void renderEvent(Render3DEvent event) {

        if (mc.world == null || mc.player == null) {
            return;
        }

        if (pearlSetting.getValue()) {
            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            for(Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityEnderPearl && ESP.mc.getRenderViewEntity().getDistance(entity) < 250D) {
                    RenderUtil.drawEntityBox(entity, pearlColor.getValue().getColor(), event.getPartialTicks());
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(1F);
                    RenderUtil.drawEntityBoundingBox(entity, pearlColor.getValue().getColor(), event.getPartialTicks());
                }
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.endRender();
            GlStateManager.popMatrix();
        }

        if (csgo.getValue()) {
            if (mc.getRenderManager() == null) return;

            mc.world.loadedEntityList.stream()
                    .filter(entity -> mc.player != entity && entity != mc.getRenderViewEntity())
                    .forEach(entity -> {
                        if (entity != mc.player && shouldRenderESP(entity)) {
                            float f = mc.getRenderViewEntity().getDistance(entity);

                            if (f < 3) {
                                f = 3;
                            }

                            float scale = 1 / (f / 3f);

                            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                            GlStateManager.disableTexture2D();
                            GlStateManager.depthMask(false);
                            GlStateManager.enableBlend();
                            GlStateManager.disableDepth();
                            GlStateManager.disableLighting();
                            GlStateManager.disableCull();
                            GlStateManager.enableAlpha();
                            GlStateManager.color(1, 1, 1);

                            GlStateManager.pushMatrix();
                            Vec3d pos = new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(
                                    new Vec3d(
                                            (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks(),
                                            (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks(),
                                            (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks())
                            );

                            GlStateManager.translate(pos.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), pos.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), pos.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
                            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

                            try {
                                GlStateManager.rotate((float) (mc.getRenderManager().options.thirdPersonView == 2 ? -1 : 1), 1.0F, 0.0F, 0.0F);
                            } catch (NullPointerException exception) {
                                GlStateManager.rotate((float) 1, 1.0F, 0.0F, 0.0F);
                            }

                            int entityColor = getColor(entity);

                            float red = (float) (entityColor >> 16 & 255) / 255.0F;
                            float green = (float) (entityColor >> 8 & 255) / 255.0F;
                            float blue = (float) (entityColor & 255) / 255.0F;

                            glColor4f(red, green, blue, 1F);

                            glLineWidth(3F * scale);
                            glEnable(GL_LINE_SMOOTH);

                            glBegin(GL_LINE_LOOP);
                            {
                                glVertex2d(-entity.width * 1.2, -(entity.height * 0.2));
                                glVertex2d(-entity.width * 1.2, entity.height * 1.2);
                                glVertex2d(entity.width * 1.2, entity.height * 1.2);
                                glVertex2d(entity.width * 1.2, -(entity.height * 0.2));
                            }
                            glEnd();

                            if (entity instanceof EntityLivingBase) {
                                glColor4f(0F, 0F, 0F, 0.3F);

                                glLineWidth(5F * scale);

                                glBegin(GL_LINES);
                                {
                                    glVertex2d(entity.width * 1.4, entity.height * 1.2);
                                    glVertex2d(entity.width * 1.4, -(entity.height * 0.2));
                                }
                                glEnd();

                                glColor4f(0F, 1F, 0F, 1F);

                                float healthFactor = ((EntityLivingBase) entity).getHealth() / ((EntityLivingBase) entity).getMaxHealth();

                                glBegin(GL_LINES);
                                {
                                    glVertex2d(entity.width * 1.4, entity.height * 1.2 * healthFactor);
                                    glVertex2d(entity.width * 1.4, -(entity.height * 0.2));
                                }
                                glEnd();

                                float absorptionFactor = ((EntityLivingBase) entity).getAbsorptionAmount() / 16F;

                                if (absorptionFactor > 0F) {
                                    glColor4f(0F, 0F, 0F, 0.3F);

                                    glBegin(GL_LINES);
                                    {
                                        glVertex2d(entity.width * 1.6, entity.height * 0.92);
                                        glVertex2d(entity.width * 1.6, -(entity.height * 0.2));
                                    }
                                    glEnd();

                                    glColor4f(0F, 1F, 0F, 1F);

                                    glColor4f(1F, 1F, 0F, 1F);

                                    glBegin(GL_LINES);
                                    {
                                        glVertex2d(entity.width * 1.6, entity.height * 0.92 * absorptionFactor);
                                        glVertex2d(entity.width * 1.6, -(entity.height * 0.2));
                                    }
                                    glEnd();
                                }
                            }

                            GlStateManager.enableCull();
                            GlStateManager.depthMask(true);
                            GlStateManager.enableTexture2D();
                            GlStateManager.enableBlend();
                            GlStateManager.enableDepth();
                            GlStateManager.resetColor();
                            GlStateManager.color(1F, 1F, 1F, 1F);

                            GlStateManager.popMatrix();
                        }
                    });

            glColor4f(1,1,1, 1);
        }

        if (mode.getValue() == Mode.BOX) {
            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            //draw the internal box
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();

            for (Entity entity : mc.world.loadedEntityList) {
                if (entity != mc.player && shouldRenderESP(entity)) {

                    //GlStateManager.glLineWidth(10.0f);


                    RenderUtil.drawEntityBox(entity, this.getColor(entity), event.getPartialTicks());

                    //draw the outline

                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(1.0f);
                    RenderUtil.drawEntityBoundingBox(entity, this.getColor(entity), event.getPartialTicks());
                }
            }

            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.endRender();
            GlStateManager.popMatrix();
        }

        if (itemSetting.getValue()) {
            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            //draw the internal box
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();

            for (Entity entity : mc.world.loadedEntityList) {
                if (entity != mc.player && entity instanceof EntityItem) {

                    //GlStateManager.glLineWidth(10.0f);


                    RenderUtil.drawEntityBox(entity, this.getColor(entity), event.getPartialTicks());

                    //draw the outline

                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(1.0f);
                    RenderUtil.drawEntityBoundingBox(entity, this.getColor(entity), event.getPartialTicks());
                }
            }

            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.endRender();
            GlStateManager.popMatrix();
        }

        if(voidSetting.getValue()) {

            if (voidMode.getValue() == VoidMode.LINE) {
                GlStateManager.pushMatrix();
                RenderUtil.beginRender();
                GlStateManager.enableBlend();
                GlStateManager.glLineWidth(voidWidth.getValue());
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();

                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                for (BlockPos pos : this.voidHolesToRender) {

                    final AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY(), pos.getZ() + 1);

                    int clr = voidColor.getValue().getColor();

                    if (mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) clr = openVoid.getValue().getColor();

                    RenderUtil.drawBoundingBox(box, clr);
                }

                GlStateManager.color(1F, 1F, 1F, 1F);
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                RenderUtil.endRender();
                GlStateManager.popMatrix();
            } else if (voidMode.getValue() == VoidMode.OUTLINE) {
                for (BlockPos pos : this.voidHolesToRender) {

                    AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);

                    axisAlignedBB = axisAlignedBB.setMaxY(axisAlignedBB.minY + voidHeight.getValue()).offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                    int clr = voidColor.getValue().getColor();

                    if (mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) clr = openVoid.getValue().getColor();

                    BlockRenderUtil.prepareGL();
                    BlockRenderUtil.drawOutline(axisAlignedBB, clr, voidWidth.getValue());
                    BlockRenderUtil.releaseGL();
                }
            } else {
                for (BlockPos pos : this.voidHolesToRender) {

                    AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);

                    axisAlignedBB = axisAlignedBB.setMaxY(axisAlignedBB.minY + voidHeight.getValue()).offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                    int clr = voidFillColor.getValue().getColor();

                    if (mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) clr = openVoidFill.getValue().getColor();

                    BlockRenderUtil.prepareGL();
                    BlockRenderUtil.drawFill(axisAlignedBB, clr);
                    BlockRenderUtil.releaseGL();

                    clr = voidColor.getValue().getColor();

                    if (mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) clr = openVoid.getValue().getColor();

                    BlockRenderUtil.prepareGL();
                    BlockRenderUtil.drawOutline(axisAlignedBB, clr, voidWidth.getValue());
                    BlockRenderUtil.releaseGL();
                }
            }

        }

        if (slimeChunks.getValue()) {
            if (mc.getRenderViewEntity() == null) return;
            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            GlStateManager.disableTexture2D();
            GlStateManager.disableAlpha();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.glLineWidth(2f);

            for (Vec2i slimeChunk : this.slimeChunkList) {
                final AxisAlignedBB chunkBox = new AxisAlignedBB(slimeChunk.getX(), 0, slimeChunk.getZ(), slimeChunk.getX() + 16, 0, slimeChunk.getZ() + 16);

                GlStateManager.pushMatrix();
                if (camera.isBoundingBoxInFrustum(chunkBox)) {
                    double x = mc.getRenderViewEntity().lastTickPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().lastTickPosX) * (double) event.getPartialTicks();
                    double y = mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double) event.getPartialTicks();
                    double z = mc.getRenderViewEntity().lastTickPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().lastTickPosZ) * (double) event.getPartialTicks();
                    RenderUtils.drawBox(chunkBox.offset(-x, -y, -z), GL11.GL_LINE_STRIP, slimeChunkColor.getValue().getColor());
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

        if (!storage.getValue()) return;

        if (storageMode.getValue() == VoidMode.LINE) {
            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            GlStateManager.enableBlend();
            GlStateManager.glLineWidth(storageWidth.getValue());
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            for (TileEntity tileEntity : mc.world.loadedTileEntityList) {

                final BlockPos entityBlockPos = tileEntity.getPos();

                final IBlockState iBlockState = mc.world.getBlockState(entityBlockPos);

                final Integer color = this.getTileEntityColor(tileEntity);

                if (color != null)
                    RenderUtil.drawBoundingBox(iBlockState.getSelectedBoundingBox(mc.world, entityBlockPos), new Color(color));
            }

            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.endRender();
            GlStateManager.popMatrix();
        } else if (storageMode.getValue() == VoidMode.OUTLINE) {
            for (TileEntity tileEntity : mc.world.loadedTileEntityList) {

                final BlockPos entityBlockPos = tileEntity.getPos();

                final Integer color = this.getTileEntityColor(tileEntity);

                if (color != null) {
                    AxisAlignedBB axisAlignedBB = mc.world.getBlockState(entityBlockPos).getBoundingBox(mc.world, entityBlockPos).offset(entityBlockPos);

                    axisAlignedBB = axisAlignedBB.offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                    BlockRenderUtil.prepareGL();
                    BlockRenderUtil.drawOutline(axisAlignedBB, color, storageWidth.getValue());
                    BlockRenderUtil.releaseGL();
                }
            }
        } else {
            for (TileEntity tileEntity : mc.world.loadedTileEntityList) {

                final BlockPos entityBlockPos = tileEntity.getPos();

                final Integer color = this.getTileEntityColor(tileEntity);

                if (color != null) {
                    AxisAlignedBB axisAlignedBB = mc.world.getBlockState(entityBlockPos).getBoundingBox(mc.world, entityBlockPos).offset(entityBlockPos);

                    axisAlignedBB = axisAlignedBB.offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                    BlockRenderUtil.prepareGL();
                    BlockRenderUtil.drawFill(axisAlignedBB, color);
                    BlockRenderUtil.releaseGL();

                    BlockRenderUtil.prepareGL();
                    BlockRenderUtil.drawOutline(axisAlignedBB, color, storageWidth.getValue());
                    BlockRenderUtil.releaseGL();
                }
            }
        }
    }

    private Framebuffer shaderBuffer;
    private ESPShader shaderOutline;

    public static boolean isShading = false;

    private void doShader() {
        if (mc.world.loadedEntityList.stream().filter(this::shouldRenderESP).count() == 0) return;

        ScaledResolution sr = new ScaledResolution(mc);

        if (shaderBuffer == null) {
            shaderBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
        } else if ((shaderBuffer.framebufferWidth != mc.displayWidth) || (shaderBuffer.framebufferHeight != mc.displayHeight)) {
            shaderBuffer.unbindFramebuffer();
            shaderBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
            if (shaderOutline != null) {
                shaderOutline.delete();
                shaderOutline = new ESPShader(shaderBuffer);
            }
        }

        if (superScale.getValue() != ESPShader.superScale) {
            ESPShader.superScale = superScale.getValue();
            shaderOutline.delete();
            shaderOutline = new ESPShader(shaderBuffer);
        } else if (shaderOutline == null) {
            shaderOutline = new ESPShader(shaderBuffer);
        }

        glPushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(770, 771);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glDepthMask(false);

        ((IEntityRenderer) mc.entityRenderer).iSetupCameraTransform(mc.getRenderPartialTicks(), 0);
        glMatrixMode(GL_MODELVIEW);
        this.shaderBuffer.bindFramebuffer(false);

        glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glClear(16640);

        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();

        mc.world.loadedEntityList.stream().filter(this::shouldRenderESP).forEach(ent -> {
            glEnable(GL_TEXTURE_2D);
            double[] iterpolation = ShaderHelper.interpolate(ent);
            double x = iterpolation[0];
            double y = iterpolation[1];
            double z = iterpolation[2];
            glPushMatrix();
            Render render = mc.getRenderManager().getEntityRenderObject(ent);
            if (render != null) {
                isShading = true;
                render.doRender(ent, x, y, z, 0.0f, mc.getRenderPartialTicks());
                isShading = false;
            }
            glDisable(GL_TEXTURE_2D);
            glPopMatrix();
        });

        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.setupOverlayRendering();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        this.shaderOutline.update();
        this.shaderBuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);

        glScaled(1.0 / ShaderProgram.superScale, 1.0 / ShaderProgram.superScale, 1.0 / ShaderProgram.superScale);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, this.shaderOutline.getTextureID());
        glBegin(GL_LINE_BIT);
        glTexCoord2d(0.0D, 1.0D);
        glVertex2d(0.0D, 0.0D);
        glTexCoord2d(0.0D, 0.0D);
        glVertex2d(0.0D, sr.getScaledHeight() * ESPShader.superScale);
        glTexCoord2d(1.0D, 0.0D);
        glVertex2d(sr.getScaledWidth() * ESPShader.superScale, sr.getScaledHeight() * ESPShader.superScale);
        glTexCoord2d(1.0D, 0.0D);
        glVertex2d(sr.getScaledWidth() * ESPShader.superScale, sr.getScaledHeight() * ESPShader.superScale);
        glTexCoord2d(1.0D, 1.0D);
        glVertex2d(sr.getScaledWidth() * ESPShader.superScale, 0.0D);
        glTexCoord2d(0.0D, 1.0D);
        glVertex2d(0.0D, 0.0D);
        glEnd();
        glScaled(ShaderProgram.superScale, ShaderProgram.superScale, ShaderProgram.superScale);

        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getFramebuffer().bindFramebuffer(false);

        RenderHelper.enableStandardItemLighting();

        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glPopMatrix();
    }

    /**
     * basically just make entities not outlined
     */
    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (mode.getValue().equals(Mode.GLOW)) {
            //Quick Credit to kami for the adjustable shader and width thing, I couldn't figure it out
            ((IShaderGroup) ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader()).getListShaders().forEach(shader -> {
                ShaderUniform radius = shader.getShaderManager().getShaderUniform("Radius");
                if (radius != null)
                    radius.set(width.getValue());
            });

            for (Entity entity : mc.world.loadedEntityList) {
                if (entity.getTeam() == null) {
                    this.enableGlowing(entity, getTextColor(entity), "");
                } else {
                    this.enableGlowing(entity, getTextColor(entity), entity.getTeam().getName());
                }
            }
        } else {

            for (Entity entity : mc.world.loadedEntityList) {
                entity.setGlowing(false);
            }


        }

        if(voidSetting.getValue()) {
            voidHolesToRender.clear();
            if (mc.player.posY < vRange.getValue()) {
                Iterable<BlockPos> blocks = BlockPos.getAllInBox(new BlockPos(mc.player.posX - 6, 0, mc.player.posZ - 6), new BlockPos(mc.player.posX + 6, 0, mc.player.posZ + 6));
                for (BlockPos pos : blocks) {
                    IBlockState state = mc.world.getBlockState(pos);
                    if (state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.END_PORTAL_FRAME) {
                        IBlockState state2 = mc.world.getBlockState(pos.add(0, 1, 0));
                        if (state2.getBlock() != Blocks.BEDROCK && state2.getBlock() != Blocks.END_PORTAL_FRAME) {
                            IBlockState state3 = mc.world.getBlockState(pos.add(0, 2, 0));
                            if (state3.getBlock() != Blocks.BEDROCK && state3.getBlock() != Blocks.END_PORTAL_FRAME) {
                                voidHolesToRender.add(pos);
                            }
                        }
                    }
                }
            }
        }

    }

    @Subscriber
    public void onEntityModelRender(RenderEntityModelEvent.Pre event) {

        if (mc.world == null || mc.player == null) return;

        if (mode.getValue() == Mode.OUTLINE && shouldRenderESP(event.getEntityIn())) {
            RenderUtil.OutlineUtils.renderOne(width.getValue().intValue());
            event.getModelBase().render(event.getEntityIn(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScale());
            RenderUtil.OutlineUtils.renderTwo();
            event.getModelBase().render(event.getEntityIn(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScale());
            RenderUtil.OutlineUtils.renderThree();
            RenderUtil.OutlineUtils.renderFour(getColor(event.getEntityIn()), 3F);
            event.getModelBase().render(event.getEntityIn(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScale());
            RenderUtil.OutlineUtils.renderFive(3F);
            //event.getModelBase().render(event.getEntityIn(), event.getLimbSwing(), event.getLimbSwingAmount(), event.getAgeInTicks(), event.getNetHeadYaw(), event.getHeadPitch(), event.getScale());
        }
    }

    /**
     * Disable glowing on disable
     */
    @Override
    public void onDisable() {
        for (Entity entity : mc.world.loadedEntityList) {
            entity.setGlowing(false);
        }
    }

    /**
     * Get color function
     *
     * @param entity the input entity to determine the color returned
     * @return the color returned for given entity
     */
    private Integer getColor(Entity entity) {
        if (entity instanceof EntityPlayer) {
            if (showTargets.getValue() && KonasGlobals.INSTANCE.targetManager.isTarget(entity)) {
                int lifespan = KonasGlobals.INSTANCE.targetManager.getTargetLifespanColor(entity);
                return new Color(255, lifespan/5, (int) (lifespan/1.0493)).hashCode();
            }
            return playerColor.getValue().getColor();
        }
        if (entity instanceof IMob) {
            return mobColor.getValue().getColor();
        }
        if (entity instanceof IAnimals || entity instanceof INpc) {
            return animalColor.getValue().getColor();
        }
        return itemColor.getValue().getColor();
    }

    /**
     * Similar to above function, but takes a tile entity
     */
    private Integer getTileEntityColor(TileEntity entity) {
        if (customColors.getValue()) {
            if (entity instanceof TileEntityChest) return chestColor.getValue().getColor();

            if (entity instanceof TileEntityEnderChest) return eChestColor.getValue().getColor();

            if (entity instanceof TileEntityFurnace
                    || entity instanceof TileEntityHopper
                    || entity instanceof TileEntityDispenser) return otherColor.getValue().getColor();
        }


        Integer color = getRawTileEntityColor(entity);
        if (color == null) {
            return null;
        }
        int alpha = (40) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color) & 0xFF;
        return new Integer(((alpha & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                ((blue & 0xFF)));
    }

    private Integer getRawTileEntityColor(TileEntity entity) {
        if (entity instanceof TileEntityChest) return Color.YELLOW.getRGB();

        if (entity instanceof TileEntityEnderChest) return Color.GREEN.darker().getRGB();

        if (entity instanceof TileEntityShulkerBox) {
            return ((TileEntityShulkerBox) entity).getColor().getColorValue();
        }

        if (entity instanceof TileEntityFurnace
                || entity instanceof TileEntityHopper
                || entity instanceof TileEntityDispenser) return Color.GRAY.getRGB();

        return null;
    }

    /**
     * Function to determine if setting is activated and if it should render the entity
     *
     * @param entity the input entity to determine if it should render esp on this entity
     * @return if the esp should be rendered on the entity
     */
    private boolean shouldRenderESP(Entity entity) {
        if (hackyFix) return false;
        if (entity instanceof EntityEnderCrystal && crystalsSetting.getValue() && mode.getValue() == Mode.SHADER) {
            return true;
        } else if (entity instanceof EntityPlayer && playerSetting.getValue() && entity != mc.player && entity != mc.getRenderViewEntity() && !FakePlayerManager.isFake(entity)) {
            return true;
        } else if ((entity instanceof IAnimals) && animalSetting.getValue()) {
            return true;
        } else if (entity instanceof IMob && mobSetting.getValue()) {
            return true;
        }

        return false;


    }

    private TextFormatting getTextColor(Entity entityIn) {
        if (entityIn instanceof EntityPlayer) {
            return TextFormatting.LIGHT_PURPLE;
        }
        if (entityIn instanceof IMob) {
            return TextFormatting.RED;
        }
        if (entityIn instanceof IAnimals) {
            return TextFormatting.GREEN;
        }
        return TextFormatting.YELLOW;
    }


    private void enableGlowing(Entity entity, TextFormatting color, String teamName) {
        //TODO figure out how to make this team stuff work :\
        ScorePlayerTeam team = mc.world.getScoreboard().getTeamNames().contains(teamName) ?
                mc.world.getScoreboard().getTeam(teamName) :
                mc.world.getScoreboard().createTeam(teamName);

        try {
            mc.world.getScoreboard().addPlayerToTeam(entity.getName(), team.getName());
            mc.world.getScoreboard().getTeam(teamName).setColor(color);
        } catch (IllegalArgumentException ignored) {

        }

        entity.setGlowing(true);
    }

    public static void renderSkeleton(EntityPlayer player, ModelPlayer playerModel, float partialTicks) {
        if(!skeleton.getValue() || !ModuleManager.getModuleByName("ESP").isEnabled()) return;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GL11.glEnable(2848);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GL11.glHint(3154, 4354);
        GlStateManager.depthMask(false);
        GL11.glEnable(2903);
        GL11.glDisable(2848);

        // Calculate Player Position
        double x = (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
        double y = (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
        double z = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);

        // Position the camera
        camera.setPosition(x, y, z);

        if(!camera.isBoundingBoxInFrustum(player.getEntityBoundingBox()) || player.isDead || !player.isEntityAlive() || player.isPlayerSleeping()) return;

        GL11.glEnable(2848);
        GL11.glLineWidth(2.0F);
        GlStateManager.color(255 / 255.0F, 255 / 255.0F, 255 / 255.0F, 1.0F);

        // Position and rotate the renderer
        GlStateManager.translate(x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
        float xOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        GL11.glRotatef(-xOffset, 0f, 1f, 0f);

        float yOffset = player.isSneaking() ? 0.6F : 0.75F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        drawRightLeg(buffer, player, yOffset, playerModel.bipedRightLeg);

        drawLeftLeg(buffer, player, yOffset, playerModel.bipedLeftLeg);

        drawRightArm(buffer, player, yOffset, playerModel.bipedRightArm);

        drawLeftArm(buffer, player, yOffset, playerModel.bipedLeftArm);

        drawHead(buffer, player, yOffset, xOffset, playerModel.bipedHead);

        drawHips(buffer, player, yOffset);

        drawSpine(buffer, yOffset);

        drawShoulders(buffer, yOffset);

        // End Render

        Gui.drawRect(0, 0, 0, 0, 0);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GL11.glDisable(2848);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
    }

    private static void drawRightLeg(BufferBuilder buffer, EntityPlayer player, float yOffset, ModelRenderer rightLeg) {
        // Translate to groin (i think)
        GlStateManager.translate(0.0D, 0.0D, player.isSneaking() ? -0.235D : 0.0D);
        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, 1.0F);
        // Translate to right side aka top of the right leg
        GlStateManager.translate(-0.125D, yOffset, 0.0D);

        if(rightLeg.rotateAngleX != 0.0F)
            GL11.glRotatef(rightLeg.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);

        if(rightLeg.rotateAngleY != 0.0F)
            GL11.glRotatef(rightLeg.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);

        if(rightLeg.rotateAngleZ != 0.0F)
            GL11.glRotatef(rightLeg.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);

        // Draw Line
        int topColor = playerColor.getValue().getOffsetColor(300);
        int bottomColor = playerColor.getValue().getOffsetColor(500);

        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, 0.0D).color((topColor >> 16) & 0xFF, (topColor >> 8) & 0xFF,  (topColor) & 0xFF, (topColor >> 24) & 0xFF).endVertex();
        buffer.pos(0.0D, -yOffset, 0.0D).color((bottomColor >> 16) & 0xFF, (bottomColor >> 8) & 0xFF,  (bottomColor) & 0xFF, (bottomColor >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.popMatrix();
    }

    private static void drawLeftLeg(BufferBuilder buffer, EntityPlayer player, float yOffset, ModelRenderer leftLeg) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, 1.0F);
        GlStateManager.translate(0.125D, yOffset, 0.0D);
        if (leftLeg.rotateAngleX != 0.0F)
            GL11.glRotatef(leftLeg.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
        if (leftLeg.rotateAngleY != 0.0F)
            GL11.glRotatef(leftLeg.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
        if (leftLeg.rotateAngleZ != 0.0F)
            GL11.glRotatef(leftLeg.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);

        int topColor = playerColor.getValue().getOffsetColor(300);
        int bottomColor = playerColor.getValue().getOffsetColor(500);

        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, 0.0D).color((topColor >> 16) & 0xFF, (topColor >> 8) & 0xFF,  (topColor) & 0xFF, (topColor >> 24) & 0xFF).endVertex();
        buffer.pos(0.0D, -yOffset, 0.0D).color((bottomColor >> 16) & 0xFF, (bottomColor >> 8) & 0xFF,  (bottomColor) & 0xFF, (bottomColor >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }

    private static void drawLeftArm(BufferBuilder buffer, EntityPlayer player, float yOffset, ModelRenderer leftArm) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.375D, yOffset + 0.55D, 0.0D);

        if(leftArm.rotateAngleX != 0.0F)
            GlStateManager.rotate(leftArm.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);

        if(leftArm.rotateAngleY != 0.0F)
            GlStateManager.rotate(leftArm.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);

        if(leftArm.rotateAngleZ != 0.0F)
            GlStateManager.rotate(-leftArm.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);

        int topColor = playerColor.getValue().getOffsetColor(100);
        int bottomColor = playerColor.getValue().getOffsetColor(300);

        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, 0.0D).color((topColor >> 16) & 0xFF, (topColor >> 8) & 0xFF,  (topColor) & 0xFF, (topColor >> 24) & 0xFF).endVertex();
        buffer.pos(0.0D, -0.5D, 0.0D).color((bottomColor >> 16) & 0xFF, (bottomColor >> 8) & 0xFF,  (bottomColor) & 0xFF, (bottomColor >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }

    private static void drawRightArm(BufferBuilder buffer, EntityPlayer player, float yOffset, ModelRenderer rightArm) {
        GlStateManager.translate(0.0D, 0.0D, player.isSneaking() ? 0.25D : 0.0D);
        GlStateManager.color(1f, 1f, 1f, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, player.isSneaking() ? -0.05D : 0.0D, player.isSneaking() ? -0.01725D : 0.0D);
        GlStateManager.color(1f, 1f, 1f, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.375D, yOffset + 0.55D, 0.0D);
        if (rightArm.rotateAngleX != 0.0F)
            GL11.glRotatef(rightArm.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        if (rightArm.rotateAngleY != 0.0F)
            GL11.glRotatef(rightArm.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
        if (rightArm.rotateAngleZ != 0.0F)
            GL11.glRotatef(-rightArm.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);

        int topColor = playerColor.getValue().getOffsetColor(100);
        int bottomColor = playerColor.getValue().getOffsetColor(300);

        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, 0.0D).color((topColor >> 16) & 0xFF, (topColor >> 8) & 0xFF,  (topColor) & 0xFF, (topColor >> 24) & 0xFF).endVertex();
        buffer.pos(0.0D, -0.5D, 0.0D).color((bottomColor >> 16) & 0xFF, (bottomColor >> 8) & 0xFF,  (bottomColor) & 0xFF, (bottomColor >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }

    private static void drawHead(BufferBuilder buffer, EntityPlayer player, float yOffset, float xOffset, ModelRenderer head) {
        GL11.glRotatef(xOffset - player.rotationYawHead, 0.0F, 1.0F, 0.0F);
        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, 1.0F);
        GlStateManager.translate(0.0D, yOffset + 0.55D, 0.0D);
        if (head.rotateAngleX != 0) {
            GL11.glRotatef(head.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
        }
        int color = playerColor.getValue().getOffsetColor(100);
        int neckColor = playerColor.getValue().getOffsetColor(0);
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, 0.0D).color((color >> 16) & 0xFF, (color >> 8) & 0xFF,  (color) & 0xFF, (color >> 24) & 0xFF).endVertex();
        buffer.pos(0.0D, 0.3D, 0.0D).color((neckColor >> 16) & 0xFF, (neckColor >> 8) & 0xFF,  (neckColor) & 0xFF, (neckColor >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private static void drawHips(BufferBuilder buffer, EntityPlayer player, float yOffset) {
        GL11.glRotatef(player.isSneaking() ? 25.0F : 0.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0D, player.isSneaking() ? -0.16175D : 0.0D, player.isSneaking() ? -0.48025D : 0.0D);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        int color = playerColor.getValue().getOffsetColor(300);
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-0.125D, 0.0D, 0.0D).color((color >> 16) & 0xFF, (color >> 8) & 0xFF,  (color) & 0xFF, (color >> 24) & 0xFF).endVertex();
        buffer.pos(0.125D, 0.0D, 0.0D).color((color >> 16) & 0xFF, (color >> 8) & 0xFF,  (color) & 0xFF, (color >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }

    private static void drawSpine(BufferBuilder buffer, float yOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, 1.0F);
        GlStateManager.translate(0.0D, yOffset, 0.0D);
        int topColor = playerColor.getValue().getOffsetColor(300);
        int bottomColor = playerColor.getValue().getOffsetColor(100);
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.0D, 0.0D, 0.0D).color((topColor >> 16) & 0xFF, (topColor >> 8) & 0xFF,  (topColor) & 0xFF, (topColor >> 24) & 0xFF).endVertex();
        buffer.pos(0.0D, 0.55D, 0.0D).color((bottomColor >> 16) & 0xFF, (bottomColor >> 8) & 0xFF,  (bottomColor) & 0xFF, (bottomColor >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }

    private static void drawShoulders(BufferBuilder buffer, float yOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, yOffset + 0.55D, 0.0D);
        int color = playerColor.getValue().getOffsetColor(100);
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-0.375D, 0.0D, 0.0D).color((color >> 16) & 0xFF, (color >> 8) & 0xFF,  (color) & 0xFF, (color >> 24) & 0xFF).endVertex();
        buffer.pos(0.375D, 0.0D, 0.0D).color((color >> 16) & 0xFF, (color >> 8) & 0xFF,  (color) & 0xFF, (color >> 24) & 0xFF).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
    }


}
