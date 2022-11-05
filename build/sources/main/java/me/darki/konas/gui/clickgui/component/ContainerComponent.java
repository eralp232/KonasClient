package me.darki.konas.gui.clickgui.component;

import me.darki.konas.gui.container.Container;
import me.darki.konas.gui.container.ContainerManager;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;

import java.awt.*;
import java.util.ArrayList;

public class ContainerComponent extends Component {

    private final Container container;

    private final ArrayList<Component> components = new ArrayList<>();

    public ContainerComponent(Container container, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(container.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.container = container;
    }

    @Override
    public void initialize() {
        super.initialize();

        float childOffsetY = getHeight();

        for (Setting setting : ContainerManager.getSettingList(getContainer())) {
            float tempXOffset = setting.hasParent() ? 5F : 1F;
            float tempWidth = setting.hasParent() ? 6F : 2F;
            if (setting.getValue() instanceof Boolean) {
                getComponents().add(new BooleanSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempWidth, 12.0F));
                childOffsetY += 12F;
            }else if (setting.getValue() instanceof Parent) {
                getComponents().add(new ParentSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempWidth, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Number) {
                getComponents().add(new SliderSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempWidth, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Enum) {
                getComponents().add(new EnumSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempWidth, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof ColorSetting) {
                getComponents().add(new ColorSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempWidth, 60F));
                childOffsetY += 12F;
            }
        }

        getEnabledComponents().forEach(Component::initialize);
    }

    @Override
    public void onMove(float parentX, float parentY) {
        super.onMove(parentX, parentY);
        getEnabledComponents().forEach(component -> component.onMove(getAbsoluteX(), getAbsoluteY()));
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);

        float childOffsetY = getHeight();

        for (Component component : getEnabledComponents()) {
            component.setOffsetY(childOffsetY);
            childOffsetY += component.getHeight();
        }

        int color =  container.isVisible() ?
                ClickGUIModule.color.getValue().getColor() :
                ClickGUIModule.secondary.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if(container.isVisible()) {
                color = ClickGUIModule.color.getValue().getColorObject().brighter().hashCode();
            } else {
                color = new Color(96, 96, 96, 100).hashCode();
            }
        }


        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);

        ClickGUIFontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 4.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getName()) / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());

        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onRender(mouseX, mouseY, partialTicks));
        }
    }

    @Override
    public void onKeyTyped(char character, int keyCode) {
        super.onKeyTyped(character, keyCode);
        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onKeyTyped(character, keyCode));
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        boolean hovered = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
        if (hovered) {
            switch (mouseButton) {
                case 0: {
                    getContainer().toggleVisibility();
                    return true;
                }
                case 1: {
                    setExtended(!isExtended());
                    return true;
                }
            }
        }

        if (isExtended()) {
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
        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onMouseReleased(mouseX, mouseY, mouseButton));
        }
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick));
        }
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public ArrayList<Component> getEnabledComponents() {
        ArrayList<Component> enabledComponents = new ArrayList<>();
        for (Component component : getComponents()) {
            if (component instanceof BooleanSettingComponent) {
                if (((BooleanSettingComponent) component).getBooleanSetting().isVisible()) {
                    enabledComponents.add(component);
                }
            } else if (component instanceof ColorSettingComponent) {
                if (((ColorSettingComponent) component).getSetting().isVisible()) {
                    enabledComponents.add(component);
                }
            } else if (component instanceof EnumSettingComponent) {
                if (((EnumSettingComponent) component).getEnumSetting().isVisible()) {
                    enabledComponents.add(component);
                }
            }
            else if (component instanceof SliderSettingComponent) {
                if (((SliderSettingComponent) component).getNumberSetting().isVisible()) {
                    enabledComponents.add(component);
                }
            } else {
                enabledComponents.add(component);
            }
        }
        return enabledComponents;
    }

    public Container getContainer() {
        return container;
    }
}
