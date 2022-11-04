package me.darki.konas.gui.kgui.element.container;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.element.setting.*;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.RoundedRectangle;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.setting.IBind;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Preview;
import me.darki.konas.setting.Setting;

import java.util.ArrayList;

public class ParentContainer extends Element {
    private final Module module;
    private final Parent parent;

    private ArrayList<Element> settings = new ArrayList<>();

    public ParentContainer(String name, Module module, Parent parent, float parentX, float parentY, float offsetX, float offsetY, float width, boolean right) {
        super(name, parentX, parentY, offsetX, offsetY, width, 0);
        this.module = module;
        this.parent = parent;
        float offset = 48F;
        if (parent == null && !module.getDescription().isEmpty()) {
            offset += 16F;
        }
        for (Setting setting : ModuleManager.getSettingList(module)) {
            if ((!setting.hasParent() && parent == null) || (setting.getParent() != null && setting.getParent().getValue() == parent)) {
                if (setting.getValue() instanceof Boolean) {
                    BooleanSetting booleanSetting = new BooleanSetting(setting, getPosX(), getPosY(), 16, offset, getWidth() - 32);
                    settings.add(booleanSetting);
                    offset += booleanSetting.getHeight() + 16;
                } else if (setting.getValue() instanceof IBind) {
                    BindSetting bindSetting = new BindSetting(setting, getPosX(), getPosY(), 16, offset, getWidth() - 32);
                    settings.add(bindSetting);
                    offset += bindSetting.getHeight() + 16;
                } else if (setting.getValue() instanceof Enum) {
                    EnumSetting enumSetting = new EnumSetting(setting, getPosX(), getPosY(), 16, offset, getWidth() - 32);
                    settings.add(enumSetting);
                    offset += 16 + enumSetting.getHeight();
                } else if (setting.getValue() instanceof Number) {
                    SliderSetting sliderSetting = new SliderSetting(setting, getPosX(), getPosY(), 16, offset, getWidth() - 32);
                    settings.add(sliderSetting);
                    offset += 16 + sliderSetting.getHeight();
                } else if (setting.getValue() instanceof me.darki.konas.setting.ColorSetting) {
                    me.darki.konas.gui.kgui.element.setting.ColorSetting colorSetting = new me.darki.konas.gui.kgui.element.setting.ColorSetting(setting, getPosX(), getPosY(), 16, offset, getWidth() - 32, right);
                    settings.add(colorSetting);
                    offset += 16 + colorSetting.getHeight();
                } else if (setting.getValue() instanceof Preview) {
                    PreviewSetting previewSetting = new PreviewSetting(setting, getPosX(), getPosY(), 16, offset, getWidth() - 32, 200);
                    settings.add(previewSetting);
                    offset += 16 + previewSetting.getHeight();
                }
            }
        }
        setHeight(offset);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        RoundedRectangle master = new RoundedRectangle(getPosX(), getPosY(), getWidth(), getHeight(), 3F);
        Renderer.fillShape(master, foregroundFill);
        Renderer.fontRenderer.drawString(getName(), getPosX() + 16, getPosY() + 16, fontFill);
        if (parent == null && !module.getDescription().isEmpty()) {
            Renderer.subFontRendrer.drawString(module.getDescription(), getPosX() + 16, getPosY() + 32, subFontFill);
        }
        settings.forEach(setting -> setting.onRender(mouseX, mouseY, partialTicks));
    }

    @Override
    public void onRenderPost(int mouseX, int mouseY, float partialTicks) {
        super.onRenderPost(mouseX, mouseY, partialTicks);
        settings.forEach(setting -> setting.onRenderPost(mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Element setting : settings) {
            if (setting.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (Element setting : settings) {
            if (setting.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) return true;
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        settings.forEach(setting -> setting.mouseReleased(mouseX, mouseY, state));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        settings.forEach(setting -> setting.keyTyped(typedChar, keyCode));
    }

    @Override
    public void setOffsetY(float offsetY) {
        super.setOffsetY(offsetY);
        settings.forEach(setting -> setting.onResize(getPosX(), getPosY()));
    }

    @Override
    public void onResize(float parentX, float parentY) {
        super.onResize(parentX, parentY);
        settings.forEach(setting -> setting.onResize(getPosX(), getPosY()));
    }
}
