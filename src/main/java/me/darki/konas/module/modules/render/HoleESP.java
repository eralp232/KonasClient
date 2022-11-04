package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.RootEvent;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.combat.VulnerabilityUtil;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.render.BlockRenderUtil;
import me.darki.konas.util.render.FaceMasks;
import me.darki.konas.util.render.RenderUtil;
import me.darki.konas.util.render.TessellatorUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HoleESP extends Module {
    private final Setting<Integer> rangeXZ = new Setting<>("RangeXZ", 8, 25, 1, 1);
    private final Setting<Integer> rangeY = new Setting<>("RangeY", 5, 25, 1, 1);

    private final Setting<Float> width = new Setting<>("Width", 1.5F, 10F, 0F, 0.1F);
    private final Setting<Float> height = new Setting<>("Height", 1F, 8F, -2F, 0.1F);

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.FULL);
    private final Setting<Integer> fadeAlpha = new Setting<>("FadeAlpha", 0, 255, 0, 1).withVisibility(() -> mode.getValue() == Mode.FADE);
    private final Setting<Boolean> depth = new Setting<>("Depth", true).withVisibility(() -> mode.getValue() == Mode.FADE);
    private final Setting<Boolean> noLineDepth = new Setting<>("NotLines", true).withVisibility(() -> mode.getValue() == Mode.FADE && depth.getValue());
    private final Setting<Lines> lines = new Setting<>("Lines", Lines.BOTTOM).withVisibility(() -> mode.getValue() == Mode.FADE);
    private final Setting<Boolean> sides = new Setting<>("Sides", false).withVisibility(() -> mode.getValue() == Mode.FULL || mode.getValue() == Mode.FADE);
    private final Setting<Boolean> notSelf = new Setting<>("NotSelf", true).withVisibility(() -> mode.getValue() == Mode.FADE);

    private final Setting<Boolean> twoBlock = new Setting<>("TwoBlock", false);

    private final Setting<Boolean> bedrock = new Setting<>("Bedrock", true);
    private final Setting<ColorSetting> bRockHoleColor = new Setting<>("BedrockColor", new ColorSetting(0x8800FF00)).withVisibility(bedrock::getValue);
    private final Setting<ColorSetting> bRockLineColor = new Setting<>("BedrockLineColor", new ColorSetting(0xFF00FF00)).withVisibility(bedrock::getValue);
    private final Setting<Boolean> obsidian = new Setting<>("Obsidian", true);
    private final Setting<ColorSetting> obiHoleColor = new Setting<>("ObiColor", new ColorSetting(0x88FF0000)).withVisibility(obsidian::getValue);
    private final Setting<ColorSetting> obiLineHoleColor = new Setting<>("ObiLineColor", new ColorSetting(0xFFFF0000)).withVisibility(obsidian::getValue);

    private final Setting<Boolean> vunerable = new Setting<>("Vulnerable", false);
    private final Setting<Boolean> selfVunerable = new Setting<>("Self", false);
    private final Setting<ColorSetting> vunerableColor = new Setting<>("VunColor", new ColorSetting(0x66FF00FF)).withVisibility(vunerable::getValue);
    private final Setting<ColorSetting> vunerableLineColor = new Setting<>("VunLineColor", new ColorSetting(0xFFFF00FF)).withVisibility(vunerable::getValue);

    private List<BlockPos> obiHoles = new ArrayList<>();
    private List<BlockPos> bedrockHoles = new ArrayList<>();

    private List<TwoBlockHole> obiHolesTwoBlock = new ArrayList<>();
    private List<TwoBlockHole> bedrockHolesTwoBlock = new ArrayList<>();

    private enum Lines {
        FULL, BOTTOM, TOP
    }

    private enum Mode {
        BOTTOM,
        OUTLINE,
        FULL,
        WIREFRAME,
        FADE
    }

    public HoleESP() {
        super("HoleESP", "Shows you holes", Category.RENDER);
    }

    @Subscriber
    public void onUpdate(RootEvent event) {
        if (mc.world == null || mc.player == null) return;
        obiHoles.clear();
        bedrockHoles.clear();
        obiHolesTwoBlock.clear();
        bedrockHolesTwoBlock.clear();
        Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-rangeXZ.getValue(), -rangeY.getValue(), -rangeXZ.getValue()), mc.player.getPosition().add(rangeXZ.getValue(), rangeY.getValue(), rangeXZ.getValue()));

        for (BlockPos pos : blocks) {
            if (!(
                    mc.world.getBlockState(pos).getMaterial().blocksMovement() &&
                            mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial().blocksMovement() &&
                            mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial().blocksMovement()
            )) {


                if (BlockUtils.validObi(pos) && obsidian.getValue()) {
                    this.obiHoles.add(pos);
                } else {
                    final BlockPos validTwoBlock = BlockUtils.validTwoBlockObiXZ(pos);
                    if (validTwoBlock != null && obsidian.getValue() && twoBlock.getValue()) {
                        this.obiHolesTwoBlock.add(new TwoBlockHole(pos, pos.add(validTwoBlock.getX(), validTwoBlock.getY(), validTwoBlock.getZ())));
                    }
                }

                if (BlockUtils.validBedrock(pos) && bedrock.getValue()) {
                    this.bedrockHoles.add(pos);
                } else {
                    final BlockPos validTwoBlock = BlockUtils.validTwoBlockBedrockXZ(pos);
                    if (validTwoBlock != null && bedrock.getValue() && twoBlock.getValue()) {
                        this.bedrockHolesTwoBlock.add(new TwoBlockHole(pos, pos.add(validTwoBlock.getX(), validTwoBlock.getY(), validTwoBlock.getZ())));
                    }
                }


            }
        }

    }

    @Override
    public String getExtraInfo() {
        return mode.getValue().toString().charAt(0) + mode.getValue().toString().substring(1).toLowerCase();
    }

    @Subscriber
    public void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (mode.getValue() == Mode.BOTTOM) {
            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            GlStateManager.enableBlend();
            GlStateManager.glLineWidth(5.0f);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            for (BlockPos pos : this.bedrockHoles) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY(), pos.getZ() + 1);

                RenderUtil.drawBoundingBox(box, bRockHoleColor.getValue().getColor());
            }

            for (BlockPos pos : this.obiHoles) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY(), pos.getZ() + 1);

                RenderUtil.drawBoundingBox(box, obiHoleColor.getValue().getColor());
            }

            for (TwoBlockHole pos : this.bedrockHolesTwoBlock) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.getOne().getX(), pos.getOne().getY(), pos.getOne().getZ(), pos.getExtra().getX() + 1, pos.getExtra().getY(), pos.getExtra().getZ() + 1);

                RenderUtil.drawBoundingBox(box, bRockHoleColor.getValue().getColor());
            }

            for (TwoBlockHole pos : this.obiHolesTwoBlock) {
                final AxisAlignedBB box = new AxisAlignedBB(pos.getOne().getX(), pos.getOne().getY(), pos.getOne().getZ(), pos.getExtra().getX() + 1, pos.getExtra().getY(), pos.getExtra().getZ() + 1);

                RenderUtil.drawBoundingBox(box, obiHoleColor.getValue().getColor());
            }

            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.endRender();
            GlStateManager.popMatrix();
        } else {
            for (BlockPos pos : this.bedrockHoles) {
                drawHole(pos, bRockHoleColor.getValue(), bRockLineColor.getValue());
            }

            for (BlockPos pos : this.obiHoles) {
                drawHole(pos, obiHoleColor.getValue(), obiLineHoleColor.getValue());
            }

            for (TwoBlockHole pos : this.bedrockHolesTwoBlock) {
                drawHoleTwoBlock(pos.getOne(), pos.getExtra(), bRockHoleColor.getValue(), bRockLineColor.getValue());
            }

            for (TwoBlockHole pos : this.obiHolesTwoBlock) {
                drawHoleTwoBlock(pos.getOne(), pos.getExtra(), obiHoleColor.getValue(), obiLineHoleColor.getValue());
            }
        }

        if (vunerable.getValue()) {
            List<Entity> targetsInRange = mc.world.loadedEntityList.
                    stream()
                    .filter(e -> e instanceof EntityPlayer)
                    .filter(e -> e.getDistance(mc.player) < rangeXZ.getValue())
                    .filter(e -> e != mc.player || selfVunerable.getValue())
                    .filter(e -> !Friends.isFriend(e.getName()))
                    .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                    .collect(Collectors.toList());

            for (Entity target : targetsInRange) {
                ArrayList<BlockPos> vuns = VulnerabilityUtil.getVulnerablePositions(new BlockPos(target));

                for (BlockPos pos : vuns) {
                    AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);
                    TessellatorUtil.prepare();
                    TessellatorUtil.drawBox(axisAlignedBB, true, 1, vunerableColor.getValue(), vunerableColor.getValue().getAlpha(), FaceMasks.Quad.ALL);
                    TessellatorUtil.drawBoundingBox(axisAlignedBB, width.getValue(), vunerableLineColor.getValue());
                    TessellatorUtil.release();
                }
            }
        }
    }

    public void drawHole(BlockPos pos, ColorSetting color, ColorSetting lineColor) {
        AxisAlignedBB axisAlignedBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);

        axisAlignedBB = axisAlignedBB.setMaxY(axisAlignedBB.minY + height.getValue());

        if (mode.getValue() == Mode.FULL) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(axisAlignedBB, true, 1, color, color.getAlpha(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            TessellatorUtil.release();
        }

        if (mode.getValue() == Mode.FULL || mode.getValue() == Mode.OUTLINE) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBoundingBox(axisAlignedBB, width.getValue(), lineColor);
            TessellatorUtil.release();
        }

        if (mode.getValue() == Mode.WIREFRAME) {
            BlockRenderUtil.prepareGL();
            BlockRenderUtil.drawWireframe(axisAlignedBB.offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ()), lineColor.getColor(), width.getValue());
            BlockRenderUtil.releaseGL();
        }

        if (mode.getValue() == Mode.FADE) {
            AxisAlignedBB tBB = mc.world.getBlockState(pos).getBoundingBox(mc.world, pos).offset(pos);
            tBB = tBB.setMaxY(tBB.minY + height.getValue());

            if (mc.player.getEntityBoundingBox() != null && tBB.intersects(mc.player.getEntityBoundingBox()) && notSelf.getValue()) {
                tBB = tBB.setMaxY(Math.min(tBB.maxY, mc.player.posY + 1D));
            }

            TessellatorUtil.prepare();
            if (depth.getValue()) {
                GlStateManager.enableDepth();
                tBB = tBB.shrink(0.01D);
            }
            TessellatorUtil.drawBox(tBB, true, height.getValue(), color, fadeAlpha.getValue(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            if (width.getValue() >= 0.1F) {
                if (lines.getValue() == Lines.BOTTOM) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.minY, tBB.minZ, tBB.maxX, tBB.minY, tBB.maxZ);
                } else if (lines.getValue() == Lines.TOP) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.maxY, tBB.minZ, tBB.maxX, tBB.maxY, tBB.maxZ);
                }
                if (noLineDepth.getValue()) {
                    GlStateManager.disableDepth();
                }
                TessellatorUtil.drawBoundingBox(tBB, width.getValue(), lineColor, fadeAlpha.getValue());
            }
            TessellatorUtil.release();
        }
    }

    public void drawHoleTwoBlock(BlockPos pos, BlockPos two, ColorSetting color, ColorSetting lineColor) {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), two.getX() + 1, two.getY() + height.getValue(), two.getZ() + 1);

        if (mode.getValue() == Mode.FULL) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(axisAlignedBB, true, 1, color, color.getAlpha(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            TessellatorUtil.release();
        }

        if (mode.getValue() == Mode.FULL || mode.getValue() == Mode.OUTLINE) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBoundingBox(axisAlignedBB, width.getValue(), lineColor);
            TessellatorUtil.release();
        }

        if (mode.getValue() == Mode.WIREFRAME) {
            BlockRenderUtil.prepareGL();
            BlockRenderUtil.drawWireframe(axisAlignedBB.offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ()), lineColor.getColor(), width.getValue());
            BlockRenderUtil.releaseGL();
        }

        if (mode.getValue() == Mode.FADE) {
            AxisAlignedBB tBB = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), two.getX() + 1, two.getY() + height.getValue(), two.getZ() + 1);

            if (tBB.intersects(mc.player.getEntityBoundingBox()) && notSelf.getValue()) {
                tBB = tBB.setMaxY(Math.min(tBB.maxY, mc.player.posY + 1D));
            }

            TessellatorUtil.prepare();
            if (depth.getValue()) {
                GlStateManager.enableDepth();
                tBB = tBB.shrink(0.01D);
            }
            TessellatorUtil.drawBox(tBB, true, height.getValue(), color, fadeAlpha.getValue(), sides.getValue() ? FaceMasks.Quad.NORTH | FaceMasks.Quad.SOUTH | FaceMasks.Quad.WEST | FaceMasks.Quad.EAST : FaceMasks.Quad.ALL);
            if (width.getValue() >= 0.1F) {
                if (lines.getValue() == Lines.BOTTOM) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.minY, tBB.minZ, tBB.maxX, tBB.minY, tBB.maxZ);
                } else if (lines.getValue() == Lines.TOP) {
                    tBB = new AxisAlignedBB(tBB.minX, tBB.maxY, tBB.minZ, tBB.maxX, tBB.maxY, tBB.maxZ);
                }
                if (noLineDepth.getValue()) {
                    GlStateManager.disableDepth();
                }
                TessellatorUtil.drawBoundingBox(tBB, width.getValue(), lineColor, fadeAlpha.getValue());
            }
            TessellatorUtil.release();
        }
    }

    private static class TwoBlockHole {

        private final BlockPos one;
        private final BlockPos extra;

        public TwoBlockHole(BlockPos one, BlockPos extra) {
            this.one = one;
            this.extra = extra;
        }

        public BlockPos getOne() {
            return one;
        }

        public BlockPos getExtra() {
            return extra;
        }

    }


}
