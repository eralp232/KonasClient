package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.interaction.RotationManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;

public class AntiAim extends Module {
    private static Setting<Mode> mode = new Setting<>("Mode", Mode.SPIN);
    private static Setting<PitchMode> pitchMode = new Setting<>("Pitch", PitchMode.JITTER);
    private static Setting<Integer> speed = new Setting<>("Speed", 10, 55, 1, 1);
    private static Setting<Integer> yawAdd = new Setting<>("YawAdd", 0, 180, -180, 10);

    private enum Mode {
        SPIN, JITTER, STARE
    }

    private enum PitchMode {
        NONE, JITTER, STARE, DOWN
    }

    public AntiAim() {
        super("AntiAim", "Breaks motion prediction in bad clients", Category.PLAYER, "SpinBot");
    }

    private float currentYaw = 0f;
    private float currentPitch = 0f;

    @Subscriber(priority = 0)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;


        if (mode.getValue() == Mode.SPIN) {
            currentYaw += speed.getValue();
        } else if (mode.getValue() == Mode.JITTER) {
            if (Math.random() > 0.5) {
                currentYaw += speed.getValue() * Math.random();
            } else {
                currentYaw -= speed.getValue() * Math.random();
            }
        } else {
            EntityPlayer nearestEntity = getNearestEntity();
            if (nearestEntity != null) {
                currentYaw = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), nearestEntity.getPositionEyes(1F))[0] - 180;
            } else {
                currentYaw = mc.player.rotationYaw;
            }
        }

        currentYaw += yawAdd.getValue();

        currentYaw = MathHelper.wrapDegrees((int) currentYaw);

        if (pitchMode.getValue() == PitchMode.NONE) {
            currentPitch = mc.player.rotationPitch;
        } else if (pitchMode.getValue() == PitchMode.JITTER) {
            if (Math.random() > 0.5) {
                currentPitch += speed.getValue() * Math.random();
            } else {
                currentPitch -= speed.getValue() * Math.random();
            }
        } else if (pitchMode.getValue() == PitchMode.STARE) {
            EntityPlayer nearestEntity = getNearestEntity();
            if (nearestEntity != null) {
                currentPitch = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), nearestEntity.getPositionEyes(1F))[1];
            } else {
                currentPitch = mc.player.rotationPitch;
            }
        } else {
            currentPitch = 90;
        }

        if (currentPitch > 89) {
            currentPitch = 89;
        } else if (currentPitch < -89) {
            currentPitch = -89;
        }

        KonasGlobals.INSTANCE.rotationManager.setRotations(currentYaw, currentPitch);

    }

    private EntityPlayer getNearestEntity() {
        return mc.world.playerEntities.stream()
                .filter(e -> e != mc.player)
                .filter(e -> !Friends.isFriend(e.getName()))
                .filter(e -> e.getDistance(mc.player) < 10)
                .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                .orElse(null);
    }
}
