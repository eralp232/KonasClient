package me.darki.konas.util.network;

import com.google.common.collect.Maps;
import com.google.gson.*;
import kotlin.Pair;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.text.TextFormatting;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class APIUtils {

    private static final JsonParser jsonParser = new JsonParser();

    private static final Map<String, String> uuidNameCache = Maps.newConcurrentMap();

    public void clearCache() {
        uuidNameCache.clear();
    }

    public static String getNameFromUUID(String inputUuid) {

        String uuid = inputUuid.replace("-", "");
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }

        final String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try {
            final String nameJson = Requester.toString(new URL(url));
            if (nameJson != null && nameJson.length() > 0) {
                final JsonArray jsonArray = (JsonArray) jsonParser.parse(nameJson);
                if (jsonArray != null) {
                    final JsonObject latestName = (JsonObject) jsonArray.get(jsonArray.size() - 1);
                    if (latestName != null) {
                        String returnString = latestName.get("name").toString();
                        uuidNameCache.put(returnString, uuid);
                        return returnString.substring(1, returnString.length() - 1);
                    }
                }
            }
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

    private static AbstractMap.SimpleEntry<Integer, Long> entry = new AbstractMap.SimpleEntry<>(0, System.currentTimeMillis());

    public static int getPrioQueueLength() {
        if (entry.getKey() != 0) {
            if (System.currentTimeMillis() - entry.getValue() <= 30000) {
                return entry.getKey();
            }
        }
        final String url = "https://api.2b2t.dev/prioq";
        try {
            final String response = Requester.toString(new URL(url));
            if (response != null && response.length() > 0) {
                final JsonArray jsonArray = (JsonArray) jsonParser.parse(response);
                if (jsonArray != null) {
                    JsonPrimitive object = jsonArray.get(1).getAsJsonPrimitive();
                    int prioQLength = object.getAsInt();
                    entry = new AbstractMap.SimpleEntry<>(prioQLength, System.currentTimeMillis());
                    return prioQLength;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String[] getServerStates() {
        String[] responses = new String[3];
        final String url = "https://status.mojang.com/check";
        try {
            final String nameJson = Requester.toString(new URL(url));
            if (nameJson != null && nameJson.length() > 0) {
                JsonArray array = (JsonArray) jsonParser.parse(nameJson);
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.get("account.mojang.com") != null) {
                        responses[0] = obj.get("account.mojang.com").getAsString();
                    }
                    if (obj.get("authserver.mojang.com") != null) {
                        responses[1] = obj.get("authserver.mojang.com").getAsString();
                    }
                    if (obj.get("sessionserver.mojang.com") != null) {
                        responses[2] = obj.get("sessionserver.mojang.com").getAsString();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responses;
    }

    public static Pair<String, TextFormatting> parseResponse(String response) {
        String parsedResponse;
        TextFormatting parsedColor;

        switch (response) {
            case "green":
                parsedResponse = "Good";
                parsedColor = TextFormatting.GREEN;
                break;
            case "yellow":
                parsedResponse = "Ok";
                parsedColor = TextFormatting.YELLOW;
                break;
            default:
                parsedResponse = "Offline";
                parsedColor = TextFormatting.RED;
                break;
        }
        return new Pair<>(parsedResponse, parsedColor);
    }

    public static InputStream getFaceInputStream(String uuid) {
        InputStream stream = null;
        try {
            URL url = new URL("https://crafatar.com/avatars/" + uuid.replaceAll("-", "") + "?size=64&default=MHF_Steve&overlay");
            stream = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    public static HashMap<String, InputStream> cachedSkins = new HashMap<>();

    public static InputStream getSkinInputStream(String uuid) {
        if (cachedSkins.containsKey(uuid)) {
            return cachedSkins.get(uuid);
        }
        try {
            URL url = new URL("https://crafatar.com/skins/" + uuid.replaceAll("-", "") + "?default=MHF_Steve");
            InputStream stream = url.openStream();
            cachedSkins.put(uuid, stream);
            return stream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DynamicTexture getCapeDynamicTexture(String type) {
        try {
            URL url = new URL("https://capes.konasclient.com/capes/" + type + ".png");
            InputStream stream = Requester.toInputStream(url);
            return new DynamicTexture(ImageIO.read(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LoginCombo sendMicrosoftLoginRequest(String username, String password) {
        try {
            URL url = new URL("https://konasclient.com/billgates?username=" + username + "&password=" + password);
            JsonObject body =  new JsonParser().parse(Requester.toString(url)).getAsJsonObject();
            return new LoginCombo(body.get("token").getAsString(), body.get("name").getAsString(), body.get("uuid").getAsString());
        } catch (IOException e) {
            System.err.println("Microsoft returned invalid credentials");
        }
        return null;
    }

    public static class LoginCombo {

        private String token;
        private String name;
        private String uuid;

        public LoginCombo(String token, String name, String uuid) {
            this.token = token;
            this.name = name;
            this.uuid = uuid;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }

    public static AbstractMap.SimpleEntry<String, String> getUsernameAndUUID(String token) {
        try {
            URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
            String body = Requester.toStringWithHeader(url, new AbstractMap.SimpleEntry<>("Authorization", "Bearer " + token));
            JsonObject jsonBody = new JsonParser().parse(body).getAsJsonObject();
            return new AbstractMap.SimpleEntry<>(jsonBody.getAsJsonPrimitive("name").getAsString(), jsonBody.getAsJsonPrimitive("id").getAsString());
        } catch (Exception e) {
            return null;
        }
    }

    public static HashMap<String, String> getCapeTypes() {
        HashMap<String, String> capes = new HashMap<>();
        try {
            Thread thread = new Thread(() -> {
                try {
                    new JsonParser().parse(Requester.toStringWithHeader(new URL("https://capes.konasclient.com"), new AbstractMap.SimpleEntry<>("KONAS", getTruth())))
                            .getAsJsonArray().forEach(obj -> {
                        capes.put(obj.getAsJsonObject().getAsJsonPrimitive("Name").getAsString().toUpperCase(),
                                obj.getAsJsonObject().getAsJsonPrimitive("Type").getAsString());
                    });
                } catch (IOException e) {
                    System.err.println("Error fetching capes");
                }
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            System.err.println("Error fetching capes");
        }
        return capes;
    }

    public static String getTruth() {
        AtomicReference<String> truth = new AtomicReference<>();
        try {
            Thread thread = new Thread(() -> {
                try {
                    truth.set(new JsonParser().parse(Requester.toStringWithHeader(new URL("https://truth.konasclient.com"), new AbstractMap.SimpleEntry<>("KONAS", "0c5c77308be49e7cc4f86d2aff08e7cd9c3e1cf6195761c1b27ae472decc1d23bd4a518339a91c04")))
                            .getAsJsonObject().getAsJsonPrimitive("key").getAsString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return truth.get();
    }

    public static HashMap<String, Boolean> cachedSkinTypes = new HashMap<>();

    public static boolean isSlim(String uuid) {
        if (cachedSkinTypes.containsKey(uuid)) {
            return cachedSkinTypes.get(uuid);
        }
        try {
            String nameJson = Requester.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid));
            JsonParser parser = new JsonParser();
            if (nameJson != null && nameJson.length() > 0) {
                JsonObject object = (JsonObject) parser.parse(nameJson);
                JsonArray properties = object.getAsJsonArray("properties");
                String valueBase64 = properties.get(0).getAsJsonObject().getAsJsonPrimitive("value").getAsString();
                String decoded = new String(Base64.getDecoder().decode(valueBase64));
                JsonObject decodedObject = (JsonObject) parser.parse(decoded);
                JsonObject skin = decodedObject.getAsJsonObject("textures").getAsJsonObject("SKIN");
                if (skin.getAsJsonObject("metadata") != null) {
                    cachedSkinTypes.put(uuid, true);
                    return true;
                } else {
                    cachedSkinTypes.put(uuid, false);
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            cachedSkinTypes.put(uuid, false);
            return false;
        }
        cachedSkinTypes.put(uuid, false);
        return false;
    }

}
