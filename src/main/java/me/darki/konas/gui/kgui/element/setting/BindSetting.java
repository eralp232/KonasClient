package me.darki.konas.gui.kgui.element.setting;

import me.darki.konas.gui.kgui.KonasGuiScreen;
import me.darki.konas.gui.kgui.element.Element;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.gui.kgui.shape.RoundedRectangle;
import me.darki.konas.module.modules.client.KonasGui;
import me.darki.konas.setting.IBind;
import me.darki.konas.setting.Setting;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public class BindSetting extends Element {
    private Setting<IBind> setting;
    private boolean isBinding = false;

    public BindSetting(Setting<IBind> setting, float parentX, float parentY, float offsetX, float offsetY, float width) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, setting.hasDescription() ? 54 : 38);
        this.setting = setting;
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        RoundedRectangle bindBox = new RoundedRectangle(getPosX(), getPosY() + (setting.hasDescription() ? 34 : 18), 132F, 20F, 3F);
        Renderer.fillShape(bindBox, mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + (setting.hasDescription() ? 34 : 18), 132F, 20F) ? primaryFill.brighten(KonasGui.highlight.getValue()) : primaryFill);
        Renderer.fontRenderer.drawString(setting.getName(), getPosX(), getPosY(), fontFill);
        if (setting.hasDescription()) {
            Renderer.subFontRendrer.drawString(setting.getDescription(), getPosX(), (int) (getPosY() + 18F), subFontFill);
        }
        String state = isBinding ? "Press a key..." : GameSettings.getKeyDisplayString(setting.getValue().getKeyCode());
        Renderer.fontRenderer.drawString(state, (int) (getPosX() + 66F - Renderer.fontRenderer.getStringWidth(state) / 2F), (int) (getPosY() + (setting.hasDescription() ? 43.5F : 27.5F) - Renderer.fontRenderer.getStringHeight(state) / 2F), darkFontFill);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseWithinBounds(mouseX, mouseY, getPosX(), getPosY() + (setting.hasDescription() ? 34 : 18), 132F, 20F)) {
            isBinding = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        if (isBinding) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                setting.getValue().setKeyCode(Keyboard.KEY_NONE);
                isBinding = false;
                KonasGuiScreen.bindedEsc = true;
                return;
            }

            setting.getValue().setKeyCode(keyCode);
            isBinding = false;
        }
    }
}
