package me.darki.konas.gui.clickgui.frame;

import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.gui.clickgui.component.Component;
import me.darki.konas.gui.clickgui.component.ModuleComponent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;

import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

public class CategoryFrame extends Frame {
    private final Module.Category moduleCategory;

    private final ArrayList<Component> components = new ArrayList<>();

    private int scrollY;

    private int enabledTicks = 0;

    private boolean shouldDisable = false;

    public CategoryFrame(Module.Category moduleCategory, float posX, float posY, float width, float height) {
        super(moduleCategory.name(), posX, posY, width, height);
        this.moduleCategory = moduleCategory;
    }

    @Override
    public void initialize() {
        float offsetY = getHeight();

        ArrayList<Module> modulesList = ModuleManager.getModulesByCategory(getModuleCategory());
        modulesList.sort(Comparator.comparing(Module::getName));

        for (Module module : modulesList) {
            getComponents().add(new ModuleComponent(module, getPosX(), getPosY(), 2.0F, offsetY, getWidth() - 4.0F, 14.0F));
            offsetY += 14.0F;
        }

        getComponents().forEach(Component::initialize);
    }

    @Override
    public void onMove(float posX, float posY) {
        components.forEach(component -> component.onMove(posX, posY));
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        if(ClickGUI.isHudEditor()) return;

        // Dragging
        if (isDragging()) {
            setPosX(mouseX + getPrevPosX());
            setPosY(mouseY + getPrevPosY());
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        // This is very important, to maintain bounds within screen
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        if (getPosX() < 0.0F) {
            setPosX(0.0F);
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        if (getPosX() + getWidth() > scaledResolution.getScaledWidth()) {
            setPosX(scaledResolution.getScaledWidth() - getWidth());
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        if (getPosY() < 0.0F) {
            setPosY(0.0F);
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
        }

        if (getPosY() + getHeight() + Math.min(getTotalHeight(), ClickGUIModule.height.getValue()) > scaledResolution.getScaledHeight()) {
            setPosY(scaledResolution.getScaledHeight() - getHeight() - Math.min(getTotalHeight(), ClickGUIModule.height.getValue()));
            getEnabledComponents().forEach(component -> component.onMove(getPosX(), getPosY() + getScrollY()));
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
            GuiRenderHelper.drawRect(getPosX(), getPosY() + getHeight(), getWidth(), Math.min(getTotalHeight(), ClickGUIModule.height.getValue()), ClickGUIModule.background.getValue().getColor());
            getEnabledComponents().forEach(component -> component.onRender(mouseX, mouseY, partialTicks));
            GL11.glDisable(GL_SCISSOR_TEST);
            if (ClickGUIModule.outline.getValue() && !ClickGUIModule.animate.getValue()) {
                float thick = (float) ClickGUIModule.thickness.getValue();
                GuiRenderHelper.drawRect(getPosX() - thick, getPosY() - thick, thick, getHeight() - thick + Math.min(getTotalHeight(), ClickGUIModule.height.getValue()) + thick*2F, ClickGUIModule.color.getValue().getColor());
                GuiRenderHelper.drawRect(getPosX(), getPosY() - thick, getWidth(), thick, ClickGUIModule.color.getValue().getColor());
                GuiRenderHelper.drawRect(getPosX() + getWidth(), getPosY() - thick, thick, getHeight() - thick + Math.min(getTotalHeight(), ClickGUIModule.height.getValue()) + thick*2F, ClickGUIModule.color.getValue().getColor());
                GuiRenderHelper.drawRect(getPosX(), getPosY() + getHeight() + Math.min(getTotalHeight(), ClickGUIModule.height.getValue()) - thick, getWidth(), thick, ClickGUIModule.color.getValue().getColor());
            }
            GL11.glPopMatrix();
        } else if (ClickGUIModule.outline.getValue() && !ClickGUIModule.animate.getValue()) {
            float thick = (float) ClickGUIModule.thickness.getValue();
            GuiRenderHelper.drawRect(getPosX() - thick, getPosY() - thick, thick, getHeight() + thick*2F, ClickGUIModule.color.getValue().getColor());
            GuiRenderHelper.drawRect(getPosX(), getPosY() - thick, getWidth(), thick, ClickGUIModule.color.getValue().getColor());
            GuiRenderHelper.drawRect(getPosX() + getWidth(), getPosY() - thick, thick, getHeight() + thick*2F, ClickGUIModule.color.getValue().getColor());
            GuiRenderHelper.drawRect(getPosX(), getPosY() + getHeight(), getWidth(), thick, ClickGUIModule.color.getValue().getColor());
        }

        updateComponentOffsets(mouseX, mouseY);
    }

    private void updateComponentOffsets(int mouseX, int mouseY) {
        float offsetY = getHeight();

        for (Component component: getEnabledComponents()) {
            component.setOffsetY(offsetY);
            component.onMove(getPosX(), getPosY() + getScrollY());
            if (component instanceof ModuleComponent) {
                if (offsetY <= ClickGUIModule.height.getValue()) {
                    ((ModuleComponent) component).handleDescription(mouseX, mouseY);
                }
                if (component.isExtended()) {
                    for (Component child : ((ModuleComponent) component).getEnabledComponents())
                        offsetY += child.getHeight();
                }
            }
            offsetY += component.getHeight();
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

    @Override
    public void onKeyTyped(char character, int keyCode) {
        super.onKeyTyped(character, keyCode);
        if(ClickGUI.isHudEditor()) return;
        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onKeyTyped(character, keyCode));
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        if(ClickGUI.isHudEditor()) return false;
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
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    setExtended(!isExtended());
                    return true;
                }
                break;
            }
        }

        if (isExtended() && mouseWithinBounds(mouseX, mouseY, getPosX(), (getPosY() + getHeight()), getWidth(), (((getTotalHeight() > ClickGUIModule.height.getValue()) ? ClickGUIModule.height.getValue() : getTotalHeight())))) {
            for(Component component : getEnabledComponents()) {
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
        if(ClickGUI.isHudEditor()) return;
        if (mouseButton == 0 && isDragging()) {
            setDragging(false);
        }

        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onMouseReleased(mouseX, mouseY, mouseButton));
        }
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if(ClickGUI.isHudEditor()) return;
        if(isExtended()) {
            getEnabledComponents().forEach(component -> component.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
        }
    }

    private float getTotalHeight() {
        float currentHeight = 0.0F;
        if (!isExtended()) return currentHeight;

        for (Component component : getEnabledComponents()) {
            currentHeight += component.getHeight();

            if (component instanceof ModuleComponent && component.isExtended()) {
                for (Component child : ((ModuleComponent) component).getEnabledComponents()) {
                    currentHeight += child.getHeight();
                }
            }
        }

        return currentHeight;
    }

    public Module.Category getModuleCategory() {
        return moduleCategory;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public ArrayList<Component> getEnabledComponents() {
        ArrayList<Component> enabledComponents = new ArrayList<>();
        for (Component component : getComponents()) {
            if (component instanceof ModuleComponent) {
                if (((ModuleComponent) component).getModule().isProtocolValid()) {
                    enabledComponents.add(component);
                }
            } else {
                enabledComponents.add(component);
            }
        }
        return enabledComponents;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }
}