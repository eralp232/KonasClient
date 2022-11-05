package me.darki.konas.event.listener;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.KeyEvent;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.macro.Macro;
import me.darki.konas.macro.MacroManager;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.Macros;
import me.darki.konas.util.client.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

public class KeyListener {

    public static KeyListener INSTANCE = new KeyListener();

    public static boolean isMacro = false;

    @Subscriber
    public void onKeyPress(KeyEvent event) {
        if (event.getKey() == Keyboard.KEY_NONE) return;
        if (Keyboard.isKeyDown(Keyboard.KEY_F3)) return;
        for (Module m : ModuleManager.getModules()) {
            if (!m.isHold() && m.getKeybind() == event.getKey()) {
                m.toggle();
            }
        }
        if (ModuleManager.getModuleByClass(Macros.class).isEnabled()) {
            for (Macro m : MacroManager.getMacros()) {
                if (m.getBind() == event.getKey()) {
                    isMacro = true;
                    m.runMacro();
                    isMacro = false;
                }
            }
        }
    }

    @Subscriber
    public void onRender2D(Render2DEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null && (Minecraft.getMinecraft().currentScreen instanceof ClickGUI || Minecraft.getMinecraft().currentScreen instanceof GuiChat)) return;
        for (Module m : ModuleManager.getModules()) {
            if (m.isHold()) {
                if (PlayerUtils.isKeyDown(m.getKeybind()) && !m.isEnabled()) {
                    m.toggle();
                } else if (!PlayerUtils.isKeyDown(m.getKeybind()) && m.isEnabled()) {
                    m.toggle();
                }
            }
        }
    }

}
