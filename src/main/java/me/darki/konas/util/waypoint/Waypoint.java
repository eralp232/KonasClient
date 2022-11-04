package me.darki.konas.util.waypoint;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class Waypoint {
    private final String name;

    private final double x;
    private final double y;
    private final double z;

    private final int dimension;

    private String server;

    private final WaypointType type;

    public Waypoint(String name, double x, double y, double z, int dimension, WaypointType type) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.server = "";
        try {
            if (Minecraft.getMinecraft().isSingleplayer()) {
                this.server = Minecraft.getMinecraft().player.getEntityWorld().getWorldInfo().getWorldName();
            } else if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                this.server = Minecraft.getMinecraft().getCurrentServerData().serverIP.replaceAll(":", "_");
            }
        } catch (NullPointerException npe) {
            this.server = "";
        }
        this.type = type;
    }

    public Waypoint(String name, double x, double y, double z, int dimension, String server, WaypointType type) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.server = server;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getDimension() {
        return dimension;
    }

    public String getServer() {
        return server;
    }

    public boolean isCurrentServer() {
        try {
            if (Minecraft.getMinecraft().isSingleplayer()) {
                return this.server.equalsIgnoreCase(Minecraft.getMinecraft().player.getEntityWorld().getWorldInfo().getWorldName());
            } else if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                return this.server.equalsIgnoreCase(Minecraft.getMinecraft().getCurrentServerData().serverIP.replaceAll(":", "_"));
            }
        } catch (NullPointerException npe) {
            this.server = "";
        }

        return false;
    }

    public WaypointType getType() {
        return type;
    }

    public double distanceTo(Waypoint otherWaypoint) {
        double scaleFactor = 1D;
        if (this.dimension == -1 && otherWaypoint.getDimension() != -1) {
            scaleFactor = 8;
        }
        double d0 = otherWaypoint.getX() - this.x * scaleFactor;
        double d1 = otherWaypoint.getY() - this.y * scaleFactor;
        double d2 = otherWaypoint.getZ() - this.z * scaleFactor;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    @Override
    public String toString() {
        return "Waypoint: {" +
                "name=" + name +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", dimension=" + dimension +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        return Double.compare(waypoint.x, x) == 0D && Double.compare(waypoint.y, y) == 0D && Double.compare(waypoint.z, z) == 0D && dimension == waypoint.dimension && type == waypoint.type && server.equalsIgnoreCase(waypoint.getServer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, dimension, server);
    }
}
