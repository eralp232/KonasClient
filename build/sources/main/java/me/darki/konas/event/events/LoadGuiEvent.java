package me.darki.konas.event.events;

import net.minecraft.client.gui.GuiScreen;

public class LoadGuiEvent extends CancellableEvent {

    private GuiScreen gui;

    public LoadGuiEvent(GuiScreen gui) {
        this.gui = gui;
    }

    public GuiScreen getGui() {
        return gui;
    }

    public void setGui(GuiScreen gui) {
        this.gui = gui;
    }
}
