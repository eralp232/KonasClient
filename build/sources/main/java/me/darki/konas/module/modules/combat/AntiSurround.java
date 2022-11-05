package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerUpdateEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.player.AutoTool;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.combat.VulnerabilityUtil;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.render.FaceMasks;
import me.darki.konas.util.render.TessellatorUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.darki.konas.util.interaction.InteractionUtil.checkAxis;

public class AntiSurround extends Module {
    private Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private Setting<Boolean> swing = new Setting<>("Swing", true);
    private Setting<Float> range = new Setting<>("Range", 4F, 6F, 1F, 0.1F);
    private Setting<Float> delay = new Setting<>("Delay", 2F, 10F, 0.1F, 0.1F);
    private Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);
    private Setting<SwapMode> swap = new Setting<>("Swap", SwapMode.NORMAL);
    private Setting<Boolean> instant = new Setting<>("Instant", false);
    private Setting<Boolean> limit = new Setting<>("Limit", false).withVisibility(instant::getValue);

    private Setting<Parent> render = new Setting<>("Render", new Parent(false));
    private Setting<Boolean> showMining = new Setting<>("ShowMining", true).withParent(render);
    private Setting<ColorSetting> miningColor = new Setting<>("Mining", new ColorSetting(0x55FF0000)).withParent(render);
    private Setting<ColorSetting> miningLineColor = new Setting<>("MiningOutline", new ColorSetting(Color.RED.hashCode())).withParent(render);
    private Setting<ColorSetting> readyColor = new Setting<>("Ready", new ColorSetting(0x5500FF00)).withParent(render);
    private Setting<ColorSetting> readyLineColor = new Setting<>("ReadyOutline", new ColorSetting(Color.GREEN.hashCode())).withParent(render);
    private Setting<Float> width = new Setting<>("Width", 1.5F, 10F, 0F, 0.1F).withParent(render);

    private enum SwapMode {
        OFF, NORMAL, SILENT
    }

    public AntiSurround() {
        super("AntiSurround", "Mines enemy surrounds", Category.COMBAT);
    }

    private Timer silentTimer = new Timer();

    private BlockPos prevPos;

    private BlockPos currentPos;
    private EnumFacing currentFacing;

    private float curBlockDamage;
    private Timer mineTimer = new Timer();
    private boolean stopped;

    private Timer delayTimer = new Timer();

    private Runnable postAction = null;

    private int priorSlot = -1;

    public void onEnable() {
        prevPos = null;
        currentPos = null;
        currentFacing = null;
        curBlockDamage = 0F;
        postAction = null;
        stopped = false;
        priorSlot = -1;
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (currentPos != null && curBlockDamage < 1F) {
            IBlockState iblockstate = mc.world.getBlockState(currentPos);

            if (iblockstate.getMaterial() == Material.AIR) {
                prevPos = currentPos;
                currentPos = null;
                return;
            }

            int bestSlot = AutoTool.findBestTool(currentPos);
            if (bestSlot == -1) bestSlot = mc.player.inventory.currentItem;
            int prevItem = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = bestSlot;
            curBlockDamage += iblockstate.getPlayerRelativeBlockHardness(mc.player, mc.player.world, currentPos);
            mc.player.inventory.currentItem = prevItem;
            mineTimer.reset();
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange && currentPos != null) {
            if (((SPacketBlockChange) event.getPacket()).getBlockPosition().equals(currentPos) && ((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() instanceof BlockAir) {
                prevPos = currentPos;
                currentPos = null;
                currentFacing = null;
            }
        }
    }

    @Subscriber(priority = 90)
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent.Pre event) {
        if (event.isCancelled() || !InteractionUtil.canPlaceNormally(rotate.getValue())) return;

        if (currentPos != null) {
            if (curBlockDamage >= 1F) {
                if (stopped) {
                    if (mineTimer.hasPassed(1500)) {
                        currentPos = null;
                        currentFacing = null;
                    }
                } else {
                    stopped = true;
                    if (swap.getValue() != SwapMode.OFF) {
                        int bestSlot = AutoTool.findBestTool(currentPos);
                        if (bestSlot != -1 && bestSlot != mc.player.inventory.currentItem) {
                            if (swap.getValue() == SwapMode.SILENT) {
                                priorSlot = mc.player.inventory.currentItem;
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(bestSlot));
                                silentTimer.reset();
                            } else {
                                mc.player.inventory.currentItem = bestSlot;
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(bestSlot));
                            }
                        }
                    }
                }
            }
        } else if (delayTimer.hasPassed(delay.getValue() * 1000)) {
            EntityPlayer target = getNearestTarget();

            if (target != null) {
                ArrayList<BlockPos> vulnerablePos = VulnerabilityUtil.getVulnerablePositions(new BlockPos(target));
                BlockPos bestPos = vulnerablePos.stream().min(Comparator.comparing(pos -> mc.player.getDistanceSq(pos))).orElse(null);
                if (bestPos != null) {
                    EnumFacing bestFacing = getFacing(bestPos, strictDirection.getValue());
                    if (bestFacing != null) {

                        currentPos = bestPos;
                        currentFacing = bestFacing;
                        curBlockDamage = 0F;
                        stopped = false;
                        delayTimer.reset();

                        if (instant.getValue() && currentPos.equals(prevPos)) {
                            curBlockDamage = 1F;
                            mineTimer.reset();
                            postAction = () -> {
                                if (limit.getValue()) {
                                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPos, currentFacing.getOpposite()));
                                }
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentPos, currentFacing));
                                if (swing.getValue()) {
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                }
                            };
                        } else {
                            postAction = () -> {
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPos, currentFacing));
                                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentPos, currentFacing));
                                if (swing.getValue()) {
                                    mc.player.swingArm(EnumHand.MAIN_HAND);
                                }
                            };
                        }
                    }
                }
            }
        }

        if (rotate.getValue() && currentPos != null) {
            Vec3d hitVec = new Vec3d(currentPos)
                    .add(0.5, 0.5, 0.5)
                    .add(new Vec3d(currentFacing.getDirectionVec()).scale(0.5));
            KonasGlobals.INSTANCE.rotationManager.lookAtVec3d(hitVec);
        }
    }

    private EnumFacing getFacing(BlockPos pos, boolean strictDirection) {
        List<EnumFacing> validAxis = new ArrayList<>();
        Vec3d eyePos = mc.player.getPositionEyes(1.0f);
        if (strictDirection) {
            Vec3d blockCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            IBlockState blockState = mc.world.getBlockState(pos);
            boolean isFullBox = blockState.getBlock() == Blocks.AIR || blockState.isFullBlock();
            validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
            validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
            validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
            validAxis = validAxis.stream().filter(facing -> mc.world.rayTraceBlocks(eyePos, new Vec3d(pos)
                    .add(0.5, 0.5, 0.5)
                    .add(new Vec3d(facing.getDirectionVec()).scale(0.5))) == null).collect(Collectors.toList());
            if (validAxis.isEmpty()) {
                validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
                validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
                validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
            }
        } else {
            validAxis = Arrays.asList(EnumFacing.values());
        }
        return validAxis.stream().min(Comparator.comparing(enumFacing -> new Vec3d(pos)
                .add(0.5, 0.5, 0.5)
                .add(new Vec3d(enumFacing.getDirectionVec()).scale(0.5)).distanceTo(eyePos))).orElse(null);
    }

    @Subscriber(priority = 15)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (postAction != null) {
            postAction.run();
            postAction = null;
        }

        if (priorSlot != -1 && silentTimer.hasPassed(350)) {
            mc.player.inventory.currentItem = priorSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(priorSlot));
            priorSlot = -1;
        }
    }

    public void onDisable() {
        if (priorSlot != -1 && mc.player != null) {
            mc.player.inventory.currentItem = priorSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(priorSlot));
            priorSlot = -1;
        }
    }

    private EntityPlayer getNearestTarget() {
        return mc.world.playerEntities
                .stream()
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> e != mc.player)
                .filter(e -> !e.isDead)
                .filter(e -> !Friends.isUUIDFriend(e.getUniqueID().toString()))
                .filter(e -> e.getHealth() > 0)
                .filter(e -> mc.player.getDistance(e) <= range.getValue())
                .filter(VulnerabilityUtil::isVulnerable)
                .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                .orElse(null);
    }

    @Subscriber
    public void onRender3D(Render3DEvent event) {
        try {
            if (currentPos != null && showMining.getValue()) {
                AxisAlignedBB axisAlignedBB = mc.world.getBlockState(currentPos).getBoundingBox(mc.world, currentPos).offset(currentPos);
                TessellatorUtil.prepare();
                TessellatorUtil.drawBox(axisAlignedBB, true, 1, curBlockDamage >= 1F ? readyColor.getValue() : miningColor.getValue(), FaceMasks.Quad.ALL);
                TessellatorUtil.drawBoundingBox(axisAlignedBB, width.getValue(), curBlockDamage >= 1F ? readyLineColor.getValue() : miningLineColor.getValue());
                TessellatorUtil.release();
            }
        } catch (NullPointerException npe) {

        }
    }
}
