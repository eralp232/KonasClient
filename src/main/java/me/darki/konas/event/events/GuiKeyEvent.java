package me.darki.konas.event.events;

public class GuiKeyEvent extends CancellableEvent {

    private int keyCode;

    public GuiKeyEvent(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }
}
