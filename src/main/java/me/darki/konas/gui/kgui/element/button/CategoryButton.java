package me.darki.konas.gui.kgui.element.button;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.RoundedRectangle;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.util.KonasGlobals;

public class CategoryButton extends Element {
    // 74 wide
    private final Module.Category category;
    private final int corners;

    private RoundedRectangle master;
    private RoundedRectangle outline;

    public CategoryButton(Module.Category category, float parentX, float parentY, float offsetX, float offsetY, float width, float height, int corners) {
        super(category.name(), parentX, parentY, offsetX, offsetY, width, height);
        this.category = category;
        this.corners = corners;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        boolean current = KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().getCurrentCategory() == category;
        if (current) {
            Renderer.fillShape(outline, mouseWithinBounds(mouseX, mouseY) ? accentFill.brighten(KonasGui.highlight.getValue()) : accentFill);
            Renderer.drawShape(outline, outlineFill, 1F);
        } else {
            Renderer.fillShape(master, mouseWithinBounds(mouseX, mouseY) ? categoryFill.brighten(KonasGui.highlight.getValue()) : categoryFill);
        }
        String name = getName();
        if (name.equals("MOVEMENT")) {
            name = "TRAVEL";
        }
        Renderer.fontRenderer.drawString(name, (int) (getPosX() + getWidth() / 2F - Renderer.fontRenderer.getStringWidth(name) / 2F), (int) (getPosY() + (getHeight() / 2F) - (Renderer.fontRenderer.getStringHeight(getName()) / 2F)), fontFill);
    }

    @Override
    protected void generateShapes() {
        master = new RoundedRectangle(getPosX(), getPosY(), getWidth(), getHeight(), 3F, 25, corners);
        outline = new RoundedRectangle(getPosX(), getPosY() - 1F, getWidth(), getHeight() + 2F, 3F, 25, RoundedRectangle.TOP_LEFT | RoundedRectangle.TOP_RIGHT);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY)) {
            KonasGlobals.INSTANCE.konasGuiScreen.getMasterContainer().setCurrentCategory(category);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
