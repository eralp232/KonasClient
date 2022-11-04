package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.command.chunks.PlayerChunk;
import me.darki.konas.util.Logger;

import java.util.ArrayList;

public class PartyCommand extends Command {

    public static ArrayList<String> party = new ArrayList<>();

    public PartyCommand() {
        super("party", "Add or Remove People to/from your party", new SyntaxChunk("<add/del/clear/list>") {
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
                if (!party.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\nParty List:\n");
                    party.stream().forEach(member -> sb.append(member + "\n"));
                    Logger.sendChatMessage(sb.toString());
                } else {
                    Logger.sendChatMessage("There is no one in your party");
                }
            } else if (args[1].equalsIgnoreCase("clear")) {
                if(!party.isEmpty()) {
                    party.clear();
                    Logger.sendChatMessage("Cleared your party!");
                } else {
                    Logger.sendChatErrorMessage("Your party is already empty!");
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
            for (String member : party) {
                if (member.equalsIgnoreCase(args[2])) {
                    Logger.sendChatErrorMessage("Player §b" + args[2] + "§r is already in your party!");
                    return;
                }
            }
            party.add(args[2]);
            Logger.sendChatMessage("Added §b" + args[2] + "§r to your party!");
        } else if (args[1].equalsIgnoreCase("del")) {
            for (String member : party) {
                if (member.equalsIgnoreCase(args[2])) {
                    party.remove(member);
                    Logger.sendChatMessage("Deleted §b" + args[2] + "§r from your party");
                    return;
                }
            }
            Logger.sendChatErrorMessage("Player §b" + args[2] + "§r is not in your party!");
        }

    }


}
