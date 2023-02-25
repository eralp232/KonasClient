package me.darki.konas.module;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.command.Command;
import me.darki.konas.module.modules.client.Notify;
import me.darki.konas.setting.Bind;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public abstract class Module {

    private String name;

    private String extraInfo = null;

    private String description = "";
    private Setting<Bind> keybind = new Setting<>("Bind", new Bind(Keyboard.KEY_NONE)).withDescription("Sets the module toggle key");
    private Setting<Boolean> holdKeybind = new Setting<>("Hold", false).withVisibility(() -> false).withDescription("Only activate while bind is being held down");
    private Category category;
    private ArrayList<String> aliases = new ArrayList<>();

    private boolean enabled;

    private boolean visible = true;

    public static final Minecraft mc = Minecraft.getMinecraft();

    private int minProtocol = 0;
    private int maxProtocol = 1000;

    public boolean isHold() {
        return holdKeybind.getValue();
    }

    public void setHold(boolean hold) {
        holdKeybind.setValue(hold);
    }

    public Module(String name, String description, int keybind, Category category, String... aliases) {
        this.name = name;
        this.description = description;
        this.keybind.getValue().setKeyCode(keybind);
        this.category = category;
        Collections.addAll(this.aliases, aliases);

        EventDispatcher.Companion.register(this);

    }

    public Module(String name, int keybind, Category category, String... aliases) {
        this.name = name;
        this.keybind.getValue().setKeyCode(keybind);
        this.category = category;
        Collections.addAll(this.aliases, aliases);

        EventDispatcher.Companion.register(this);

    }


    public Module(String name, String description, Category category, String... aliases) {
        this.name = name;
        this.description = description;
        this.category = category;
        Collections.addAll(this.aliases, aliases);

        EventDispatcher.Companion.register(this);

    }


    public Module(String name, Category category, String... aliases) {
        this.name = name;
        this.category = category;
        Collections.addAll(this.aliases, aliases);

        EventDispatcher.Companion.register(this);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getKeybind() {
        return keybind.getValue().getKeyCode();
    }

    public void setKeybind(int key) {
        this.keybind.setValue(new Bind(key));
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ArrayList<String> getAliases() {
        return aliases;
    }

    public void setAliases(ArrayList<String> aliases) {
        this.aliases = aliases;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void toggle() {
        if (!enabled) {
            EventDispatcher.Companion.subscribe(this);
            this.enabled = true;
            onEnable();
            doEnableMessage(this);
        } else {
            EventDispatcher.Companion.unsubscribe(this);
            this.enabled = false;
            onDisable();
            doDisableMessage(this);
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public static void doEnableMessage(Module m) {
        if (mc.player == null || mc.world == null) return;
        if ((Boolean) ModuleManager.getSettingByNameAndModuleName("Notify", "Modules").getValue() && ModuleManager.getModuleByName("Notify").isEnabled()) {
            Logger.sendOptionalDeletableMessage(m.getName() + Command.SECTIONSIGN + "a enabled");
        }
    }

    public static void doDisableMessage(Module m) {
        if (mc.player == null || mc.world == null) return;
        if ((Boolean) ModuleManager.getSettingByNameAndModuleName("Notify", "Modules").getValue() && ModuleManager.getModuleByName("Notify").isEnabled()) {
            Logger.sendOptionalDeletableMessage(m.getName() + Command.SECTIONSIGN + "c disabled");
        }
    }

    public void doNotification(String text, TrayIcon.MessageType type) {
        Notify notifs = (Notify) ModuleManager.getModuleByClass(Notify.class);
        if (notifs != null && notifs.isEnabled()) {
            boolean systemTray = (boolean) ModuleManager.getSettingByNameAndModuleName("Notify", "SystemTray").getValue();
            if(systemTray) {
                notifs.displayNotification(this.getName(), text, type);
            }
        }
    }

    public boolean isNotif() {
        return ModuleManager.getModuleByClass(Notify.class).isEnabled();
    }

    public void setProtocolRange(int minProtocol, int maxProtocol) {
        this.minProtocol = minProtocol;
        this.maxProtocol = maxProtocol;
    }

    public int getMinProtocol() {
        return minProtocol;
    }

    public int getMaxProtocol() {
        return maxProtocol;
    }

    public enum Category {
        COMBAT, MOVEMENT, PLAYER, RENDER, MISC, EXPLOIT, CLIENT
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
