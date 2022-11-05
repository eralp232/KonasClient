package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IChatLine;
import me.darki.konas.mixin.mixins.IGuiNewChat;
import me.darki.konas.mixin.mixins.ISPacketChat;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.LoginUtils;
import me.darki.konas.util.network.APIUtils;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.client.event.ClientChatEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KonasChat extends Module {

    private final Timer cooldown = new Timer();
    public static boolean firstRun = true;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String truth = null;

    public ListenableSettingDecorator<Mode> mode = new ListenableSettingDecorator<>("Mode", Mode.ROT13, value -> {
        if(value == Mode.PROTOCOL) {
            if(!firstRun && !cooldown.hasPassed(10000)) {
                Logger.sendChatMessage("Please wait another " + Math.abs(((System.currentTimeMillis() - cooldown.getTime()) / 1000) - 10) + " seconds, before you enable this setting again!");
                this.mode.cancel();
            } else {
                if (truth == null) {
                    truth = APIUtils.getTruth();
                }
                connect();
                cooldown.reset();
            }
        } else {
            disconnect();
        }
    });
    public Setting<Boolean> encrypt = new Setting<>("Encrypt", true);

    private final String rotCode = "kcr";

    // unencrypted, encrypted
    private final ConcurrentHashMap<String, String> sentMessages = new ConcurrentHashMap<>();

    public KonasChat() {
        super("KonasChat", "Encrypt chat messages among Konas Users", Category.MISC);
    }

    private enum Mode {
        ROT13, PROTOCOL
    }

    @Subscriber
    public void onChatMessage(ClientChatEvent event) {
        String message = event.getMessage();

        if(message.startsWith("/") || message.startsWith(Command.getPrefix()) || !encrypt.getValue()) return;

        switch (mode.getValue()) {
            case ROT13:
                String encodedMessage = rotCode + rot13(message);
                sentMessages.put(message, encodedMessage);
                event.setMessage(encodedMessage);
                break;
            case PROTOCOL:
                sendSocketMessage(message);
                event.setCanceled(true);
                break;
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (ModuleManager.getModuleByClass(ChatAppend.class).isEnabled()) {
            ModuleManager.getModuleByClass(ChatAppend.class).setEnabled(false);
        }

        List<ChatLine> chatHistory = new ArrayList<>(((IGuiNewChat) mc.ingameGUI.getChatGUI()).getDrawnChatLines());
        for(ChatLine line : chatHistory) {
            for(Map.Entry<String, String> entry : sentMessages.entrySet()) {
                String unencryptedMessage = entry.getKey();
                String encryptedMessage = entry.getValue();
                if (line.getChatComponent().getUnformattedText().contains(encryptedMessage)) {
                    ((IChatLine) line).setLineString(new Logger.ChatMessage(Command.SECTIONSIGN + "bKonasChat:" + Command.SECTIONSIGN + "r " + line.getChatComponent().getFormattedText().replace(encryptedMessage, unencryptedMessage)));
                    sentMessages.remove(unencryptedMessage);
                }
            }
        }
        ((IGuiNewChat) mc.ingameGUI.getChatGUI()).setDrawnChatLines(chatHistory);
    }

    @Override
    public void onEnable() {
        if (truth == null) {
            truth = APIUtils.getTruth();
        }
        if(mode.getValue() == Mode.PROTOCOL) {
            if(!firstRun && !cooldown.hasPassed(10000)) {
                Logger.sendChatMessage("Please wait another " + Math.abs(((System.currentTimeMillis() - cooldown.getTime()) / 1000) - 10) + " seconds, before you enable this module again!");
                this.toggle();
            } else {
                connect();
                cooldown.reset();
            }
        }
    }

    @Override
    public void onDisable() {
        disconnect();
    }

    @Subscriber
    public void onChatMessageReceived(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketChat) {
            Optional<Map.Entry<String, String>> parsedMessage = parseChatMessage(((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText());
            if (parsedMessage.isPresent()) {
                Map.Entry<String, String> entry = parsedMessage.get();
                if (entry.getValue().startsWith(rotCode)) {
                    String decodedMessage = rot13(entry.getValue().substring(rotCode.length()));
                    ((ISPacketChat) event.getPacket()).setChatComponent(new Logger.ChatMessage(Command.SECTIONSIGN + "bKonasChat: " + Command.SECTIONSIGN + "f<" + entry.getKey() + "> " + Command.SECTIONSIGN + "r" + decodedMessage));
                }
            }
        }
    }

    private void connect() {

        try {
            if(!LoginUtils.sessionValid()) {
                Logger.sendChatErrorMessage("Please log in to use Konas Chat");
                return;
            }
            if(socket != null) {
                socket.close();
            }
            socket = new Socket("auth.konasclient.com", 21122);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Listener listener = new Listener(socket, in);
            listener.start();
            out.writeUTF("CONNECT");
            out.writeUTF(truth);
            if(socket == null) {
                Logger.sendChatErrorMessage("Couldn't authenticate with Chat Server, please restart your client!");
                return;
            }
            out.writeUTF(mc.player.getName());
            out.flush();
        } catch (IOException e) {
            System.err.println("Couldn't connect to Chat Server");
        }
    }

    private void disconnect() {
        try {
            if(socket != null) {
                socket.close();
            }
            socket = null;
            in = null;
            out = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSocketMessage(String message) {
        if(out == null) return;
        try {
            out.writeUTF("MESSAGE");
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Sending message failed");
        }
    }

    private String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'm') c += 13;
            else if (c >= 'A' && c <= 'M') c += 13;
            else if (c >= 'n' && c <= 'z') c -= 13;
            else if (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
    }

    public static Optional<Map.Entry<String, String>> parseChatMessage(String messageRaw) {

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

    private static class Listener extends Thread {

        Socket socket;
        DataInputStream in;

        public Listener(Socket socket, DataInputStream in) {
            this.socket = socket;
            this.in = in;
        }

        @Override
        public void run() {
                while(socket.isConnected()) {
                    try {
                        String input = in.readUTF();
                        if(input.equals("MESSAGE")) {
                            String user = in.readUTF();
                            String message = in.readUTF();
                            Logger.sendChatMessage("§b" + user + ": " + message);
                        }
                    } catch (IOException ignored) {}
                }
        }

    }

}
