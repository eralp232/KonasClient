package me.darki.konas.util;

import me.darki.konas.event.listener.KeyListener;
import me.darki.konas.module.modules.client.Macros;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import org.jetbrains.annotations.NotNull;

import java.util.ConcurrentModificationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class Logger {

    private static final String PREFIX = "&7[&5yoinkhack&7]&f ";

    public static void sendChatMessage(String message) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) {
            return;
        }
        if (KeyListener.isMacro && Macros.oneLine.getValue()) {
            sendOptionalDeletableMessage(message, 4444);
            return;
        }
        Minecraft.getMinecraft().player.sendMessage(new ChatMessage(PREFIX + message.replaceAll("§", "\u00a7")) {
        });
    }

    public static void sendOptionalDeletableMessage(String message) {
        sendOptionalDeletableMessage(message, 69420);
    }

    public static void sendOptionalDeletableMessage(String message, int id) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) {
            return;
        }
        try {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatMessage(PREFIX + message.replaceAll("§", "\u00a7")),
                    id);
        } catch (ConcurrentModificationException exception) {
            Minecraft.getMinecraft().player.sendMessage(new ChatMessage(PREFIX + message.replaceAll("§", "\u00a7")) {
            });
        }
    }

    public static void sendChatErrorMessage(String message) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) {
            return;
        }
        Minecraft.getMinecraft().player.sendMessage(new ChatMessage(PREFIX + "&c" + message.replaceAll("§", "\u00a7")) {
        });
    }

    public static class ChatMessage extends TextComponentBase {

        private final String text;

        public ChatMessage(String text) {

            Pattern p = Pattern.compile("&[0123456789abcdefrlosmk]");
            Matcher m = p.matcher(text);
            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                String replacement = "\u00A7" + m.group().substring(1);
                m.appendReplacement(sb, replacement);
            }

            m.appendTail(sb);

            this.text = sb.toString();
        }

        @NotNull
        public String getUnformattedComponentText() {
            return text;
        }

        @NotNull
        @Override
        public ITextComponent createCopy() {
            return new ChatMessage(text);
        }

    }

}
