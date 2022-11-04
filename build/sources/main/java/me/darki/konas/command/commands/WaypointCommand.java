package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.Waypoints;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.Logger;
import me.darki.konas.util.waypoint.Waypoint;
import me.darki.konas.util.waypoint.WaypointType;
import net.minecraft.entity.player.EntityPlayer;

import java.text.DecimalFormat;
import java.util.Map;

public class WaypointCommand extends Command {

    private final DecimalFormat df = new DecimalFormat("#.##");

    public WaypointCommand() {
        super("waypoint", "Add, delete, and list waypoints", new SyntaxChunk("<add/del/get/list>"), new SyntaxChunk("<name>"),
                new SyntaxChunk("[x]"), new SyntaxChunk("[y]"), new SyntaxChunk("[z]"));
    }

    @Override
    public void onFire(String[] args) {
        Waypoints waypointsModule = (Waypoints) ModuleManager.getModuleByClass(Waypoints.class);

        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("list")) {
                StringBuilder waypointList = new StringBuilder();
                for (Waypoint waypoint : KonasGlobals.INSTANCE.waypointManager.getWaypoints()) {
                    waypointList.append(" ").append(waypoint.getName());
                }

                for (Map.Entry<EntityPlayer, Long> entry : waypointsModule.getLoggedPlayers().entrySet()) {
                    EntityPlayer entity = entry.getKey();
                    if (entity != mc.player) {
                        waypointList.append(" ").append(entity.getName());
                    }
                }

                Logger.sendChatMessage(waypointList.toString());
            } else {
                Logger.sendChatErrorMessage(getChunksAsString());
            }
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("get")) {
                for (Waypoint waypoint : KonasGlobals.INSTANCE.waypointManager.getWaypoints()) {
                    if (waypoint.getName().equalsIgnoreCase(args[2])) {
                        Logger.sendChatMessage(waypoint.toString());
                    }
                }

                for (Map.Entry<EntityPlayer, Long> entry : waypointsModule.getLoggedPlayers().entrySet()) {
                    EntityPlayer entity = entry.getKey();
                    if (entity != mc.player && entity.getName().equalsIgnoreCase(args[2])) {
                        Logger.sendChatMessage("Waypoint: {" +
                                "name=" + entity.getName() +
                                ", x=" + entity.posX +
                                ", y=" + entity.posY +
                                ", z=" + entity.posZ +
                                ", dimension =" + mc.player.dimension +
                                '}');
                    }
                }
            } else if (args[1].equalsIgnoreCase("del")) {
                for (Waypoint waypoint : KonasGlobals.INSTANCE.waypointManager.getWaypoints()) {
                    if (waypoint.getName().equalsIgnoreCase(args[2])) {
                        KonasGlobals.INSTANCE.waypointManager.delWaypoint(args[2]);
                        Logger.sendChatMessage("Deleted Waypoint &b" + args[2]);
                    }
                }

                waypointsModule.getLoggedPlayers().forEach((entityPlayer, aLong) -> {
                    if (entityPlayer.getName().equalsIgnoreCase(args[2])) {
                        waypointsModule.getLoggedPlayers().remove(entityPlayer);
                        Logger.sendChatMessage("Deleted Waypoint &b" + args[2]);
                    }
                });
            } else if (args[1].equalsIgnoreCase("add")) {
                if(KonasGlobals.INSTANCE.waypointManager.getWaypoint(args[2]) != null) {
                    Logger.sendChatErrorMessage("A Waypoint with this name already exists on this server!");
                    return;
                }
                Waypoint newWaypoint = new Waypoint(args[2], Double.parseDouble(df.format(mc.player.posX)), Double.parseDouble(df.format(mc.player.posY)), Double.parseDouble(df.format(mc.player.posZ)), mc.player.dimension, WaypointType.CUSTOM);
                KonasGlobals.INSTANCE.waypointManager.addWaypoint(newWaypoint);
                Logger.sendChatMessage("Added Waypoint " + newWaypoint.getName() + " at " + newWaypoint.getX() + ", " + newWaypoint.getY() + ", " + newWaypoint.getZ());
            } else {
                Logger.sendChatErrorMessage(getChunksAsString());
            }
        } else if (args.length == 6) {
            if(args[1].equalsIgnoreCase("add")) {
                try {
                    if(KonasGlobals.INSTANCE.waypointManager.getWaypoint(args[2]) != null) {
                        Logger.sendChatErrorMessage("A Waypoint with this name already exists on this server!");
                        return;
                    }
                    Waypoint newWaypoint = new Waypoint(args[2], Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]), mc.player.dimension, WaypointType.CUSTOM);
                    KonasGlobals.INSTANCE.waypointManager.addWaypoint(newWaypoint);
                    Logger.sendChatMessage("Added Waypoint " + newWaypoint.getName() + " at " + newWaypoint.getX() + ", " + newWaypoint.getY() + ", " + newWaypoint.getZ());
                } catch (Exception e) {
                    Logger.sendChatMessage("Please provide a valid coordinate value");
                }
            } else {
                Logger.sendChatErrorMessage(getChunksAsString());
            }
        } else {
            Logger.sendChatErrorMessage(getChunksAsString());
        }
    }
}
