package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.player.FastUse;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.client.RotationUtil;
import me.darki.konas.util.combat.CrystalUtils;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.interaction.RotationManager;
import me.darki.konas.util.render.BlockRenderUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PistonAura extends Module {
    private Setting<Mode> mode = new Setting<>("Mode", Mode.DAMAGE);
    private Setting<Boolean> smart = new Setting<>("Smart", true).withVisibility(() -> mode.getValue() == Mode.PUSH);
    private Setting<Boolean> triggerable = new Setting<>("DisableAfterPush", true).withVisibility(() -> mode.getValue() == Mode.PUSH);
    private Setting<Boolean> disableWhenNone = new Setting<>("DisableWhenNone", false).withVisibility(() -> mode.getValue() == Mode.DAMAGE);
    private Setting<Integer> targetRange = new Setting<>("TargetRange", 3, 6, 1, 1);
    private Setting<Integer> breakDelay = new Setting<>("Delay", 25, 100, 0, 1);
    private static Setting<Integer> actionShift = new Setting<>("ActionShift", 3, 8, 1, 1);
    private static Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 10, 0, 1);
    private Setting<Boolean> strict = new Setting<>("Strict", false);
    private Setting<Boolean> raytrace = new Setting<>("RayTrace", false);
    private Setting<Boolean> antiSuicide = new Setting<>("AntiSuicide", false);
    private Setting<Boolean> mine = new Setting<>("Mine", false);

    public static final Setting<Parent> render = new Setting<>("Render", new Parent(false));
    private static final Setting<Boolean> renderCurrent = new Setting<>("Current", true).withParent(render);
    private static final Setting<ColorSetting> colorCurrent = new Setting<>("ColorC", new ColorSetting(0x50bf40bf)).withParent(render);
    private static final Setting<ColorSetting> outlineColorCurrent = new Setting<>("OutlineC", new ColorSetting(0xFFbf40bf)).withParent(render);
    private static final Setting<Boolean> renderFull = new Setting<>("Full", true).withParent(render);
    private static final Setting<ColorSetting> colorFull = new Setting<>("ColorF", new ColorSetting(0x30bf4040)).withParent(render);
    private static final Setting<ColorSetting> outlineColorFull = new Setting<>("OutlineF", new ColorSetting(0xCFbf4040)).withParent(render);
    private static final Setting<Boolean> arrow = new Setting<>("Arrow", true).withParent(render);
    private static final Setting<ColorSetting> arrowColor = new Setting<>("ArrowColor", new ColorSetting(0xFFFF00FF)).withParent(render);
    private static final Setting<Boolean> topArrow = new Setting<>("Top", false).withParent(render);
    private static final Setting<Boolean> bottomArrow = new Setting<>("Bottom", true).withParent(render);

    public PistonAura() {
        super("PistonAura", "Automatically faceplaces people using pistons", Category.COMBAT);
    }

    private enum Mode {
        DAMAGE, PUSH
    }

    private Stage stage = Stage.SEARCHING;

    public BlockPos facePos;
    public EnumFacing faceOffset;

    public BlockPos crystalPos;

    public BlockPos pistonNeighbour;
    public EnumFacing pistonOffset;

    private BlockPos torchPos;
    private Timer torchTimer = new Timer();

    private boolean skipPiston;

    private Timer delayTimer = new Timer();
    private int delayTime;

    private Timer renderTimer = new Timer();

    private Runnable postAction = null;

    private int tickCounter = 0;

    private BlockPos placedPiston = null;
    private Timer placedPistonTimer = new Timer();

    private Timer actionTimer = new Timer();

    private enum Stage {
        SEARCHING,
        CRYSTAL,
        REDSTONE,
        BREAKING,
        EXPLOSION
    }

    public void onEnable() {
        if (mc.player == null || mc.world == null) return;
        stage = Stage.SEARCHING;
        facePos = null;
        faceOffset = null;
        crystalPos = null;
        pistonNeighbour = null;
        pistonOffset = null;
        delayTime = 0;
        tickCounter = 0;
        postAction = null;
        torchPos = null;
        skipPiston = false;
        placedPiston = null;
    }

    @Subscriber
    public void onReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange && torchPos != null) {
            if (((SPacketBlockChange) event.getPacket()).getBlockPosition().equals(torchPos) && ((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() instanceof BlockAir) {
                torchPos = null;
            }
        }
    }

    private void handleAction(boolean extra) {
        if (actionTimer.hasPassed(1000) && disableWhenNone.getValue()) {
            toggle();
        }
        if (!delayTimer.hasPassed(delayTime)) return;
        if (strict.getValue() && Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ) > 9.0E-4D) return;
        if (mode.getValue() == Mode.DAMAGE) {
            switch (stage) {
                case SEARCHING: {
                    List<EntityPlayer> candidates = getTargets();
                    for (EntityPlayer candidate : candidates) {
                        if (evaluateTarget(candidate)) {
                            int itemSlot = getPistonSlot();
                            if (itemSlot == -1) {
                                Logger.sendChatErrorMessage("No pistons found!");
                                toggle();
                                return;
                            }

                            if (skipPiston) {
                                stage = Stage.CRYSTAL;
                                skipPiston = false;
                                return;
                            }

                            boolean changeItem = mc.player.inventory.currentItem != itemSlot;
                            boolean isSprinting = mc.player.isSprinting();
                            boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(pistonNeighbour);

                            Vec3d vec = new Vec3d(pistonNeighbour)
                                    .add(0.5, 0.5, 0.5)
                                    .add(new Vec3d(pistonOffset.getDirectionVec()).scale(0.5));

                            if (extra) {
                                float[] angle = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec);
                                RotationUtil.update(angle[0], angle[1]);
                            } else {
                                KonasGlobals.INSTANCE.rotationManager.lookAtVec3d(vec);
                            }

                            postAction = () -> {
                                renderTimer.reset();

                                if (changeItem) {
                                    mc.player.inventory.currentItem = itemSlot;
                                    mc.player.connection.sendPacket(new CPacketHeldItemChange(itemSlot));
                                }

                                if (isSprinting) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                                }

                                if (shouldSneak) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                                }

                                // Block utils has cringe right click method
                                mc.playerController.processRightClickBlock(mc.player, mc.world, pistonNeighbour, pistonOffset, vec,
                                        EnumHand.MAIN_HAND);
                                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                                if (shouldSneak) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                                }

                                if (isSprinting) {
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                                }

                                stage = Stage.CRYSTAL;
                            };
                            return;
                        }
                    }
                    break;
                }
                case CRYSTAL: {
                    if (torchPos != null) {
                        if (mc.world.getBlockState(torchPos).getBlock() == Blocks.AIR) {
                            torchPos = null;
                        }
                    }
                    if (torchPos != null) {
                        if (torchTimer.hasPassed(1000)) {
                            final RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(torchPos.getX() + 0.5, torchPos.getY() + 0.5, torchPos.getZ() + 0.5));
                            final EnumFacing f = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;

                            Vec3d vec = new Vec3d(torchPos)
                                    .add(0.5, 0.5, 0.5)
                                    .add(new Vec3d(f.getDirectionVec()).scale(0.5));

                            if (extra) {
                                float[] angle = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec);
                                RotationUtil.update(angle[0], angle[1]);
                            } else {
                                KonasGlobals.INSTANCE.rotationManager.lookAtVec3d(vec);
                            }

                            postAction = () -> {
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, torchPos, f));
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, torchPos, f));
                                torchTimer.reset();
                            };
                        }
                        return;
                    }

                    if (!isOffhand()) {
                        int crystalSlot = CrystalUtils.getCrystalSlot();

                        if (crystalSlot == -1) {
                            Logger.sendChatErrorMessage("No crystals found!");
                            toggle();
                            return;
                        }

                        if (mc.player.inventory.currentItem != crystalSlot) {
                            mc.player.inventory.currentItem = crystalSlot;
                            mc.playerController.updateController();
                        }
                    }

                    if (crystalPos == null) {
                        stage = Stage.SEARCHING;
                        return;
                    }

                    if (extra) {
                        float[] angle = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(crystalPos.getX() + .5, crystalPos.getY() + .5, crystalPos.getZ() + .5));
                        RotationUtil.update(angle[0], angle[1]);
                    } else {
                        KonasGlobals.INSTANCE.rotationManager.lookAtVec3d(new Vec3d(crystalPos.getX() + .5, crystalPos.getY() + .5, crystalPos.getZ() + .5));
                    }

                    postAction = () -> {
                        renderTimer.reset();

                        final RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(crystalPos.getX() + 0.5, crystalPos.getY() - 0.5, crystalPos.getZ() + 0.5));
                        final EnumFacing f = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
                        FastUse.ignore = true;
                        BlockUtils.rightClickBlock(crystalPos, mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0), isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, f, true);
                        stage = Stage.REDSTONE;
                        torchTimer.setTime(0);
                    };
                    return;
                }
                case REDSTONE: {
                    if (facePos == null) {
                        stage = Stage.SEARCHING;
                        return;
                    }

                    int itemSlot = getRedstoneBlockSlot();

                    if (itemSlot == -1) {
                        Logger.sendChatErrorMessage("No redstone found!");
                        toggle();
                        return;
                    }

                    Optional<BlockUtils.ClickLocation> posCL = BlockUtils.generateClickLocation(facePos.offset(faceOffset, 3), false, ((ItemBlock) mc.player.inventory.getStackInSlot(getRedstoneBlockSlot()).getItem()).getBlock() == Blocks.REDSTONE_TORCH);

                    if (!posCL.isPresent() && ((ItemBlock) mc.player.inventory.getStackInSlot(getRedstoneBlockSlot()).getItem()).getBlock() == Blocks.REDSTONE_TORCH) {
                        for (EnumFacing torchFacing : EnumFacing.HORIZONTALS) {
                            if (torchFacing.equals(faceOffset) || torchFacing.equals(faceOffset.getOpposite()))
                                continue;
                            posCL = BlockUtils.generateClickLocation(facePos.offset(faceOffset, 2).offset(torchFacing), false, ((ItemBlock) mc.player.inventory.getStackInSlot(getRedstoneBlockSlot()).getItem()).getBlock() == Blocks.REDSTONE_TORCH);
                            if (posCL.isPresent()) break;
                        }
                    }

                    if (posCL.isPresent()) {
                        boolean changeItem = mc.player.inventory.currentItem != itemSlot;
                        boolean isSprinting = mc.player.isSprinting();
                        boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(posCL.get().neighbour);

                        Vec3d vec = new Vec3d(posCL.get().neighbour)
                                .add(0.5, 0.5, 0.5)
                                .add(new Vec3d(posCL.get().opposite.getDirectionVec()).scale(0.5));

                        if (extra) {
                            float[] angle = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec);
                            RotationUtil.update(angle[0], angle[1]);
                        } else {
                            KonasGlobals.INSTANCE.rotationManager.lookAtVec3d(vec);
                        }

                        final Optional<BlockUtils.ClickLocation> finalCL = posCL;

                        postAction = () -> {
                            delayTimer.reset();
                            renderTimer.reset();
                            delayTime = breakDelay.getValue() * 10;

                            if (changeItem) {
                                // Manually sending packets seems to work better than updateController() onUpdateWalkingPlayer
                                mc.player.inventory.currentItem = itemSlot;
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(itemSlot));
                            }

                            if (isSprinting) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                            }

                            if (shouldSneak) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                            }

                            // Block utils has cringe right click method
                            mc.playerController.processRightClickBlock(mc.player, mc.world, finalCL.get().neighbour, finalCL.get().opposite, vec,
                                    EnumHand.MAIN_HAND);
                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                            if (shouldSneak) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                            }

                            if (isSprinting) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                            }

                            stage = Stage.BREAKING;
                        };
                        return;
                    }
                    stage = Stage.BREAKING;
                    return;
                }
                case BREAKING: {
                    Entity nearestCrystal = mc.world.loadedEntityList.stream()
                            .filter(e -> e instanceof EntityEnderCrystal)
                            .filter(e -> mc.player.getDistance(e) <= targetRange.getValue() + 4)
                            .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                            .orElse(null);

                    if (nearestCrystal != null) {
                        if (antiSuicide.getValue() && CrystalUtils.calculateDamage((EntityEnderCrystal) nearestCrystal, mc.player) >= mc.player.getHealth() + mc.player.getAbsorptionAmount())
                            return;
                        delayTimer.reset();
                        renderTimer.reset();
                        delayTime = breakDelay.getValue() * 10;
                        if (extra) {
                            float[] angle = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), nearestCrystal.getPositionVector());
                            RotationUtil.update(angle[0], angle[1]);
                        } else {
                            KonasGlobals.INSTANCE.rotationManager.lookAtVec3d(nearestCrystal.getPositionVector());
                        }
                        postAction = () -> {
                            mc.playerController.attackEntity(mc.player, nearestCrystal);
                            mc.player.connection.sendPacket(new CPacketAnimation(isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
                        };
                    } else {
                        if (extra) {
                            float[] angle = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(facePos.getX() + 0.5, facePos.getY(), facePos.getZ() + 0.5));
                            RotationUtil.update(angle[0], angle[1]);
                        } else {
                            KonasGlobals.INSTANCE.rotationManager.lookAtXYZ(facePos.getX() + 0.5, facePos.getY(), facePos.getZ() + 0.5);
                        }
                    }
                }
            }
        } else {
            stage = Stage.SEARCHING;
            int pistonSlot = getPistonSlot();
            if (pistonSlot == -1) {
                Logger.sendChatErrorMessage("No pistons found!");
                toggle();
                return;
            }

            int redstoneBlockSlot = getRedstoneBlockSlot();

            if (redstoneBlockSlot == -1) {
                Logger.sendChatErrorMessage("No redstone found!");
                toggle();
                return;
            }

            List<EntityPlayer> candidates = getTargets();
            candidateBlock:
            for (EntityPlayer candidate : candidates) {
                if (smart.getValue()) {
                    if (!BlockUtils.isHole(new BlockPos(candidate)) && mc.world.getBlockState(new BlockPos(candidate)).getBlock() == Blocks.AIR) continue;
                }
                BlockPos candidatePos = new BlockPos(candidate).up();
                if (antiSuicide.getValue() && candidatePos.equals(new BlockPos(mc.player))) continue;
                for (EnumFacing faceTryOffset : EnumFacing.HORIZONTALS) {
                    if (mc.world.getBlockState(candidatePos.offset(faceTryOffset)).getBlock() instanceof BlockPistonBase || (!placedPistonTimer.hasPassed(CrystalUtils.ping() + 150) && candidatePos.offset(faceTryOffset).equals(placedPiston))) {
                        if (mc.world.getBlockState(candidatePos.offset(faceTryOffset)).getBlock() instanceof BlockPistonBase) {
                            EnumFacing enumfacing = (EnumFacing)mc.world.getBlockState(candidatePos.offset(faceTryOffset)).getValue(BlockDirectional.FACING);
                            if (!enumfacing.equals(faceTryOffset.getOpposite())) continue;
                        }
                        if (mc.world.getBlockState(candidatePos.offset(faceTryOffset, 2)).getBlock() == Blocks.REDSTONE_BLOCK || mc.world.getBlockState(candidatePos.offset(faceTryOffset, 2)).getBlock() == Blocks.REDSTONE_TORCH) {
                           break candidateBlock;
                        } else {
                            if (InteractionUtil.canPlaceBlock(candidatePos.offset(faceTryOffset, 2), raytrace.getValue())) {
                                InteractionUtil.Placement placement = InteractionUtil.preparePlacement(candidatePos.offset(faceTryOffset, 2), true, extra, raytrace.getValue());
                                if (placement != null) {
                                    postAction = () -> {
                                        boolean changeItem = mc.player.inventory.currentItem != redstoneBlockSlot;
                                        int startingItem = mc.player.inventory.currentItem;

                                        if (changeItem) {
                                            mc.player.inventory.currentItem = redstoneBlockSlot;
                                            mc.player.connection.sendPacket(new CPacketHeldItemChange(redstoneBlockSlot));
                                        }

                                        InteractionUtil.placeBlockSafely(placement, EnumHand.MAIN_HAND, true);
                                        delayTimer.reset();
                                        delayTime = CrystalUtils.ping() + 150;

                                        if (changeItem) {
                                            mc.player.inventory.currentItem = startingItem;
                                            mc.player.connection.sendPacket(new CPacketHeldItemChange(startingItem));
                                        }

                                        if (triggerable.getValue()) {
                                            toggle();
                                        }
                                    };
                                    return;
                                }
                            }
                        }
                        break candidateBlock;
                    }
                }
                for (EnumFacing faceTryOffset : EnumFacing.HORIZONTALS) {
                    if (InteractionUtil.canPlaceBlock(candidatePos.offset(faceTryOffset), raytrace.getValue()) && (raytrace.getValue() ? InteractionUtil.canPlaceBlock(candidatePos.offset(faceTryOffset, 2), true) : mc.world.getBlockState(candidatePos.offset(faceTryOffset, 2)).getBlock() == Blocks.AIR)) {
                        float[] rots = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), new Vec3d(candidatePos.offset(faceTryOffset).getX() + 0.5, candidatePos.offset(faceTryOffset).getY() + 1D, candidatePos.offset(faceTryOffset).getZ() + 0.5));
                        EnumFacing facing = EnumFacing.fromAngle(rots[0]);
                        if (Math.abs(rots[1]) > 55) continue; // pitch
                        if (facing != faceTryOffset) continue;
                        InteractionUtil.Placement placement = InteractionUtil.preparePlacement(candidatePos.offset(faceTryOffset), true, extra, raytrace.getValue());
                        if (placement != null) {
                            postAction = () -> {
                                boolean changeItem = mc.player.inventory.currentItem != pistonSlot;
                                int startingItem = mc.player.inventory.currentItem;

                                if (changeItem) {
                                    mc.player.inventory.currentItem = pistonSlot;
                                    mc.player.connection.sendPacket(new CPacketHeldItemChange(pistonSlot));
                                }

                                InteractionUtil.placeBlockSafely(placement, EnumHand.MAIN_HAND, true);
                                placedPiston = candidatePos.offset(faceTryOffset);
                                placedPistonTimer.reset();

                                if (changeItem) {
                                    mc.player.inventory.currentItem = startingItem;
                                    mc.player.connection.sendPacket(new CPacketHeldItemChange(startingItem));
                                }
                            };
                            return;
                        }
                    }
                }
            }
        }
    }

    @Subscriber(priority = 99)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;

        if (stage == Stage.BREAKING) {
            KonasGlobals.INSTANCE.rotationManager.lookAtXYZ(facePos.getX() + 0.5, facePos.getY(), facePos.getZ() + 0.5);
        }

        if (tickCounter < actionInterval.getValue()) {
            return;
        }

        handleAction(false);
    }

    @Subscriber(priority = 20)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (postAction != null) {
            actionTimer.reset();
            tickCounter = 0;
            postAction.run();
            postAction = null;
            int extraBlocks = 0;
            while (extraBlocks < actionShift.getValue() - 1) {
                handleAction(true);
                if (postAction != null) {
                    postAction.run();
                    postAction = null;
                } else {
                    return;
                }
                extraBlocks++;
            }
        }
        postAction = null;
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            if (crystalPos == null) return;
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                if (crystalPos.up().getDistance((int) packet.getX(), (int) packet.getY(), (int) packet.getZ()) <= 2) {
                    stage = Stage.SEARCHING;
                    delayTime = 0;
                }
            }
        }
    }

    @Subscriber
    public void onRender3D(Render3DEvent event) {
        if (facePos == null || faceOffset == null) return;
        if (renderTimer.hasPassed(1000)) return;
        if (renderCurrent.getValue()) {
            BlockPos renderBlock = null;

            switch (stage) {
                case SEARCHING: {
                    renderBlock = facePos.down().offset(faceOffset, 2);
                    break;
                } case CRYSTAL:
                case BREAKING: {
                    renderBlock = facePos.down().offset(faceOffset, 1);
                    break;
                } case REDSTONE: {
                    renderBlock = facePos.down().offset(faceOffset, 3);
                    break;
                }
            }

            if (renderBlock != null) {
                AxisAlignedBB axisAlignedBB = mc.world.getBlockState(renderBlock).getBoundingBox(mc.world, renderBlock).offset(renderBlock);

                axisAlignedBB = axisAlignedBB.offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                BlockRenderUtil.prepareGL();
                BlockRenderUtil.drawFill(axisAlignedBB, colorCurrent.getValue().getColor());
                BlockRenderUtil.releaseGL();

                BlockRenderUtil.prepareGL();
                BlockRenderUtil.drawOutline(axisAlignedBB, outlineColorCurrent.getValue().getColor(), 1.5F);
                BlockRenderUtil.releaseGL();
            }
        }
        if (renderFull.getValue()) {
            AxisAlignedBB axisAlignedBB = null;

            switch (faceOffset) {
                case NORTH: {
                    axisAlignedBB = new AxisAlignedBB(0.0D, 0.0D, 0D, 1.0D, 1.0D, -3.0D).offset(facePos.down());
                    break;
                } case SOUTH: {
                    axisAlignedBB = new AxisAlignedBB(0.0D, 0.0D, 0D, 1.0D, 1.0D, 3.0D).offset(facePos.down());
                    break;
                } case EAST: {
                    axisAlignedBB = new AxisAlignedBB(0D, 0.0D, 0.0D, 3.0D, 1.0D, 1.0D).offset(facePos.down());
                    break;
                } case WEST: {
                    axisAlignedBB = new AxisAlignedBB(0D, 0.0D, 0.0D, -3.0D, 1.0D, 1.0D).offset(facePos.down());
                    break;
                }
            }

            if (axisAlignedBB != null) {

                axisAlignedBB = axisAlignedBB.offset(-((IRenderManager) mc.getRenderManager()).getRenderPosX(), -((IRenderManager) mc.getRenderManager()).getRenderPosY(), -((IRenderManager) mc.getRenderManager()).getRenderPosZ());

                BlockRenderUtil.prepareGL();
                BlockRenderUtil.drawFill(axisAlignedBB, colorFull.getValue().getColor());
                BlockRenderUtil.releaseGL();

                BlockRenderUtil.prepareGL();
                BlockRenderUtil.drawOutline(axisAlignedBB, outlineColorFull.getValue().getColor(), 1.5F);
                BlockRenderUtil.releaseGL();
            }
        }
        if (arrow.getValue()) {
            Vec3d firstVec = null;
            Vec3d secondVec = null;
            Vec3d thirdVec = null;

            BlockPos offsetPos = facePos.offset(faceOffset, 2);

            Vec3d properPos = new Vec3d(offsetPos.getX() + 0.5 -((IRenderManager) mc.getRenderManager()).getRenderPosX(), offsetPos.getY() + 1 -((IRenderManager) mc.getRenderManager()).getRenderPosY(), offsetPos.getZ() + 0.5 -((IRenderManager) mc.getRenderManager()).getRenderPosZ());

            switch (faceOffset) {
                case NORTH: {
                    firstVec = new Vec3d(properPos.x - 0.5D, properPos.y, properPos.z - 0.5D);
                    secondVec = new Vec3d(properPos.x, properPos.y, properPos.z + 0.5D);
                    thirdVec = new Vec3d(properPos.x + 0.5D, properPos.y, properPos.z - 0.5D);
                    break;
                } case SOUTH: {
                    firstVec = new Vec3d(properPos.x - 0.5D, properPos.y, properPos.z + 0.5D);
                    secondVec = new Vec3d(properPos.x, properPos.y, properPos.z - 0.5D);
                    thirdVec = new Vec3d(properPos.x + 0.5D, properPos.y, properPos.z + 0.5D);
                    break;
                } case EAST: {
                    firstVec = new Vec3d(properPos.x + 0.5D, properPos.y, properPos.z - 0.5D);
                    secondVec = new Vec3d(properPos.x - 0.5D, properPos.y, properPos.z);
                    thirdVec = new Vec3d(properPos.x + 0.5D, properPos.y, properPos.z + 0.5D);
                    break;
                } case WEST: {
                    firstVec = new Vec3d(properPos.x - 0.5D, properPos.y, properPos.z - 0.5D);
                    secondVec = new Vec3d(properPos.x + 0.5D, properPos.y, properPos.z);
                    thirdVec = new Vec3d(properPos.x - 0.5D, properPos.y, properPos.z + 0.5D);
                    break;
                }
            }

            if (firstVec != null) {
                BlockRenderUtil.prepareGL();
                GL11.glPushMatrix();

                GL11.glEnable(3042);

                GL11.glBlendFunc(770, 771);

                GL11.glDisable(2896);
                GL11.glDisable(3553);
                GL11.glEnable(2848);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);

                GL11.glLineWidth(5F);

                GL11.glColor4f(((arrowColor.getValue().getColor() >> 16) & 0xFF) / 255F, ((arrowColor.getValue().getColor() >> 8) & 0xFF) / 255F, ((arrowColor.getValue().getColor()) & 0xFF) / 255F, ((arrowColor.getValue().getColor() >> 24) & 0xFF) / 255F);

                if (topArrow.getValue()) {
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(firstVec.x, firstVec.y, firstVec.z);
                    GL11.glVertex3d(secondVec.x, secondVec.y, secondVec.z);
                    GL11.glEnd();

                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(thirdVec.x, thirdVec.y, thirdVec.z);
                    GL11.glVertex3d(secondVec.x, secondVec.y, secondVec.z);
                    GL11.glEnd();
                }

                if (bottomArrow.getValue()) {
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(firstVec.x, firstVec.y - 1, firstVec.z);
                    GL11.glVertex3d(secondVec.x, secondVec.y - 1, secondVec.z);
                    GL11.glEnd();

                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(thirdVec.x, thirdVec.y - 1, thirdVec.z);
                    GL11.glVertex3d(secondVec.x, secondVec.y -1, secondVec.z);
                    GL11.glEnd();
                }

                GL11.glLineWidth(1.0f);

                GL11.glDisable(2848);
                GL11.glEnable(3553);
                GL11.glEnable(2896);
                GL11.glEnable(2929);

                GL11.glDepthMask(true);

                GL11.glDisable(3042);
                GL11.glPopMatrix();

                BlockRenderUtil.releaseGL();
            }
        }
    }

    public boolean isOffhand() {
        return mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
    }

    private boolean evaluateTarget(EntityPlayer candidate) {
        if (getRedstoneBlockSlot() == -1) {
            Logger.sendChatErrorMessage("No redstone found!");
            toggle();
            return false;
        }
        BlockPos tempFacePos = new BlockPos(candidate).up();
        if (evaluateTarget(tempFacePos)) {
            return true;
        }
        tempFacePos = new BlockPos(candidate).up().up();
        return evaluateTarget(tempFacePos);
    }

    public static boolean canPlaceCrystal(BlockPos blockPos) {
        if (!(mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)) return false;

        BlockPos boost = blockPos.add(0, 1, 0);

        if (!(mc.world.getBlockState(boost).getBlock() == Blocks.AIR || mc.world.getBlockState(boost).getBlock() == Blocks.PISTON_HEAD)) return false;

        BlockPos boost2 = blockPos.add(0, 2, 0);

        if (!AutoCrystal.protocol.getValue()) {
            if (!(mc.world.getBlockState(boost2).getBlock() == Blocks.AIR)) {
                return false;
            }
        }

        return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost, boost2.add(1, 1, 1))).isEmpty();
    }

    public boolean evaluateTarget(BlockPos tempFacePos) {
        if (!isAir(tempFacePos) && !mine.getValue()) return false;
        for (EnumFacing faceTryOffset : EnumFacing.HORIZONTALS) {
            torchPos = null;
            skipPiston = false;
            if (!canPlaceCrystal(tempFacePos.offset(faceTryOffset).down())) continue;

            if (getRedstoneBlockSlot() == -1) {
                return false;
            }

            ItemStack stack = mc.player.inventory.getStackInSlot(getRedstoneBlockSlot());
            Block block = ((ItemBlock)stack.getItem()).getBlock();
            if (block == Blocks.REDSTONE_BLOCK) {
                if (!isAir(tempFacePos.offset(faceTryOffset, 3))) {
                    if (mine.getValue() && (mc.world.getBlockState(tempFacePos.offset(faceTryOffset, 3)).getBlock() == Blocks.REDSTONE_TORCH || mc.world.getBlockState(tempFacePos.offset(faceTryOffset, 3)).getBlock() == Blocks.REDSTONE_BLOCK)) {
                        torchPos = tempFacePos.offset(faceTryOffset, 3);
                    } else {
                        continue;
                    }
                }
            } else {
                Optional<BlockUtils.ClickLocation> posCL = BlockUtils.generateClickLocation(tempFacePos.offset(faceTryOffset, 3), false, true);

                if (!posCL.isPresent() && mine.getValue() && (mc.world.getBlockState(tempFacePos.offset(faceTryOffset, 3)).getBlock() == Blocks.REDSTONE_TORCH || mc.world.getBlockState(tempFacePos.offset(faceTryOffset, 3)).getBlock() == Blocks.REDSTONE_BLOCK)) {
                    torchPos = tempFacePos.offset(faceTryOffset, 3);
                }

                if (!posCL.isPresent() && torchPos == null && ((ItemBlock)mc.player.inventory.getStackInSlot(getRedstoneBlockSlot()).getItem()).getBlock() == Blocks.REDSTONE_TORCH) {
                    for (EnumFacing torchFacing : EnumFacing.HORIZONTALS) {
                        if (torchFacing.equals(faceTryOffset) || torchFacing.equals(faceTryOffset.getOpposite())) continue;
                        posCL = BlockUtils.generateClickLocation(tempFacePos.offset(faceTryOffset, 2).offset(torchFacing), false, true);
                        if (posCL.isPresent()) {
                            break;
                        } else if (mine.getValue() && mc.world.getBlockState(tempFacePos.offset(faceTryOffset, 2).offset(torchFacing)).getBlock() == Blocks.REDSTONE_TORCH) {
                            torchPos = tempFacePos.offset(faceTryOffset, 2).offset(torchFacing);
                            break;
                        }
                    }
                }

                if (!posCL.isPresent() && torchPos == null) {
                    continue;
                }
            }

            Optional<BlockUtils.ClickLocation> posCL = BlockUtils.generateClickLocation(tempFacePos.offset(faceTryOffset, 2));
            skipPiston = mine.getValue() && mc.world.getBlockState(tempFacePos.offset(faceTryOffset, 2)).getBlock() instanceof BlockPistonBase;
            if (posCL.isPresent() || skipPiston) {
                if (!skipPiston) {
                    BlockPos currentPos = posCL.get().neighbour;
                    EnumFacing currentFace = posCL.get().opposite;
                    double[] yawPitch = BlockUtils.calculateLookAt(currentPos.getX(), currentPos.getY(), currentPos.getZ(), currentFace, mc.player);
                    EnumFacing facing = EnumFacing.fromAngle(yawPitch[0]);
                    if (Math.abs(yawPitch[1]) > 55) continue; // pitch
                    if (facing != faceTryOffset) continue;

                    if (raytrace.getValue()) {
                        if (!rayTrace(posCL.get().neighbour)) continue;
                    }
                    pistonNeighbour = currentPos;
                    pistonOffset = currentFace;
                }

                facePos = tempFacePos;
                faceOffset = faceTryOffset;
                crystalPos = tempFacePos.offset(faceTryOffset).down();
                return true;
            }
        }
        return false;
    }

    private boolean rayTrace(BlockPos pos) {
        for (double xS = 0.1D; xS < 0.9D; xS += 0.1D) {
            for (double yS = 0.1D; yS < 0.9D; yS += 0.1D) {
                for (double zS = 0.1D; zS < 0.9D; zS += 0.1D) {
                    Vec3d eyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox()).minY + mc.player.getEyeHeight(), mc.player.posZ);
                    Vec3d posVec = (new Vec3d(pos)).add(xS, yS, zS);

                    double distToPosVec = eyesPos.distanceTo(posVec);
                    double diffX = posVec.x - eyesPos.x;
                    double diffY = posVec.y - eyesPos.y;
                    double diffZ = posVec.z - eyesPos.z;
                    double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

                    double[] tempPlaceRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))};

                    float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                    float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                    float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                    float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                    Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                    Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);


                    RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, false, false);
                    if (rayTraceResult != null) {
                        if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                            if (rayTraceResult.getBlockPos().equals(pos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isAir(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() instanceof BlockAir;
    }

    private List<EntityPlayer> getTargets() {
        return mc.world.playerEntities.stream()
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName()))
                .filter(entityPlayer -> entityPlayer != mc.player)
                .filter(e -> mc.player.getDistance(e) < targetRange.getValue())
                .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                .collect(Collectors.toList());
    }

    public static int getPistonSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block instanceof BlockPistonBase) {
                    slot = i;
                    break;
                }
            }
        }

        return slot;
    }

    private int getRedstoneBlockSlot(){
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block == Blocks.REDSTONE_BLOCK || block == Blocks.REDSTONE_TORCH) {
                    slot = i;
                    break;
                }
            }
        }

        return slot;
    }
}
