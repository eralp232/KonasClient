package me.darki.konas.util.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.darki.konas.KonasMod;
import me.darki.konas.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String PREFIX = ChatFormatting.GRAY + "[" + ChatFormatting.DARK_PURPLE + KonasMod.NAME + ChatFormatting.GRAY + "] " + ChatFormatting.RESET;

    public static void info(int id, ChatFormatting color, String msg, Object... args) {
        sendMsg(id, color, msg, args);
    }

    public static void info(ChatFormatting color, String msg, Object... args) {
        info(0, color, msg, args);
    }

    public static void info(int id, String msg, Object... args) {
        info(id, ChatFormatting.WHITE, msg, args);
    }

    public static void info(String msg, Object... args) {
        sendMsg(0, ChatFormatting.WHITE, msg, args);
    }

    public static void warning(String msg, Object... args) {
        sendMsg(0, ChatFormatting.YELLOW, msg, args);
    }

    public static void error(String msg, Object... args) {
        sendMsg(0, ChatFormatting.RED, msg, args);
    }

    public static void lineBreak() {
        sendMsg(0, ChatFormatting.WHITE, "");
    }

    private static void sendMsg(int id, ChatFormatting color, String msg, Object... args) {
        if (mc.world == null) return;

        String message = PREFIX;
        if (color != null) message += color.toString();
        message += formatMessage(msg, color, args);

        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatMessage(message), id);
    }

    private static String formatMessage(String message, ChatFormatting color, Object... args) {
        String msg = String.format(message, args);
        msg = msg.replaceAll("\\(h\\)", ChatFormatting.AQUA.toString()); // Highlight
        msg = msg.replaceAll("\\(b\\)", ChatFormatting.BOLD.toString()); // Bold
        msg = msg.replaceAll("\\(ul\\)", ChatFormatting.UNDERLINE.toString()); // Underline
        msg = msg.replaceAll("\\(r\\)", color.toString()); // Reset to original

        return msg;
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
            return new Logger.ChatMessage(text);
        }

    }

}
