package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerUpdateEvent;
import me.darki.konas.module.Module;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class AntiBot extends Module {

    public static ArrayList<EntityPlayer> bots = new ArrayList<>();

    public AntiBot() {
        super("AntiBot", Category.MISC);
    }

    @Override
    public void onDisable() {
        bots.clear();
    }

    public static List<EntityPlayer> getBots() {
        return bots;
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        setExtraInfo(bots.size() + "");
        if (mc.currentScreen instanceof GuiDownloadTerrain && !bots.isEmpty())
            bots.clear();
        if (mc.world != null) {
            mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer).filter(e -> e != mc.player && e != null).filter(e -> isBot((EntityPlayer)e)).forEach(entity -> {
                if (!bots.contains(entity))
                    bots.add((EntityPlayer)entity);
            });
        }
    }

    public boolean isBot(EntityPlayer player) {
        NetworkPlayerInfo npi = mc.getConnection().getPlayerInfo(player.getGameProfile().getId());
        return npi == null || npi.getResponseTime() <= 0 && !player.equals(mc.player) && npi.getGameProfile() == null && player.hasCustomName();
    }
}
