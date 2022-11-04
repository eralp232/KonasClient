package me.darki.konas.module.modules.client;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Notify extends Module {

    public TrayIcon trayIcon;
    public SystemTray systemTray;

    private int killStreakCount = 0;

    public Setting<Boolean> moduleNotifs = new Setting<>("Modules", true);
    public Setting<Boolean> twobtwotKick = new Setting<>("2b2tKick", false);
    public ListenableSettingDecorator<Boolean> killStreak = new ListenableSettingDecorator<>("KillStreak", false, (value) -> {
        killStreakCount = 0;
    });
    public Setting<Boolean> donkeyNotifs = new Setting<>("Donkeys", false);
    public Setting<Boolean> llamaNotifs = new Setting<>("Llamas", false);
    public Setting<Boolean> slimeNotifs = new Setting<>("Slimes", false);
    public Setting<Boolean> ghasts = new Setting<>("Ghasts", false);
    public Setting<Boolean> sound = new Setting<>("Sound", true);
    public ListenableSettingDecorator<Boolean> systemTraySetting = new ListenableSettingDecorator<>("SystemTray", false, (value) -> {
        if(value) {
            if (!SystemTray.isSupported()) {
                Logger.sendChatErrorMessage("Your computer does not support system tray notifications.");
                toggle();
                return;
            }
            InputStream resourceAsStream = Notify.class.getResourceAsStream("assets/minecraft/konas/textures/konas.png");
            Image image = null;
            try {
                image = ImageIO.read(resourceAsStream);
            } catch (IOException ignored) {

            }
            if (image == null) {
                Logger.sendChatErrorMessage("Failed to load assets.");
                toggle();
                return;
            }
            trayIcon = new TrayIcon(image, "Konas");
            systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add(trayIcon);
            } catch (AWTException exception) {
                Logger.sendChatErrorMessage("Failed to initialize tray icon.");
                toggle();
                return;
            }
        } else {
            systemTray.remove(trayIcon);
        }
    });

    private ArrayList<Entity> entityList = new ArrayList<>();

    private Timer kickTimer = new Timer();

    private HashMap<Long, Boolean> kickHashmap = new HashMap<Long, Boolean>() {{
        put(20700000L, false);
        put(21000000L, false);
        put(21300000L, false);
        put(21540000L, false);
        put(21570000L, false);
    }};

    public Notify() {
        super("Notify", "Notifies you of various things", Category.CLIENT);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if((!ghasts.getValue() && !donkeyNotifs.getValue() && !llamaNotifs.getValue() && !slimeNotifs.getValue()) || mc.player == null || mc.world == null || event.getPhase() == TickEvent.Phase.START) return;
        for(Entity entity : mc.world.loadedEntityList) {
            if(entity instanceof EntityDonkey && donkeyNotifs.getValue() && !entityList.contains(entity)) {
                Logger.sendChatMessage("Found &bDonkey &fat &b" + "x=" + entity.getPosition().getX() + ",y=" + entity.getPosition().getY() + ",z=" + entity.getPosition().getZ());
                entityList.add(entity);
                if(sound.getValue()) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
                }
            } else if(entity instanceof EntityLlama && llamaNotifs.getValue() && !entityList.contains(entity)) {
                Logger.sendChatMessage("Found &bLlama &fat &b" + "x=" + entity.getPosition().getX() + ",y=" + entity.getPosition().getY() + ",z=" + entity.getPosition().getZ());
                entityList.add(entity);
                if(sound.getValue()) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
                }
            } else if (entity instanceof EntitySlime && slimeNotifs.getValue() && !entityList.contains(entity)) {
                Logger.sendChatMessage("Found &bSlime &fat &b" + "x=" + entity.getPosition().getX() + ",y=" + entity.getPosition().getY() + ",z=" + entity.getPosition().getZ());
                entityList.add(entity);
                if(sound.getValue()) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
                }
            } else if (entity instanceof EntityGhast && ghasts.getValue() && !entityList.contains(entity)) {
                Logger.sendChatMessage("Found &bGhast &fat &b" + "x=" + entity.getPosition().getX() + ",y=" + entity.getPosition().getY() + ",z=" + entity.getPosition().getZ());
                entityList.add(entity);
                if(sound.getValue()) {
                    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F));
                }
            }
        }
    }

    @Subscriber
    public void onUpdate2(UpdateEvent event) {

        if(mc.player == null || mc.world == null || event.getPhase() == TickEvent.Phase.START || !twobtwotKick.getValue()) return;

        if(kickTimer.hasPassed(20700000L) && !kickHashmap.get(20700000L)) {
            Logger.sendChatMessage("You will get kicked in 15 Minutes!");
            kickHashmap.put(20700000L, true);
        }

        if(kickTimer.hasPassed(21000000L) && !kickHashmap.get(21000000L)) {
            Logger.sendChatMessage("You will get kicked in 10 Minutes!");
            kickHashmap.put(21000000L, true);
        }

        if(kickTimer.hasPassed(21300000L) && !kickHashmap.get(21300000L)) {
            Logger.sendChatMessage("You will get kicked in 5 Minutes!");
            kickHashmap.put(21300000L, true);
        }

        if(kickTimer.hasPassed(21540000L) && !kickHashmap.get(21540000L)) {
            Logger.sendChatMessage("You will get kicked in 1 Minute!");
            kickHashmap.put(21540000L, true);
        }

        if(kickTimer.hasPassed(21570000L) && !kickHashmap.get(21570000L)) {
            Logger.sendChatMessage("You will get kicked in 30 Seconds!");
            kickHashmap.put(21570000L, true);
        }

    }

    @Subscriber
    public void onUpdate3(UpdateEvent event) {

        if(mc.player == null || mc.world == null) return;

        if(mc.player.getHealth() <= 0 || mc.player.isDead) {
            killStreakCount = 0;
        }

    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getPacket() instanceof SPacketChat) {
            if(mc.getCurrentServerData().serverIP.toLowerCase().contains("2b2t.org")) {
                if(((SPacketChat) event.getPacket()).getChatComponent().getFormattedText().contains("Position in queue:")) {
                    resetKickTimer();
                }
            }
        }
    }

    @Subscriber
    public void onGuiOpen(LoadGuiEvent event) {
        if(event.getGui() instanceof GuiMultiplayer || event.getGui() instanceof GuiMainMenu) {
            resetKickTimer();
            killStreakCount = 0;
        }
    }

    @Subscriber
    public void onKillTarget(TargetKillEvent event) {
        killStreakCount++;
        if(killStreakCount > 1) {
            Logger.sendChatMessage("You are on a " + killStreakCount + " Kill Streak!");
        }
    }

    private void resetKickTimer() {
        kickTimer.reset();
        for(Map.Entry<Long, Boolean> entry : kickHashmap.entrySet()) {
            kickHashmap.put(entry.getKey(), false);
        }
    }

    @Subscriber
    public void onWorldInit(WorldClientInitEvent event) {
        entityList.clear();
    }

    @Override
    public void onEnable() {
        entityList.clear();
    }

    public void displayNotification(String caption, String text, TrayIcon.MessageType type) {
        trayIcon.displayMessage(caption, text, type);
    }

}
