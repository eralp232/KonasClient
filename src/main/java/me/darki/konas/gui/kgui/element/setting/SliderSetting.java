package me.darki.konas.gui.kgui.element.setting;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.RoundedRectangle;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.math.RoundingUtil;
import net.minecraft.util.math.MathHelper;

public class SliderSetting extends Element {
    private Setting<Number> setting;
    private boolean sliding = false;

    public SliderSetting(Setting<Number> setting, float parentX, float parentY, float offsetX, float offsetY, float width) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, setting.hasDescription() ? 54 : 38);
        this.setting = setting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        Renderer.fontRenderer.drawString(setting.getName(), getPosX(), getPosY(), fontFill);
        if (setting.hasDescription()) {
            Renderer.subFontRendrer.drawString(setting.getDescription(), getPosX(), (int) (getPosY() + 18F), subFontFill);
        }
        RoundedRectangle slider = new RoundedRectangle(getPosX() + 16, getPosY() + (setting.hasDescription() ? 34 : 18), getWidth() - 32, 6F, 3F);
        Renderer.fillShape(slider, secondaryFill);

        float length = MathHelper.floor(((setting.getValue()).floatValue() - ( ( setting.getMin())).floatValue()) / (( setting.getMax()).floatValue() - ( ( setting.getMin())).floatValue()) * (getWidth() - 32));
        
        if (length < 0) {
            setting.setValue(setting.getMin());
            sliding = false;
        } else if (length > getWidth()) {
            setting.setValue(setting.getMax());
            sliding = false;
        }

        if (sliding) {
            if (setting.getValue() instanceof Float) {
                float newValue = (mouseX - (getPosX() + 16)) * ((setting.getMax()).floatValue() - (setting.getMin()).floatValue()) / (getWidth() - 32) + (setting.getMin()).floatValue();
                setting.setValue(MathHelper.clamp(RoundingUtil.roundFloat(RoundingUtil.roundToStep(newValue, (float) setting.getSteps()), 2), (float) setting.getMin(), (float) setting.getMax()));
            } else if (setting.getValue() instanceof Integer) {
                int newValue = (int) ((mouseX - (getPosX() + 16)) * ((setting.getMax()).intValue() - (setting.getMin()).intValue()) / (getWidth() - 32) + (setting.getMin()).intValue());
                setting.setValue(newValue);
            } else if (setting.getValue() instanceof Double) {
                double newValue = (mouseX - (getPosX() + 16)) * ((setting.getMax()).doubleValue() - (setting.getMin()).doubleValue()) / (getWidth() - 32) + (setting.getMin()).doubleValue();

                setting.setValue(MathHelper.clamp(RoundingUtil.roundDouble(RoundingUtil.roundToStep(newValue, (double) setting.getSteps()), 2), (double) setting.getMin(), (double) setting.getMax()));
            } else if (setting.getValue() instanceof Long) {
                long newValue = (long) ((mouseX - (getPosX() + 16)) * ((setting.getMax()).doubleValue() - ( setting.getMin()).doubleValue()) / (getWidth() - 32) + (setting.getMin()).doubleValue());

                setting.setValue(newValue);
            }
        }

        RoundedRectangle slidah = new RoundedRectangle(getPosX() + 10, getPosY() + (setting.hasDescription() ? 34 : 18), 12 + length, 6F, 6F);
        Renderer.fillShape(slidah, mouseWithinBounds(mouseX, mouseY, getPosX() + 10 + length, getPosY() + (setting.hasDescription() ? 31 : 15), 12, 12) ? primaryFill.brighten(KonasGui.highlight.getValue()) : primaryFill);
        Renderer.drawShape(slidah, outlineFill, 1F);

        RoundedRectangle circle = new RoundedRectangle(getPosX() + 10 + length, getPosY() + (setting.hasDescription() ? 31 : 15), 12, 12, 6F);
        Renderer.fillShape(circle, mouseWithinBounds(mouseX, mouseY, getPosX() + 10 + length, getPosY() + (setting.hasDescription() ? 31 : 15), 12, 12) ? primaryFill.brighten(KonasGui.highlight.getValue()) : primaryFill);
        Renderer.drawShape(circle, outlineFill, 1F);
        Renderer.subFontRendrer.drawString(setting.getValue().toString(), (int) (getPosX() + 16F + length - Renderer.subFontRendrer.getStringWidth(setting.getValue().toString()) / 2F), (int) (getPosY() + (setting.hasDescription() ? 48 : 32)), fontFill);

        Renderer.subFontRendrer.drawString("-", getPosX(), getPosY() + (setting.hasDescription() ? 34 : 18) + 3 - Renderer.subFontRendrer.getStringHeight("-"), fontFill);
        Renderer.subFontRendrer.drawString("+", getPosX() + getWidth() - Renderer.subFontRendrer.getStringWidth("+"), getPosY() + (setting.hasDescription() ? 34 : 18) + 3 - Renderer.subFontRendrer.getStringHeight("+"), fontFill);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY, getPosX() + 16, getPosY() + (setting.hasDescription() ? 31 : 15), getWidth() - 32F, 12F) && mouseButton == 0) {
            sliding = true;
            return true;
        } else if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + (setting.hasDescription() ? 31 : 15), 12, 12)) {
            if (setting.getValue() instanceof Float) {
                float newValue = setting.getValue().floatValue() - setting.getSteps().floatValue();
                setting.setValue(MathHelper.clamp(RoundingUtil.roundFloat(RoundingUtil.roundToStep(newValue, (float) setting.getSteps()), 2), (float) setting.getMin(), (float) setting.getMax()));
            } else if (setting.getValue() instanceof Integer) {
                int newValue = setting.getValue().intValue() - setting.getSteps().intValue();
                setting.setValue(newValue);
            } else if (setting.getValue() instanceof Double) {
                double newValue = setting.getValue().doubleValue() +-setting.getSteps().doubleValue();
                setting.setValue(MathHelper.clamp(RoundingUtil.roundDouble(RoundingUtil.roundToStep(newValue, (double) setting.getSteps()), 2), (double) setting.getMin(), (double) setting.getMax()));
            } else if (setting.getValue() instanceof Long) {
                long newValue = setting.getValue().longValue() - setting.getSteps().longValue();
                setting.setValue(newValue);
            }
        } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + getWidth() - 12, getPosY() + (setting.hasDescription() ? 31 : 15), 12, 12)) {
            if (setting.getValue() instanceof Float) {
                float newValue = setting.getValue().floatValue() + setting.getSteps().floatValue();
                setting.setValue(MathHelper.clamp(RoundingUtil.roundFloat(RoundingUtil.roundToStep(newValue, (float) setting.getSteps()), 2), (float) setting.getMin(), (float) setting.getMax()));
            } else if (setting.getValue() instanceof Integer) {
                int newValue = setting.getValue().intValue() + setting.getSteps().intValue();
                setting.setValue(newValue);
            } else if (setting.getValue() instanceof Double) {
                double newValue = setting.getValue().doubleValue() + setting.getSteps().doubleValue();
                setting.setValue(MathHelper.clamp(RoundingUtil.roundDouble(RoundingUtil.roundToStep(newValue, (double) setting.getSteps()), 2), (double) setting.getMin(), (double) setting.getMax()));
            } else if (setting.getValue() instanceof Long) {
                long newValue = setting.getValue().longValue() + setting.getSteps().longValue();
                setting.setValue(newValue);
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!mouseWithinBounds(mouseX, mouseY, getPosX() + 16, getPosY() + (setting.hasDescription() ? 31 : 15), getWidth() - 32F, 12F)) {
            sliding = false;
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (sliding) {
            sliding = false;
        }
    }
}