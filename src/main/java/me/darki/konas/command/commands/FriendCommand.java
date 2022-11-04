package me.darki.konas.command.commands;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.command.chunks.PlayerChunk;
import me.darki.konas.event.events.DirectMessageEvent;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.misc.ExtraChat;
import me.darki.konas.util.Logger;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.network.APIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.regex.Pattern;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", "Add and remove friends", new SyntaxChunk("<add/del/list>") {
            @Override
            public String predict(String currentArg) {
                if (currentArg.toLowerCase().startsWith("a")) {
                    return "add";
                } else if (currentArg.toLowerCase().startsWith("d")) {
                    return "del";
                } else if (currentArg.toLowerCase().startsWith("l")) {
                    return "list";
                }
                return currentArg;
            }
        }, new PlayerChunk("[name]"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                if (!Friends.getFriends().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n");
                    Friends.getFriends().stream().forEach(friend -> sb.append(friend.getName() + "\n"));
                    Logger.sendChatMessage(sb.toString());
                } else {
                    Logger.sendChatMessage("You dont have any friends :(");
                }
            } else {
                Logger.sendChatErrorMessage("Invalid Argument. Check syntax");
            }
            return;
        }
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {

            if (args[2].equalsIgnoreCase(mc.player.getName())) {
                Logger.sendChatErrorMessage("You cant add yourself as a friend");
                return;
            }

            if (args[2].equalsIgnoreCase("subaru")) {
                Logger.sendChatErrorMessage("Downloading subaruhack...");
                return;
            }

            args[2] = args[2].replaceAll(String.valueOf(Pattern.compile("[^a-zA-Z0-9_]{1,16}")), "");

            if (Minecraft.getMinecraft().world.getPlayerEntityByName(args[2]) != null) {
                if (Friends.isFriend(args[2])) {
                    Logger.sendChatMessage(args[2] + " is already your friend!");
                    return;
                }
                EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByName(args[2]);
                Friends.addFriend(player.getName(), player.getUniqueID().toString().replace("-", ""));
                if (ModuleManager.getModuleByClass(ExtraChat.class).isEnabled() && ExtraChat.friended.getValue()) {
                    EventDispatcher.Companion.dispatch(new DirectMessageEvent(args[2], "I just friended you on Konas!"));
                }
                Logger.sendChatMessage("Added &b" + args[2] + "&f as friend");
            } else if (APIUtils.getUUIDFromName(args[2]) != null) {
                if (Friends.isFriend(args[2])) {
                    Logger.sendChatMessage(args[2] + " is already your friend!");
                    return;
                }
                Friends.addFriend(APIUtils.getNameFromUUID(APIUtils.getUUIDFromName(args[2])), APIUtils.getUUIDFromName(args[2]));
                if (ModuleManager.getModuleByClass(ExtraChat.class).isEnabled() && ExtraChat.friended.getValue()) {
                    EventDispatcher.Companion.dispatch(new DirectMessageEvent(args[2], "I just friended you on Konas!"));
                }
                Logger.sendChatMessage("Added &b" + APIUtils.getNameFromUUID(APIUtils.getUUIDFromName(args[2])) + "&f as friend");
            } else {
                Logger.sendChatErrorMessage("Player not found");
            }

        } else if (args[1].equalsIgnoreCase("del")) {

            args[2] = args[2].replaceAll(String.valueOf(Pattern.compile("[^a-zA-Z0-9_]{1,16}")), "");

            if (Friends.isFriend(args[2])) {
                Friends.delFriend(args[2]);
                Logger.sendChatMessage("Removed &b" + args[2] + "&f as friend");
            } else {
                Logger.sendChatErrorMessage("Player not found");
            }
        }

    }


}
