package me.darki.konas.gui.kgui.element.container;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.element.button.ModuleButton;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.KonasRectangle;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.render.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ResultsContainer extends Element {
    private ArrayList<ModuleButton> moduleButtons = new ArrayList<>();

    private Module currentModule = null;

    private KonasRectangle master;

    public ResultsContainer(String query, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(query, parentX, parentY, offsetX, offsetY, width, height);

        int offY = 0;
        for (Module module : ModuleManager.getModules()) {
            if (module.getName().toLowerCase().contains(query.toLowerCase())) {
                moduleButtons.add(new ModuleButton(module, getPosX(), getPosY(),  0, offY, width, 32));
                offY += 40;
            }
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        if(!moduleButtons.isEmpty()) {
            if (currentModule == null) {
                currentModule = moduleButtons.get(0).getModule();
                KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().setModuleContainer(currentModule);
            }
            if (mouseWithinBounds(mouseX, mouseY)) {
                if (dWheel > 0) {
                    if (moduleButtons.get(0).getOffsetY() < 0) {
                        moduleButtons.forEach(button -> button.setOffsetY(button.getOffsetY() + 20));
                    }
                } else if (dWheel < 0) {
                    if (moduleButtons.get(moduleButtons.size() - 1).getPosY() + moduleButtons.get(moduleButtons.size() - 1).getHeight() > getPosY() + getHeight()) {
                        moduleButtons.forEach(button -> button.setOffsetY(button.getOffsetY() - 20));
                    }
                }
            }
        }
        Renderer.fillShape(master, foregroundFill);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiRenderHelper.prepareScissorBox(new ScaledResolution(Minecraft.getMinecraft()), getPosX(), getPosY(), getWidth(), getHeight());
        moduleButtons.forEach(button -> {
            if (button.getPosY() + button.getHeight() >= getPosY() && button.getPosY() <= getPosY() + getHeight()) {
                button.onRender(mouseX, mouseY, partialTicks);
            }
        });
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void setup() {
        if (currentModule == null) {
            currentModule = moduleButtons.get(0).getModule();
        }
        KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().setModuleContainer(currentModule);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (ModuleButton button : moduleButtons) {
            if (button.getPosY() + button.getHeight() >= getPosY() && button.getPosY() <= getPosY() + getHeight()) {
                if (button.mouseClicked(mouseX, mouseY, mouseButton)) return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void generateShapes() {
        super.generateShapes();
        master = new KonasRectangle(getPosX(), getPosY(), getWidth(), getHeight());
    }

    @Override
    public void onResize(float parentX, float parentY) {
        super.onResize(parentX, parentY);
        moduleButtons.forEach(button -> button.onResize(getPosX(), getPosY()));
    }

    public Module getCurrentModule() {
        return currentModule;
    }

    public void setCurrentModule(Module currentModule) {
        this.currentModule = currentModule;
        KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().setModuleContainer(currentModule);
    }
}

