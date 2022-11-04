package me.darki.konas.gui.clickgui.frame;

import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.gui.clickgui.component.Component;
import me.darki.konas.gui.clickgui.component.ContainerComponent;
import me.darki.konas.gui.container.Container;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

public class HudFrame extends Frame {

    private final ArrayList<Component> components = new ArrayList<>();

    private int scrollY;

    private int enabledTicks = 0;

    private boolean shouldDisable = false;

    public HudFrame(float posX, float posY, float width, float height) {
        super("Containers", posX, posY, width, height);
    }

    @Override
    public void initialize() {
        float offsetY = getHeight();
        for(Container container : KonasGlobals.INSTANCE.containerManager.getContainers()) {
            getComponents().add(new ContainerComponent(container, getPosX(), getPosY(), 2.0F, offsetY, getWidth() - 4.0F, 12.0F));
            offsetY += 12.0F;
        }

        getComponents().forEach(Component::initialize);
    }

    @Override
    public void onMove(float posX, float posY) {
        if(!ClickGUI.isHudEditor()) return;
        components.forEach(component -> component.onMove(posX, posY));
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);

        if(!ClickGUI.isHudEditor()) return;

        // Dragging
        if (isDragging()) {
            setPosX(mouseX + getPrevPosX());
            setPosY(mouseY + getPrevPosY());
            getComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        // This is very important, to maintain bounds within screen
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        if (getPosX() < 0.0F) {
            setPosX(0.0F);
            getComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        if (getPosX() + getWidth() > scaledResolution.getScaledWidth()) {
            setPosX(scaledResolution.getScaledWidth() - getWidth());
            getComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        if (getPosY() < 0.0F) {
            setPosY(0.0F);
            getComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        if (getPosY() + getHeight() + Math.min(getTotalHeight(), ClickGUIModule.height.getValue()) > scaledResolution.getScaledHeight()) {
            setPosY(scaledResolution.getScaledHeight() - getHeight() - Math.min(getTotalHeight(), ClickGUIModule.height.getValue()));
            getComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        // Title/Background
        GuiRenderHelper.drawRect(getPosX(), getPosY(), getWidth(), getHeight(), ClickGUIModule.header.getValue().getColor());
        ClickGUIFontRenderWrapper.drawStringWithShadow(getName(), (int) (getPosX() + 3.0F), (int) (getPosY() + getHeight() / 2 - (ClickGUIFontRenderWrapper.getFontHeight() / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());

        // Scrolling
        if (isExtended()) {
            if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + getHeight(), getWidth(), ((Math.min(getTotalHeight(), ClickGUIModule.height.getValue())))) && getTotalHeight() > ClickGUIModule.height.getValue()) {
                // Movement of the wheel since last time getDWheel() was called
                int dWheel = Mouse.getDWheel();

                if (dWheel < 0) {
                    if ((getScrollY() - ClickGUIModule.scrollSpeed.getValue()) < -(getTotalHeight() - Math.min(getTotalHeight(), ClickGUIModule.height.getValue()))) {
                        setScrollY((int)-(getTotalHeight() - Math.min(getTotalHeight(), ClickGUIModule.height.getValue())));
                    } else {
                        setScrollY(getScrollY() - ClickGUIModule.scrollSpeed.getValue());
                    }
                } else if (dWheel > 0) {
                    setScrollY(getScrollY() + ClickGUIModule.scrollSpeed.getValue());
                }
            }

            if (getScrollY() > 0) {
                setScrollY(0);
            }

            if (getTotalHeight() > ClickGUIModule.height.getValue()) {
                if ((getScrollY() - ClickGUIModule.scrollSpeed.getValue()) < -(getTotalHeight() - ClickGUIModule.height.getValue())) {
                    setScrollY((int) -(getTotalHeight() - ClickGUIModule.height.getValue()));
                }
            } else if (getScrollY() < 0) {
                setScrollY(0);
            }

            // Scissor for scrolling
            GL11.glPushMatrix();
            GL11.glEnable(GL_SCISSOR_TEST);
            if (ClickGUIModule.animate.getValue()) {
                GuiRenderHelper.prepareScissorBox(scaledResolution, getPosX(), getPosY() + getHeight(), getWidth(), Math.min(ClickGUIModule.height.getValue(), ClickGUIModule.height.getValue() * ((float) enabledTicks / (float) ClickGUIModule.animationSpeed.getValue())));
                if (shouldDisable) {
                    enabledTicks--;
                    if (enabledTicks <= 0) {
                        super.setExtended(false);
                        shouldDisable = false;
                    }
                } else if (enabledTicks < ClickGUIModule.animationSpeed.getValue()) {
                    enabledTicks++;
                }
            } else {
                GuiRenderHelper.prepareScissorBox(scaledResolution, getPosX(), getPosY() + getHeight(), getWidth(), ClickGUIModule.height.getValue());
                if (shouldDisable) {
                    super.setExtended(false);
                    shouldDisable = false;
                }
            }
            GuiRenderHelper.drawRect(getPosX(), getPosY() + getHeight(), getWidth(), Math.min(getTotalHeight(), ClickGUIModule.height.getValue()) + 2F, ClickGUIModule.background.getValue().getColor());
            getComponents().forEach(component -> component.onRender(mouseX, mouseY, partialTicks));
            GL11.glDisable(GL_SCISSOR_TEST);
            GL11.glPopMatrix();
        }

        updateComponentOffsets();
    }

    private void updateComponentOffsets() {
        float offsetY = getHeight();

        for (Component component: getComponents()) {
            component.setOffsetY(offsetY);
            component.onMove(getPosX(), getPosY() + getScrollY());
            if (component instanceof ContainerComponent && component.isExtended()) {
                for (Component child : ((ContainerComponent) component).getEnabledComponents())
                    offsetY += child.getHeight();
            }
            offsetY += component.getHeight();
        }
    }

    @Override
    public void onKeyTyped(char character, int keyCode) {
        super.onKeyTyped(character, keyCode);
        if(!ClickGUI.isHudEditor()) return;
        if (isExtended()) {
            getComponents().forEach(component -> component.onKeyTyped(character, keyCode));
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        if(!ClickGUI.isHudEditor()) return false;
        boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), getWidth(), getHeight());

        switch (mouseButton) {
            case 0: {
                if (withinBounds) {
                    setDragging(true);
                    setPrevPosX(getPosX() - mouseX);
                    setPrevPosY(getPosY() - mouseY);
                    return true;
                }
                break;
            }
            case 1: {
                if (withinBounds) {
                    setExtended(!isExtended());
                    return true;
                }
                break;
            }
        }

        if (isExtended() && mouseWithinBounds(mouseX, mouseY, getPosX(), (getPosY() + getHeight()), getWidth(), (((getTotalHeight() > ClickGUIModule.height.getValue()) ? ClickGUIModule.height.getValue() : getTotalHeight())))) {
            for(Component component : getComponents()) {
                if(component.onMouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.onMouseReleased(mouseX, mouseY, mouseButton);
        if(!ClickGUI.isHudEditor()) return;
        if (mouseButton == 0 && isDragging()) {
            setDragging(false);
        }

        if (isExtended()) {
            getComponents().forEach(component -> component.onMouseReleased(mouseX, mouseY, mouseButton));
        }
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if(!ClickGUI.isHudEditor()) return;
        if(isExtended()) {
            getComponents().forEach(component -> component.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
        }
    }

    @Override
    public void setExtended(boolean extended) {
        if (extended) {
            enabledTicks = 0;
            super.setExtended(extended);
        } else {
            shouldDisable = true;
            enabledTicks = ClickGUIModule.animationSpeed.getValue();
        }
    }

    private float getTotalHeight() {
        float currentHeight = 0.0F;
        if (!isExtended()) return currentHeight;

        for (Component component : getComponents()) {
            currentHeight += component.getHeight();

            if (component instanceof ContainerComponent && component.isExtended()) {
                for (Component child : ((ContainerComponent) component).getEnabledComponents()) {
                    currentHeight += child.getHeight();
                }
            }
        }

        return currentHeight;
    }
    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }
}