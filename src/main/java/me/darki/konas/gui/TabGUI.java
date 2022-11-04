package me.darki.konas.gui;

import me.darki.konas.gui.container.containers.TabGuiContainer;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;

public class TabGUI extends Gui {

    private final float fontHeight = FontRendererWrapper.getFontHeight() + 4;
    private ArrayList<Module> mods;

    private float height = fontHeight + 1;
    private int totalWidth = 57;
    private float totalHeight = height;
    private float selectedHeight = height;

    private enum Panel {
        CATEGORY,
        MODULES
    }

    private Panel panelSelected = Panel.CATEGORY;

    private Module.Category originSelected = Module.Category.COMBAT;
    private int moduleSelected = 0;

    public void draw(float posX, float posY) {
        mods = drawOrigin(originSelected, posX, posY);
        if (panelSelected != Panel.CATEGORY) {
            drawModules(mods, posX, posY);
        } else {
            moduleSelected = 0;
        }
    }

    private void drawModules(ArrayList<Module> mods, float posX, float posY) {
        height = selectedHeight;
        // height = fh + 1;
        final int[] width = {67};
        mods.forEach(m -> {
            if(FontRendererWrapper.getStringWidth(m.getName()) > width[0]) width[0] = (int) (FontRendererWrapper.getStringWidth(m.getName()) + 6);
        });

        for (Module m : mods) {
            int textColor = Color.WHITE.hashCode();
            GuiRenderHelper.drawRect(posX + 61, posY + height, width[0], FontRendererWrapper.getFontHeight() + 3, new Color(43, 43, 43, 200).hashCode());
            int x = 65;
            if (moduleSelected == mods.indexOf(m)) {
                GuiRenderHelper.drawRect(posX + 61, posY + height, 2, FontRendererWrapper.getFontHeight() + 2, TabGuiContainer.selected.getValue().getColor());
                x = 67;
            }

            if (m.isEnabled()) {
                textColor = TabGuiContainer.toggle.getValue().getColor();
            }

            FontRendererWrapper.drawStringWithShadow(m.getName(), (int) posX + x, (int) posY + (int) (height + 2), textColor);
            height += FontRendererWrapper.getFontHeight() + 3;
        }

        setTotalWidth(61 + width[0]);
        setTotalHeight(height);
    }

    private ArrayList<Module> drawOrigin(Module.Category selected, float posX, float posY) {
        height = 1;
        GuiRenderHelper.drawRect(posX + 2, posY + height - 1, 57, 1, new Color(43, 43, 43, 200).hashCode());
        for (Module.Category category : Module.Category.values()) {
            int color = new Color(43, 43, 43, 200).hashCode();
            int x = 6;
            GuiRenderHelper.drawRect(posX + 2, posY + height, 57, FontRendererWrapper.getFontHeight() + 3, color);
            if (category.equals(selected)) {
                GuiRenderHelper.drawRect(posX + 2, posY + height, 2, FontRendererWrapper.getFontHeight() + 2, TabGuiContainer.selected.getValue().getColor());
                x = 8;
                selectedHeight = height;
            }
            FontRendererWrapper.drawStringWithShadow(category.toString().substring(0, 1).toUpperCase() + category.toString().substring(1).toLowerCase(), (int) (posX + x), (int) ((int) posY + (height + 2)), category.equals(originSelected) ? TabGuiContainer.selected.getValue().getColor() : Color.white.hashCode());
            height += FontRendererWrapper.getFontHeight() + 3;
        }
        setTotalHeight(fontHeight + 1 + ((FontRendererWrapper.getFontHeight() + 3) * Module.Category.values().length));
        return ModuleManager.getModulesByCategory(selected);
    }

    public void cycleDown() {
        if (panelSelected == Panel.CATEGORY) {
            int timeToStop = 0;
            for (Module.Category c : Module.Category.values()) {
                if (c.equals(originSelected)) {
                    timeToStop = 1;
                } else if (timeToStop == 1) {
                    originSelected = c;
                    timeToStop = 2;
                    break;
                }
            }
            if (timeToStop == 1) {
                originSelected = Module.Category.values()[0];
            }
        } else if (panelSelected == Panel.MODULES && mods.size() != 0) {
            int timeToStop = 0;
            for (Module m : mods) {
                if (mods.indexOf(m) == moduleSelected) {
                    timeToStop = 1;
                } else if (timeToStop == 1) {
                    moduleSelected = mods.indexOf(m);
                    timeToStop = 2;
                    break;
                }
            }
            if (timeToStop == 1) {
                moduleSelected = 0;
            }
        }
    }

    public void cycleUp() {
        if (panelSelected == Panel.CATEGORY) {
            Module.Category prev = Module.Category.values()[Module.Category.values().length - 1];
            for (Module.Category c : Module.Category.values()) {
                if (c.equals(originSelected)) {
                    originSelected = prev;
                    break;
                } else {
                    prev = c;
                }
            }
        } else if (panelSelected == Panel.MODULES && mods.size() != 0) {
            int prev = mods.size() - 1;
            for (Module m : mods) {
                if (mods.indexOf(m) == moduleSelected) {
                    moduleSelected = prev;
                    break;
                } else {
                    prev = mods.indexOf(m);
                }
            }
        }
    }

    public void cycleRight() {
        int timeToStop = 0;
        if (mods.size() != 0) {
            for (Panel p : Panel.values()) {
                if (p.equals(panelSelected)) {
                    timeToStop = 1;
                } else if (timeToStop == 1) {
                    panelSelected = p;
                    timeToStop = 2;
                    break;
                }
            }
            if (timeToStop == 1) {
                mods.get(moduleSelected).toggle();
            }
        }
    }

    public void cycleLeft() {
        if (mods.size() != 0) {
            Panel prev = Panel.CATEGORY;
            for (Panel p : Panel.values()) {
                if (p.equals(panelSelected)) {
                    panelSelected = prev;
                    break;
                } else {
                    prev = p;
                }
            }
        }
    }

    public int getTotalWidth() {
        if(panelSelected == Panel.MODULES) {
            return totalWidth;
        } else {
            return 61;
        }
    }

    public void setTotalWidth(int totalWidth) {
        this.totalWidth = totalWidth;
    }

    public float getTotalHeight() {
        return totalHeight;
    }

    public void setTotalHeight(float totalHeight) {
        this.totalHeight = totalHeight;
    }
}
