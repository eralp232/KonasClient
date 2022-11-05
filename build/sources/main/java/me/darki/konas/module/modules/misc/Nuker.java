package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.ProcessRightClickBlockEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.ICPacketPlayer;
import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Nuker extends Module {
    public Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL);
    public Setting<Timing> timing = new Setting<>("Timing", Timing.SEQUENTIAL);
    public Setting<Float> range = new Setting<>("Range", 3.5F, 6F, 1F, 0.5F);
    public Setting<Boolean> below = new Setting<>("Below", false);
    public Setting<Boolean> rotate = new Setting<>("Rotate", true);

    private Block rightClicked = null;

    private float yaw;
    private float pitch;
    private Timer angleInactivityTimer = new Timer();

    private BlockPos pos = null;

    private enum Mode {
        NORMAL, RIGHTCLICK
    }

    private enum Timing {
        SEQUENTIAL, VANILLA
    }

    public Nuker() {
        super("Nuker", "Automatically mines blocks around you", Category.MISC, "AutoDig");
    }

    public void onEnable() {
        rightClicked = null;
    }

    @Subscriber
    public void onRightClickBlock(ProcessRightClickBlockEvent event) {
        if (mode.getValue() == Mode.RIGHTCLICK) {
            Block block = mc.world.getBlockState(event.getPos()).getBlock();
            if (block != rightClicked) {
                rightClicked = block;
                event.setCancelled(true);
            }
        }
    }

    @Subscriber(priority = 10)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        pos = null;

        if (timing.getValue() != Timing.SEQUENTIAL) return;

        switch (mode.getValue()) {
            case NORMAL:
                pos = findBlock(false);
                break;
            case RIGHTCLICK:
                pos = findBlock(true);
                break;
        }

        if (rotate.getValue() && pos != null) {
            rotate(pos);
        }

        if (!angleInactivityTimer.hasPassed(350) && rotate.getValue()) {
            KonasGlobals.INSTANCE.rotationManager.setRotations(yaw, pitch);
        }
    }

    @Subscriber(priority = 18)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (timing.getValue() != Timing.SEQUENTIAL) return;
        if (pos != null && breakable(pos)) {
            mc.playerController.onPlayerDamageBlock(pos, mc.player.getHorizontalFacing());
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (event.getPhase() == TickEvent.Phase.END) return;
        if (timing.getValue() != Timing.VANILLA) return;
        if(mc.player == null || mc.world == null) return;

        BlockPos pos = null;

        switch (mode.getValue()) {
            case NORMAL:
                pos = findBlock(false);
                break;
            case RIGHTCLICK:
                pos = findBlock(true);
                break;
        }

        if (rotate.getValue() && pos != null) {
            rotate(pos);
        }

        if (pos != null && breakable(pos)) {
            mc.playerController.onPlayerDamageBlock(pos, mc.player.getHorizontalFacing());
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    @Subscriber
    private void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof CPacketPlayer
                && !angleInactivityTimer.hasPassed(350) && rotate.getValue() && timing.getValue() == Timing.VANILLA) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (event.getPacket() instanceof CPacketPlayer.Position) {
                event.setCancelled(true);
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(mc.player.posX), packet.getY(mc.player.posY), packet.getZ(mc.player.posZ), yaw, pitch, packet.isOnGround()));
            } else {
                ((ICPacketPlayer) packet).setYaw(yaw);
                ((ICPacketPlayer) packet).setPitch(pitch);
            }
        }
    }

    public void rotate(BlockPos pos) {
        Vec3d placeVec = null;
        double[] placeRotation = null;

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

                    // inline values for slightly better perfornamce
                    float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                    float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                    float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                    float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                    Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                    Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);

                    RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, false, true);
                    if (rayTraceResult != null) {
                        if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                            Vec3d currVec = posVec;
                            double[] currRotation = tempPlaceRotation;

                            if (placeVec != null && placeRotation != null) {
                                if (Math.hypot((((currRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (currRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                        Math.hypot((((placeRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (placeRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                    placeVec = currVec;
                                    placeRotation = currRotation;
                                }
                            } else {
                                placeVec = currVec;
                                placeRotation = currRotation;
                            }
                        }
                    }
                }
            }
        }
        if (placeRotation != null) {
            yaw = (float) placeRotation[0];
            pitch = (float) placeRotation[1];
            angleInactivityTimer.reset();
        }
    }
    private BlockPos findBlock(boolean onlyRightClicked) {
        BlockPos result = null;

        float r = range.getValue();
        for (float x = r; x >= -r; x--) {
            for (float y = r; y >= -r; y--) {
                for (float z = r; z >= -r; z--) {
                    final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                    final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                    if (dist <= r && (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) && breakable(pos)) {
                        if (onlyRightClicked) {
                            if ((rightClicked == null) || !mc.world.getBlockState(pos).getBlock().equals(rightClicked)) {
                                continue;
                            }
                        }

                        if (pos.getY() < mc.player.posY && !below.getValue()) {
                            continue;
                        }

                        r = (float) dist;
                        result = pos;
                    }
                }
            }
        }

        return result;
    }

    private boolean breakable(BlockPos pos) {
        IBlockState blockState = mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, mc.world, pos) != -1;
    }
}
