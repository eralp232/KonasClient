package me.darki.konas.event.events;


public class OrientCameraEvent extends CancellableEvent {
    private double distance;

    public OrientCameraEvent(double distance) {
        super();
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public static class Pre extends OrientCameraEvent {
        public Pre(double distance) {
            super(distance);
        }
    }

    public static class Post extends OrientCameraEvent {
        public Post(double distance) {
            super(distance);
        }
    }
}
