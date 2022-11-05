package me.darki.konas.util.friends;

public class Friend {

    private final String name;
    private final String uuid;

    public Friend(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object input) {
        if(!(input instanceof Friend)) return false;
        Friend friend = (Friend) input;
        return friend.name.equals(this.name) && friend.uuid.equals(this.uuid);
    }

}
