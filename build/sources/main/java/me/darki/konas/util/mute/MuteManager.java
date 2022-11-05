package me.darki.konas.util.mute;

import java.util.ArrayList;

public class MuteManager {

    private static final ArrayList<String> muted = new ArrayList<>();

    public static boolean isMuted(String name) {
        for(String string : muted) {
            if(string.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static void addMuted(String add) {
        muted.add(add);
    }

    public static void removeMuted(String remove) {
        muted.remove(remove);
    }

    public static void clearMuted() {
        muted.clear();
    }

    public static ArrayList<String> getMuted() {
        return muted;
    }

}
