package me.darki.konas.gui.kgui.element.container;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ModuleContainer extends Element {
    private final Module module;

    private ArrayList<ParentContainer> parentContainers = new ArrayList<>();

    public ModuleContainer(Module module, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(module.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.module = module;
        boolean isRight = !KonasGui.singleColumn.getValue();
        float leftOffset = 16;
        float rightOffset = 16;
        parentContainers.add(new ParentContainer("General", module, null, getPosX(), getPosY(), 16, leftOffset, 296, false));
        leftOffset += 16 + parentContainers.get(0).getHeight();
        for (Setting setting : ModuleManager.getSettingList(module)) {
            if (setting.getValue() instanceof Parent) {
                if (isRight && leftOffset < rightOffset) {
                    isRight = false;
                }
                ParentContainer container = new ParentContainer(setting.getName(), module, (Parent) setting.getValue(), getPosX(), getPosY(), isRight ? 328 : 16, isRight ? rightOffset : leftOffset, 296, isRight);
                parentContainers.add(container);
                if (isRight) {
                    rightOffset += 16 + container.getHeight();
                } else {
                    leftOffset += 16 + container.getHeight();
                }
                if (KonasGui.singleColumn.getValue()) {
                    isRight = false;
                } else {
                    isRight = !isRight;
                }
            }
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiRenderHelper.prepareScissorBox(new ScaledResolution(Minecraft.getMinecraft()), getPosX(), getPosY(), getWidth(), getHeight());
        parentContainers.forEach(parentContainer -> parentContainer.onRender(mouseX, mouseY, partialTicks));
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (mouseWithinBounds(mouseX, mouseY)) {
            if (dWheel > 0) {
                if (parentContainers.get(0).getOffsetY() < 0) {
                    parentContainers.forEach(button -> button.setOffsetY(button.getOffsetY() + 40));
                }
            } else if (dWheel < 0) {
                if (parentContainers.get(parentContainers.size() - 1).getPosY() + parentContainers.get(parentContainers.size() - 1).getHeight() > getPosY() + getHeight() || (parentContainers.size() > 1 && parentContainers.get(parentContainers.size() - 2).getPosY() + parentContainers.get(parentContainers.size() - 2).getHeight() > getPosY() + getHeight())) {
                    parentContainers.forEach(button -> button.setOffsetY(button.getOffsetY() - 40));
                }
            }
        }
    }

    @Override
    public void onRenderPost(int mouseX, int mouseY, float partialTicks) {
        super.onRenderPost(mouseX, mouseY, partialTicks);
        parentContainers.forEach(parentContainer -> parentContainer.onRenderPost(mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        for (ParentContainer parentContainer : parentContainers) {
            if (parentContainer.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
            return true;
        }
        for (ParentContainer parentContainer : parentContainers) {
            if (parentContainer.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for (ParentContainer parentContainer : parentContainers) {
            parentContainer.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        parentContainers.forEach(parentContainer -> parentContainer.keyTyped(typedChar, keyCode));
    }

    @Override
    public void onResize(float parentX, float parentY) {
        super.onResize(parentX, parentY);
        parentContainers.forEach(parentContainer -> parentContainer.onResize(getPosX(), getPosY()));
    }
}
