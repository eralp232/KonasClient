package me.darki.konas.event.listener;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.util.mute.MuteManager;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteListener {

    private static final Pattern PATTERN_CHAT = Pattern.compile("^<([a-zA-Z0-9_]{3,16})> (.+)$");

    public static MuteListener INSTANCE = new MuteListener();

    @Subscriber(priority = -1)
    public void onChatMessage(ClientChatReceivedEvent event) {
        try {
            Matcher matcher = PATTERN_CHAT.matcher(event.getMessage().getUnformattedText());
            while (matcher.find()) {
                String name = matcher.group(1);
                if(MuteManager.isMuted(name)) {
                    event.setCanceled(true);
                }
            }
        } catch (Exception ignored) {}
    }

}
