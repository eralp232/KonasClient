package me.darki.konas.gui.clickgui.frame;

import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class ButtonFrame extends Frame {

    public ButtonFrame() {
        super("HUD Editor", 20F, 100F, 100F, 16.0F);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        setPosX(sr.getScaledWidth() - getWidth() - 5F);
        setPosY(sr.getScaledHeight() - getHeight() - 5F);
        int color = ClickGUIModule.color.getValue().getColor();
        if (ClickGUIModule.hover.getValue() && mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), getWidth(), getHeight()))
            color = ClickGUIModule.color.getValue().getColorObject().brighter().hashCode();
        GuiRenderHelper.drawRect(getPosX() - 2F, getPosY() - 2F, getWidth() + 4, getHeight() + 4, ClickGUIModule.secondary.getValue().getColor());
        GuiRenderHelper.drawRect(getPosX(), getPosY(), getWidth(), getHeight(), color);
        String s = ClickGUI.isHudEditor() ? "Modules" : "HUD Editor";
        ClickGUIFontRenderWrapper.drawString(s, (int) ((getPosX() + (getWidth() / 2F)) - (ClickGUIFontRenderWrapper.getStringWidth(s) / 2F)), (int) (getPosY() + (getHeight() / 2F) - (ClickGUIFontRenderWrapper.getFontHeight() / 2F)), 0xFFFFFF);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY(), getWidth(), getHeight()) && mouseButton == 0) {
            ClickGUI.setHudEditor(!ClickGUI.isHudEditor());
            return true;
        }
        return false;
    }


}
