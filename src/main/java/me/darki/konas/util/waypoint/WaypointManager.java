package me.darki.konas.util.waypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WaypointManager {
    private final ArrayList<Waypoint> waypoints = new ArrayList<>();

    public boolean addWaypoint(Waypoint waypoint) {
        for (Waypoint existingWaypoint : waypoints) {
            if (waypoint.equals(existingWaypoint)) {
                return false;
            }
        }

        waypoints.add(waypoint);
        return true;
    }

    public void delDeaths() {
        waypoints.removeIf(waypoint -> waypoint.getType() == WaypointType.DEATH);
    }

    public void delWaypoint(String name) {
        waypoints.removeIf(waypoint -> waypoint.getName().equalsIgnoreCase(name) && waypoint.isCurrentServer());
    }

    public void clear() {
        waypoints.clear();
    }

    public Waypoint getWaypoint(String name) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.getName().equalsIgnoreCase(name) && waypoint.isCurrentServer()) {
                return waypoint;
            }
        }

        return null;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints.stream().filter(Waypoint::isCurrentServer).collect(Collectors.toList());
    }

    public ArrayList<Waypoint> getRawWaypoints() {
        return waypoints;
    }
}
