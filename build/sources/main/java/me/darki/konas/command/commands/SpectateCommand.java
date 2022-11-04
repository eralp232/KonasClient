package me.darki.konas.command.commands;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.command.chunks.PlayerChunk;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.util.Logger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;

public class SpectateCommand extends Command {

    public EntityPlayer spectateEntity;

    public boolean spectating = false;

    public SpectateCommand() {
        super("spectate", "Makes you spectate other players", new PlayerChunk("<player>"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        if (args[1].equals("off") && spectateEntity != null) {
            doSpectateDisable();
            return;
        }

        if (mc.world.getPlayerEntityByName(args[1]) != null) {
            spectateEntity = mc.world.getPlayerEntityByName(args[1]);
            spectating = true;
            mc.setRenderViewEntity(spectateEntity);
            Logger.sendChatMessage("You are now spectating " + Command.SECTIONSIGN + "b" + spectateEntity.getName());
        } else {
            Logger.sendChatMessage("Cant find player " + Command.SECTIONSIGN + "b" + args[1]);
        }

    }

    @Subscriber
    public void onTick(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (!spectating) return;
        if (mc.world.getEntityByID(spectateEntity.getEntityId()) == null) {
            doSpectateDisable();
        }
    }

    @Subscriber
    public void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;
        if (!spectating) return;
        if ((event.getPacket() instanceof CPacketPlayer.PositionRotation
                || event.getPacket() instanceof CPacketPlayer.Position
                || event.getPacket() instanceof CPacketPlayer.Rotation)
                || event.getPacket() instanceof CPacketAnimation
                && spectating) {
            event.setCancelled(true);
        }
    }

    private void doSpectateDisable() {
        spectating = false;
        mc.setRenderViewEntity(mc.player);
        Logger.sendChatMessage("Stopped spectating " + Command.SECTIONSIGN + "b" + spectateEntity.getName());
        spectateEntity = null;
    }

    @Subscriber
    public void onPacket(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;
        if (!spectating) return;
        if (event.getPacket() instanceof SPacketAnimation) {
            SPacketAnimation packet = (SPacketAnimation) event.getPacket();
            if (packet.getAnimationType() == 0) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }

}
