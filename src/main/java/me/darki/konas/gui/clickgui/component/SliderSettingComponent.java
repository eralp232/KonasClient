package me.darki.konas.gui.clickgui.component;

import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.math.RoundingUtil;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.util.math.MathHelper;

public class SliderSettingComponent extends Component {
    private final Setting numberSetting;

    private boolean isSliding;

    public SliderSettingComponent(Setting numberSetting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(numberSetting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.numberSetting = numberSetting;
    }

    // The numbers mason... what do they mean?
    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        
        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), 0xFFFFFF);

        float length = MathHelper.floor((((Number)getNumberSetting().getValue()).floatValue() - ((Number) ((Number) getNumberSetting().getMin())).floatValue()) / (((Number) getNumberSetting().getMax()).floatValue() - ((Number) ((Number) getNumberSetting().getMin())).floatValue()) * (getWidth()));

        if (length < 0) {
            getNumberSetting().setValue(getNumberSetting().getMin());
            setSliding(false);
        } else if (length > getWidth()) {
            getNumberSetting().setValue(getNumberSetting().getMax());
            setSliding(false);
        }

        GuiRenderHelper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        int color = isSliding() ? ClickGUIModule.color.getValue().getColorObject().brighter().getRGB() : ClickGUIModule.color.getValue().getColorObject().darker().getRGB();
        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), length, getHeight(), color);

        ClickGUIFontRenderWrapper.drawStringWithShadow(getName() + ": " + getNumberSetting().getValue(), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(getName() + ": " + getNumberSetting().getValue()) / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());

        if (isSliding) {
            if (getNumberSetting().getValue() instanceof Float) {
                float newValue = (mouseX - (getAbsoluteX())) * (((Number) getNumberSetting().getMax()).floatValue() - ((Number) getNumberSetting().getMin()).floatValue()) / (getWidth()) + ((Number) getNumberSetting().getMin()).floatValue();

                getNumberSetting().setValue(MathHelper.clamp(RoundingUtil.roundFloat(RoundingUtil.roundToStep(newValue, (float) getNumberSetting().getSteps()), 2), (float) getNumberSetting().getMin(), (float) getNumberSetting().getMax()));
            } else if (getNumberSetting().getValue() instanceof Integer) {
                int newValue = (int) ((mouseX - (getAbsoluteX())) * (((Number) getNumberSetting().getMax()).intValue() - ((Number) getNumberSetting().getMin()).intValue()) / (getWidth()) + ((Number) getNumberSetting().getMin()).intValue());
                getNumberSetting().setValue(newValue);
            } else if (getNumberSetting().getValue() instanceof Double) {
                double newValue = (mouseX - (getAbsoluteX())) * (((Number) getNumberSetting().getMax()).doubleValue() - ((Number) getNumberSetting().getMin()).doubleValue()) / (getWidth()) + ((Number) getNumberSetting().getMin()).doubleValue();

                getNumberSetting().setValue(MathHelper.clamp(RoundingUtil.roundDouble(RoundingUtil.roundToStep(newValue, (double) getNumberSetting().getSteps()), 2), (double) getNumberSetting().getMin(), (double) getNumberSetting().getMax()));
            } else if (getNumberSetting().getValue() instanceof Long) {
                long newValue = (long) ((mouseX - (getAbsoluteX())) * (((Number) getNumberSetting().getMax()).doubleValue() - ((Number) getNumberSetting().getMin()).doubleValue()) / (getWidth()) + ((Number) getNumberSetting().getMin()).doubleValue());

                getNumberSetting().setValue(newValue);
            }
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        if (mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getAbsoluteX() + getWidth(), getHeight()) && button == 0) {
            setSliding(true);
            return true;
        }
        return false;
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int button) {
        super.onMouseReleased(mouseX, mouseY, button);
        if (isSliding()) {
            setSliding(false);
        }
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getAbsoluteX() + getWidth(), getHeight())) {
            setSliding(false);
        }
    }

    public void setSliding(boolean sliding) {
        this.isSliding = sliding;
    }

    public Setting getNumberSetting() {
        return this.numberSetting;
    }

    public boolean isSliding() {
        return this.isSliding;
    }
}
