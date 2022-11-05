package me.darki.konas.gui.protocol;

import com.viaversion.viafabric.ViaFabric;
import com.viaversion.viafabric.protocol.ProtocolSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class GuiProtocolSlider extends GuiButton
{
    private float sliderValue;
    public boolean dragging;
    private final float minValue;
    private final float maxValue;

    public GuiProtocolSlider(int buttonId, int x, int y, int width, int height)
    {
        super(buttonId, x, y, width, height, "Protocol");
        this.sliderValue = 1.0F;
        this.minValue = 0;
        this.maxValue = ProtocolSorter.getProtocolVersions().size() - 1;
        for (int i = 0; i < ProtocolSorter.getProtocolVersions().size(); i++) {
            if (ProtocolSorter.getProtocolVersions().get(i).getVersion() == ViaFabric.clientSideVersion) {
                sliderValue = (float) i / (float) ProtocolSorter.getProtocolVersions().size();
                this.displayString = "Protocol: " + ProtocolSorter.getProtocolVersions().get(i).getName();
            }
        }
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            if (this.dragging)
            {
                this.sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
                int actualVersion = (int) (sliderValue * maxValue);
                ViaFabric.clientSideVersion = ProtocolSorter.getProtocolVersions().get(actualVersion).getVersion();
                this.displayString = "Protocol: " + ProtocolSorter.getProtocolVersions().get(actualVersion).getName();
            }

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.x + (int)(this.sliderValue * (float)(this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.x + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            this.sliderValue = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            int actualVersion = (int) (sliderValue * maxValue);
            ViaFabric.clientSideVersion = ProtocolSorter.getProtocolVersions().get(actualVersion).getVersion();
            this.displayString = "Protocol: " + ProtocolSorter.getProtocolVersions().get(actualVersion).getName();
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY)
    {
        this.dragging = false;
    }
}