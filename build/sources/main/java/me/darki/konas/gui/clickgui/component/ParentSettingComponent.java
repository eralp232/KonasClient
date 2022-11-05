package me.darki.konas.gui.clickgui.component;

import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;

public class ParentSettingComponent extends Component {

    private final Setting<Parent> parentSetting;

    public ParentSettingComponent(Setting<Parent> parentSetting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(parentSetting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.parentSetting = parentSetting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        GuiRenderHelper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        int color = getParentSetting().getValue().isExtended() ?
                ClickGUIModule.color.getValue().getColor() :
                ClickGUIModule.secondary.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if (getParentSetting().getValue().isExtended()) {
                color = ClickGUIModule.color.getValue().getColorObject().brighter().hashCode();
            } else {
                color = new Color(96, 96, 96, 100).hashCode();
            }
        }
        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        ClickGUIFontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getName()) / 2)), ClickGUIModule.font.getValue().getColor());
        ClickGUIFontRenderWrapper.drawStringWithShadow(getParentSetting().getValue().isExtended() ? "-" : "+", (int) (getAbsoluteX() + getWidth() - 5F - ClickGUIFontRenderWrapper.getStringWidth(getParentSetting().getValue().isExtended() ? "-" : "+")), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getParentSetting().getValue().isExtended() ? "-" : "+") / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

        if (withinBounds && (mouseButton == 0 || mouseButton == 1)) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            getParentSetting().getValue().setExtended(!getParentSetting().getValue().isExtended());
            return true;
        }
        return false;
    }

    public Setting<Parent> getParentSetting() {
        return parentSetting;
    }
}
