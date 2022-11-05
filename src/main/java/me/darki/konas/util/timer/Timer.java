package me.darki.konas.util.timer;

public final class Timer {
    private long time;

    public Timer() {
        time = System.currentTimeMillis();
    }

    public boolean hasPassed(double ms) {
        return System.currentTimeMillis() - time >= ms;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
