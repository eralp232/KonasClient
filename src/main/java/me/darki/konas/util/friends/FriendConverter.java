package me.darki.konas.util.friends;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import me.darki.konas.util.Logger;
import net.minecraft.client.Minecraft;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FriendConverter {

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final String PATH_FUTURE = System.getProperty("user.home") + FILE_SEPARATOR + "Future" + FILE_SEPARATOR + "friends.json";
    private static final String PATH_PYRO = Minecraft.getMinecraft().gameDir + FILE_SEPARATOR + "pyro" + FILE_SEPARATOR + "friends.json";
    private static final String PATH_RUSHERHACK = Minecraft.getMinecraft().gameDir + FILE_SEPARATOR + "rusherhack" + FILE_SEPARATOR + "friends.json";

    // TODO: Check for operating system when reading friends file!!

    public static Set<String> futureParse() {

        JsonReader json;
        try {
            json = new JsonReader(new FileReader(PATH_FUTURE));
        } catch (FileNotFoundException e) {
            Logger.sendChatErrorMessage("Unable to load Future friends file: " + e.getMessage());
            return Collections.emptySet();
        }

        Set<String> friends = new HashSet<>();

        try {
            json.beginArray();
            while (json.peek() != JsonToken.END_ARRAY) {
                json.beginObject();
                if (json.nextName().equals("friend-label")) {
                    friends.add(json.nextString());
                    json.nextName();
                    json.nextString();
                }
                json.endObject();
            }
            json.endArray();

        } catch (IOException e) {
            Logger.sendChatErrorMessage("Error while loading Future friends file: " + e.getMessage());
            return Collections.emptySet();
        }

        return friends;

    }

    public static Set<String> pyroParse() {

        JsonParser parser = new JsonParser();

        JsonObject object;
        try {
            FileReader reader = new FileReader(PATH_PYRO);
            object = (JsonObject) parser.parse(reader);
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Unable to load Pyro friends file: " + e.getMessage());
            return Collections.emptySet();
        }

        Set<String> friends = new HashSet<>();

        try {
            JsonArray friendsArray = object.getAsJsonArray("friends");
            for(JsonElement friendElement : friendsArray) {
                JsonObject friendObject = friendElement.getAsJsonObject();
                friends.add(friendObject.getAsJsonPrimitive("c").getAsString());
            }
        } catch (Exception e) {
            Logger.sendChatErrorMessage("Error while loading Pyro friends file: " + e.getMessage());
            return Collections.emptySet();
        }

        return friends;

    }

    public static Set<String> rusherhackParse() {

        JsonReader json;
        try {
            json = new JsonReader(new FileReader(PATH_RUSHERHACK));
        } catch (FileNotFoundException e) {
            Logger.sendChatErrorMessage("Unable to load Rusherhack friends file: " + e.getMessage());
            return Collections.emptySet();
        }

        Set<String> friends = new HashSet<>();

        try {
            json.beginArray();
            while (json.peek() != JsonToken.END_ARRAY) {
                json.beginObject();
                if (json.nextName().equals("name")) {
                    friends.add(json.nextString());
                }
                json.endObject();
            }
            json.endArray();

        } catch (IOException e) {
            Logger.sendChatErrorMessage("Error while loading Rusherhack friends file: " + e.getMessage());
            return Collections.emptySet();
        }

        return friends;

    }

    public static boolean futureAdd(Set<String> friends) {

        // TODO: append to future friend file
        return false;

    }

}
