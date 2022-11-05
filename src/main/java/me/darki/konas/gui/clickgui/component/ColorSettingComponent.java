package me.darki.konas.gui.clickgui.component;

import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.RenderUtil;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class ColorSettingComponent extends Component {
    private final Setting colorSetting;

    public int alpha;
    public int red;
    public int green;
    public int blue;

    float sliderWidth = 45F;
    float steps = sliderWidth / 10F;
    float sliderX = getAbsoluteX() + 1F;
    float sliderY = getAbsoluteY() + 48F;
    float sliderHeight = 10F;

    float pickerBoxX = getAbsoluteX() + 1F;
    float pickerBoxY = getAbsoluteY() + 18F;
    float pickerBoxHeight = 26F;
    float pickerBoxWidth = 44F;

    private boolean isOpen = false;

    private final Timer timer = new Timer();

    public ColorSettingComponent(Setting colorSetting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(colorSetting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.colorSetting = colorSetting;
        this.alpha = (getColorSetting().getRawColor() >> 24) & 0xff;
        this.red = (getColorSetting().getRawColor() >> 16) & 0xFF;
        this.green = (getColorSetting().getRawColor() >> 8) & 0xFF;
        this.blue = (getColorSetting().getRawColor()) & 0xFF;
    }

    public void refresh() {
        this.alpha = (getColorSetting().getRawColor() >> 24) & 0xff;
        this.red = (getColorSetting().getRawColor() >> 16) & 0xFF;
        this.green = (getColorSetting().getRawColor() >> 8) & 0xFF;
        this.blue = (getColorSetting().getRawColor()) & 0xFF;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public float getHeight() {
        return isOpen() ? super.getHeight() : 12F;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);

        float[] hsb = Color.RGBtoHSB(red, green, blue, null);

        GuiRenderHelper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), 12F, ClickGUIModule.color.getValue().getColor());
        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), 12F, ClickGUIModule.secondary.getValue().getColor());

        int color =  isOpen() ?
                ClickGUIModule.color.getValue().getColorObject().darker().getRGB() :
                ClickGUIModule.secondary.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight())) {
            if(isOpen()) {
                color = ClickGUIModule.color.getValue().getColorObject().brighter().getRGB();
            } else {
                color = new Color(96, 96, 96, 100).hashCode();
            }
        }

        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), 12F, color);
        ClickGUIFontRenderWrapper.drawStringWithShadow(getName(), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + 12F / 2.0F - (ClickGUIFontRenderWrapper.getFontHeight() / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());
        ClickGUIFontRenderWrapper.drawStringWithShadow(isOpen() ? "-" : "+", (int) (getAbsoluteX() + getWidth() - 5F - ClickGUIFontRenderWrapper.getStringWidth(isOpen() ? "-" : "+")), (int) (getAbsoluteY() + 12F / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(isOpen() ? "-" : "+") / 2) - 0.5F), ClickGUIModule.font.getValue().getColor());

        if (isOpen) {
            GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY() + 17F, 46F, 28F, ClickGUIModule.color.getValue().getColor());

            pickerBoxX = getAbsoluteX() + 1F;
            pickerBoxY = getAbsoluteY() + 18F;

            RenderUtil.draw2DGradientRect(pickerBoxX, pickerBoxY, pickerBoxX + pickerBoxWidth, pickerBoxY + pickerBoxHeight, Color.getHSBColor(hsb[0], 0f, 0f).getRGB(), Color.getHSBColor(hsb[0], 0f, 1f).getRGB(), Color.getHSBColor(hsb[0], 1f, 0f).getRGB(), Color.getHSBColor(hsb[0], 1f, 1f).getRGB());

            GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY() + 47F, 46F, 12F, ClickGUIModule.color.getValue().getColor());

            sliderX = getAbsoluteX() + 1F;
            sliderY = getAbsoluteY() + 48F;

            for (float i = 0.0F; i + steps <= sliderWidth; i += steps) {
                RenderUtil.draw1DGradientRect(sliderX + i, sliderY, sliderX + steps + i, sliderY + sliderHeight, Color.getHSBColor(i / sliderWidth, 1f, 1f).getRGB(), Color.getHSBColor((i + steps) / sliderWidth, 1f, 1f).getRGB());
            }

            GuiRenderHelper.drawRect(getAbsoluteX() + 45F, getAbsoluteY() + 47F, 1F, 12F, ClickGUIModule.color.getValue().getColor());

            int maxAlpha = ((0xFF) << 24) |
                    ((red & 0xFF) << 16) |
                    ((green & 0xFF) << 8) |
                    ((blue & 0xFF));

            int minAlpha = ((0) << 24) |
                    ((red & 0xFF) << 16) |
                    ((green & 0xFF) << 8) |
                    ((blue & 0xFF));

            float alphaX = getAbsoluteX() + getWidth() - 11F;
            float alphaY = getAbsoluteY() + 17F;
            float alphaWidth = 10F;
            float alphaHeight = 42F;


            RenderUtil.draw2DGradientRect(alphaX, alphaY, getAbsoluteX() + getWidth() - 1F, getAbsoluteY() + getHeight() - 2F, minAlpha, maxAlpha, minAlpha, maxAlpha);

            // DRAW RGBA TEXT

            ClickGUIFontRenderWrapper.drawString("R" + red, (int) getAbsoluteX() + 3 + (int) sliderWidth, (int) getAbsoluteY() + 16, 0xFFFFFF);
            ClickGUIFontRenderWrapper.drawString("G" + green, (int) getAbsoluteX() + 3 + (int) sliderWidth, (int) getAbsoluteY() + 18 + ClickGUIFontRenderWrapper.getStringHeight("RGB0:1234567890") - 0.5F, 0xFFFFFF);
            ClickGUIFontRenderWrapper.drawString("B" + blue, (int) getAbsoluteX() + 3 + (int) sliderWidth, (int) getAbsoluteY() + 20 + (ClickGUIFontRenderWrapper.getStringHeight("RGB0:1234567890") * 2) - 0.5F, 0xFFFFFF);

            // DRAW CHECKBOX

            if (getColorSetting().isCycle()) {
                GuiRenderHelper.drawOutlineRect(getAbsoluteX() + 3 + sliderWidth, (int) getAbsoluteY() + 22 + (ClickGUIFontRenderWrapper.getFontHeight() * 3), 10, 10, 2f, getColorSetting().getColor());
            }

            GuiRenderHelper.drawRect(getAbsoluteX() + 4 + sliderWidth, (int) getAbsoluteY() + 23 + (ClickGUIFontRenderWrapper.getFontHeight() * 3), 8, 8, getColorSetting().getColor());

            ClickGUIFontRenderWrapper.drawString("RB", (int) (getAbsoluteX() + 15 + sliderWidth), (int) getAbsoluteY() + 22 + (ClickGUIFontRenderWrapper.getFontHeight() * 3), 0xFFFFFF);

            // DRAW INDICATORS

            int indicatorColor = new Color(255, 255, 255, 140).hashCode();

            // HUE INDICATOR
            int indicatorHue = (int) (sliderX + (int) (hsb[0] * sliderWidth));
            GuiRenderHelper.drawRect(indicatorHue, sliderY, 2F, sliderHeight, indicatorColor);

            // ALPHA INDICATOR
            int indicatorAlpha = (int) ((alphaHeight + alphaY) - (int) ((alpha / 255f) * alphaHeight));
            GuiRenderHelper.drawRect(alphaX, MathHelper.clamp(indicatorAlpha - 1F, alphaY, alphaY + alphaHeight), alphaWidth, 2f, indicatorColor);

            int indicatorX = (int) (pickerBoxX + (int) (hsb[1] * pickerBoxWidth));
            int indicatorY = (int) ((pickerBoxHeight + pickerBoxY) - (int) (hsb[2] * pickerBoxHeight));

            GuiRenderHelper.drawRect(indicatorX - 1F, indicatorY - 1F, 2F, 2F, indicatorColor);
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY() , getWidth(), 12F)) {
            if (mouseButton == 2) {
                if (ClickGUIModule.clipboard == -1) {
                    ClickGUIModule.clipboard = getColorSetting().getRawColor();
                } else {
                    getColorSetting().setColor(ClickGUIModule.clipboard);
                    ClickGUIModule.clipboard = -1;
                    refresh();
                }
            } else {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                setOpen(!isOpen());
            }
            return true;
        }

        return handleMouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        handleMouseClick(mouseX, mouseY, clickedMouseButton);
    }

    private boolean handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY() , getWidth(), getHeight())) {
            return false;
        }

        if(mouseButton != 0) return false;

        if (isOpen()) {
            if (mouseWithinBounds(mouseX, mouseY, pickerBoxX, pickerBoxY, pickerBoxWidth, pickerBoxHeight - 1F)) {
                getRGBfromClick(mouseX, mouseY);
            } else if (mouseWithinBounds(mouseX, mouseY, sliderX, sliderY, sliderWidth - 2F, sliderHeight)) {
                getHueFromClick(mouseX);
            } else if (mouseWithinBounds(mouseX, mouseY, getAbsoluteX() + getWidth() - 11F, getAbsoluteY() + 17F, 10F, 42F)) {
                getAlphaFromClick(mouseY);
            } else if (mouseWithinBounds(mouseX, mouseY, getAbsoluteX() + 4 + sliderWidth, getAbsoluteY() + 25 + (ClickGUIFontRenderWrapper.getFontHeight() * 3), 8, 8)) {
                if (timer.hasPassed(500)) {
                    getColorSetting().toggleCycle();
                    timer.reset();
                }
            }
        }
        return true;
    }

    private void getRGBfromClick(float mouseX, float mouseY) {
        mouseX = mouseX - getAbsoluteX();
        mouseY = mouseY - getAbsoluteY();

        float sat = (mouseX - 1F) / pickerBoxWidth;

        float bri = 1f - (mouseY - 18F) / pickerBoxHeight;

        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        int rgb = Color.HSBtoRGB(hsb[0], sat, bri);
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = (rgb) & 0xFF;
        updateColor();
    }

    private void getHueFromClick(float mouseX) {
        mouseX = mouseX - getAbsoluteX();
        float hue = (mouseX - 1F) / sliderWidth;
        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        int rgb = Color.HSBtoRGB(hue, hsb[1], hsb[2]);
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = (rgb) & 0xFF;
        updateColor();
    }

    private void getAlphaFromClick(float mouseY) {
        mouseY = mouseY - getAbsoluteY();
        this.alpha = 255 - (int) (((mouseY - 17F) / 42F) * 255);
        updateColor();
    }

    private void updateColor() {
        int rgb = ((alpha & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8)  |
                ((blue & 0xFF));
        getColorSetting().setColor(rgb);
    }

    public ColorSetting getColorSetting() {
        return (ColorSetting) colorSetting.getValue();
    }

    public Setting getSetting() {
        return colorSetting;
    }
}
