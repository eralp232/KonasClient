package me.darki.konas.util.timer;

public class NanoTimer {
    private long time;

    public NanoTimer() {
        time = System.nanoTime();
    }

    public boolean hasPassed(double ns) {
        return System.nanoTime() - time >= ns;
    }

    public void reset() {
        time = System.nanoTime();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
