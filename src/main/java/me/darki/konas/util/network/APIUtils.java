package me.darki.konas.util.network;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class APIUtils {

    private static final JsonParser jsonParser = new JsonParser();

    private static final Map<String, String> uuidNameCache = Maps.newConcurrentMap();

    public static String getNameFromUUID(String inputUuid) {

        Gson gson = new Gson();
        String uuid = inputUuid.replace("-", "");
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }

        final String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;
        try {
            HttpURLConnection connection = (HttpURLConnection)(new URL(url + uuid.toString().replace("-", ""))).openConnection();
            JsonObject response = gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
            String name = response.get("name").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getUUIDFromName(String inputName) {

        String name = inputName.replaceAll(String.valueOf(Pattern.compile("[^a-zA-Z0-9_]{1,16}")), "");

        if (uuidNameCache.containsValue(name)) {
            return uuidNameCache.get(name);
        }

        final String[] returnUUID = {null};

        final CountDownLatch latch = new CountDownLatch(1);
        String finalName = name;
        new Thread(() -> {
            final String url = "https://api.mojang.com/users/profiles/minecraft/" + finalName;
            try {
                final String nameJson = Requester.toString(new URL(url));
                if (nameJson != null && nameJson.length() > 0) {
                    final JsonObject object = (JsonObject) jsonParser.parse(nameJson);
                    returnUUID[0] = object.get("id").getAsString();
                    uuidNameCache.put(finalName, returnUUID[0]);
                } else {
                    returnUUID[0] = null;
                }
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return returnUUID[0];
    }
}
