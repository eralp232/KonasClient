package me.darki.konas.gui.kgui.element.setting;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.Arrow;
import me.darki.konas.gui.kgui.shape.RoundedRectangle;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.setting.Setting;

public class EnumSetting extends Element {
    private Setting<Enum> setting;
    private boolean extended = false;

    public EnumSetting(Setting<Enum> setting, float parentX, float parentY, float offsetX, float offsetY, float width) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, setting.hasDescription() ? 54 : 38);
        this.setting = setting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        RoundedRectangle enumBox = new RoundedRectangle(getPosX(), getPosY() + (setting.hasDescription() ? 34 : 18), getWidth(), 20F, 3F, 25, extended ? RoundedRectangle.TOP_LEFT | RoundedRectangle.TOP_RIGHT : RoundedRectangle.ALL);
        Renderer.fillShape(enumBox, mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + (setting.hasDescription() ? 34 : 18), getWidth(), 20F) ? primaryFill.brighten(KonasGui.highlight.getValue()) : primaryFill);
        Arrow arrow = new Arrow(getPosX() + getWidth() - 17, getPosY() + (setting.hasDescription() ? 41 : 25), 10, 6, extended);
        Renderer.drawShape(arrow, darkFontFill, 3F);
        Renderer.fontRenderer.drawString(setting.getName(), getPosX(), getPosY(), fontFill);
        if (setting.hasDescription()) {
            Renderer.subFontRendrer.drawString(setting.getDescription(), getPosX(), (int) (getPosY() + 18F), subFontFill);
        }
        Renderer.fontRenderer.drawString(setting.getValue().toString(), (int) (getPosX() + 6F), (int) (getPosY() + (setting.hasDescription() ? 43.5F : 27.5F) - Renderer.fontRenderer.getStringHeight(setting.getValue().toString()) / 2F), darkFontFill);
    }

    @Override
    public void onRenderPost(int mouseX, int mouseY, float partialTicks) {
        super.onRenderPost(mouseX, mouseY, partialTicks);
        if (extended) {
            float offset = setting.hasDescription() ? 54F : 38F;
            for (int i = 0; i <= setting.getValue().getClass().getEnumConstants().length - 1; i++) {
                Enum e = setting.getValue().getClass().getEnumConstants()[i];
                if (e.name().equalsIgnoreCase(setting.getValue().toString())) continue;
                boolean rounded = i == setting.getValue().getClass().getEnumConstants().length - 1 || (i == setting.getValue().getClass().getEnumConstants().length - 2 && setting.getValue().getClass().getEnumConstants()[i+1].name().equalsIgnoreCase(setting.getValue().toString()));
                RoundedRectangle choice = new RoundedRectangle(getPosX(), getPosY() + offset, getWidth(), 20F, 3F,25, rounded ? RoundedRectangle.BOTTOM_LEFT | RoundedRectangle.BOTTOM_RIGHT : 0);
                Renderer.fillShape(choice, mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + offset, getWidth(), 20F) ? primaryFill.brighten(KonasGui.highlight.getValue()) : primaryFill);
                Renderer.fontRenderer.drawString(e.name(), getPosX() + 6, (int) (getPosY() + offset + 9.5F - Renderer.fontRenderer.getStringHeight(e.name()) / 2F), darkFontFill);
                offset += 20F;
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + (setting.hasDescription() ? 34F : 18F), getWidth(), 20F)) {
            if (mouseButton == 0) {
                extended = !extended;
            } else if (mouseButton == 1) {
                int i = setting.getEnum(setting.getValue().toString());
                if(isIndexInBounds(i + 1)) {
                    setting.setValue(setting.getValue().getClass().getEnumConstants()[i + 1]);
                } else {
                    setting.setValue(setting.getValue().getClass().getEnumConstants()[0]);
                }
            }
            return true;
        }
        if (extended) {
            float offset = setting.hasDescription() ? 54F : 38F;
            for (int i = 0; i <= setting.getValue().getClass().getEnumConstants().length - 1; i++) {
                Enum e = setting.getValue().getClass().getEnumConstants()[i];
                if (e.name().equalsIgnoreCase(setting.getValue().toString())) continue;
                if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + offset, getWidth(), 20F)) {
                    setting.setEnumValue(e.name());
                    extended = false;
                    return true;
                }
                offset += 20F;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean isIndexInBounds(int index) {
        return index <= setting.getValue().getClass().getEnumConstants().length - 1 && index >= 0;
    }
}
