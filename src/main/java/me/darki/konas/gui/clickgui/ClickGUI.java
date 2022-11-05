package me.darki.konas.gui.clickgui;

import me.darki.konas.gui.clickgui.frame.*;
import me.darki.konas.gui.container.Container;
import me.darki.konas.module.Module;
import me.darki.konas.util.KonasGlobals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ClickGUI extends GuiScreen {
    private final ArrayList<Frame> frames = new ArrayList<>();
    private boolean initialized = false;

    private static boolean noEsc = false;

    private static boolean hudEditor = false;

    public void initialize() {
        int x = ((new ScaledResolution(Minecraft.getMinecraft())).getScaledWidth()/2)-350;
        int y = 20;

        for (Module.Category moduleCategory : Module.Category.values()) {
            getFrames().add(new CategoryFrame(moduleCategory, x, y, 95F, 14.0F));
            x += 110;
        }

        getFrames().add(new HudFrame(300F, y, 95F, 14.0F));

        for(Container container : KonasGlobals.INSTANCE.containerManager.getContainers()) {
            getFrames().add(new ContainerFrame(container));
        }

        getFrames().add(new ButtonFrame());

        getFrames().forEach(frame -> {
            if (!(frame instanceof ContainerFrame)) {
                frame.setExtended(true);
            }
        });

        getFrames().add(new DescriptionFrame());

        getFrames().forEach(Frame::initialize);



        initialized = true;
    }

    @Override
    public void onGuiClosed() {
        getFrames().forEach(frame -> frame.setDragging(false));
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        getFrames().forEach(frame -> frame.onRender(mouseX, mouseY, partialTicks));
    }

    @Override
    protected void keyTyped(char character, int keyCode) throws IOException {
        if (!noEsc) {
            super.keyTyped(character, keyCode);
        }
        getFrames().forEach(frame -> frame.onKeyTyped(character, keyCode));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for(int i = getFrames().size() - 1; i >= 0; i--) {
            Frame frame = getFrames().get(i);
            if(frame.onMouseClicked(mouseX, mouseY, mouseButton)) {
                Collections.swap(getFrames(), i, getFrames().size() - 2);
                break;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        getFrames().forEach(frame -> frame.onMouseReleased(mouseX, mouseY, mouseButton));
        for (Frame frame : getFrames()) {
            for (Frame otherFrame : getFrames()) {
                if (frame == otherFrame) continue;
                if (frame instanceof ContainerFrame) continue;
                if (frame.getPosX() == otherFrame.getPosX() && frame.getPosY() == otherFrame.getPosY()) {
                    otherFrame.setPosX(otherFrame.getPosX() + 10F);
                    otherFrame.setPosY(otherFrame.getPosY() + 10F);
                    if (otherFrame instanceof CategoryFrame) {
                        ((CategoryFrame) otherFrame).getEnabledComponents().forEach(component -> component.onMove(otherFrame.getPosX(), otherFrame.getPosY() + ((CategoryFrame) otherFrame).getScrollY()));
                    }
                }
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        getFrames().forEach(frame -> frame.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public ArrayList<Frame> getFrames() {
        return this.frames;
    }

    public static boolean isNoEsc() {
        return noEsc;
    }

    public static void setNoEsc(boolean noEsc) {
        ClickGUI.noEsc = noEsc;
    }

    public static boolean isHudEditor() {
        return hudEditor;
    }

    public static void setHudEditor(boolean hudEditor) {
        ClickGUI.hudEditor = hudEditor;
    }
}
