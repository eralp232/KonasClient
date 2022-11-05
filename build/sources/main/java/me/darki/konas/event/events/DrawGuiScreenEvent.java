package me.darki.konas.event.events;

import net.minecraft.client.gui.GuiScreen;

public class DrawGuiScreenEvent {
    private static DrawGuiScreenEvent INSTANCE = new DrawGuiScreenEvent();

    private GuiScreen gui;

    public static DrawGuiScreenEvent get(GuiScreen gui) {
        INSTANCE.gui = gui;
        return INSTANCE;
    }

    public GuiScreen getGui() {
        return gui;
    }
}
