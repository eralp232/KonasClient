package me.darki.konas.gui.kgui.element.setting;

import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.KonasRectangle;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.RenderUtil;

import java.awt.*;

public class ColorSetting extends Element {
    private Setting<me.darki.konas.setting.ColorSetting> setting;
    private boolean extended = false;

    public int alpha;
    public int red;
    public int green;
    public int blue;

    float sliderWidth = 100F;
    float steps = sliderWidth / 10F;
    float sliderX = getPosX() + 246;
    float sliderY = getPosY() + 82;
    float sliderHeight = 12F;

    private final boolean right;

    public ColorSetting(Setting<me.darki.konas.setting.ColorSetting> setting, float parentX, float parentY, float offsetX, float offsetY, float width, boolean right) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, setting.hasDescription() ? 28 : 12);
        this.setting = setting;
        this.right = right;
        refresh();
    }

    public void refresh() {
        this.alpha = (getColorSetting().getRawColor() >> 24) & 0xff;
        this.red = (getColorSetting().getRawColor() >> 16) & 0xFF;
        this.green = (getColorSetting().getRawColor() >> 8) & 0xFF;
        this.blue = (getColorSetting().getRawColor()) & 0xFF;
    }

    private void updateColor() {
        int rgb = ((alpha & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8)  |
                ((blue & 0xFF));
        getColorSetting().setColor(rgb);
    }

    public me.darki.konas.setting.ColorSetting getColorSetting() {
        return setting.getValue();
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        Renderer.fontRenderer.drawString(setting.getName(), getPosX(), getPosY(), fontFill);
        if (setting.hasDescription()) {
            Renderer.subFontRendrer.drawString(setting.getDescription(), getPosX(), (int) (getPosY() + 18F), subFontFill);
        }
        GuiRenderHelper.drawRect(getPosX() + 240, getPosY(), 24, 12, setting.getValue().getColor());
        GuiRenderHelper.drawOutlineRect(getPosX() + 240, getPosY(), 24, 12, 1F, setting.getValue().withAlpha(255).getColor());

        if (extended) {
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F, 40F, 17.5F)) {
                if (dWheel > 0) {
                    this.red++;
                } else if (dWheel < 0) {
                    this.red--;
                }
                dWheel = 0;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F, 40F, 17.5F)) {
                if (dWheel > 0) {
                    this.green++;
                } else if (dWheel < 0) {
                    this.green--;
                }
                dWheel = 0;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*2F, 40F, 17.5F)) {
                if (dWheel > 0) {
                    this.blue++;
                } else if (dWheel < 0) {
                    this.blue--;
                }
                dWheel = 0;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*3F, 40F, 17.5F)) {
                if (dWheel > 0) {
                    this.alpha++;
                } else if (dWheel < 0) {
                    this.alpha--;
                }
                dWheel = 0;
            }
            normalize();
            updateColor();
        }
    }

    public void normalize() {
        if (alpha > 255) {
            alpha = 255;
        }
        if (red > 255) {
            red = 255;
        }
        if (green > 255) {
            green = 255;
        }
        if (blue > 255) {
            blue = 255;
        }
        if (alpha < 0) {
            alpha = 0;
        }
        if (red < 0) {
            red = 0;
        }
        if (green < 0) {
            green = 0;
        }
        if (blue < 0) {
            blue = 0;
        }
    }

    @Override
    public void onRenderPost(int mouseX, int mouseY, float partialTicks) {
        super.onRenderPost(mouseX, mouseY, partialTicks);
        if (extended) {
            float[] hsb = Color.RGBtoHSB(red, green, blue, null);
            KonasRectangle konasRectangle = new KonasRectangle(getPosX() + 240 - (right ? 108 : 0), getPosY(), 156, 100);
            Renderer.fillShape(konasRectangle, foregroundFill);
            RenderUtil.draw2DGradientRect(getPosX() + 246 - (right ? 108 : 0), getPosY() + 6, getPosX() + 346 - (right ? 108 : 0), getPosY() + 76, Color.getHSBColor(hsb[0], 0f, 0f).getRGB(), Color.getHSBColor(hsb[0], 0f, 1f).getRGB(), Color.getHSBColor(hsb[0], 1f, 0f).getRGB(), Color.getHSBColor(hsb[0], 1f, 1f).getRGB());

            sliderX = getPosX() + 246 - (right ? 108 : 0);
            sliderY = getPosY() + 82;

            for (float i = 0.0F; i + steps <= sliderWidth; i += steps) {
                RenderUtil.draw1DGradientRect(sliderX + i, sliderY, sliderX + steps + i, sliderY + sliderHeight, Color.getHSBColor(i / sliderWidth, 1f, 1f).getRGB(), Color.getHSBColor((i + steps) / sliderWidth, 1f, 1f).getRGB());
            }

            int indicatorHue = (int) (sliderX + (int) (hsb[0] * sliderWidth));
            GuiRenderHelper.drawRect(indicatorHue, sliderY, 2F, sliderHeight, 0xCCFFFFFF);

            int indicatorX = (int) (getPosX() + 246 + (hsb[1] * 100F));
            int indicatorY = (int) ((70F + getPosY() + 6) - (int) (hsb[2] * 70F));

            GuiRenderHelper.drawRect(indicatorX - 1F - (right ? 108 : 0), indicatorY - 1F, 2F, 2F, 0xCCFFFFFF);

            GuiRenderHelper.drawRect(getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F, 40F * (red / 255F), 17.5F, 0x44FF0000);
            GuiRenderHelper.drawRect(getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F, 40F * (green / 255F), 17.5F, 0x4400FF00);
            GuiRenderHelper.drawRect(getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*2F, 40F * (blue / 255F), 17.5F, 0x440000FF);
            GuiRenderHelper.drawRect(getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*3F, 40F * (alpha / 255F), 17.5F, 0x44FFFFFF);

            GuiRenderHelper.drawRect(getPosX() + 350 - (right ? 108 : 0), sliderY, 40F, sliderHeight, setting.getValue().isCycle() ? setting.getValue().withAlpha(50).getColor() : 0x00000000);

            Renderer.fontRenderer.drawString("R" + red, getPosX() + 352 - (right ? 108 : 0), (int) (getPosY() + 6F + 8.75F - Renderer.fontRenderer.getStringHeight("R") / 2F), fontFill);
            Renderer.fontRenderer.drawString("G" + green, getPosX() + 352 - (right ? 108 : 0), (int) (getPosY() + 6F + 26.25  - Renderer.fontRenderer.getStringHeight("G") / 2F), fontFill);
            Renderer.fontRenderer.drawString("B" + blue, getPosX() + 352 - (right ? 108 : 0), (int) (getPosY() + 6F + 43.75F - Renderer.fontRenderer.getStringHeight("B") / 2F), fontFill);
            Renderer.fontRenderer.drawString("A" + alpha, getPosX() + 352 - (right ? 108 : 0), (int) (getPosY() + 6F + 61.25F - Renderer.fontRenderer.getStringHeight("A") / 2F), fontFill);

            Renderer.fontRenderer.drawString("Cycle", getPosX() + 352 - (right ? 108 : 0), (int) (sliderY + sliderHeight/2F - Renderer.fontRenderer.getStringHeight("Cycle")/2F), fontFill);
        }
    }

    private void getRGBfromClick(float mouseX, float mouseY) {
        mouseX = mouseX - (getPosX() + 246 - (right ? 108 : 0));
        mouseY = mouseY - (getPosY() + 6);

        float sat = (mouseX - 1F) / 100F;

        float bri = 1f - (mouseY) / 70F;

        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        int rgb = Color.HSBtoRGB(hsb[0], sat, bri);
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = (rgb) & 0xFF;
        updateColor();
    }

    private void getHueFromClick(float mouseX) {
        mouseX = mouseX - sliderX;
        float hue = (mouseX) / sliderWidth;
        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        int rgb = Color.HSBtoRGB(hue, hsb[1], hsb[2]);
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = (rgb) & 0xFF;
        updateColor();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (extended) {
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 248 - (right ? 108 : 0), getPosY() + 6, 98F, 70F)) {
                getRGBfromClick(mouseX, mouseY);
                return true;
            }
            if (mouseWithinBounds(mouseX, mouseY, sliderX, sliderY, sliderWidth - 2F, sliderHeight)) {
                getHueFromClick(mouseX);
                return true;
            }
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F, 40F, 17.5F)) {
                this.red = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F, 40F, 17.5F)) {
                this.green = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*2F, 40F, 17.5F)) {
                this.blue = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*3F, 40F, 17.5F)) {
                this.alpha = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            }
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), sliderY, 40F, sliderHeight)) {
                setting.getValue().setCycle(!setting.getValue().isCycle());
            }
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 240 - (right ? 108 : 0), getPosY(), 156, 100)) {
                return true;
            } else {
                extended = false;
            }
        }
        if (mouseWithinBounds(mouseX, mouseY, getPosX() + 240, getPosY(), 24, 12)) {
            if (mouseButton == 0) {
                extended = !extended;
            } else if (mouseButton == 1) {
                setting.getValue().setColor(KonasGui.clipboard);
            } else if (mouseButton == 2) {
                KonasGui.clipboard = setting.getValue().getRawColor();
            }
            return true;
        } else {
            extended = false;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (extended) {
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 248 - (right ? 108 : 0), getPosY() + 6, 98F, 70F)) {
                getRGBfromClick(mouseX, mouseY);
                return true;
            }
            if (mouseWithinBounds(mouseX, mouseY, sliderX, sliderY, sliderWidth - 2F, sliderHeight)) {
                getHueFromClick(mouseX);
                return true;
            }
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F, 40F, 17.5F)) {
                this.red = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F, 40F, 17.5F)) {
                this.green = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*2F, 40F, 17.5F)) {
                this.blue = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            } else if (mouseWithinBounds(mouseX, mouseY, getPosX() + 350 - (right ? 108 : 0), getPosY() + 6F + 17.5F*3F, 40F, 17.5F)) {
                this.alpha = (int) (((mouseX - (getPosX() + 350 - (right ? 108 : 0))) / 40F) * 255F);
                updateColor();
                return true;
            }
            if (mouseWithinBounds(mouseX, mouseY, getPosX() + 240 - (right ? 108 : 0), getPosY(), 156, 100)) {
                return true;
            } else {
                extended = false;
            }
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
}
