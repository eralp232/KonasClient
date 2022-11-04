package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.event.events.ChatEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import org.lwjgl.input.Keyboard;

public class ChatAppend extends Module {

    public static String KONAS_APPEND = " \u23D0 \uff2b\uff4f\uff4e\uff41\uff53";
    public static String KONAS_2B = " : Konas";

    private final Setting<Boolean> strict = new Setting<>("Strict", false);

    public ChatAppend() {
        super("ChatAppend", Keyboard.KEY_NONE, Category.MISC, "ChatSuffix");
    }

    @Subscriber
    public void onMessage(ChatEvent event) {
        if (mc.world == null || mc.player == null) return;
        String s = event.getMessage();
        if (s.startsWith("/") || s.startsWith(Command.getPrefix())) {
            return;
        }
        s += strict.getValue() ? KONAS_2B: KONAS_APPEND;
        if (s.length() >= 256) s = s.substring(0, 256);
        event.setMessage(s);
    }
}
