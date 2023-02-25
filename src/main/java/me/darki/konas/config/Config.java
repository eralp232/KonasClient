package me.darki.konas.config;

import com.google.gson.*;
import me.darki.konas.command.Command;
import me.darki.konas.command.commands.FontCommand;

import me.darki.konas.gui.clickgui.component.ColorSettingComponent;
import me.darki.konas.gui.clickgui.component.ContainerComponent;
import me.darki.konas.gui.clickgui.component.ModuleComponent;
import me.darki.konas.gui.clickgui.frame.CategoryFrame;
import me.darki.konas.gui.clickgui.frame.ContainerFrame;
import me.darki.konas.gui.clickgui.frame.Frame;
import me.darki.konas.gui.clickgui.frame.HudFrame;
import me.darki.konas.gui.container.Container;
import me.darki.konas.gui.container.ContainerManager;
import me.darki.konas.macro.Macro;
import me.darki.konas.macro.MacroManager;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.module.modules.client.ConfigModule;
import me.darki.konas.module.modules.client.HUD;
import me.darki.konas.module.modules.client.RPC;
import me.darki.konas.module.modules.misc.*;
import me.darki.konas.module.modules.render.Nametags;
import me.darki.konas.setting.*;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.friends.Friend;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.mute.MuteManager;
import me.darki.konas.util.render.font.CustomFontRenderer;
import me.darki.konas.util.waypoint.Waypoint;
import me.darki.konas.util.waypoint.WaypointType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Config {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static final File KONAS_FOLDER = new File(mc.gameDir, "Konas");

    public static final File CONFIG = new File(KONAS_FOLDER, "config.json");
    public static final File CONFIGS = new File(KONAS_FOLDER, "configs");

    public static final File ACCOUNTS = new File(KONAS_FOLDER, "accounts.json");

    public static File currentConfig = null;

    @Deprecated public static final File CONFIG_OLD = new File(mc.gameDir + File.separator + "KonasConfig.json");
    @Deprecated public static final File ACCOUNTS_OLD = new File(mc.gameDir + File.separator + "accounts.json");

    // Temporary for config migration
    public static void migrate(File oldFile, File newFile) {
        if (oldFile.exists()) {
            if (newFile.exists()) oldFile.delete();
            else {
                try {
                    Files.move(oldFile.toPath(), newFile.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void load(File config, boolean loadAccounts) {

        if (!config.exists() || !ACCOUNTS.exists()) save(config);

        RPC.firstRun = true;

        // Read Config
        try {
            FileReader reader = new FileReader(config);
            JsonParser parser = new JsonParser();

            JsonArray array = null;
            try {
                array = (JsonArray) parser.parse(reader);
            } catch (ClassCastException e) {
                save(config);
            }

            JsonArray modules = null;

            try {
                JsonObject modulesObject = (JsonObject) array.get(0);
                modules = modulesObject.getAsJsonArray("Modules");
            } catch (Exception e) {
                System.err.println("Module Array not found, skipping!");
            }

            JsonArray containers = null;

            try {
                JsonObject containersObject = (JsonObject) array.get(1);
                containers = containersObject.getAsJsonArray("Containers");
            } catch (Exception e) {
                System.err.println("Container Array not found, skipping!");
            }

            JsonArray panels = null;

            try {
                JsonObject panelsObject = (JsonObject) array.get(2);
                panels = panelsObject.getAsJsonArray("Panels");
            } catch (Exception e) {
                System.err.println("Panel Array not found, skipping!");
            }

            JsonArray friends = null;

            try {
                JsonObject friendsObject = (JsonObject) array.get(3);
                friends = friendsObject.getAsJsonArray("Friends");
            } catch (Exception e) {
                System.err.println("Friend Array not found, skipping!");
            }

            JsonObject prefix = null;

            try {
                prefix = (JsonObject) array.get(4);
            } catch (Exception e) {
                System.err.println("Prefix Object not found, skipping!");
            }

            JsonObject language = null;

            try {
                language = (JsonObject) array.get(5);
            } catch (Exception e) {
                System.err.println("Language Object not found, skipping!");
            }

            JsonArray macros = null;
            try {
                JsonObject macrosObject = (JsonObject) array.get(6);
                macros = macrosObject.getAsJsonArray("Macros");
            } catch (Exception e) {
                System.err.println("Macro Array not found, skipping!");
            }

            JsonArray waypoints = null;
            try {
                JsonObject waypointsObject = (JsonObject) array.get(7);
                waypoints = waypointsObject.getAsJsonArray("Waypoints");
            } catch (Exception e) {
                System.err.println("Waypoints Array not found, skipping!");
            }

            try {
                JsonElement fontElement = array.get(8);
                FontCommand.lastFont = fontElement.getAsString();
                HUD.customFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 18);
                ClickGUIModule.customFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 17);
                Nametags.customFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 20);
                Nametags.highResFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 60);
            } catch (Exception e) {
                System.err.println("Font not found, skipping!");
            }

            JsonArray muted = null;
            try {
                JsonObject mutedObject = (JsonObject) array.get(9);
                muted = mutedObject.getAsJsonArray("Muted");
            } catch (Exception e) {
                System.err.println("Friend Array not found, skipping!");
            }
            if (modules != null) {
                modules.forEach(m -> {
                    try {
                        parseModule(m.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            Nametags.setFontRenderer(Nametags.customFont.getValue());

            if (containers != null) {
                containers.forEach(c -> {
                    try {
                        parseContainer(c.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            if (panels != null) {
                panels.forEach(f -> {
                    try {
                        parseFrame(f.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            if (macros != null) {
                MacroManager.clearMacros();
                macros.forEach(m -> {
                    try {
                        parseMacro(m.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            if (waypoints != null) {
                KonasGlobals.INSTANCE.waypointManager.clear();
                waypoints.forEach(w -> {
                    try {
                        parseWaypoint(w.getAsJsonObject());
                    } catch (NullPointerException e) {
                        System.err.println(e.getMessage());
                    }
                });
            }

            if (friends != null) {
                Friends.clear();
                friends.forEach(f -> parseFriend(f.getAsJsonObject()));
            }

            if (prefix != null) {
                parsePrefix(prefix);
            }

            if (language != null) {
                parseLanguage(language);
            }

            KonasGlobals.INSTANCE.clickGUI.getFrames().forEach(frame -> {
                if (frame instanceof CategoryFrame) {
                    ((CategoryFrame) frame).getComponents().forEach(component -> {
                        if (component instanceof ModuleComponent) {
                            ((ModuleComponent) component).getComponents().forEach(component1 -> {
                                if (component1 instanceof ColorSettingComponent) {
                                    ((ColorSettingComponent) component1).refresh();
                                }
                            });
                        }
                    });
                } else if (frame instanceof HudFrame) {
                    ((HudFrame) frame).getComponents().forEach(component -> {
                        if (component instanceof ContainerComponent) {
                            ((ContainerComponent) component).getComponents().forEach(component1 -> {
                                if (component1 instanceof ColorSettingComponent) {
                                    ((ColorSettingComponent) component1).refresh();
                                }
                            });
                        }
                    });
                }
            });

            if (muted != null) {
                MuteManager.clearMuted();
                muted.forEach(f -> parseMuted(f.getAsJsonObject()));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        currentConfig = config;
        loadAutoGGFiles();
        loadAnnouncerFiles();
        loadExtraChatFiles();
        loadBlockAuraFiles();
        RPC.firstRun = false;
        }

    public static JsonArray loadJsonArray(File config) {
        try {
            FileReader reader = new FileReader(config);
            JsonParser parser = new JsonParser();
            JsonArray array = (JsonArray) parser.parse(reader);
            reader.close();
            return array;
        } catch (Exception e) {
            System.err.println("Couldn't load config");
            return null;
        }
    }

    public static void saveSingleModule(Module m, File config) {

        JsonArray array = loadJsonArray(config);

        JsonArray modules = null;

        try {
            JsonObject modulesObject = (JsonObject) array.get(0);
            modules = modulesObject.getAsJsonArray("Modules");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Module Array not found!");
            return;
        }

        int index = -1;

        for(int i = 0; i < modules.size(); i++) {
            JsonObject object = modules.get(i).getAsJsonObject();
            if(object.getAsJsonObject(m.getName()) != null) {
                index = i;
            }
        }

        if(index != -1) {
            modules.set(index, getModuleObject(m));
            JsonObject modulesObj = new JsonObject();
            modulesObj.add("Modules", modules);

            array.set(0, modulesObj);

            writeJsonArrayToFile(array, config);
        }

    }

    public static void loadSingleModule(Module m, File config) {

        try {
            FileReader reader = new FileReader(config);
            JsonParser parser = new JsonParser();

            JsonArray array = null;
            try {
                array = (JsonArray) parser.parse(reader);
            } catch (ClassCastException ignored) {}

            JsonArray modules = null;

            try {
                JsonObject modulesObject = (JsonObject) array.get(0);
                modules = modulesObject.getAsJsonArray("Modules");
            } catch (Exception e) {
                System.err.println("Module Array not found!");
            }

            for(JsonElement element : modules) {
                JsonObject topObject = element.getAsJsonObject();
                if(topObject.getAsJsonObject(m.getName()) != null) {
                    parseModule(topObject);
                }
            }
        } catch (Exception e) {
            System.err.println("Error while loading module " + m.getName());
        }
    }

    public static void saveOnlyFriends(File file) {

        try {
            JsonArray array = loadJsonArray(file);

            if(ConfigModule.overwriteFriends.getValue()) {
                JsonObject friendsObject = new JsonObject();
                friendsObject.add("Friends", getFriendList());
                array.set(3, friendsObject);
                writeJsonArrayToFile(array, file);
            } else {

                JsonArray friends;

                try {
                    JsonObject friendsObject = (JsonObject) array.get(3);
                    friends = friendsObject.getAsJsonArray("Friends");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Friends Array not found!");
                    return;
                }


                for(JsonElement ownFriend : getFriendList()) {
                    if(!friends.contains(ownFriend)) {
                        friends.add(ownFriend);
                    }
                }

                JsonObject friendsObject = new JsonObject();
                friendsObject.add("Friends", friends);
                array.set(3, friendsObject);

                writeJsonArrayToFile(array, file);
            }


        } catch (Exception e) {
            System.err.println("Error while saving friends!");
        }

    }

    public static void loadOnlyFriends(File file) {

        try {
            JsonArray array = loadJsonArray(file);

            JsonArray friends;

            try {
                JsonObject friendsObject = (JsonObject) array.get(3);
                friends = friendsObject.getAsJsonArray("Friends");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Friends Array not found!");
                return;
            }

            if(ConfigModule.overwriteFriends.getValue()) {
                Friends.clear();
            }

            for(JsonElement friend : friends) {
                parseFriend(friend.getAsJsonObject());
            }

        } catch (Exception e) {
            System.err.println("Error while loading friends");
        }

    }

    public static void save() {
        save(CONFIG);
    }

    public static void save(File config) {

        try {

            if (!config.exists()) {
                config.createNewFile();
            }

            JsonArray array = new JsonArray();

            JsonObject modulesObj = new JsonObject();
            modulesObj.add("Modules", getModuleArray());
            array.add(modulesObj);

            JsonObject containersObj = new JsonObject();
            containersObj.add("Containers", getContainersArray());
            array.add(containersObj);

            JsonObject panelsObj = new JsonObject();
            panelsObj.add("Panels", getPanelArray());
            array.add(panelsObj);

            JsonObject friendsObj = new JsonObject();
            friendsObj.add("Friends", getFriendList());
            array.add(friendsObj);

            JsonObject prefixObj = new JsonObject();
            prefixObj.addProperty("Prefix", Command.getPrefix());
            array.add(prefixObj);

            JsonObject languageObj = new JsonObject();
            languageObj.addProperty("Language", Translate.targetLanguage);
            array.add(languageObj);

            JsonObject macrosObj = new JsonObject();
            macrosObj.add("Macros", getMacroArray());
            array.add(macrosObj);

            JsonObject waypointsObj = new JsonObject();
            waypointsObj.add("Waypoints", getWaypointsArray());
            array.add(waypointsObj);

            array.add(FontCommand.lastFont);

            JsonObject mutedObj = new JsonObject();
            mutedObj.add("Muted", getMutedList());
            array.add(mutedObj);

            FileWriter writer = new FileWriter(config);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(array, writer);
            writer.close();

        } catch (IOException e) {
            System.err.println("Cant write to config file!");
        }
    }

    public static void writeJsonArrayToFile(JsonArray array, File file) {

        try {
            FileWriter writer = new FileWriter(file);
            new GsonBuilder().setPrettyPrinting().create().toJson(array, writer);
            writer.close();
        } catch (Exception e) {
            System.err.println("Cant write to config file!");
        }

    }

    private static void parseModule(JsonObject object) throws NullPointerException {

        Module module = ModuleManager.getModules().stream()
                .filter(m -> object.getAsJsonObject(m.getName()) != null)
                .findFirst().orElse(null);

        if (module != null) {

            JsonObject moduleObject = object.getAsJsonObject(module.getName());

            try {

                // Bind
                String bind = moduleObject.getAsJsonPrimitive("Bind").getAsString();
                module.setKeybind(getKeyIndex(bind));

                // Enabled
                boolean enabled = moduleObject.getAsJsonPrimitive("Enabled").getAsBoolean();
                if (module.isEnabled() != enabled) {
                    module.toggle();
                }

                // Visible
                boolean visible = moduleObject.getAsJsonPrimitive("Visible").getAsBoolean();
                module.setVisible(visible);

                //Settings
                if (!ModuleManager.getSettingList(module).isEmpty()) {
                    for (Setting s : ModuleManager.getSettingList(module)) {
                        try {
                            if (s.getValue() instanceof Float) {
                                s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsFloat());
                            } else if (s.getValue() instanceof Double) {
                                s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsDouble());
                            } else if (s.getValue() instanceof Integer) {
                                s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsInt());
                            } else if (s.getValue() instanceof Boolean) {
                                s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsBoolean());
                            } else if (s.getValue() instanceof Parent) {
                                ((Parent) s.getValue()).setExtended(moduleObject.getAsJsonPrimitive(s.getName()).getAsBoolean());
                            } else if (s.getValue() instanceof Enum) {
                                s.setEnumValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsString());
                            } else if (s.getValue() instanceof SubBind) {
                                s.setValue(new SubBind(moduleObject.getAsJsonPrimitive(s.getName()).getAsInt()));
                            } else if (s.getValue() instanceof String) {
                                s.setValue(moduleObject.getAsJsonPrimitive(s.getName()).getAsString());
                            } else if (s.getValue() instanceof ColorSetting) {
                                JsonArray array = moduleObject.getAsJsonArray(s.getName());
                                ((ColorSetting) s.getValue()).setColor(array.get(0).getAsInt());
                                ((ColorSetting) s.getValue()).setCycle(array.get(1).getAsBoolean());
                                ((ColorSetting) s.getValue()).setGlobalOffset(array.get(2).getAsInt());
                            } else if (s.getValue() instanceof BlockListSetting) {
                                JsonArray array = moduleObject.getAsJsonArray(s.getName());
                                array.forEach(jsonElement -> {
                                    String str = jsonElement.getAsString();
                                    ((BlockListSetting) s.getValue()).addBlock(str);
                                });
                                ((BlockListSetting) s.getValue()).refreshBlocks();
                            } else if (s.getValue() instanceof ItemListSetting) {
                                JsonArray array = moduleObject.getAsJsonArray(s.getName());
                                array.forEach(jsonElement -> {
                                    String str = jsonElement.getAsString();
                                    ((ItemListSetting) s.getValue()).addItem(str);
                                });
                            }
                        } catch (Exception e) {}
                    }
                }
            } catch (Exception e) {
                throw new NullPointerException("Error loading module: " + module.getName());
            }
        } else {
            throw new NullPointerException("Couldn't find module");
        }
    }

    private static void parseContainer(JsonObject object) throws NullPointerException {

        Container container = KonasGlobals.INSTANCE.containerManager.getContainers().stream()
                .filter(c -> object.getAsJsonObject(c.getName()) != null)
                .findFirst().orElse(null);

        if (container != null) {

            JsonObject containerObject = object.getAsJsonObject(container.getName());

            try {

                // Enabled
                boolean enabled = containerObject.getAsJsonPrimitive("Enabled").getAsBoolean();
                container.setVisible(enabled);

                int a = containerObject.getAsJsonPrimitive("A").getAsInt();
                float x = containerObject.getAsJsonPrimitive("X").getAsFloat();
                float y = containerObject.getAsJsonPrimitive("Y").getAsFloat();
                container.setAnchor(a);
                container.setOffsetX(x);
                container.setOffsetY(y);

                // Settings

                if (!ContainerManager.getSettingList(container).isEmpty()) {
                    for (Setting s : ContainerManager.getSettingList(container)) {
                        if (s.getValue() instanceof Float) {
                            s.setValue(containerObject.getAsJsonPrimitive(s.getName()).getAsFloat());
                        } else if (s.getValue() instanceof Double) {
                            s.setValue(containerObject.getAsJsonPrimitive(s.getName()).getAsDouble());
                        } else if (s.getValue() instanceof Integer) {
                            s.setValue(containerObject.getAsJsonPrimitive(s.getName()).getAsInt());
                        } else if (s.getValue() instanceof Boolean) {
                            s.setValue(containerObject.getAsJsonPrimitive(s.getName()).getAsBoolean());
                        } else if (s.getValue() instanceof Enum) {
                            s.setEnumValue(containerObject.getAsJsonPrimitive(s.getName()).getAsString());
                        } else if (s.getValue() instanceof String) {
                            s.setValue(containerObject.getAsJsonPrimitive(s.getName()).getAsString());
                        } else if (s.getValue() instanceof ColorSetting) {
                            JsonArray array = containerObject.getAsJsonArray(s.getName());
                            ((ColorSetting) s.getValue()).setColor(array.get(0).getAsInt());
                            ((ColorSetting) s.getValue()).setCycle(array.get(1).getAsBoolean());
                        } else if (s.getValue() instanceof BlockListSetting) {
                            JsonArray array = containerObject.getAsJsonArray(s.getName());
                            array.forEach(jsonElement -> {
                                String str = jsonElement.getAsString();
                                ((BlockListSetting) s.getValue()).addBlock(str);
                            });
                            ((BlockListSetting) s.getValue()).refreshBlocks();
                        } else if (s.getValue() instanceof ItemListSetting) {
                            JsonArray array = containerObject.getAsJsonArray(s.getName());
                            array.forEach(jsonElement -> {
                                String str = jsonElement.getAsString();
                                ((ItemListSetting) s.getValue()).addItem(str);
                            });
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new NullPointerException("Error loading container: " + container.getName());
            }
        } else {
            throw new NullPointerException("Couldn't find container");
        }
    }

    private static void parseFrame(JsonObject object) throws NullPointerException {

        Frame frame = KonasGlobals.INSTANCE.clickGUI.getFrames().stream()
                .filter(f -> object.getAsJsonObject(f.getName()) != null)
                .findFirst().orElse(null);

        if (frame instanceof ContainerFrame) return;

        if (frame != null) {
            JsonObject panelObject = object.getAsJsonObject(frame.getName());

            try {
                int x = panelObject.getAsJsonPrimitive("X").getAsInt();
                frame.setPosX(x);

                int y = panelObject.getAsJsonPrimitive("Y").getAsInt();
                frame.setPosY(y);

                boolean extended = panelObject.getAsJsonPrimitive("Extended").getAsBoolean();
                frame.setExtended(extended);
            } catch (Exception e) {
                throw new NullPointerException("Error parsing frame: " + frame.getName());
            }
        } else {
            throw new NullPointerException("Frame not found");
        }
    }

    private static void parseMacro(JsonObject macro) {
        MacroManager.addMacro(new Macro(macro.get("Name").getAsString(), macro.get("Text").getAsString(), Keyboard.getKeyIndex(macro.get("Bind").getAsString())));
    }

    private static void parseWaypoint(JsonObject waypoint) {
        WaypointType type = null;
        for (WaypointType e : WaypointType.class.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(waypoint.get("Type").getAsString())) {
                type = e;
            }
        }

        KonasGlobals.INSTANCE.waypointManager.addWaypoint(new Waypoint(waypoint.get("Name").getAsString(), waypoint.get("X").getAsDouble(), waypoint.get("Y").getAsDouble(), waypoint.get("Z").getAsDouble(), waypoint.get("Dimension").getAsInt(), waypoint.get("Server").getAsString(), type));
    }

    private static void parseFriend(JsonObject friend) {
        if(friend.get("Name") != null && friend.get("UUID") != null) {
            Friends.addFriend(friend.get("Name").getAsString(), friend.get("UUID").getAsString());
        }
    }

    private static void parseMuted(JsonObject muted) {
        MuteManager.addMuted(muted.get("Name").getAsString());
    }

    private static void parsePrefix(JsonObject object) {
        try {
            Command.setPrefix(object.get("Prefix").getAsString());
        } catch (NullPointerException ignored) {

        }
    }

    private static void parseLanguage(JsonObject object) {
        try {
            Translate.targetLanguage = (object.get("Language").getAsString());
        } catch (NullPointerException ignored) {
        }
    }

    private static JsonArray getModuleArray() {
        JsonArray modulesArray = new JsonArray();
        for (Module m : ModuleManager.getModules()) {
            modulesArray.add(getModuleObject(m));
        }
        return modulesArray;
    }

    public static JsonObject getModuleObject(Module m) {
        JsonObject attribs = new JsonObject();
        attribs.addProperty("Bind", GameSettings.getKeyDisplayString(m.getKeybind()));
        attribs.addProperty("Enabled", m.isEnabled());
        attribs.addProperty("Visible", m.isVisible());
        if (ModuleManager.getSettingList(m) != null) {
            for (Setting s : ModuleManager.getSettingList(m)) {
                if (s.getValue() instanceof Number) {
                    attribs.addProperty(s.getName(), (Number) s.getValue());
                } else if (s.getValue() instanceof Boolean) {
                    attribs.addProperty(s.getName(), (Boolean) s.getValue());
                } else if (s.getValue() instanceof Parent) {
                    attribs.addProperty(s.getName(), ((Parent) s.getValue()).isExtended());
                } else if (s.getValue() instanceof SubBind) {
                    attribs.addProperty(s.getName(), ((SubBind) s.getValue()).getKeyCode());
                } else if (s.getValue() instanceof Enum || s.getValue() instanceof String) {
                    attribs.addProperty(s.getName(), String.valueOf(s.getValue()));
                } else if (s.getValue() instanceof ColorSetting) {

                    JsonArray array = new JsonArray();
                    array.add(((ColorSetting) s.getValue()).getRawColor());
                    array.add(((ColorSetting) s.getValue()).isCycle());
                    array.add(((ColorSetting) s.getValue()).getGlobalOffset());

                    attribs.add(s.getName(), array);

                } else if (s.getValue() instanceof BlockListSetting) {
                    JsonArray array = new JsonArray();

                    for (String str : ((BlockListSetting) s.getValue()).getBlocksAsString()) {
                        array.add(str);
                    }

                    attribs.add(s.getName(), array);
                } else if (s.getValue() instanceof ItemListSetting) {
                    JsonArray array = new JsonArray();

                    for (String str : ((ItemListSetting) s.getValue()).getItems()) {
                        array.add(str);
                    }

                    attribs.add(s.getName(), array);
                }
            }
        }
        JsonObject moduleObject = new JsonObject();
        moduleObject.add(m.getName(), attribs);
        return moduleObject;
    }

    private static JsonArray getContainersArray() {
        JsonArray containersArray = new JsonArray();
        for (Container c : KonasGlobals.INSTANCE.containerManager.getContainers()) {
            JsonObject attribs = new JsonObject();
            attribs.addProperty("A", c.getAnchor());
            attribs.addProperty("X", c.getOffsetX());
            attribs.addProperty("Y", c.getOffsetY());
            attribs.addProperty("Enabled", c.isVisible());
            attribs.addProperty("Visible", c.isVisible());
            if (!ContainerManager.getSettingList(c).isEmpty()) {
                for (Setting s : ContainerManager.getSettingList(c)) {
                    if (s.getValue() instanceof Number) {
                        attribs.addProperty(s.getName(), (Number) s.getValue());
                    } else if (s.getValue() instanceof Boolean) {
                        attribs.addProperty(s.getName(), (Boolean) s.getValue());
                    } else if (s.getValue() instanceof Enum || s.getValue() instanceof String) {
                        attribs.addProperty(s.getName(), String.valueOf(s.getValue()));
                    } else if (s.getValue() instanceof ColorSetting) {

                        JsonArray array = new JsonArray();
                        array.add(((ColorSetting) s.getValue()).getRawColor());
                        array.add(((ColorSetting) s.getValue()).isCycle());

                        attribs.add(s.getName(), array);

                    } else if (s.getValue() instanceof BlockListSetting) {
                        JsonArray array = new JsonArray();

                        for (String str : ((BlockListSetting) s.getValue()).getBlocksAsString()) {
                            array.add(str);
                        }

                        attribs.add(s.getName(), array);
                    } else if (s.getValue() instanceof ItemListSetting) {
                        JsonArray array = new JsonArray();

                        for (String str : ((ItemListSetting) s.getValue()).getItems()) {
                            array.add(str);
                        }

                        attribs.add(s.getName(), array);
                    }
                }
            }
            JsonObject moduleObject = new JsonObject();
            moduleObject.add(c.getName(), attribs);
            containersArray.add(moduleObject);
        }
        return containersArray;
    }

    private static JsonArray getPanelArray() {
        JsonArray panelArray = new JsonArray();

        for (Frame frame : KonasGlobals.INSTANCE.clickGUI.getFrames()) {
            if (frame instanceof ContainerFrame) continue;
            JsonObject attribs = new JsonObject();
            attribs.addProperty("X", frame.getPosX());
            attribs.addProperty("Y", frame.getPosY());
            attribs.addProperty("Extended", frame.isExtended());
            JsonObject panelObj = new JsonObject();
            panelObj.add(frame.getName(), attribs);
            panelArray.add(panelObj);
        }

        return panelArray;
    }

    private static JsonArray getMacroArray() {
        JsonArray macroArray = new JsonArray();
        for (Macro macro : MacroManager.getMacros()) {
            JsonObject macroObj = new JsonObject();
            macroObj.addProperty("Name", macro.getName());
            macroObj.addProperty("Bind", GameSettings.getKeyDisplayString(macro.getBind()));
            macroObj.addProperty("Text", macro.getText());
            macroArray.add(macroObj);
        }

        return macroArray;
    }

    private static JsonArray getWaypointsArray() {
        JsonArray waypointsArray = new JsonArray();
        for (Waypoint waypoint : KonasGlobals.INSTANCE.waypointManager.getRawWaypoints()) {
            JsonObject waypointObj = new JsonObject();
            waypointObj.addProperty("Name", waypoint.getName());
            waypointObj.addProperty("X", waypoint.getX());
            waypointObj.addProperty("Y", waypoint.getY());
            waypointObj.addProperty("Z", waypoint.getZ());
            waypointObj.addProperty("Dimension", waypoint.getDimension());
            waypointObj.addProperty("Server", waypoint.getServer());
            waypointObj.addProperty("Type", waypoint.getType().toString());
            waypointsArray.add(waypointObj);
        }

        return waypointsArray;
    }

    private static JsonArray getFriendList() {
        JsonArray friendsList = new JsonArray();

        for (Friend friend : Friends.getFriends()) {
            friendsList.add(getFriendObject(friend));
        }
        return friendsList;
    }

    private static JsonObject getFriendObject(Friend friend) {
        JsonObject friendObj = new JsonObject();
        friendObj.addProperty("Name", friend.getName());
        friendObj.addProperty("UUID", friend.getUuid());
        return friendObj;
    }


    private static JsonArray getMutedList() {
        JsonArray mutedList = new JsonArray();

        for (String muted : MuteManager.getMuted()) {
            JsonObject mutedObj = new JsonObject();
            mutedObj.addProperty("Name", muted);
            mutedList.add(mutedObj);
        }
        return mutedList;
    }

    public static void loadAutoGGFiles() {
        try {
            File folder = new File(KONAS_FOLDER, "autogg");
            File gg = new File(folder, "gg.txt");
            File ez = new File(folder, "ez.txt");
            File log = new File(folder, "log.txt");
            File excuse = new File(folder, "excuse.txt");
            File pop = new File(folder, "pop.txt");

            if (!folder.exists()) folder.mkdirs();

            if (!gg.exists()) {
                gg.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(gg));
                for (String string : AutoGG.KONASGG) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            if (!ez.exists()) {
                ez.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(ez));
                for (String string : AutoGG.KONASEZ) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            if (!log.exists()) {
                log.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(log));
                for (String string : AutoGG.KONASLOG) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            if (!excuse.exists()) {
                excuse.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(excuse));
                for (String string : AutoGG.KONASEXCUSE) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            if (!pop.exists()) {
                pop.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(pop));
                for (String string : AutoGG.KONASPOP) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            BufferedReader reader = new BufferedReader(new FileReader(gg));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            AutoGG.KONASGG.clear();
            AutoGG.KONASGG.addAll(lines);

            reader = new BufferedReader(new FileReader(ez));
            lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            AutoGG.KONASEZ.clear();
            AutoGG.KONASEZ.addAll(lines);

            reader = new BufferedReader(new FileReader(log));
            lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            AutoGG.KONASLOG.clear();
            AutoGG.KONASLOG.addAll(lines);

            reader = new BufferedReader(new FileReader(excuse));
            lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            AutoGG.KONASEXCUSE.clear();
            AutoGG.KONASEXCUSE.addAll(lines);

            reader = new BufferedReader(new FileReader(pop));
            lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            AutoGG.KONASPOP.clear();
            AutoGG.KONASPOP.addAll(lines);


        } catch (IOException e) {
            System.err.println("Cant load AutoGG Files!");
        }
    }

    public static void loadAnnouncerFiles() {
        try {
            File folder = new File(mc.gameDir + File.separator + "Konas" + File.separator + "announcer");
            File welcome = new File(mc.gameDir + File.separator + "Konas" + File.separator + "announcer" + File.separator + "welcome.txt");
            File goodbye = new File(mc.gameDir + File.separator + "Konas" + File.separator + "announcer" + File.separator + "goodbye.txt");


            if (!folder.exists()) {
                folder.mkdirs();
            }

            if (!welcome.exists()) {
                welcome.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(welcome));
                for (String string : Announcer.WELCOMES) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            if (!goodbye.exists()) {
                goodbye.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(goodbye));
                for (String string : Announcer.GOODBYES) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            BufferedReader reader = new BufferedReader(new FileReader(welcome));

            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            Announcer.WELCOMES.clear();
            Announcer.WELCOMES.addAll(lines);

            reader = new BufferedReader(new FileReader(goodbye));
            lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            Announcer.GOODBYES.clear();
            Announcer.GOODBYES.addAll(lines);


        } catch (IOException e) {
            System.err.println("Cant load AutoGG Files!");
        }
    }

    public static void loadExtraChatFiles() {
        try {
            File folder = new File(KONAS_FOLDER, "extrachat");
            File taunts = new File(folder, "taunts.txt");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            if (!taunts.exists()) {
                taunts.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(taunts));
                for (String string : ExtraChat.TAUNTS) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            BufferedReader reader = new BufferedReader(new FileReader(taunts));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            ExtraChat.TAUNTS.clear();
            ExtraChat.TAUNTS.addAll(lines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadBlockAuraFiles() {
        try {
            File folder = new File(KONAS_FOLDER, "blockaura");
            File sign = new File(folder, "sign.txt");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            if (!sign.exists()) {
                sign.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(sign));
                for (String string : BlockAura.LINES) {
                    writer.write(string + "\n");
                }
                writer.close();
            }

            BufferedReader reader = new BufferedReader(new FileReader(sign));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("")) {
                    lines.add(line);
                }
            }
            BlockAura.LINES.clear();
            BlockAura.LINES.addAll(lines);
            if(lines.size() < 4) {
                for(int i = 0; i <= 4 - lines.size(); i++) {
                    BlockAura.LINES.add("");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadSpammerFile(String value, boolean create) {
        try {
            File folder = new File(KONAS_FOLDER, "spammer");
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, value);

            if (create && !file.exists()) file.createNewFile();

            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    ArrayList<String> lines = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }

                    boolean newline = false;

                    for (String l : lines) {
                        if (l.equals("")) {
                            newline = true;
                            break;
                        }
                    }

                    Spammer.spamList.clear();
                    ArrayList<String> spamList = new ArrayList<>();

                    if (newline) {
                        StringBuilder spamChunk = new StringBuilder();

                        for (String l : lines) {
                            if (l.equals("")) {
                                if (spamChunk.length() > 0) {
                                    spamList.add(spamChunk.toString());
                                    spamChunk = new StringBuilder();
                                }
                            } else {
                                spamChunk.append(l).append(" ");
                            }
                        }
                        spamList.add(spamChunk.toString());
                    } else {
                        spamList.addAll(lines);
                    }

                    Spammer.spamList = spamList;
                } catch (Exception e) {
                    System.err.println("Could not load spammer file " + value);
                }
            }).start();
        } catch (IOException e) {
            System.err.println("Could not load spammer file " + value);
        }

    }

    public static boolean delete(File file) {
        return file.delete();
    }

    public static List<File> getConfigList() {
        if (!KONAS_FOLDER.exists() || KONAS_FOLDER.listFiles() == null) return null;

        if (CONFIGS.listFiles() != null) {
            return Arrays.stream(CONFIGS.listFiles()).filter(f -> f.getName().endsWith(".json")).collect(Collectors.toList());
        }

        return null;
    }

    public static File getLastModified() {
        if (!KONAS_FOLDER.exists() || KONAS_FOLDER.listFiles() == null) {
            KONAS_FOLDER.mkdir();
            return CONFIG;
        }

        if (CONFIGS.listFiles() != null) {
            List<File> configFiles = Arrays.stream(CONFIGS.listFiles()).filter(f -> f.getName().endsWith(".json")).collect(Collectors.toList());
            configFiles.add(CONFIG);

            return configFiles.stream().max(Comparator.comparingLong(File::lastModified)).orElse(CONFIG);
        }

        return CONFIG;
    }

    public static int getKeyIndex(String name) {

        String leftMouse = I18n.format("key.mouse.left");
        String rightMouse = I18n.format("key.mouse.right");
        String middleMouse = I18n.format("key.mouse.middle");

        if(name.equalsIgnoreCase(leftMouse)) {
            return -100;
        } else if(name.equalsIgnoreCase(rightMouse)) {
            return -99;
        } else if(name.equalsIgnoreCase(middleMouse)) {
            return -98;
        } else {
            for(int i = 0; i < 15; i++) {
                if(name.equalsIgnoreCase(I18n.format("key.mouseButton", i + 4))) {
                    return (i + 4) - 101;
                }
            }
            return Keyboard.getKeyIndex(name);
        }

    }

}
