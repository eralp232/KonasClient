package me.darki.konas.event.events;

import java.util.UUID;

public class PlayerConnectEvent {

    private String name;
    private UUID uuid;

    public PlayerConnectEvent(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public static class Join extends PlayerConnectEvent {

        public Join(String name, UUID uuid) {
            super(name, uuid);
        }
    }

    public static class Leave extends PlayerConnectEvent {

        public Leave(String name, UUID uuid) {
            super(name, uuid);
        }
    }

}
