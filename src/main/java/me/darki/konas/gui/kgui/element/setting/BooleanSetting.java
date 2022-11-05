package me.darki.konas.gui.kgui.element.setting;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.Check;
import me.darki.konas.gui.kgui.shape.RoundedRectangle;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.setting.Setting;

public class BooleanSetting extends Element {
    private Setting<Boolean> setting;

    public BooleanSetting(Setting<Boolean> setting, float parentX, float parentY, float offsetX, float offsetY, float width) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, setting.hasDescription() ? 38 : 18);
        this.setting = setting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        RoundedRectangle checkBox = new RoundedRectangle(getPosX(), getPosY(), 18, 18, 3F);
        Renderer.fillShape(checkBox, mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), 18, 18) ? secondaryFill.brighten(KonasGui.highlight.getValue()) : secondaryFill);
        Renderer.drawShape(checkBox, outlineFill, 1F);
        if (setting.getValue()) {
            Check check = new Check(getPosX(), getPosY(), 18, 18);
            Renderer.drawShape(check, fontFill, 2F);
        }
        Renderer.fontRenderer.drawString(setting.getName(), getPosX() + 26, (int) (getPosY() + (9F - Renderer.fontRenderer.getStringHeight(setting.getName()) / 2F)), fontFill);
        if (setting.hasDescription()) {
            Renderer.subFontRendrer.drawString(setting.getDescription(), getPosX(), (int) (getPosY() + 26F), subFontFill);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), 18, 18) && mouseButton == 0) {
            setting.setValue(!setting.getValue());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
