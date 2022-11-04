package me.darki.konas.event.events;

public class GuiMouseEvent {

    private final int mouseX;
    private final int mouseY;
    private final int mouseButton;

    private boolean actionDone;

    public GuiMouseEvent(int mouseX, int mouseY, int mouseButton) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.mouseButton = mouseButton;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getMouseButton() {
        return mouseButton;
    }

    public boolean isActionDone() {
        return actionDone;
    }

    public void setActionDone(boolean actionDone) {
        this.actionDone = actionDone;
    }
}
