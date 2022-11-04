package me.darki.konas.command;

import me.darki.konas.command.commands.*;

import java.util.ArrayList;

public class CommandManager {

    private static ArrayList<Command> commands = new ArrayList<>();

    public static void init() {

        commands.add(new BindCommand());
        commands.add(new CommandsCommand());
        commands.add(new DrawCommand());
        commands.add(new LanguageCommand());
        commands.add(new ToggleCommand());
        commands.add(new FriendCommand());
        commands.add(new SpectateCommand());
        commands.add(new FriendSyncCommand());
        commands.add(new PrefixCommand());
        commands.add(new SetCommand());
        commands.add(new ModulesCommand());
        commands.add(new SettingsCommand());
        commands.add(new SearchCommand());
        commands.add(new FastPlaceCommand());
        commands.add(new InvCleanerCommand());
        commands.add(new NoDesyncCommand());
        commands.add(new GotoCommand());
        commands.add(new YawCommand());
        commands.add(new VanishCommand());
        commands.add(new VClipCommand());
        commands.add(new HClipCommand());
        commands.add(new TeleportCommand());
        commands.add(new FOVCommand());
        commands.add(new GrabCommand());
        commands.add(new MacroCommand());
        commands.add(new ConfigCommand());
        commands.add(new FontCommand());
        commands.add(new WaypointCommand());
        commands.add(new SpammerCommand());
        commands.add(new XRayCommand());
        commands.add(new BookCommand());
        commands.add(new PartyCommand());
        commands.add(new BackupCommand());
        commands.add(new MuteCommand());
        commands.add(new ModuleConfigCommand());
        commands.add(new DisconnectCommand());
        commands.add(new ScaffoldCommand());
        commands.add(new SeizureCommand());
        commands.add(new BreadcrumsCommand());
        commands.add(new NameMCCommand());

    }

    public static ArrayList<Command> getCommands() {
        return commands;
    }

    public static Command getCommandByName(String name) {
        for (Command command : commands) {
            if (name.startsWith(Command.prefix)) {
                name = name.substring(1);
            }
            if (command.isNameOrAlias(name)) {
                return command;
            }
        }

        return null;

    }

}
