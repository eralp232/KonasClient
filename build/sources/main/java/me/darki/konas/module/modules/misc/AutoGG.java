package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.TargetKillEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;

import java.util.ArrayList;
import java.util.Random;

public class AutoGG extends Module {
    private static Setting<Boolean> gg = new Setting<>("GG", true);
    private static Setting<Boolean> ez = new Setting<>("EZ", false);
    private static Setting<Boolean> log = new Setting<>("Log", false);
    private static Setting<Boolean> excuses = new Setting<>("Excuses", false);
    private static Setting<Boolean> pop = new Setting<>("Pops", false);

    public static final ArrayList<String> KONASGG = new ArrayList<>();

    public static final ArrayList<String> KONASEZ = new ArrayList<>();

    public static final ArrayList<String> KONASPOP = new ArrayList<>();

    public static final ArrayList<String> KONASLOG = new ArrayList<>();

    public static final ArrayList<String> KONASEXCUSE = new ArrayList<>();


    private Random random = new Random();

    private Timer excuseTimer = new Timer();

    public AutoGG() {
        super("AutoGG", Category.MISC);

        // Default GG
        KONASGG.add("Good fight! Konas owns me and all \u2022\u1d17\u2022");

        // Default EZ
        KONASEZ.add("you just got nae nae'd by konas <player>!");
        KONASEZ.add("<player> tango down");
        KONASEZ.add("<player> you just felt the wrath of konas client");
        KONASEZ.add("I guess konas ca is too fast for you <player>!");
        KONASEZ.add("<player> konas ca is too fast!");
        KONASEZ.add("you just got ez'd by konas client <player>");

        // Default POP
        KONASPOP.add("keep popping <player>");
        KONASPOP.add("ez pop <player>");

        // Default LOG
        KONASLOG.add("ez log <player>");
        KONASLOG.add("I just made <player> log with the power of Konas!");

        // Default EXCUSE
        KONASEXCUSE.add("You're such a ping player!");
        KONASEXCUSE.add("Photoshop! Konas users can never die.");
        KONASEXCUSE.add("I was AFK!");
        KONASEXCUSE.add("I bet you wet yourself while killing me! Stupid bedwetter...");

    }

    @Subscriber
    public void onTargetKill(TargetKillEvent event) {
        if (gg.getValue()) {
            sendGG(event.getTarget());
            return;
        }
        if (ez.getValue()) {
            sendEZ(event.getTarget());
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 35) {
                Entity entity = packet.getEntity(mc.world);

                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    if (KonasGlobals.INSTANCE.targetManager.isTarget(player)) {
                        if (pop.getValue()) {
                            sendPOP(player);
                        }
                    }
                }
            }
        }
    }

    @Subscriber
    public void onDeath(LoadGuiEvent event) {
        if (event.getGui() instanceof GuiGameOver) {
            if (excuses.getValue() && excuseTimer.hasPassed(2000)) {
                sendEXCUSE();
                excuseTimer.reset();
            }
        }
    }

    @Subscriber
    public void onJoin(PacketEvent.Receive event) {
        if(mc.player == null || mc.world == null) return;

        if (event.getPacket() instanceof SPacketPlayerListItem) {
            SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
            if(packet.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
                for (SPacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                    EntityPlayer player = mc.world.getPlayerEntityByUUID(data.getProfile().getId());
                    if (player != null && log.getValue() && mc.player.ticksExisted > 100 && KonasGlobals.INSTANCE.targetManager.isTarget(player)) {
                        sendLOG(player);
                    }
                }
            }
        }
    }

    public void sendGG(EntityPlayer player) {
        mc.player.sendChatMessage(KONASGG.get(random.nextInt(KONASGG.size())).replaceAll("<player>", player.getName()));
    }

    public void sendEZ(EntityPlayer player) {
        mc.player.sendChatMessage(KONASEZ.get(random.nextInt(KONASEZ.size())).replaceAll("<player>", player.getName()));
    }

    public void sendPOP(EntityPlayer player) {
        mc.player.sendChatMessage(KONASPOP.get(random.nextInt(KONASPOP.size())).replaceAll("<player>", player.getName()));
    }

    public void sendLOG(EntityPlayer player) {
        mc.player.sendChatMessage(KONASLOG.get(random.nextInt(KONASLOG.size())).replaceAll("<player>", player.getName()));
    }

    public void sendEXCUSE() {
        mc.player.sendChatMessage(KONASEXCUSE.get(random.nextInt(KONASEXCUSE.size())));
    }

}
