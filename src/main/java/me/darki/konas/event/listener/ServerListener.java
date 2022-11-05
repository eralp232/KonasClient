package me.darki.konas.event.listener;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.config.Config;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.event.events.WorldClientInitEvent;
import me.darki.konas.util.client.FakePlayerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;

import java.io.File;

public class ServerListener {

    public static ServerListener INSTANCE = new ServerListener();
    private static final Minecraft mc = Minecraft.getMinecraft();

    @Subscriber
    public void onDisplayerGuiScreen(LoadGuiEvent event) {
        if (event.getGui() instanceof GuiConnecting && mc.getCurrentServerData() != null) {
            FakePlayerManager.clear();
            File file = new File(Config.CONFIGS, "@" + mc.getCurrentServerData().serverIP + ".json");

            if (file.exists()) {
                new Thread(() -> {
                    Config.save(Config.currentConfig);
                    Config.load(file, false);
                    System.out.println("Loaded Config for " + mc.getCurrentServerData().serverIP);
                }).start();
            }
        }
    }

}
