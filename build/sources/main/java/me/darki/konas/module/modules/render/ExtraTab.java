package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.event.events.RenderPlayerInTabEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.friends.Friends;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExtraTab extends Module {
    private static Setting<Integer> maxSize = new Setting<>("MaxSize", 200, 400, 10, 10);
    private static Setting<SortMode> sortMode = new Setting<>("SortMode", SortMode.VANILLA);
    private static Setting<Boolean> onlyFriends = new Setting<>("OnlyFriends", false);

    private enum SortMode {
        VANILLA, PING, LENGTH
    }

    public ExtraTab() {
        super("ExtraTab", Category.RENDER, "TabPlus", "MoreTab");
    }

    public static List<NetworkPlayerInfo> subList(List<NetworkPlayerInfo> list, List<NetworkPlayerInfo> newList) {
        if (ModuleManager.getModuleByClass(ExtraTab.class).isEnabled()) {
            if (sortMode.getValue() == SortMode.VANILLA) {
                return list.stream()
                        .filter(networkPlayerInfo -> !onlyFriends.getValue() || Friends.isUUIDFriend(networkPlayerInfo.getGameProfile().getId().toString()))
                        .limit(maxSize.getValue()).collect(Collectors.toList());
            } else if (sortMode.getValue() == SortMode.PING) {
                return list.stream()
                        .filter(networkPlayerInfo -> !onlyFriends.getValue() || Friends.isUUIDFriend(networkPlayerInfo.getGameProfile().getId().toString()))
                        .sorted(Comparator.comparing(NetworkPlayerInfo::getResponseTime))
                        .limit(maxSize.getValue()).collect(Collectors.toList());
            } else {
                return list.stream()
                        .filter(networkPlayerInfo -> !onlyFriends.getValue() || Friends.isUUIDFriend(networkPlayerInfo.getGameProfile().getId().toString()))
                        .sorted(Comparator.comparing(e -> e.getGameProfile().getName().length()))
                        .limit(maxSize.getValue()).collect(Collectors.toList());
            }
        } else {
            return newList;
        }
    }

    @Subscriber
    public void onPlayerRender(RenderPlayerInTabEvent event) {
        if(Friends.isFriend(event.getName())) {
            event.setName(Command.SECTIONSIGN + "b" + event.getName());
        }
    }

}
