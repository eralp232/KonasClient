package me.darki.konas.gui.clickgui.component;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.setting.Setting;
import me.darki.konas.setting.SubBind;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class SubBindSettingComponent extends Component {
    private final Setting setting;

    private boolean isBinding;

    public SubBindSettingComponent(Setting setting, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(setting.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.setting = setting;
        EventDispatcher.Companion.register(this);
        EventDispatcher.Companion.subscribe(this);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        GuiRenderHelper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        int color = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()) ? new Color(96, 96, 96, 100).hashCode() : ClickGUIModule.secondary.getValue().getColor();
        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        ClickGUIFontRenderWrapper.drawStringWithShadow(isBinding() ? "Press new bind..." : (getName() + ": " + GameSettings.getKeyDisplayString(((SubBind) setting.getValue()).getKeyCode())), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(isBinding() ? "Press new bind..." : (getName() + ": " + GameSettings.getKeyDisplayString(((SubBind) setting.getValue()).getKeyCode()))) / 2) - 0.5F), 0xFFFFFF);
    }

    @Override
    public void onKeyTyped(char character, int keyCode) {
        super.onKeyTyped(character, keyCode);
        if (isBinding()) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                ((SubBind) setting.getValue()).setKeyCode(Keyboard.KEY_NONE);
                setBinding(false);
                return;
            }

            ((SubBind) setting.getValue()).setKeyCode(keyCode);
            setBinding(false);
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        if (isBinding()) {
            ((SubBind) setting.getValue()).setKeyCode(mouseButton - 100);
            setBinding(false);
            return true;
        } else {
            boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

            if (withinBounds && mouseButton == 0) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                setBinding(!isBinding());
                return true;
            }
        }
        return false;
    }

    public boolean isBinding() {
        return isBinding;
    }

    public void setBinding(boolean binding) {
        isBinding = binding;
        ClickGUI.setNoEsc(binding);
    }

    public Setting getSetting() {
        return setting;
    }
}
