package me.darki.konas.event.events;

public class UpdateWalkingPlayerEvent extends CancellableEvent {

    protected double x;
    protected double y;
    protected double z;
    protected float yaw;
    protected float pitch;
    protected boolean onGround;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public static class Pre extends UpdateWalkingPlayerEvent {
        private static UpdateWalkingPlayerEvent.Pre INSTANCE = new UpdateWalkingPlayerEvent.Pre();

        public static UpdateWalkingPlayerEvent.Pre get(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            INSTANCE.setCancelled(false);
            INSTANCE.x = x;
            INSTANCE.y = y;
            INSTANCE.z = z;
            INSTANCE.yaw = yaw;
            INSTANCE.pitch = pitch;
            INSTANCE.onGround = onGround;
            return INSTANCE;
        }
    }

    public static class Post extends UpdateWalkingPlayerEvent {
        private static UpdateWalkingPlayerEvent.Post INSTANCE = new UpdateWalkingPlayerEvent.Post();

        public static UpdateWalkingPlayerEvent.Post get(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            INSTANCE.setCancelled(false);
            INSTANCE.x = x;
            INSTANCE.y = y;
            INSTANCE.z = z;
            INSTANCE.yaw = yaw;
            INSTANCE.pitch = pitch;
            INSTANCE.onGround = onGround;
            return INSTANCE;
        }
    }

}
