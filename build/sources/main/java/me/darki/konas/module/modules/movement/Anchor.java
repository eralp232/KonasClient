package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerMoveEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.timer.Timer;
import net.minecraft.util.math.BlockPos;

import static me.darki.konas.util.client.PlayerUtils.calculateLookAt;

public class Anchor extends Module {
    private static Setting<Integer> vRange = new Setting<>("VRange", 4, 10, 1, 1);
    private static Setting<Integer> delay = new Setting<>("Delay", 10, 100, 1, 1);
    private static Setting<Mode> mode = new Setting<>("Mode", Mode.BEDROCK);
    private static Setting<Boolean> pitchTrigger = new Setting<>("PitchTrigger", false);
    private static Setting<Integer> pitch = new Setting<>("Pitch", 0, 90, -90, 1).withVisibility(pitchTrigger::getValue);
    private static Setting<Boolean> turnOffAfter = new Setting<>("TurnOffAfter", true);
    private static Setting<Boolean> magnet = new Setting<>("Magnet", false).withVisibility(turnOffAfter::getValue);
    private static Setting<Integer> magnetization = new Setting<>("Magnetization", 6, 10, 1, 1).withVisibility(() -> magnet.getValue() && turnOffAfter.getValue());
    private final Setting<Integer> rangeXZ = new Setting<>("RangeXZ", 8, 25, 1, 1).withVisibility(() -> magnet.getValue() && turnOffAfter.getValue());
    
    private Timer timer = new Timer();

    private enum Mode {
        BEDROCK, BOTH
    }

    public Anchor() {
        super("Anchor", Category.MOVEMENT);
    }

    @Subscriber
    public void onPlayerMove(PlayerMoveEvent event) {
        if (pitchTrigger.getValue() && mc.player.rotationPitch < pitch.getValue()) return;

        BlockPos playerPos = new BlockPos(mc.player);
        if (isHole(playerPos)) {
            timer.reset();
            if (turnOffAfter.getValue()) {
                toggle();
            }
        }

        if (!timer.hasPassed(delay.getValue() * 100)) return;

        boolean isAboveHole = false;

        for (int i = 1; i < vRange.getValue(); i++) {
            if (isHole(playerPos.down(i))) {
                isAboveHole = true;
                break;
            }
        }

        if (!isAboveHole) {
            if (magnet.getValue() && mc.player.onGround) {
                BlockPos block = null;

                Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-rangeXZ.getValue(), -vRange.getValue(), -rangeXZ.getValue()), mc.player.getPosition().add(rangeXZ.getValue(), 0, rangeXZ.getValue()));

                for (BlockPos pos : blocks) {
                    if (BlockUtils.isHole(pos) && !pos.equals(new BlockPos(mc.player))) {
                        if (block == null) {
                            block = pos;
                        } else {
                            if (mc.player.getDistanceSq(pos) < mc.player.getDistanceSq(block)) {
                                block = pos;
                            }
                        }
                    }
                }

                if (block == null) return;

                double[] v = calculateLookAt(block.getX(), block.getY(), block.getZ(), mc.player);

                double[] dir = directionSpeed(magnetization.getValue() * 0.05, (float) v[0]);

                event.setX(dir[0]);
                event.setZ(dir[1]);
            }
        } else {
            event.setX(0);
            event.setZ(0);

            mc.player.setPosition(playerPos.getX() + 0.5, mc.player.posY, playerPos.getZ() + 0.5);
        }
    }

    public double[] directionSpeed(final double speed, final float yaw) {
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = speed * cos + speed * sin;
        final double posZ = speed * sin - speed * cos;
        return new double[]{posX, posZ};
    }

    private boolean isHole(BlockPos pos) {
        if (mode.getValue() == Mode.BOTH) {
            return BlockUtils.isHole(pos);
        }
        return BlockUtils.validBedrock(pos);
    }
}
