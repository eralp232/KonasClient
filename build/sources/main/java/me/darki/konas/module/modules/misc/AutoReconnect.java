package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.gui.reconnect.KonasGuiDisconnected;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.event.world.WorldEvent;

public class AutoReconnect extends Module {

    private ServerData lastServer;

    public Setting<Integer> delay = new Setting<>("Delay", 5, 30, 1, 1);

    public AutoReconnect() {
        super("AutoReconnect", Category.MISC);
    }

    @Subscriber
    public void onLoadGui(LoadGuiEvent event) {
        if(event.getGui() instanceof GuiDisconnected) {
            updateLastServer();
            event.setGui(new KonasGuiDisconnected((GuiDisconnected) event.getGui(), lastServer, delay.getValue()));
        }
    }

    @Subscriber
    public void onWorldUnload(WorldEvent.Unload event) {
        updateLastServer();
    }

    private void updateLastServer() {
        ServerData data = mc.getCurrentServerData();
        if(data != null) {
            lastServer = data;
        }
    }

}
