package me.darki.konas.gui.container;

import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.render.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public abstract class Container {

    public Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x00000000, false));
    public Setting<ColorSetting> outlineColor = new Setting<>("Outline", new ColorSetting(0x00888888, false));

    private final String name;

    private float offsetX;
    private float offsetY;

    private float width;
    private float height;

    private boolean visible = false;
    
    private boolean dragging = false;

    private int anchor = 1;

    public static final Minecraft mc = Minecraft.getMinecraft();

    ScaledResolution sr = new ScaledResolution(mc);

    public Container(String name, float posX, float posY, float width, float height) {
        setOffsetX(posX);
        setOffsetY(posY);
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public void initialize() {
    }

    public void onRender() {
        GuiRenderHelper.drawBorderedRect(getPosX(), getPosY(), getWidth(), getHeight(), outlineColor.getValue().getColor(), color.getValue().getColor());
    }

    public float getAnchorX() {
        switch (anchor) {
            case 1:
            case 3:
                return 0F;
            case 2:
            case 4:
                return sr.getScaledWidth();
        }
        return 0F;
    }

    public float getAnchorY() {
        switch (anchor) {
            case 1:
            case 2:
                return 0F;
            case 3:
            case 4:
                return sr.getScaledHeight();
        }
        return 0F;
    }

    public void onKeyTyped(int keyCode) {}

    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {}

    public String getName() {
        return name;
    }

    public float getPosX() {
        switch (anchor) {
            case 1:
            case 3:
                return getOffsetX();
            case 2:
            case 4:
                return getAnchorX() + getOffsetX() - width;
        }
        return 0F;
    }

    public void setPosX(float posX) {
        if (getPosY() + (getHeight() / 2F) > sr.getScaledHeight() / 2F) {
            if (posX + (getWidth() / 2F) > sr.getScaledWidth() / 2F) {
                setAnchor(4);
                setOffsetX(posX + getWidth() - getAnchorX());
            } else {
                setAnchor(3);
                setOffsetX(posX);
            }
        } else {
            if (posX + (getWidth() / 2F) > sr.getScaledWidth() / 2F) {
                setAnchor(2);
                setOffsetX(posX + getWidth() - getAnchorX());
            } else {
                setAnchor(1);
                setOffsetX(posX);
            }
        }
    }

    public float getPosY() {
        switch (anchor) {
            case 1:
            case 2:
                return getOffsetY();
            case 3:
            case 4:
                return getAnchorY() + getOffsetY() - height;
        }
        return 0F;
    }

    public void setPosY(float posY) {
        if (getPosX() + (getWidth() / 2F) > sr.getScaledWidth() / 2F) {
            if (posY + (getHeight() / 2F) > sr.getScaledHeight() / 2F) {
                setAnchor(4);
                setOffsetY(posY + getHeight() - getAnchorY());
            } else {
                setAnchor(2);
                setOffsetY(posY);
            }
        } else {
            if (posY + (getHeight() / 2F) > sr.getScaledHeight() / 2F) {
                setAnchor(3);
                setOffsetY(posY + getHeight() - getAnchorY());
            } else {
                setAnchor(1);
                setOffsetY(posY);
            }
        }
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void toggleVisibility() {
        this.visible = !this.visible;
    }

    public boolean isDragging() {
        if (mc.currentScreen instanceof ClickGUI) {
            return dragging;
        }
        return false;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public int getAnchor() {
        return anchor;
    }

    public void setAnchor(int anchor) {
        this.anchor = anchor;
    }

    public static boolean mouseWithinBounds(int mouseX, int mouseY, double x, double y, double width, double height) {
        return (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
    }
}
