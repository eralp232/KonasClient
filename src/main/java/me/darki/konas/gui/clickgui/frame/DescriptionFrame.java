package me.darki.konas.gui.clickgui.frame;

import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;

import java.awt.*;

public class DescriptionFrame extends Frame {

    public static String desc = null;

    public DescriptionFrame() {
        super("Description", 0, 0, 0, 0);
        this.setExtended(false);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        if (desc != null && !(desc.isEmpty())) {
            float width = ClickGUIFontRenderWrapper.getStringWidth(desc) + 4;
            float height = ClickGUIFontRenderWrapper.getStringHeight(desc) + 4;
            setPosX(mouseX);
            setPosY(mouseY - height);
            setWidth(width);
            setHeight(height);
            GuiRenderHelper.drawRect(mouseX, mouseY - height, width, height, Color.BLACK.hashCode());
            ClickGUIFontRenderWrapper.drawString(desc, mouseX + 2, mouseY - height + 2, Color.WHITE.hashCode());
        }
    }

}
