package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.mixin.mixins.ICPacketChatMessage;
import me.darki.konas.module.Module;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChat;

public class SmartWhisper extends Module {

    private String whisperSender;
    private boolean whispered;
    private boolean newWhisperWhileChatOpen;

    public SmartWhisper() {
        super("SmartWhisper", Category.MISC, "SmartReply");
    }

    @Subscriber
    public void onPacket(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;

        if (event.getPacket() instanceof SPacketChat) {

            String s = ((SPacketChat) event.getPacket()).getChatComponent().getUnformattedText();

            if (s.contains("whispers: ")) {
                if (mc.ingameGUI.getChatGUI().getChatOpen() && whispered) {
                    newWhisperWhileChatOpen = true;
                } else {
                    whisperSender = s.split(" ")[0];
                    whispered = true;
                }
            }

        }

    }

    @Subscriber
    public void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;

        if (event.getPacket() instanceof CPacketChatMessage) {

            CPacketChatMessage packet = (CPacketChatMessage) event.getPacket();

            String s = packet.getMessage();

            if (whisperSender != null && whispered
                    && s.split(" ")[0].equalsIgnoreCase("/r")
                    && newWhisperWhileChatOpen) {
                ((ICPacketChatMessage) packet).setMessage("/msg " + whisperSender + " " + s.substring(3));
            }
        }

    }

}
