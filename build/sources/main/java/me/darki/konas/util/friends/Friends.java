package me.darki.konas.util.friends;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Friends {
    public static CopyOnWriteArrayList<Friend> friends = new CopyOnWriteArrayList<>();

    public static void addFriend(String name, String uuid) {
        if(!friends.contains(new Friend(name, uuid.replaceAll("-", "")))) {
            friends.add(new Friend(name, uuid.replaceAll("-", "")));
        }
    }

    public static void delFriend(String name) {
        friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public static void clear() {
        friends.clear();
    }

    public static boolean isFriend(String name) {
        if (name == null) return false;
        for (Friend friend : friends) {
            if (friend.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUUIDFriend(String uuid) {
        if (uuid == null) return false;
        for (Friend friend : friends) {
            if (friend.getUuid().equals(uuid.replaceAll("-", ""))) {
                return true;
            }
        }
        return false;
    }

    public static CopyOnWriteArrayList<Friend> getFriends() {
        return friends;
    }

    public Friend getFriendByUUID(String uuid) {
        for (Friend friend : friends) {
            if (friend.getUuid().equalsIgnoreCase(uuid)) {
                return friend;
            }
        }

        return null;

    }


}
