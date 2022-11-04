package me.darki.konas.command.commands;

import me.darki.konas.command.Command;

public class SeizureCommand extends Command {
    public static boolean seizure = false;

    public SeizureCommand() {
        super("seizure", "Gives you a seizure");
    }

    @Override
    public void onFire(String[] args) {
        seizure = true;
    }
}
