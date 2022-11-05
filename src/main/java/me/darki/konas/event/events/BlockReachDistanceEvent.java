package me.darki.konas.event.events;

public class BlockReachDistanceEvent {
    private static BlockReachDistanceEvent INSTANCE = new BlockReachDistanceEvent();

    private float reachDistance;

    public static BlockReachDistanceEvent get(float reachDistance) {
        INSTANCE.reachDistance = reachDistance;
        return INSTANCE;
    }

    public float getReachDistance() {
        return reachDistance;
    }

    public void setReachDistance(float reachDistance) {
        this.reachDistance = reachDistance;
    }
}
