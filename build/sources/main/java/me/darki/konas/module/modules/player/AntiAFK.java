package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;

import java.text.DecimalFormat;
import java.util.Random;

public class AntiAFK extends Module {

    private Setting<Integer> seconds = new Setting<>("Seconds", 30, 120, 0, 1);
    private Setting<Boolean> jump = new Setting<>("Jump", false);
    private Setting<Boolean> rotations = new Setting<>("Rotations", true);
    private Setting<Boolean> move = new Setting<>("Move", true);
    private Setting<Boolean> autoreply = new Setting<>("AutoReply", false);
    private Setting<Boolean> friendCoords = new Setting<>("FriendCoords", false).withVisibility(() -> autoreply.getValue());
    private Setting<Float> delay = new Setting<>("Delay", 1f, 10f, 1f, 1f);
    private static Setting<Boolean> safe = new Setting<>("Safe", true);

    public AntiAFK() {
        super("AntiAFK", "Prevents you from getting kicked while AFK", Category.PLAYER, "NoAFK");
    }

    Timer afkTimer = new Timer();
    Timer rotationTimer = new Timer();
    Timer motionTimer = new Timer();
    Timer jumpTimer = new Timer();

    boolean afk = false;

    boolean firstRun = true;

    @Subscriber
    public void onUpdate(UpdateWalkingPlayerEvent.Pre event) {
        if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;
        if(mc.player == null || mc.world == null) {
            afkTimer.reset();
            return;
        }

        if(firstRun) {
            afkTimer.reset();
            firstRun = false;
        }

        if(PlayerUtils.isPlayerMoving()) {
            afkTimer.reset();
        }

        if(afkTimer.hasPassed(seconds.getValue() * 1000)) {
            afk = true;

            if(rotations.getValue() && rotationTimer.hasPassed(delay.getValue() * 100)) {
                float min = -5;
                float max = 5;

                float randomPitch = (float) (Math.random() * (max - min + 1) + min);
                float randomYaw = (float) (Math.random() * (max - min + 1) + min);

                KonasGlobals.INSTANCE.rotationManager.setRotations(mc.player.rotationYaw + randomYaw, mc.player.rotationPitch + randomPitch);
                rotationTimer.reset();
            }
        } else {
            afk = false;
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getPacket() instanceof SPacketChat && autoreply.getValue() && afk) {
            String[] msg = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText().split(" ");
            if(((SPacketChat) event.getPacket()).getType() == ChatType.SYSTEM && msg[1].startsWith("whispers:")) {
                DecimalFormat df = new DecimalFormat("#.#");
                double x = Double.parseDouble(df.format(mc.player.posX));
                double y = Double.parseDouble(df.format(mc.player.posY));
                double z = Double.parseDouble(df.format(mc.player.posZ));
                mc.player.sendChatMessage("/r I'm currently afk " + (friendCoords.getValue() && Friends.isFriend(msg[0]) ? " at " + x + ", " + y + ", " + z : ""));
            }
        }
    }

    @Subscriber
    public void onMoveInput(MoveInputEvent event) {
        if(afk) {
            if(move.getValue() && motionTimer.hasPassed(delay.getValue() * 100)) {
                event.getInput().moveForward = new Random().nextFloat() * 2 - 1;
                event.getInput().moveStrafe = new Random().nextFloat() * 2 - 1;
                motionTimer.reset();
            }
            if(jump.getValue() && mc.player.onGround && jumpTimer.hasPassed(delay.getValue() * 100)) {
                event.getInput().jump = new Random().nextBoolean();
                jumpTimer.reset();
            }
        }
    }

    @Subscriber
    public void onPlayerMove(PlayerMoveEvent event) {
        double x = event.getX();
        double z = event.getZ();

        if (safe.getValue()) {
            double i;

            for (i = 0.05D; x != 0.0D && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, -1.0f, 0.0D)).isEmpty(); ) {
                if (x < i && x >= -i) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= i;
                } else {
                    x += i;
                }
            }

            while (z != 0.0D && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0D, -1.0f, z)).isEmpty()) {
                if (z < i && z >= -i) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= i;
                } else {
                    z += i;
                }
            }

            while (x != 0.0D && z != 0.0D && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, -1.0f, z)).isEmpty()) {
                if (x < i && x >= -i) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= i;
                } else {
                    x += i;
                }
                if (z < i && z >= -i) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= i;
                } else {
                    z += i;
                }
            }
        }


        event.setX(x);
        event.setZ(z);
    }

    @Subscriber
    public void onKey(KeyEvent event) {
        afkTimer.reset();
    }

}
