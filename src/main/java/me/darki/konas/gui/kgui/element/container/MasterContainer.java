package me.darki.konas.gui.kgui.element.container;

import me.darki.konas.KonasMod;
import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.element.button.CategoryButton;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.KonasRectangle;
import me.darki.konas.gui.kgui.shape.RoundedRectangle;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;

public class MasterContainer extends Element {
    private Module.Category currentCategory = Module.Category.COMBAT;
    private ModuleContainer moduleContainer;
    private ResultsContainer resultsContainer = null;

    private RoundedRectangle master;

    private RoundedRectangle header;

    private KonasRectangle accent;
    private RoundedRectangle footer;

    private RoundedRectangle searchBar;

    private boolean isTyping = false;
    private String query;

    private Timer flashingTimer = new Timer();

    private int catWidth = KonasGui.singleColumn.getValue() ? 65 : 75;
    private int catStart = KonasGui.singleColumn.getValue() ? 16 : 56;

    private final CategoryButton[] categoryButtons = new CategoryButton[]{
            new CategoryButton(Module.Category.COMBAT, getPosX(), getPosY(), catStart, 8, catWidth, 38, RoundedRectangle.TOP_LEFT),
            new CategoryButton(Module.Category.MOVEMENT, getPosX(), getPosY(), catStart + catWidth, 8, catWidth, 38, 0),
            new CategoryButton(Module.Category.PLAYER, getPosX(), getPosY(), catStart + catWidth * 2, 8, catWidth, 38, 0),
            new CategoryButton(Module.Category.RENDER, getPosX(), getPosY(), catStart + catWidth * 3, 8, catWidth, 38, 0),
            new CategoryButton(Module.Category.MISC, getPosX(), getPosY(), catStart + catWidth * 4, 8, catWidth, 38, 0),
            new CategoryButton(Module.Category.EXPLOIT, getPosX(), getPosY(), catStart + catWidth * 5, 8, catWidth, 38, 0),
            new CategoryButton(Module.Category.CLIENT, getPosX(), getPosY(), catStart + catWidth * 6, 8, catWidth, 38, RoundedRectangle.TOP_RIGHT)};

    private final HashMap<Module.Category, CategoryContainer> categoryContainers = new HashMap<>();

    public MasterContainer(float width, float height) {
        super("Konas", 0F, 0F, 0F, 0F, width, height);
        for (Module.Category category : Module.Category.values()) {
            categoryContainers.put(category, new CategoryContainer(category, getPosX(), getPosY(), 0, 48, 160, getHeight() - 72));
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);

        Renderer.fillShape(master, backgroundFill);
        Renderer.fillShape(header, mainFill);
        Renderer.fillShape(footer, mainFill);
        Renderer.fillShape(accent, accentFill);

        if (!KonasGui.singleColumn.getValue()) {
            Renderer.fillShape(searchBar, textBoxFill);
        }

        Renderer.drawShape(master, outlineFill, 1F);

        if (isTyping || resultsContainer != null) {
            Renderer.fontRenderer.drawString(query, getPosX() + 599, (int) (getPosY() + 24 - Renderer.fontRenderer.getStringHeight(query) / 2F), darkFontFill);
            if (flashingTimer.hasPassed(500)) {
                KonasRectangle typingIndicator = new KonasRectangle(getPosX() + 599 + (query.equals("") ? 0 : Renderer.fontRenderer.getStringWidth(query)) + 1F, getPosY() + 24 - (query.equals("") ? Renderer.fontRenderer.getStringHeight("Search features") : Renderer.fontRenderer.getStringHeight(query)) / 2F, 1F, (query.equals("") ? Renderer.fontRenderer.getStringHeight("Search features") : Renderer.fontRenderer.getStringHeight(query)));
                Renderer.fillShape(typingIndicator, darkFontFill);
                if(flashingTimer.hasPassed(1000)) {
                    flashingTimer.reset();
                }
            }
        } else if (!KonasGui.singleColumn.getValue()) {
            Renderer.fontRenderer.drawString("Search features", getPosX() + 599, (int) (getPosY() + 24 - Renderer.fontRenderer.getStringHeight("Search features") / 2F), darkFontFill);
        }

        for (CategoryButton categoryButton : categoryButtons) {
            categoryButton.onRender(mouseX, mouseY, partialTicks);
        }

        if (resultsContainer != null) {
            resultsContainer.onRender(mouseX, mouseY, partialTicks);
        } else {
            categoryContainers.get(currentCategory).onRender(mouseX, mouseY, partialTicks);
        }

        moduleContainer.onRender(mouseX, mouseY, partialTicks);

        Renderer.fontRenderer.drawString(KonasMod.VERSION + " for Minecraft 1.12.2", getPosX() + 8, (int) (getPosY() + getHeight() - 12 - Renderer.fontRenderer.getStringHeight("konasclient.com") / 2F), fontFill);
        Renderer.fontRenderer.drawString("konasclient.com", getPosX() + getWidth() - 8 - Renderer.fontRenderer.getStringWidth("konasclient.com"), (int) (getPosY() + getHeight() - 12 - Renderer.fontRenderer.getStringHeight("konasclient.com") / 2F), fontFill);
    }

