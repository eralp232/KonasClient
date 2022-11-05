package me.darki.konas.event.events;

public class GetChatHeightEvent {

    private int height;

    public GetChatHeightEvent(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
