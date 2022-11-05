package me.darki.konas.event.events;

public class KeyEvent {

    private final int key;

    public KeyEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