    @Override
    public void onRenderPost(int mouseX, int mouseY, float partialTicks) {
        super.onRenderPost(mouseX, mouseY, partialTicks);
        moduleContainer.onRenderPost(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void generateShapes() {
        master = new RoundedRectangle(getPosX(), getPosY(), getWidth(), getHeight(), 3F, 25, RoundedRectangle.ALL);

        header = new RoundedRectangle(getPosX(), getPosY(), getWidth(), 46, 3F, 25, RoundedRectangle.TOP_LEFT | RoundedRectangle.TOP_RIGHT);
        accent = new KonasRectangle(getPosX(), getPosY() + 46, getWidth(), 2);

        footer = new RoundedRectangle(getPosX(), getPosY() + getHeight() - 24, getWidth(), 24, 3F, 25, RoundedRectangle.BOTTOM_LEFT | RoundedRectangle.BOTTOM_RIGHT);

        searchBar = new RoundedRectangle(getPosX() + 594, getPosY() + 14, 192, 20, 3F);
    }

    public static void updateColors() {
        generateFills();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY, getPosX() + 594, getPosY() + 14, 192, 20) && !KonasGui.singleColumn.getValue()) {
            isTyping = true;
            query = "";
            return true;
        } else {
            isTyping = false;
        }

        for (CategoryButton categoryButton : categoryButtons) {
            if ((categoryButton.mouseClicked(mouseX, mouseY, mouseButton))) return true;
        }

        if (resultsContainer != null) {
            if (resultsContainer.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        } else if (categoryContainers.get(currentCategory).mouseClicked(mouseX, mouseY, mouseButton)) return true;

        if (moduleContainer.mouseClicked(mouseX, mouseY, mouseButton)) return true;

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (moduleContainer.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) return true;
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        moduleContainer.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        if (isTyping) {
            if (keyCode == Keyboard.KEY_RETURN) {
                isTyping = false;
                resultsContainer = new ResultsContainer(query, getPosX(), getPosY(), 0, 48, 160, getHeight() - 72);
                resultsContainer.onResize(getPosX(), getPosY());
            } else if (keyCode == Keyboard.KEY_BACK) {
                query = StringUtils.chop(query);
            } else if(typedChar >= 'A' && typedChar <= 'z'){
                query = query + typedChar;
            }
        } else {
            moduleContainer.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onResize(float parentX, float parentY) {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        setOffsetX((int) ((resolution.getScaledWidth() / 2F) - ((getWidth()) / 2F)));
        setOffsetY((int) ((resolution.getScaledHeight() / 2F) - ((getHeight()) / 2F)));

        for (CategoryButton categoryButton : categoryButtons) {
            categoryButton.onResize(getPosX(), getPosY());
        }

        if (resultsContainer != null) {
            resultsContainer.onResize(getPosX(), getPosY());
        }

        categoryContainers.forEach((category, categoryContainer) -> categoryContainer.onResize(getPosX(), getPosY()));

        if (moduleContainer != null) {
            moduleContainer.onResize(getPosX(), getPosY());
        }

        super.onResize(parentX, parentY);
    }

    public Module.Category getCurrentCategory() {
        return currentCategory;
    }

    public void setCurrentCategory(Module.Category currentCategory) {
        resultsContainer = null;
        this.currentCategory = currentCategory;
        categoryContainers.get(currentCategory).setup();
    }

    public HashMap<Module.Category, CategoryContainer> getCategoryContainers() {
        return categoryContainers;
    }

    public ModuleContainer getModuleContainer() {
        return moduleContainer;
    }

    public void setModuleContainer(Module module) {
        this.moduleContainer = new ModuleContainer(module, getPosX(), getPosY(), 160, 48, KonasGui.singleColumn.getValue() ? 328 : 640, getHeight() - 72);
    }
}
