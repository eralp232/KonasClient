package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.network.play.server.SPacketChat;

public class AntiSpam extends Module {

    private static String[] discordStringArray =
            {
                    "discord.gg",
            };

    private static String[] domainStringArray =
            {
                    ".com",
                    ".ru",
                    ".net",
                    ".in",
                    ".ir",
                    ".au",
                    ".uk",
                    ".de",
                    ".br",
                    ".xyz",
                    ".org",
                    ".co",
                    ".cc",
                    ".me",
                    ".tk",
                    ".us",
                    ".bar",
                    ".gq",
                    ".nl",
                    ".space"
            };

    private static String[] announcerStringArray =
            {
                    "Looking for new anarchy servers?",
                    "I just walked",
                    "I just flew",
                    "I just placed",
                    "I just ate",
                    "I just healed",
                    "I just took",
                    "I just spotted",
                    "I walked",
                    "I flew",
                    "I walked",
                    "I flew",
                    "I placed",
                    "I ate",
                    "I healed",
                    "I took",
                    "I gained",
                    "I mined",
                    "I lost",
                    "I moved"
            };

    private static Setting<Boolean> discordLinks = new Setting<>("Discord Invites", true);
    private static Setting<Boolean> domains = new Setting<>("Domains", false);
    private static Setting<Boolean> announcer = new Setting<>("Announcer", true);

    public AntiSpam() {
        super("AntiSpam", Category.MISC, "NoSpam", "NoLinks");
    }

    @Subscriber
    private void onPacket(PacketEvent.Receive event) {

        if (mc.world == null || mc.player == null) return;

        if (!(event.getPacket() instanceof SPacketChat)) {
            return;
        }

        SPacketChat chatMessage = (SPacketChat) event.getPacket();


        if (detectSpam(chatMessage.getChatComponent().getUnformattedText())) {
            event.setCancelled(true);
        }
    }


    private boolean detectSpam(String message) {
        if (discordLinks.getValue()) {
            for (String discordSpam : discordStringArray) {
                if (message.contains(discordSpam)) {
                    return true;
                }
            }
        }

        if (announcer.getValue()) {
            for (String announcerSpam : announcerStringArray) {
                if (message.contains(announcerSpam)) {
                    return true;
                }
            }
        }

        if(domains.getValue()) {
            for (String domainSpam : domainStringArray) {
                if(message.contains(domainSpam)) {
                    return true;
                }
            }
        }

        return false;
    }

}