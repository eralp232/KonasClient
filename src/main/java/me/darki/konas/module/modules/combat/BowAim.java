package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.timer.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;

import java.util.Comparator;

public class BowAim extends Module {
    public static final Timer angleInactivityTimer = new Timer();

    public static double yaw = 0.0D;
    public static double pitch = 0.0D;

    public BowAim() {
        super("BowAim", Category.COMBAT, "BowAimbot");
    }

    @Subscriber(priority = 100)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (event.isCancelled()) return;
        if (!(mc.player.getActiveItemStack().getItem() instanceof ItemBow)) return;

        EntityPlayer nearestTarget = mc.world.playerEntities
                .stream()
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> e != mc.player)
                .filter(e -> !e.isDead)
                .filter(e -> !Friends.isUUIDFriend(e.getUniqueID().toString()))
                .filter(e -> e.getHealth() > 0)
                .min(Comparator.comparing(t -> mc.player.getDistance(t))).orElse(null);

        if (nearestTarget == null) return;

        float currentDuration = (float) (mc.player.getActiveItemStack().getMaxItemUseDuration() - mc.player.getItemInUseCount()) / 20.0f;

        currentDuration = (currentDuration * currentDuration + currentDuration * 2.0f) / 3.0f;
        if (currentDuration >= 1.0f) {
            currentDuration = 1.0f;
        }
        double duration = currentDuration * 3.0f;
        double coeff = 0.05000000074505806;

        float pitch = (float)(-Math.toDegrees(calculateArc(nearestTarget, duration, coeff)));

        if (Float.isNaN(pitch)) {
            return;
        }

        double iX = nearestTarget.posX - nearestTarget.lastTickPosX;
        double iZ = nearestTarget.posZ - nearestTarget.lastTickPosZ;

        double d = mc.player.getDistance(nearestTarget);

        d -= d % 2.0;

        iX = d / 2.0 * iX * (mc.player.isSprinting() ? 1.3 : 1.1);
        iZ = d / 2.0 * iZ * (mc.player.isSprinting() ? 1.3 : 1.1);

        float yaw = (float)Math.toDegrees(Math.atan2(nearestTarget.posZ + iZ - mc.player.posZ, nearestTarget.posX + iX - mc.player.posX)) - 90.0f;
        KonasGlobals.INSTANCE.rotationManager.setRotations(yaw, pitch);
        setYawAndPitch(yaw, pitch);
        angleInactivityTimer.reset();
    }

    private float calculateArc(EntityPlayer target, double duration, double coeff) {
        double yArc = target.posY + (double)(target.getEyeHeight() / 2.0f) - (mc.player.posY + (double)mc.player.getEyeHeight());
        double dX = target.posX - mc.player.posX;
        double dZ = target.posZ - mc.player.posZ;

        double dirRoot = Math.sqrt(dX * dX + dZ * dZ);

        return calculateArc(duration, coeff, dirRoot, yArc);
    }

    private float calculateArc(double duration, double coeff, double dirRoot, double yArc) {
        double dirCoeff = coeff * (dirRoot * dirRoot);

        yArc = 2.0 * yArc * (duration * duration);
        yArc = coeff * (dirCoeff + yArc);
        yArc = Math.sqrt(duration * duration * duration * duration - yArc);

        duration = duration * duration - yArc;

        yArc = Math.atan2(duration * duration + yArc, coeff * dirRoot);

        duration = Math.atan2(duration, coeff * dirRoot);

        return (float)Math.min(yArc, duration);
    }

    private static void setYawAndPitch(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
    }
}
