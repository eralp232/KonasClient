package me.darki.konas.gui.kgui.element.button;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.KonasRectangle;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.util.KonasGlobals;

public class ModuleButton extends Element {
    private final Module module;

    public ModuleButton(Module module, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(module.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.module = module;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        KonasRectangle master = new KonasRectangle(getPosX(), getPosY(), getWidth(), getHeight());
        KonasRectangle accent = new KonasRectangle(getPosX(), getPosY(), 4, getHeight());

        if (module.isEnabled()) {
            Renderer.fillShape(master, mouseWithinBounds(mouseX, mouseY) ? secondaryFill.brighten(KonasGui.highlight.getValue()) : secondaryFill);
            Renderer.drawShape(master, outlineFill, 1F);
        } else if (mouseWithinBounds(mouseX, mouseY)) {
            Renderer.fillShape(master, foregroundFill.brighten(KonasGui.highlight.getValue()));
        }

        if (KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().getCategoryContainers().get(module.getCategory()).getCurrentModule() != null && KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().getCategoryContainers().get(module.getCategory()).getCurrentModule().equals(module)) {
            Renderer.fillShape(accent, accentFill);
            Renderer.drawShape(accent, outlineFill, 1F);
        }

        Renderer.fontRenderer.drawString(getName(), getPosX() + 12, (int) (getPosY() + (getHeight() / 2F) - (Renderer.fontRenderer.getStringHeight(getName()) / 2F)), fontFill);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY)) {
            if (mouseButton == 0) {
                module.toggle();
                return true;
            } else if (mouseButton == 1) {
                KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().getCategoryContainers().get(module.getCategory()).setCurrentModule(module);
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public Module getModule() {
        return module;
    }
}
