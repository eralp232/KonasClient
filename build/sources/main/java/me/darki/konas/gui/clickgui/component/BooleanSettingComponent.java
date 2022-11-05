package me.darki.konas.gui.clickgui.component;

import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;

public class BooleanSettingComponent extends Component {

    private final Setting<Boolean> booleanSetting;

    public BooleanSettingComponent(Setting<Boolean> booleanSetting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(booleanSetting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.booleanSetting = booleanSetting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        GuiRenderHelper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        int color =  getBooleanSetting().getValue() ?
                ClickGUIModule.color.getValue().getColorObject().darker().getRGB() :
                ClickGUIModule.secondary.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if(getBooleanSetting().getValue()) {
                color = ClickGUIModule.color.getValue().getColorObject().brighter().getRGB();
            } else {
                color = new Color(96, 96, 96, 100).hashCode();
            }
        }

        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        ClickGUIFontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getName()) / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

        if (withinBounds && mouseButton == 0) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            getBooleanSetting().setValue(!getBooleanSetting().getValue());
            return true;
        }
        return false;
    }

    public Setting<Boolean> getBooleanSetting() {
        return booleanSetting;
    }
}
