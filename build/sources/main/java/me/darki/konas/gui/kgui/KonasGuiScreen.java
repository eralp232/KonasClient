package me.darki.konas.gui.kgui;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.element.container.MasterContainer;
import me.darki.konas.module.modules.client.KonasGui;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class KonasGuiScreen extends GuiScreen {
    private MasterContainer masterContainer;

    public void initialize() {
        masterContainer = new MasterContainer(KonasGui.singleColumn.getValue() ? 488 : 800, KonasGui.height.getValue());
    }

    public MasterContainer getMasterContainer() {
        return masterContainer;
    }

    public static boolean bindedEsc = false;

    @Override
    public void initGui() {
        super.initGui();
        masterContainer.onResize(0, 0);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (KonasGui.reload.getValue()) {
            KonasGui.reload.setValue(false);
            initialize();
            masterContainer.onResize(0, 0);
        }
        Element.setdWheel(Mouse.getDWheel());
        masterContainer.onRender(mouseX, mouseY, partialTicks);
        masterContainer.onRenderPost(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (masterContainer.mouseClicked(mouseX, mouseY, mouseButton)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        masterContainer.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        masterContainer.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        masterContainer.keyTyped(typedChar, keyCode);
        if (bindedEsc) {
            bindedEsc = false;
            return;
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }
}
