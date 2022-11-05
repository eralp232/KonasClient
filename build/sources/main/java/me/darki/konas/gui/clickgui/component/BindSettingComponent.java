package me.darki.konas.gui.clickgui.component;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.util.render.GuiRenderHelper;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.SoundEvents;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class BindSettingComponent extends Component {

    private final Module module;

    private boolean isBinding;

    public BindSettingComponent(Module module, float parentX, float parentY, float offsetX, float offsetY, float width, float height) {
        super(module.getName(), parentX, parentY, offsetX, offsetY, width, height);
        this.module = module;
        EventDispatcher.Companion.register(this);
        EventDispatcher.Companion.subscribe(this);
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        super.onRender(mouseX, mouseY, partialTicks);
        GuiRenderHelper.drawRect(getParentX(), getAbsoluteY(), getOffsetX(), getHeight(), ClickGUIModule.color.getValue().getColor());
        int color = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight()) ? new Color(96, 96, 96, 100).hashCode() : ClickGUIModule.secondary.getValue().getColor();
        GuiRenderHelper.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), color);
        ClickGUIFontRenderWrapper.drawStringWithShadow(isBinding() ? "Press new bind..." : ((getModule().isHold() ? "Hold: " : "Bind: ") + GameSettings.getKeyDisplayString(getModule().getKeybind())), (int) (getAbsoluteX() + 5.0F), (int) (getAbsoluteY() + getHeight() / 2.0F - (ClickGUIFontRenderWrapper.getStringHeight(isBinding() ? "Press new bind..." : ("Bind: " + GameSettings.getKeyDisplayString(getModule().getKeybind()))) / 2) - 0.5F), 0xFFFFFF);
    }

    @Override
    public void onKeyTyped(char character, int keyCode) {
        super.onKeyTyped(character, keyCode);
        if (isBinding()) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                getModule().setKeybind(Keyboard.KEY_NONE);
                setBinding(false);
                return;
            }

            getModule().setKeybind(keyCode);
            setBinding(false);
        }
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClicked(mouseX, mouseY, mouseButton);
        if (isBinding()) {
            getModule().setKeybind(mouseButton - 100);
            setBinding(false);
            return true;
        } else {
            boolean withinBounds = mouseWithinBounds(mouseX, mouseY, getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());

            if (withinBounds && mouseButton == 0) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                setBinding(!isBinding());
                return true;
            } else if (withinBounds && mouseButton == 1) {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                getModule().setHold(!getModule().isHold());
                return true;
            }
        }
        return false;
    }

    public Module getModule() {
        return module;
    }

    public boolean isBinding() {
        return isBinding;
    }

    public void setBinding(boolean binding) {
        isBinding = binding;
        ClickGUI.setNoEsc(binding);
    }
}
