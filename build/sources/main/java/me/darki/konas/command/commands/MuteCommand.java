package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.command.chunks.PlayerChunk;
import me.darki.konas.util.Logger;
import me.darki.konas.util.mute.MuteManager;

public class MuteCommand extends Command {

    public MuteCommand() {
        super("mute", "Mute or unmute people", new SyntaxChunk("<add/del/clear/list>") {
            @Override
            public String predict(String currentArg) {
                if (currentArg.toLowerCase().startsWith("a")) {
                    return "add";
                } else if (currentArg.toLowerCase().startsWith("d")) {
                    return "del";
                } else if (currentArg.toLowerCase().startsWith("c")) {
                    return ("clear");
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
                if (!MuteManager.getMuted().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\nMute List:\n");
                    MuteManager.getMuted().stream().forEach(member -> sb.append(member + "\n"));
                    Logger.sendChatMessage(sb.toString());
                } else {
                    Logger.sendChatMessage("You don't have anyone muted");
                }
            } else if (args[1].equalsIgnoreCase("clear")) {
                if(!MuteManager.getMuted().isEmpty()) {
                    MuteManager.clearMuted();
                    Logger.sendChatMessage("Cleared your muted list!");
                } else {
                    Logger.sendChatErrorMessage("Your muted list is already clear!");
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
            for (String member : MuteManager.getMuted()) {
                if (member.equalsIgnoreCase(args[2])) {
                    Logger.sendChatErrorMessage("Player §b" + args[2] + "§r is already muted!");
                    return;
                }
            }
            MuteManager.addMuted(args[2]);
            Logger.sendChatMessage("Muted §b" + args[2]);
        } else if (args[1].equalsIgnoreCase("del")) {
            for (String member : MuteManager.getMuted()) {
                if (member.equalsIgnoreCase(args[2])) {
                    MuteManager.removeMuted(member);
                    Logger.sendChatMessage("Unmuted §b" + args[2]);
                    return;
                }
            }
            Logger.sendChatErrorMessage("Player §b" + args[2] + "§r is not muted!");
        }

    }

}
