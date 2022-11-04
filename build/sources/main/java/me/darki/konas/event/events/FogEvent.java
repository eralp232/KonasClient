package me.darki.konas.event.events;

public class FogEvent extends CancellableEvent {

    public static class Density extends FogEvent {

        private float density;

        public Density(float density) {
            this.density = density;
        }

        public float getDensity() {
            return density;
        }

        public void setDensity(float density) {
            this.density = density;
        }
    }

    public static class Color extends FogEvent {

        private float r;
        private float g;
        private float b;

        public Color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public float getR() {
            return r;
        }

        public void setR(float r) {
            this.r = r;
        }

        public float getG() {
            return g;
        }

        public void setG(float g) {
            this.g = g;
        }

        public float getB() {
            return b;
        }

        public void setB(float b) {
            this.b = b;
        }
    }



}
