package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.util.Logger;
import me.darki.konas.util.friends.FriendConverter;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.network.APIUtils;

public class FriendSyncCommand extends Command {

    public FriendSyncCommand() {
        super("friendsync", "Sync friends with other clients", new SyntaxChunk("<client>") {
            @Override
            public String predict(String currentArg) {
                if (currentArg.toLowerCase().startsWith("f")) {
                    return "future";
                } else if(currentArg.toLowerCase().startsWith("p")) {
                    return "pyro";
                } else if(currentArg.toLowerCase().startsWith("r")) {
                    return "rusherhack";
                }
                return currentArg;
            }
        });
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        if (args[1].equalsIgnoreCase("future")) {

            new Thread(() -> FriendConverter.futureParse().forEach(name -> {
                if (Friends.isFriend(name)) {
                    Logger.sendChatMessage("Is already a friend: " + name);
                    return;
                }
                String uuid = APIUtils.getUUIDFromName(name);
                if (uuid != null && !uuid.isEmpty()) {
                    Friends.addFriend(name, uuid);
                    Logger.sendChatMessage("Added friend: " + name + " (" + uuid + ")");
                } else {
                    Logger.sendChatErrorMessage("Unable to add friend: " + name);
                }
            })).start();

            return;

        } else if(args[1].equalsIgnoreCase("pyro")) {
            new Thread(() -> FriendConverter.pyroParse().forEach(name -> {
                if (Friends.isFriend(name)) {
                    Logger.sendChatMessage("Is already a friend: " + name);
                    return;
                }
                String uuid = APIUtils.getUUIDFromName(name);
                if (uuid != null && !uuid.isEmpty()) {
                    Friends.addFriend(name, uuid);
                    Logger.sendChatMessage("Added friend: " + name + " (" + uuid + ")");
                } else {
                    Logger.sendChatErrorMessage("Unable to add friend: " + name);
                }
            })).start();
            return;
        } else if(args[1].equalsIgnoreCase("rusherhack")) {
            new Thread(() -> FriendConverter.rusherhackParse().forEach(name -> {
                if (Friends.isFriend(name)) {
                    Logger.sendChatMessage("Is already a friend: " + name);
                    return;
                }
                String uuid = APIUtils.getUUIDFromName(name);
                if (uuid != null && !uuid.isEmpty()) {
                    Friends.addFriend(name, uuid);
                    Logger.sendChatMessage("Added friend: " + name + " (" + uuid + ")");
                } else {
                    Logger.sendChatErrorMessage("Unable to add friend: " + name);
                }
            })).start();
            return;
        }

        Logger.sendChatMessage(this.getChunksAsString());

    }

}
