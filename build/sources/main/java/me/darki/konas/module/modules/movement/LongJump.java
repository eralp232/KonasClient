package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerMoveEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LongJump extends Module {
    private static Setting<Mode> mode = new Setting<>("Mode", Mode.BYPASS);

    private static Setting<Float> speed = new Setting<>("Speed", 4.5f, 20.0f, 0.5f, 0.1f);
    private static Setting<Float> modifier = new Setting<>("Modifier", 5.0F, 10.0F, 0.1F, 0.1F);
    private static Setting<Float> glide = new Setting<>("Glide", 1.0F, 10.0F, 0.1F, 0.1F);

    private static Setting<Boolean> shortJump = new Setting<>("ShortJump", false);

    public static Setting<Boolean> disableStrafe = new Setting<>("DisableStrafe", true);

    public static Setting<GroundCheck> groundCheck = new Setting<>("GroundCheck", GroundCheck.NORMAL);

    public static Setting<Boolean> autoDisable = new Setting<>("AutoDisable", false);

    private enum Mode {
        NORMAL,
        BYPASS
    }

    private enum GroundCheck {
        NONE,
        NORMAL,
        EDGEJUMP
    }

    private Timer timer = new Timer();
    private boolean timerStatus;
    private boolean walkingStatus;

    private int onGroundTracker = 0;

    private double walkingState;
    private double totalWalkingState;

    private int bypassState;
    private int state;

    private double currentSpeed;

    private boolean groundTracker;

    public LongJump() {
        super("LongJump", Category.MOVEMENT);
    }

    @Subscriber
    public void WalkingPlayerUpdate(UpdateWalkingPlayerEvent.Pre event) {
        if (groundTracker) {
            if (groundCheck.getValue() == GroundCheck.NORMAL) {
                if (mc.player.onGround) {
                    groundTracker = false;
                }
            } else if (groundCheck.getValue() == GroundCheck.EDGEJUMP) {
                if (mc.player.onGround && !mc.player.isSneaking() && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, 0.0, 0.0).shrink(0.001)).isEmpty()) {
                    groundTracker = false;
                }
            }
        } else {
            if (mode.getValue() == Mode.NORMAL) {
                walkingState = mc.player.posX - mc.player.prevPosX;
                double difZ = mc.player.posZ - mc.player.prevPosZ;
                totalWalkingState = Math.sqrt(walkingState * walkingState + difZ * difZ);
            } else {
                double difX = mc.player.posX - mc.player.prevPosX;
                double difZ = mc.player.posZ - mc.player.prevPosZ;
                totalWalkingState = Math.sqrt(difX * difX + difZ * difZ);
                if (!walkingStatus) return;
                mc.player.motionY = 0.005;
            }
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (autoDisable.getValue()) {
                toggle();
            }
        }
    }

    @Subscriber
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (groundTracker) return;
        if (mc.player != mc.getRenderViewEntity()) return;
        switch (mode.getValue()) {
            case NORMAL: {
                if (mc.player.moveStrafing <= 0.0f && mc.player.moveForward <= 0.0f) {
                    state = 1;
                }
                if (roundDecimalUp((mc.player.posY - (double)((int) mc.player.posY)), 3) == 0.943) {
                    mc.player.motionY -= 0.0157 * glide.getValue();
                    event.setY(event.getY()-0.0157 * glide.getValue());
                }
                if (state == 1 && (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f)) {
                    state = 2;
                    currentSpeed = speed.getValue() * getBaseSpeed() - 0.01;
                } else if (state == 2) {
                    mc.player.motionY = 0.0848*modifier.getValue();
                    event.setY(0.0848*modifier.getValue());
                    state = 3;
                    currentSpeed *= 2.149802;
                } else if (state == 3) {
                    state = 4;
                    walkingState = 0.66 * totalWalkingState;
                    currentSpeed = totalWalkingState - walkingState;
                } else {
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() > 0 || mc.player.collidedVertically) {
                        state = 1;
                    }
                    currentSpeed = totalWalkingState - totalWalkingState / 159.0;
                }
                currentSpeed = Math.max(currentSpeed,getBaseSpeed());

                float moveForward = mc.player.movementInput.moveForward;
                float moveStrafe = mc.player.movementInput.moveStrafe;
                float rotationYaw = mc.player.rotationYaw;

                if (moveForward == 0.0f && moveStrafe == 0.0f) {
                    event.setX(0.0);
                    event.setZ(0.0);
                } else {
                    if (moveForward != 0.0f) {
                        if (moveStrafe >= 1.0f) {
                            rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
                            moveStrafe = 0.0f;
                        }
                        else {
                            if (moveStrafe <= -1.0f) {
                                rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
                                moveStrafe = 0.0f;
                            }
                        }
                        if (moveForward > 0.0f) {
                            moveForward = 1.0f;
                        } else if (moveForward < 0.0f) {
                            moveForward = -1.0f;
                        }
                    }
                }

                double cos = Math.cos(Math.toRadians(rotationYaw + 90.0f));
                double sin = Math.sin(Math.toRadians(rotationYaw + 90.0f));
                event.setX(moveForward * currentSpeed * cos + moveStrafe * currentSpeed * sin);
                event.setZ(moveForward * currentSpeed * sin - moveStrafe * currentSpeed * cos);
                return;
            }
            case BYPASS: {
                if (timerStatus) {
                    if (mc.player.onGround) {
                        timer.reset();
                    }
                    if (roundDecimalUp((mc.player.posY - (double) ((int) mc.player.posY)), 3) == 0.410) {
                        mc.player.motionY = 0.0;
                    }
                    if (mc.player.moveStrafing <= 0.0f && mc.player.moveForward <= 0.0f) {
                        bypassState = 1;
                    }
                    if (roundDecimalUp(mc.player.posY - (double) ((int) mc.player.posY), 3) == 0.943) {
                        mc.player.motionY = 0.0;
                    }
                    if (bypassState == 1) {
                        if (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) {
                            bypassState = 2;
                            currentSpeed = speed.getValue() * getBaseSpeed() - 0.01;
                        }
                    } else if (bypassState == 2) {
                        bypassState = 3;
                        if (!shortJump.getValue()) {
                            mc.player.motionY = 0.424;
                        }
                        event.setY(0.424);
                        currentSpeed *= 2.149802;
                    } else if (bypassState == 3) {
                        bypassState = 4;
                        double speed = 0.66 * (totalWalkingState - getBaseSpeed());
                        currentSpeed = totalWalkingState - speed;
                    } else {
                        if (!(mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() <= 0 && !mc.player.collidedVertically)) {
                            bypassState = 1;
                        }
                        currentSpeed = totalWalkingState - totalWalkingState / 159.0;
                    }
                    currentSpeed = Math.max(currentSpeed, getBaseSpeed());
                    float moveForward = mc.player.movementInput.moveForward;
                    float moveStrafe = mc.player.movementInput.moveStrafe;
                    float rotationYaw = mc.player.rotationYaw;
                    if (moveForward == 0.0f || moveStrafe == 0.0f) {
                        event.setX(0.0);
                        event.setZ(0.0);
                    } else {
                        if (moveStrafe >= 1.0f) {
                            rotationYaw += (float) (moveForward > 0.0f ? -45 : 45);
                            moveStrafe = 0.0f;
                        } else {
                            if (moveStrafe <= -1.0f) {
                                rotationYaw += (float) (moveForward > 0.0f ? 45 : -45);
                                moveStrafe = 0.0f;
                            }
                        }
                        if (moveForward > 0.0f) {
                            moveForward = 1.0f;
                        } else {
                            if (moveForward < 0.0f) {
                                moveForward = -1.0f;
                            }
                        }
                    }
                    double cos = Math.cos(Math.toRadians(rotationYaw + 90.0f));
                    double sin = Math.sin(Math.toRadians(rotationYaw + 90.0f));
                    event.setX((double) moveForward * currentSpeed * cos + (double) moveStrafe * currentSpeed * sin);
                    event.setZ((double) moveForward * currentSpeed * sin - (double) moveStrafe * currentSpeed * cos);
                    if (moveForward == 0.0f && moveStrafe == 0.0f) {
                        event.setX(0.0);
                        event.setZ(0.0);
                    }
                }
            }
            if (mc.player.onGround) {
                onGroundTracker++;
            } else {
                if (!mc.player.onGround && onGroundTracker != 0) {
                    onGroundTracker--;
                }
            }
            if (shortJump.getValue()) {
                if (timer.hasPassed(35L)) {
                    walkingStatus = true;
                }
                if ((timer.hasPassed(2490L))) {
                    walkingStatus = false;
                    timerStatus = false;
                    mc.player.motionX *= 0.0;
                    mc.player.motionZ *= 0.0;
                }
                if (!timer.hasPassed(2820L)) return;
                timerStatus = true;
                mc.player.motionX *= 0.0;
                mc.player.motionZ *= 0.0;
                timer.reset();
            } else {
                if (timer.hasPassed(480L)) {
                    mc.player.motionX *= 0.0;
                    mc.player.motionZ *= 0.0;
                    timerStatus = false;
                }
                if (timer.hasPassed(780L)) {
                    mc.player.motionX *= 0.0;
                    mc.player.motionZ *= 0.0;
                    timerStatus = true;
                    timer.reset();
                }
            }
        }
    }

    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            currentSpeed = getBaseSpeed();
            mc.player.onGround = true;
        }
        groundTracker = groundCheck.getValue() != GroundCheck.NONE;
        walkingStatus = false;
        timerStatus = true;
        totalWalkingState = 0.0;
        state = 1;
    }

    public void onDisable() {
        if (mc.player != null && mc.world != null) {
            if (mode.getValue() == Mode.BYPASS) {
                mc.player.onGround = false;
                mc.player.capabilities.isFlying = false;
            }
        }
    }

    public static double getBaseSpeed() {
        double baseSpeed = 0.2873;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            int speedAmplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0 + 0.2D * (double) (speedAmplifier + 1);
        }
        return baseSpeed;
    }

    public double roundDecimalUp(double d, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
