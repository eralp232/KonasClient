package me.darki.konas.event.events;

public class RenderPlayerInTabEvent {

    private String name;

    public RenderPlayerInTabEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
