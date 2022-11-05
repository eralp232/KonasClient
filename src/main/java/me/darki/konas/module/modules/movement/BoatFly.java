package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.DismountRidingEntityEvent;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerTravelEvent;
import me.darki.konas.mixin.mixins.ISPacketPlayerPosLook;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.PlayerUtils;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicBoolean;

public class BoatFly extends Module {

    private static Setting<Boolean> fixYaw = new Setting<>("FixYaw", true);
    private static Setting<Boolean> antiKick = new Setting<>("AntiKick", true);

    private static Setting<Boolean> confirm = new Setting<>("Confirm", false);
    private static Setting<Boolean> bypass = new Setting<>("Bypass", true);
    private static Setting<Boolean> semi = new Setting<>("Semi", true);
    private static Setting<Boolean> constrict = new Setting<>("Constrict", false);

    private static Setting<Float> speed = new Setting<>("Speed", 1F, 50F, 0.1F, 0.1F);
    private static Setting<Float> vSpeed = new Setting<>("VSpeed", 0.5F, 10F, 0.0F, 0.1F);

    private static Setting<Integer> safetyFactor = new Setting<>("SafetyFactor", 2, 10, 0, 1);
    private static Setting<Integer> maxSetbacks = new Setting<>("MaxSetbacks", 10, 20, 0, 1);


    public BoatFly() {
        super("BoatFly", Category.MOVEMENT, "AirBoat", "BoatSpeed", "BoatPlus");
    }

    private int currentTeleportId;

    private Vec3d prevSetback = null;
    private int setbackCounter;

    private AtomicBoolean moved = new AtomicBoolean(false);

    private int currentStage = 0;

    public void onEnable() {
        setbackCounter = 0;
        prevSetback = null;
        currentTeleportId = 0;
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
    }

    @Subscriber
    public void onDismountEntity(DismountRidingEntityEvent event) {
        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onPlayerUpdate(PlayerTravelEvent event) {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        if (mc.player.getRidingEntity() instanceof EntityBoat) {
            EntityBoat playerBoat = (EntityBoat) mc.player.getRidingEntity();

            double x = 0;
            double y = 0;
            double z = 0;

            if (PlayerUtils.isPlayerMoving()) {
                double[] dir = PlayerUtils.directionSpeed(speed.getValue());
                x = dir[0];
                z = dir[1];
            } else {
                x = 0;
                z = 0;
            }

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                y = vSpeed.getValue();
                if (antiKick.getValue() && mc.player.ticksExisted % 20 == 0) {
                    y = -0.04;
                }
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                y = -vSpeed.getValue();
            } else if (antiKick.getValue()) {
                if (mc.player.ticksExisted % 4 == 0) {
                    y = -0.04;
                }
            }

            if (fixYaw.getValue()) {
                playerBoat.rotationYaw = mc.player.rotationYaw;
            }

            if (safetyFactor.getValue() > 0) {
                if (!mc.world.isBlockLoaded(new BlockPos(playerBoat.posX + x * safetyFactor.getValue(), playerBoat.posY + y * safetyFactor.getValue(), playerBoat.posZ + z * safetyFactor.getValue()), false)) {
                    x = 0;
                    z = 0;
                }
            }

            if (!semi.getValue() || mc.player.ticksExisted % 2 != 0) {
                if (moved.get() && semi.getValue()) {
                    playerBoat.setVelocity(0, 0, 0);
                    moved.set(false);
                } else {
                    playerBoat.setVelocity(x, y, z);
                }
            }

            if (confirm.getValue()) {
                ++this.currentTeleportId;
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.currentTeleportId));
            }

            event.setCancelled(true);
        }
    }

    private double xzDistanceTo(Vec3d a, Vec3d b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return MathHelper.sqrt(dx * dx + dz * dz);
    }
    private double yDistanceTo(Vec3d a, Vec3d b) {
        double dy = a.y - b.y;
        return MathHelper.sqrt(dy * dy);
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        if (event.getPacket() instanceof SPacketPlayerPosLook && mc.player.isRiding()) {
            SPacketPlayerPosLook sPacketPlayerPosLook = (SPacketPlayerPosLook) event.getPacket();

            ((ISPacketPlayerPosLook) sPacketPlayerPosLook).setYaw(mc.player.rotationYaw);
            ((ISPacketPlayerPosLook) sPacketPlayerPosLook).setPitch(mc.player.rotationPitch);
            sPacketPlayerPosLook.getFlags().remove(SPacketPlayerPosLook.EnumFlags.X_ROT);
            sPacketPlayerPosLook.getFlags().remove(SPacketPlayerPosLook.EnumFlags.Y_ROT);
            currentTeleportId = sPacketPlayerPosLook.getTeleportId();

            if (maxSetbacks.getValue() > 0) {
                if (prevSetback == null) {
                    prevSetback = new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ());
                    setbackCounter = 1;
                } else {
                    if (PlayerUtils.isPlayerMoving() && xzDistanceTo(prevSetback, new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ())) < speed.getValue() * 0.8) {
                        prevSetback = new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ());
                        setbackCounter++;
                    } else if ((mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown()) && yDistanceTo(prevSetback, new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ())) < vSpeed.getValue() * 0.5) {
                        prevSetback = new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ());
                        setbackCounter++;
                    } else if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown() && (yDistanceTo(prevSetback, new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ())) < 0.02 || yDistanceTo(prevSetback, new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ())) > 1)) {
                        prevSetback = new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ());
                        setbackCounter++;
                    } else {
                        prevSetback = new Vec3d(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ());
                        setbackCounter = 1;
                    }
                }
            }

            if (maxSetbacks.getValue() > 0 && setbackCounter > maxSetbacks.getValue()) {
                return;
            }

            if (mc.player.isEntityAlive() && mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
                if (this.currentTeleportId <= 0) {
                    this.currentTeleportId = sPacketPlayerPosLook.getTeleportId();
                    return;
                }
                if (!confirm.getValue()) {
                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(sPacketPlayerPosLook.getTeleportId()));
                }
                event.setCancelled(true);
            }
        }

        if (event.getPacket() instanceof SPacketMoveVehicle && mc.player.isRiding()) {
            if (semi.getValue()) {
                moved.set(true);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        if (!bypass.getValue()) return;

        if (event.getPacket() instanceof CPacketVehicleMove) {
            if (mc.player.isRiding() && mc.player.ticksExisted % 2 == 0) {
                mc.playerController.interactWithEntity(mc.player, mc.player.getRidingEntity(), constrict.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            }
        } else if ((event.getPacket() instanceof CPacketPlayer.Rotation) && mc.player.isRiding()) {
            event.cancel();
        } else if (event.getPacket() instanceof CPacketInput && (!semi.getValue() || mc.player.ticksExisted % 2 == 0)) {
            event.cancel();
        }
    }
}
