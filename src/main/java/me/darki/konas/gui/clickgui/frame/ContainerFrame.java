package me.darki.konas.gui.clickgui.frame;

import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.gui.container.Container;
import me.darki.konas.gui.container.ContainerManager;
import me.darki.konas.module.modules.client.HUD;
import me.darki.konas.util.KonasGlobals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Collections;

public class ContainerFrame extends Frame {

    private final Container container;

    public ContainerFrame(Container container) {
        super(container.getName(), container.getPosX(), container.getPosY(), container.getWidth(), container.getHeight());
        this.container = container;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);

        if(!ClickGUI.isHudEditor() || !container.isVisible()) return;

        // Dragging
        if (isDragging()) {
            drag(mouseX, mouseY);
        }

        setPosX(container.getPosX());
        setPosY(container.getPosY());
        setWidth(container.getWidth());
        setHeight(container.getHeight());
        setExtended(container.isVisible());

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        // Maintain screen bounds
        if (getPosX() < 0.0F) {
            setPosX(0.0F);
        }

        if (getPosX() + getWidth() > scaledResolution.getScaledWidth()) {
            setPosX(scaledResolution.getScaledWidth() - getWidth());
        }

        if (getPosY() < 0.0F) {
            setPosY(0.0F);
        }

        if (getPosY() + getHeight() > scaledResolution.getScaledHeight()) {
            setPosY(scaledResolution.getScaledHeight() - getHeight());
        }
    }

    private void drag(float mouseX, float mouseY) {
        if (!HUD.overlap.getValue()) {
            for (Container other : KonasGlobals.INSTANCE.containerManager.getContainers()) {
                if (other.equals(container)) continue;
                if (mouseX > other.getPosX() && mouseX < other.getPosX() + other.getWidth() && mouseY > other.getPosY() && mouseY < other.getPosY() + other.getHeight()) {
                    return;
                }
            }
        }

        float newX = mouseX + getPrevPosX();
        float newY = mouseY + getPrevPosY();

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        if (newX < 0.0F) {
            newX = 0;
        }

        if (newX + container.getWidth() > scaledResolution.getScaledWidth()) {
            newX = scaledResolution.getScaledWidth() - container.getWidth();
        }

        if (newY < 0.0F) {
            newY = 0;
        }

        if (newY + container.getHeight() > scaledResolution.getScaledHeight()) {
            newY = scaledResolution.getScaledHeight() - container.getHeight();
        }

        container.setPosX(newX);
        container.setPosY(newY);
        setPosX(newX);
        setPosY(newY);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);

        if(!ClickGUI.isHudEditor()) return false;

        container.onMouseClick(mouseX, mouseY, mouseButton);

        boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), getWidth(), getHeight());

        boolean clicked = false;

        switch (mouseButton) {
            case 0: {
                if (withinBounds) {
                    if(isExtended()) {
                        setDragging(true);
                        setPrevPosX(getPosX() - mouseX);
                        setPrevPosY(getPosY() - mouseY);
                        clicked = true;
                    }
                }
                break;
            }
            case 1: {
                if (withinBounds) {
                    setExtended(!isExtended());
                    clicked = true;
                }
                break;
            }
        }

        if(clicked) {
            Collections.swap(KonasGlobals.INSTANCE.containerManager.getContainers(), KonasGlobals.INSTANCE.containerManager.getContainers().indexOf(container), KonasGlobals.INSTANCE.containerManager.getContainers().size() - 1);
        }

        return clicked;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.onMouseReleased(mouseX, mouseY, mouseButton);

        if(!ClickGUI.isHudEditor()) return;

        if (mouseButton == 0 && isDragging()) {
            setDragging(false);
        }
    }

    @Override
    public void setPosX(float posX) {
        super.setPosX(posX);
    }

    @Override
    public void setPosY(float posY) {
        super.setPosY(posY);
    }

    @Override
    public void setExtended(boolean extended) {
        super.setExtended(extended);
        container.setVisible(extended);
    }

    @Override
    public void setDragging(boolean dragging) {
        super.setDragging(dragging);
        container.setDragging(dragging);
    }


    public Container getContainer() {
        return container;
    }
}
