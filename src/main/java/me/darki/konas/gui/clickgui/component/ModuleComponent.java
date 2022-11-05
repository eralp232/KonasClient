package me.darki.konas.gui.clickgui.component;

import me.darki.konas.gui.clickgui.frame.DescriptionFrame;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.*;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class ModuleComponent extends Component {
    private final Module module;

    private final ArrayList<Component> components = new ArrayList<>();

    private final Timer hoverTimer = new Timer();

    public ModuleComponent(Module module, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(module.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.module = module;
    }

    @Override
    public void initialize() {
        super.initialize();

        float childOffsetY = getHeight();

        for (Setting setting : ModuleManager.getSettingList(getModule())) {
            float tempXOffset = setting.hasParent() ? 4F : 2F;
            if (setting.getValue() instanceof Bind) {
                getComponents().add(new BindSettingComponent(getModule(), getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof SubBind) {
                getComponents().add(new SubBindSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Boolean) {
                getComponents().add(new BooleanSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            }else if (setting.getValue() instanceof Parent) {
                getComponents().add(new ParentSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Number) {
                getComponents().add(new SliderSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof Enum) {
                getComponents().add(new EnumSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 12.0F));
                childOffsetY += 12F;
            } else if (setting.getValue() instanceof ColorSetting) {
                getComponents().add(new ColorSettingComponent(setting, getAbsoluteX(), getAbsoluteY(), tempXOffset, childOffsetY, getWidth() - tempXOffset, 60F));
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

        int color =  module.isEnabled() ?
                ClickGUIModule.color.getValue().getColor() :
                ClickGUIModule.secondary.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if(module.isEnabled()) {
                color = ClickGUIModule.color.getValue().getColorObject().brighter().hashCode();
            } else {
                color = new Color(96, 96, 96, 100).hashCode();
            }
        }

        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);

        int nameY = (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getName()) / 2));
        ClickGUIFontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 4.0F), nameY, ClickGUIModule.font.getValue().getColor());

        if (module.getKeybind() != Keyboard.KEY_NONE && ClickGUIModule.binds.getValue()) {
            String keyName = GameSettings.getKeyDisplayString(getModule().getKeybind());
            keyName = keyName.replaceAll("NUMPAD", "");
            ClickGUIFontRenderWrapper.drawStringWithShadow(keyName, (int) (getAbsoluteX() + getWidth() - 4.0F - (ClickGUIFontRenderWrapper.getStringWidth(keyName) - 0.5F)), nameY, ClickGUIModule.font.getValue().getColor());
        }

        if (isExtended()) {
            getEnabledComponents().forEach(component -> component.onRender(mouseX, mouseY, partialTicks));
        }
    }

    public void handleDescription(int mouseX, int mouseY) {
        if (mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if (hoverTimer.hasPassed(500)) {
                DescriptionFrame.desc = getModule().getDescription();
            }
        } else {
            if(DescriptionFrame.desc != null && DescriptionFrame.desc.equals(getModule().getDescription())) {
                DescriptionFrame.desc = null;
            }
            hoverTimer.reset();
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
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    getModule().toggle();
                    return true;
                }
                case 1: {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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

    public Module getModule() {
        return module;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }


    public ArrayList<Component> getEnabledComponents() {
        ArrayList<Component> enabledComponents = new ArrayList<>();
        for (Component component : getComponents()) {
            if (component instanceof BindSettingComponent) {
                enabledComponents.add(component);
            } else if (component instanceof BooleanSettingComponent) {
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
            } else if (component instanceof SubBindSettingComponent) {
                if (((SubBindSettingComponent) component).getSetting().isVisible()) {
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

}
