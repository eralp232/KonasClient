package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import io.netty.util.internal.MathUtil;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerMoveEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.ICPacketPlayer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.InterpolationHelper;
import me.darki.konas.util.math.Interpolation;
import me.darki.konas.util.timer.Timer;
import me.darki.konas.util.timer.TimerManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class Flight extends Module {
    private final Setting<Boolean> noFall = new Setting<>("NoFall", false);
    private final Setting<Boolean> useTimer = new Setting<>("UseTimer", false);

    private final Setting<Float> acceleration = new Setting<>("Acceleration", 0.5F, 5F, 0.05F, 0.05F);
    private final Setting<Float> vAcceleration = new Setting<>("VAcceleration", 0.5F, 5F, 0.05F, 0.05F);

    private final Setting<Float> speedSetting = new Setting<>("Speed", 1F, 10F, 0.1F, 0.1F);
    private final Setting<Float> vSpeedSetting = new Setting<>("VSpeed", 1F, 10F, 0.1F, 0.1F);
    private final Setting<Float> upFactor = new Setting<>("UpFactor", 0.5F, 1F, 0.1F, 0.1F);
    private final Setting<Float> maxSpeedSetting = new Setting<>("MaxSpeed", 1F, 10F, 0.1F, 0.1F);

    private final Setting<Glide> glide = new Setting<>("Glide", Glide.CONSTANT);
    private final Setting<Float> glideSpeed = new Setting<>("GlideSpeed", 1F, 10F, 0.1F, 0.1F).withVisibility(() -> glide.getValue() != Glide.OFF);
    private final Setting<Integer> glideInterval = new Setting<>("GlideInterval", 3, 20, 1, 1).withVisibility(() -> glide.getValue() == Glide.DYNAMIC);
    private final Setting<Integer> glideTicks = new Setting<>("GlideTicks", 1, 5, 1, 1).withVisibility(() -> glide.getValue() == Glide.DYNAMIC);

    private final Setting<AntiKick> antiKick = new Setting<>("AntiKick", AntiKick.NONE);
    private final Setting<Integer> antiKickInterval = new Setting<>("AntiKickInterval", 2, 20, 1, 1).withVisibility(() -> antiKick.getValue() != AntiKick.NONE);
    private final Setting<Integer> antiKickTicks = new Setting<>("AntiKickTicks", 1, 5, 1, 1).withVisibility(() -> glide.getValue() == Glide.DYNAMIC);

    private final Setting<Boolean> inAir = new Setting<>("InAir", true);
    private final Setting<Boolean> inWater = new Setting<>("InWater", true);
    private final Setting<Boolean> inLava = new Setting<>("InLava", true);

    private enum Glide {
        OFF, CONSTANT, DYNAMIC
    }

    private enum AntiKick {
        NONE, NORMAL, TOGGLE
    }

    public Flight() {
        super("Flight", Category.MOVEMENT, "CreativeFly");
    }

    private Timer setbackTimer = new Timer();
    private long zeroTime = -1L;

    private int glideCounter = 0;
    private int glideTicksCounter = 0;

    private int antiKickCounter = 0;
    private int antiKickTicksCounter = 0;

    @Subscriber
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!setbackTimer.hasPassed(350)) return;

        final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox() : mc.player.getEntityBoundingBox();
        if (bb != null) {
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block instanceof BlockAir && !inAir.getValue()) return;
                    if ((block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) && !inLava.getValue()) return;
                    if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && !inWater.getValue()) return;
                }
            }
        }

        double speed = Interpolation.lerp(0, speedSetting.getValue() * 0.2625D, Math.min(1F, (System.currentTimeMillis() - zeroTime) / (1000F * acceleration.getValue())));
        double vSpeed = Interpolation.lerp(0, vSpeedSetting.getValue() * 0.4D, Math.min(1F, (System.currentTimeMillis() - zeroTime) / (1000F * vAcceleration.getValue())));

        boolean canGoUp = true;

        if (antiKick.getValue() != AntiKick.NONE) {
            if (antiKickCounter < antiKickInterval.getValue()) {
                antiKickCounter++;
            } else {
                antiKickTicksCounter++;
                if (antiKickTicksCounter >= antiKickTicks.getValue()) {
                    antiKickCounter = 0;
                }
                if (antiKick.getValue() == AntiKick.NORMAL) {
                    speed = 0;
                    canGoUp = false;
                } else {
                    return;
                }
            }
        }

        double netSpeed = Math.sqrt(speed * speed + vSpeed * vSpeed);

        if (glideCounter < glideInterval.getValue()) {
            glideCounter++;
            glideTicksCounter = 0;
        }

        if (glide.getValue() == Glide.CONSTANT || (glideCounter >= glideInterval.getValue() && glide.getValue() == Glide.DYNAMIC)) {
            event.setY(-glideSpeed.getValue() * 0.01D);
            glideTicksCounter ++;
            if (glideTicksCounter >= glideTicks.getValue()) {
                glideCounter = 0;
            }
        }

        if (mc.gameSettings.keyBindJump.isKeyDown() && canGoUp) {
            event.setY(vSpeed * upFactor.getValue());
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            event.setY(-vSpeed);
        }

        if (netSpeed > maxSpeedSetting.getValue() * 0.6625D) {
            speed = Math.min(speedSetting.getValue() * 0.2625D, Math.sqrt(netSpeed * netSpeed - event.getY() * event.getY()));
        }

        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (forward == 0.0D && strafe == 0.0D) {
            event.setX(0.0D);
            event.setZ(0.0D);
            zeroTime = System.currentTimeMillis();
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (float) (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (float) (forward > 0.0D ? 45 : -45);
                }

                strafe = 0.0D;

                if (forward > 0.0D) {
                    forward = 1.0D;
                } else if (forward < 0.0D) {
                    forward = -1.0D;
                }
            }

            event.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
            event.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
        }
    }

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && noFall.getValue()) {
            ((ICPacketPlayer) event.getPacket()).setOnGround(true);
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            setbackTimer.reset();
            zeroTime = System.currentTimeMillis();
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (useTimer.getValue()) {
            KonasGlobals.INSTANCE.timerManager.updateTimer(this, 10, 1.088F);
        } else {
            KonasGlobals.INSTANCE.timerManager.resetTimer(this);
        }
    }

    public void onEnable() {
        zeroTime = System.currentTimeMillis();
        glideCounter = 0;
    }

    public void onDisable() {
        KonasGlobals.INSTANCE.timerManager.resetTimer(this);
    }
}
