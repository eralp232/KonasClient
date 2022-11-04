package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class DisconnectCommand extends Command {
    public DisconnectCommand() {
        super("disconnect", "Kick yourself from a server");
    }

    @Override
    public void onFire(String[] args) {
        if (mc.player != null) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(69420));
        }
    }
}
