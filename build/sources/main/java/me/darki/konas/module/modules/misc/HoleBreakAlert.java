package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.module.Module;
import me.darki.konas.util.Logger;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class HoleBreakAlert extends Module {


    public HoleBreakAlert() {
        super("HoleBreakAlert", Category.MISC, "HoleBreakNotifier");
    }


    @Subscriber
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockBreakAnim) {

            SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim) event.getPacket();

            if (isHoleBlock(packet.getPosition())) {

                Logger.sendOptionalDeletableMessage("The hole block to your " + getBlockDirectionFromPlayer(packet.getPosition()) + " is being broken by " + Objects.requireNonNull(mc.world.getEntityByID(packet.getBreakerId())).getName(), 44420);

            }

        }
    }


    public static String getBlockDirectionFromPlayer(BlockPos pos) {

        double posX = Math.floor(mc.player.posX);
        double posZ = Math.floor(mc.player.posZ);

        double x = posX - pos.getX();
        double z = posZ - pos.getZ();

        switch (mc.player.getHorizontalFacing()) {
            case SOUTH:
                if (x == 1) {
                    return "right";
                } else if (x == -1) {
                    return "left";
                } else if (z == 1) {
                    return "back";
                } else if (z == -1) {
                    return "front";
                }
                break;
            case WEST:
                if (x == 1) {
                    return "front";
                } else if (x == -1) {
                    return "back";
                } else if (z == 1) {
                    return "right";
                } else if (z == -1) {
                    return "left";
                }
                break;
            case NORTH:
                if (x == 1) {
                    return "left";
                } else if (x == -1) {
                    return "right";
                } else if (z == 1) {
                    return "front";
                } else if (z == -1) {
                    return "back";
                }
                break;
            case EAST:
                if (x == 1) {
                    return "back";
                } else if (x == -1) {
                    return "front";
                } else if (z == 1) {
                    return "left";
                } else if (z == -1) {
                    return "right";
                }
                break;
            default:
                return "undetermined";
        }

        return null;

    }

    private boolean isHoleBlock(BlockPos pos) {

        double posX = Math.floor(mc.player.posX);
        double posZ = Math.floor(mc.player.posZ);

        Block block = mc.world.getBlockState(pos).getBlock();

        if(block == Blocks.BEDROCK || block == Blocks.OBSIDIAN) {
            if (pos.getX() == (posX + 1) && pos.getY() == mc.player.getPosition().getY()) {
                return true;
            } else if (pos.getX() == (posX - 1) && pos.getY() == mc.player.getPosition().getY()) {
                return true;
            } else if (pos.getZ() == (posZ + 1) && pos.getY() == mc.player.getPosition().getY()) {
                return true;
            } else return pos.getZ() == (posZ - 1) && pos.getY() == mc.player.getPosition().getY();
        }

        return false;

    }


}
