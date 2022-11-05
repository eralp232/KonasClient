package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.MoveInputEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.pathfinding.generation.WalkingPathGenerator;
import me.darki.konas.util.pathfinding.node.WalkingNode;
import me.darki.konas.util.timer.Timer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

public class AutoWalk extends Module {
    public Setting<Boolean> pathFind = new Setting<>("PathFind", true);
    private Setting<Boolean> jump = new Setting<>("Jump", true);
    private Setting<Boolean> timeout = new Setting<>("Timeout", true);

    public WalkingPathGenerator walkingPathGenerator = null;
    public int index = 0;
    public boolean done = false;

    private Timer forwardTimer = new Timer();

    private Timer stuckTimer = new Timer();

    private BlockPos prevPlayerPos = null;

    public AutoWalk() {
        super("AutoWalk", "Walk forward", Keyboard.KEY_NONE, Category.MOVEMENT);
    }

    @Subscriber
    public void onMoveInput(MoveInputEvent event) {
        if (mc.world == null || mc.player == null) return;

        float movementSpeed = event.getInput().sneak ? 1F * 0.3F : 1;

        if (mc.player.getPosition() != prevPlayerPos) {
            stuckTimer.reset();
        }

        if (stuckTimer.hasPassed(5000)) {
            Logger.sendChatMessage("Can't find path!");
            toggle();
            return;
        }

        if (pathFind.getValue()) {
            if (walkingPathGenerator != null) {
                if (done) {
                    if (walkingPathGenerator.getPath().isEmpty()) {
                        Logger.sendChatMessage("Done!");
                        toggle();
                        return;
                    } else {
                        done = false;
                    }
                }

                BlockPos pos = new BlockPos(mc.player.posX, mc.player.onGround ? mc.player.posY + 0.5 : mc.player.posY, mc.player.posZ);

                if (walkingPathGenerator.getGoal().equals(pos)) {
                    Logger.sendChatMessage("Done!");
                    toggle();
                    return;
                }

                if (!forwardTimer.hasPassed(150)) {
                    event.getInput().moveForward = movementSpeed;
                }

                WalkingNode nextNode = walkingPathGenerator.getPath().get(index);

                int posIndex = walkingPathGenerator.getPath().indexOf(pos);

                if (pos.equals(nextNode)) {
                    index++;

                    if (index >= walkingPathGenerator.getPath().size()) {
                        done = true;
                        index = 0;
                        walkingPathGenerator.cycle();
                    }
                } else if (posIndex > index) {
                    index = posIndex + 1;

                    if (index >= walkingPathGenerator.getPath().size()) {
                        done = true;
                        index = 0;
                        walkingPathGenerator.cycle();
                    }
                }

                if (done) {
                    if (walkingPathGenerator.getPath().isEmpty()) {
                        pathFind.setValue(false);
                        toggle();
                        return;
                    } else {
                        done = false;
                    }
                }

                if (pos.getX() != nextNode.getX() || pos.getZ() != nextNode.getZ()) {
                    event.getInput().moveForward = movementSpeed;
                    forwardTimer.reset();

                    double[] yawPitch = PlayerUtils.calculateLookAt(nextNode.getX() + .5f, nextNode.getY(), nextNode.getZ() + .5f, mc.player);

                    mc.player.rotationYaw = (float) yawPitch[0];

                    if (index > 0 && walkingPathGenerator.getPath().get(index - 1).isJump() || pos.getY() < nextNode.getY()) {
                        event.getInput().jump = true;
                    }
                } else if (pos.getY() != nextNode.getY()) {
                    if (pos.getY() < nextNode.getY()) {
                        if (index < walkingPathGenerator.getPath().size() - 1 && !nextNode.up().equals(walkingPathGenerator.getPath().get(index + 1))) {
                            index++;
                        }

                        event.getInput().jump = true;
                    } else {
                        while (index < walkingPathGenerator.getPath().size() - 1 && walkingPathGenerator.getPath().get(index).down().equals(walkingPathGenerator.getPath().get(index + 1))) {
                            index++;
                        }

                        event.getInput().moveForward = movementSpeed;
                        forwardTimer.reset();
                    }
                }
            } else {
                Logger.sendChatMessage("Please use the .goto command!");
                toggle();
                return;
            }
        } else {
            event.getInput().moveForward = movementSpeed;

            if (jump.getValue() && mc.player.collidedHorizontally && mc.player.onGround) {
                event.getInput().jump = true;
            }
        }
    }
}
