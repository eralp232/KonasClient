package me.darki.konas.module.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import cookiedragon.eventsystem.EventDispatcher;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.command.commands.PartyCommand;
import me.darki.konas.event.events.*;
import me.darki.konas.event.listener.BackupListener;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.ChatUtil;
import me.darki.konas.util.friends.Friends;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtraChat extends Module {

    public static final Setting<Float> delay = new Setting<>("Delay", 0.5f, 10f, 0f, 1f);
    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.MSG);
    private static final Setting<Boolean> autoBackup = new Setting<>("AutoBackup", false);
    public static final Setting<Boolean> global = new Setting<>("Global", false).withVisibility(autoBackup::getValue);
    private final Setting<Boolean> backupOnPop = new Setting<>("PopBackup", true).withVisibility(autoBackup::getValue);
    private final Setting<Boolean> taunt = new Setting<>("Taunt", false);
    private final Setting<Boolean> antiRacism = new Setting<>("AntiRacism", false);
    private final Setting<Boolean> antiCoordLeak = new Setting<>("AntiCoordLeak", false);
    private final Setting<Boolean> embarrass = new Setting<>("Embarrass", false).withVisibility(antiRacism::getValue);
    private final Setting<Boolean> groom = new Setting<>("AutoGroom", false);
    private final Setting<Float> tauntDelay = new Setting<>("TauntDelay", 15f, 30f, 1f, 1f).withVisibility(taunt::getValue);
    private final Setting<Float> groomDelay = new Setting<>("GroomDelay", 15f, 30f, 1f, 1f).withVisibility(groom::getValue);
    public static final Setting<Boolean> friended = new Setting<>("NotifyFriended", false);
    private final Setting<Boolean> partyChat = new Setting<>("PartyChat", false);
    private final Setting<Boolean> longerChat = new Setting<>("Longer", false);
    private final Setting<Integer> chatHeight = new Setting<>("ChatHeight", 200, 500, 0, 0).withVisibility(longerChat::getValue);
    private final Setting<Boolean> clear = new Setting<>("Clear", false);
    private final Setting<Boolean> chatTimestamps = new Setting<>("ChatTimestamps", true);
    private final Setting<Boolean> twentyFourHourFormat = new Setting<>("24HourFormat", true).withVisibility(chatTimestamps::getValue);
    private final Setting<Boolean> hours = new Setting<>("Hours", true).withVisibility(chatTimestamps::getValue);
    private final Setting<Boolean> minutes = new Setting<>("Minutes", true).withVisibility(chatTimestamps::getValue);
    private final Setting<Boolean> seconds = new Setting<>("Seconds", false).withVisibility(chatTimestamps::getValue);
    private final Setting<Boolean> brackets = new Setting<>("Brackets", true).withVisibility(chatTimestamps::getValue);

    private Random random = new Random();

    private static Optional<Map.Entry<String, String>> parseChatMessage(String messageRaw) {

        Matcher matcher = Pattern.compile("^<(" + "[a-zA-Z0-9_]{3,16}" + ")> (.+)$").matcher(messageRaw);

        String senderName = null;
        String message = null;

        while (matcher.find()) {
            senderName = matcher.group(1);
            message = matcher.group(2);
        }

        if (senderName == null || senderName.isEmpty()) {
            return Optional.empty();
        }

        if (message == null || message.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new AbstractMap.SimpleEntry<>(senderName, message));
    }


    public static final ArrayList<String> TAUNTS = new ArrayList<>();
    public static final ArrayList<String> GROOMS = new ArrayList<>();

    public enum Mode {
        MSG, WHISPER
    }

    public ExtraChat() {
        super("ExtraChat", Category.MISC, "ChatTimestamp", "ChatTimestamps", "ClearChat");
        TAUNTS.add("Lol, you're so ez <player>");
        TAUNTS.add("ur bad");
        TAUNTS.add("ur fat");
        TAUNTS.add("you're dogwater kid");
        TAUNTS.add("im going to destroy you");
        TAUNTS.add("you like men");
        GROOMS.add("Send thigh pics");
        GROOMS.add("Are you a femboy?");
        GROOMS.add("Where do you live?");
        GROOMS.add("How old are you?");
        GROOMS.add("Let's have sex");
        GROOMS.add("Send skirt pics");
        GROOMS.add("I'll get you nitro if you send thigh pics");
        GROOMS.add("Hewwo cutie");
        GROOMS.add("AwA Let's have sex");
    }

    @Subscriber
    public void onChat(ClientChatReceivedEvent event) {

        if(antiRacism.getValue() && (event.getMessage().getUnformattedText().toLowerCase().contains("nigger") || event.getMessage().getUnformattedText().toLowerCase().contains("nigga"))) {
            Optional<Map.Entry<String, String>> parsedMessage = this.parseChatMessage(event.getMessage().getUnformattedText());
            if(parsedMessage.isPresent()) {
                if (embarrass.getValue()) {
                 mc.player.sendChatMessage(parsedMessage.get().getKey() + ", don't be racist");
                } else {
                    DirectMessageEvent directMessageEvent = new DirectMessageEvent(parsedMessage.get().getKey(), "Don't be racist");
                    EventDispatcher.Companion.dispatch(directMessageEvent);
                }
            }
        }

        if (chatTimestamps.getValue()) {

            StringBuilder pattern = new StringBuilder();
            if (brackets.getValue()) pattern.append("<");
            if (hours.getValue())
                pattern.append(twentyFourHourFormat.getValue() ? "HH" : "hh").append(minutes.getValue() || seconds.getValue() ? ":" : "");
            if (minutes.getValue()) pattern.append("mm").append(seconds.getValue() ? ":" : "");
            if (seconds.getValue()) pattern.append("ss");
            pattern.append(twentyFourHourFormat.getValue() ? "" : "aa").append(brackets.getValue() ? "> " : " ");

            TextComponentString textComponent = new TextComponentString(ChatFormatting.GRAY + new SimpleDateFormat(pattern.toString()).format(new Date()) + ChatFormatting.RESET);
            textComponent.appendSibling(event.getMessage());
            event.setMessage(textComponent);
        }

    }

    @Subscriber
    public void onTotemPop(TotemPopEvent event) {
        if (autoBackup.getValue() && backupOnPop.getValue() && event.getPlayer().getUniqueID().equals(mc.player.getUniqueID())) {
            String backupString = "I just popped at X:" + mc.player.getPosition().getX() + " Y:" + mc.player.getPosition().getY() + " Z:" + mc.player.getPosition().getZ() + " in the " + (mc.player.dimension == -1 ? "Nether" : "Overworld") + ", and I need backup!";
            if(global.getValue()) {
                mc.player.sendChatMessage(backupString);
            } else {
                for (String name : PartyCommand.party) {
                    DirectMessageEvent directMessageEvent = new DirectMessageEvent(name, backupString);
                    EventDispatcher.Companion.dispatch(directMessageEvent);
                }
            }
        }
    }

    @Subscriber
    public void onChatRect(ChatRectEvent event) {
        if (clear.getValue()) {
            event.cancel();
        }
    }

    @Subscriber
    public void onChatHeight(GetChatHeightEvent event) {
        if (longerChat.getValue()) {
            event.setHeight(chatHeight.getValue());
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (groom.getValue() && BackupListener.msgQueue.isEmpty() && BackupListener.messageTimer.hasPassed(groomDelay.getValue() * 1000)) {
            Entity target = KonasGlobals.INSTANCE.targetManager.getYoungestTarget();
            if (target != null && !Friends.isFriend(target.getName())) {
                DirectMessageEvent directMessageEvent = new DirectMessageEvent(target.getName(), GROOMS.get(random.nextInt(GROOMS.size())).replaceAll("<player>", target.getName()));
                EventDispatcher.Companion.dispatch(directMessageEvent);
            }
        }
        if (taunt.getValue() && BackupListener.msgQueue.isEmpty() && BackupListener.messageTimer.hasPassed(tauntDelay.getValue() * 1000)) {
            Entity target = KonasGlobals.INSTANCE.targetManager.getYoungestTarget();
            if (target != null && !Friends.isFriend(target.getName())) {
                DirectMessageEvent directMessageEvent = new DirectMessageEvent(target.getName(), TAUNTS.get(random.nextInt(TAUNTS.size())).replaceAll("<player>", target.getName()));
                EventDispatcher.Companion.dispatch(directMessageEvent);
            }
        }
    }

    @Subscriber
    public void onChatEvent(ChatEvent event) {
        if(antiCoordLeak.getValue() && event.getMessage().replaceAll("\\D", "").length() >= 6) {
            event.cancel();
            ChatUtil.error("AntiCoordLeak: Blocked message because it contained 6 or more digits");
            return;
        }
        if (partyChat.getValue() && !event.getMessage().startsWith(Command.getPrefix()) && !event.getMessage().startsWith("/")) {
            event.cancel();
            for (String name : PartyCommand.party) {
                DirectMessageEvent directMessageEvent = new DirectMessageEvent(name, event.getMessage());
                EventDispatcher.Companion.dispatch(directMessageEvent);
            }
        }
    }

    public static class PartyMessage {

        private String name;
        private String message;

        public PartyMessage(String name, String message) {
            this.name = name;
            this.message = message;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
