package me.darki.konas.gui.clickgui.component;

import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;

public class EnumSettingComponent extends Component {

    Setting<Enum> enumSetting;

    public EnumSettingComponent(Setting<Enum> enumSetting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(enumSetting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.enumSetting = enumSetting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        int color = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()) ? new Color(96, 96, 96, 100).hashCode() : ClickGUIModule.secondary.getValue().getColor();
        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        ClickGUIFontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getName()) / 2)), ClickGUIModule.font.getValue().getColor());
        GuiRenderHelper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        ClickGUIFontRenderWrapper.drawStringWithShadow(getEnumSetting().getValue().toString(), (int) (getAbsoluteX() + 5.0F + ClickGUIFontRenderWrapper.getStringWidth(getName())), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getEnumSetting().getValue().toString()) / 2)), ClickGUIModule.color.getValue().getColor());
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

        boolean forward = mouseX > getAbsoluteX() + getWidth() / 2;

        if (withinBounds && mouseButton == 0) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            //Scroll through the enum values
            int i = getEnumSetting().getEnum(getEnumSetting().getValue().toString());
            if(forward) {
                if(isIndexInBounds(i + 1)) {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[i + 1]);
                } else {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[0]);
                }
            } else {
                if(isIndexInBounds(i - 1)) {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[i - 1]);
                } else {
                    getEnumSetting().setValue(getEnumSetting().getValue().getClass().getEnumConstants()[getEnumSetting().getValue().getClass().getEnumConstants().length - 1]);
                }
            }
            return true;
        }
        return false;
    }

    private boolean isIndexInBounds(int index) {
        return index <= getEnumSetting().getValue().getClass().getEnumConstants().length - 1 && index >= 0;
    }

    public Setting<Enum> getEnumSetting() {
        return enumSetting;
    }
}
