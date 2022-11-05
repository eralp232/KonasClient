package me.darki.konas;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;

public class KonasRPC {
    private final String ClientId = "813675760953851924";
    private final Minecraft mc = Minecraft.getMinecraft();
    private final DiscordRPC rpc = DiscordRPC.INSTANCE;
    public DiscordRichPresence presence = new DiscordRichPresence();
    private String details;
    private String state;

    public boolean isRunning = false;

    public boolean cute = false;

    public boolean discordLink = false;

    public void enable() {
        if(isRunning) return;
        final DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.disconnected = ((var1, var2) -> System.out.println("Discord RPC disconnected, var1: " + var1 + ", var2: " + var2));
        rpc.Discord_Initialize(ClientId, handlers, true, "");
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.details = KonasMod.VERSION + (cute ? " (Cute)" : "");
        presence.state = "Main Menu";

        rpc.Discord_UpdatePresence(presence);
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    rpc.Discord_RunCallbacks();
                    if (mc.isIntegratedServerRunning() || mc.world == null) {
                        details = KonasMod.VERSION + (cute ? " (Cute)" : "");
                    } else {
                        details = KonasMod.VERSION + " - Playing" + (cute ? " Cute " : " ") + "Multiplayer";
                    }
                    state = "";
                    if (mc.isIntegratedServerRunning()) {
                        state = "YOINKHACK ON TOP";
                    } else if (mc.getCurrentServerData() != null) {
                        if (!mc.getCurrentServerData().serverIP.equals("")) {
                            state = "Playing on " + mc.getCurrentServerData().serverIP;
                        }
                    } else {
                        state = "Main Menu";
                    }
                    if (!details.equals(presence.details) || !state.equals(presence.state)) {
                        presence.startTimestamp = System.currentTimeMillis() / 1000L;
                    }
                    presence.details = details;
                    presence.state = state;
                    rpc.Discord_UpdatePresence(presence);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
            }
        }, "Discord-RPC-Callback-Handler").start();

        isRunning = true;
    }

    public void disable() {
        rpc.Discord_Shutdown();
        isRunning = false;
    }

    public void updateLargeImageText() {

        if(discordLink) {
            presence.largeImageText = "discord.gg/gpVZ4Y6cpq";
            return;
        }

        if(cute) {
            presence.largeImageText = "Cute Konas on top";
        } else {
            presence.largeImageText = "Konas on top";
        }

    }

    public void updateLargeImageKey() {
        presence.largeImageKey = cute ? "cutekonas" : "konas";
    }

    public void setDiscordLink(Boolean value) {
        discordLink = value;
        updateLargeImageText();
        rpc.Discord_UpdatePresence(presence);
    }

    public void setCute(Boolean value) {
        cute = value;
        updateLargeImageText();
        updateLargeImageKey();
        rpc.Discord_UpdatePresence(presence);
    }
}

