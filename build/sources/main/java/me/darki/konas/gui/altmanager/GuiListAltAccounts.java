package me.darki.konas.gui.altmanager;

import me.darki.konas.util.KonasGlobals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiListAltAccounts extends GuiListExtended
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final GuiAltManager guiAltManager;
    private ArrayList<GuiListAltAccountEntry> entries = new ArrayList<>();
    /** Index to the currently selected world */
    private int selectedIdx = -1;

    public GuiListAltAccounts(GuiAltManager guiAltManager, Minecraft clientIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(clientIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.guiAltManager = guiAltManager;
    }

    public void deleteAccount(GuiListAltAccountEntry entry) {
        entries.remove(entry);
    }

    public void addAccount(GuiListAltAccountEntry entry) {
        entries.add(entry);
        KonasGlobals.INSTANCE.altManager.loadedEntries.add(entry);
    }

    @NotNull
    @Override
    public GuiListAltAccountEntry getListEntry(int index)
    {
        return this.entries.get(index);
    }

    @Override
    protected int getSize()
    {
        return this.entries.size();
    }

    @Override
    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 20;
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            this.drawBackground();
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            // Forge: background rendering moved into separate method.
            this.drawContainerBackground(tessellator);
            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int)this.amountScrolled;

            if (this.hasListHeader)
            {
                this.drawListHeader(k, l, tessellator);
            }
            ArrayList<GuiListAltAccountEntry> cache = entries;
            boolean swapped = false;
            for (int joe = 0; joe < entries.size(); joe++) {
                if(entries.get(joe).altSummary.isLoggedIn()) {
                    Collections.swap(entries, 0, joe);
                    swapped = true;
                    break;
                }
            }
            if(swapped) {
                drawContainerBackground(this.left, this.right, this.top, this.top + 39);
            }
            this.drawSelectionBox(k, l, mouseXIn, mouseYIn, partialTicks);
            entries = cache;
            GlStateManager.disableDepth();
            this.overlayBackground(0, this.top, 255, 255);
            this.overlayBackground(this.bottom, this.height, 255, 255);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(this.left, (this.top + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.pos(this.right, (this.top + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.pos(this.right, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(this.left, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos(this.left, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(this.right, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(this.right, (this.bottom - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.pos(this.left, (this.bottom - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            int j1 = this.getMaxScroll();

            if (j1 > 0)
            {
                int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
                int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                if (l1 < this.top)
                {
                    l1 = this.top;
                }

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(i, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(j, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(j, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(i, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(i, (l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(j, (l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(j, l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(i, (l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((j - 1), (l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((j - 1), l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.renderDecorations(mouseXIn, mouseYIn);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }
    
    public int getListWidth()
    {
        return super.getListWidth() + 50;
    }

    public void selectAccount(int idx)
    {
        this.selectedIdx = idx;
        this.guiAltManager.selectAccount(this.getSelectedAccount());
    }

    @Override
    protected boolean isSelected(int slotIndex)
    {
        return slotIndex == this.selectedIdx;
    }

    @Nullable
    public GuiListAltAccountEntry getSelectedAccount()
    {
        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
    }

    public List<GuiListAltAccountEntry> getEntries() {
        return entries;
    }

    public GuiAltManager getGuiAltManager()
    {
        return this.guiAltManager;
    }

    private void drawContainerBackground(float left, double right, double top, double bottom) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(left, bottom, 0.0D).tex((left / f), ((bottom / f))).color(20, 20, 20, 255).endVertex();
        buffer.pos(right, bottom, 0.0D).tex((right / f), ((bottom / f))).color(20, 20, 20, 255).endVertex();
        buffer.pos(right, top, 0.0D).tex((right / f), ((top / f))).color(20, 20, 20, 255).endVertex();
        buffer.pos(left, top, 0.0D).tex((left / f), ((top / f))).color(20, 20, 20, 255).endVertex();
        Tessellator.getInstance().draw();
    }

}
