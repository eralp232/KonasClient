package me.darki.konas.module.modules.client;

import me.darki.konas.command.commands.FontCommand;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.IRunnable;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import me.darki.konas.util.render.font.CustomFontRenderer;
import me.darki.konas.util.render.font.DefaultFontRenderer;
import org.lwjgl.input.Keyboard;

public class ClickGUIModule extends Module {
    public static int clipboard = -1;

    public static Setting<Boolean> binds = new Setting<>("Binds", false);
    public static Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFFBA15BA));
    public static Setting<ColorSetting> font = new Setting<>("Font", new ColorSetting(0xFFFFFFFF));
    public static Setting<ColorSetting> secondary = new Setting<>("Secondary", new ColorSetting(0xFF000000));
    public static Setting<ColorSetting> header = new Setting<>("Header", new ColorSetting(0xDD000000));
    public static Setting<ColorSetting> background = new Setting<>("Background", new ColorSetting(0xDD000000));
    public static Setting<Float> height = new Setting<>("MaxHeight", 500F, 500F, 200F, 3.5F);
    public static Setting<Integer> scrollSpeed = new Setting<>("ScrollSpeed", 14, 28, 7, 1);
    public static Setting<Boolean> hover = new Setting<>("Hover", true);
    public static Setting<Boolean> animate = new Setting<>("Animate", true);
    public static Setting<Boolean> outline = new Setting<>("Outline", false).withVisibility(() -> !animate.getValue());
    public static Setting<Integer> thickness = new Setting<>("Thickness", 1, 5, 1, 1).withVisibility(() -> !animate.getValue() && outline.getValue());
    public static Setting<Integer> animationSpeed = new Setting<>("AnimationSpeed", 10, 20, 1, 1).withVisibility(animate::getValue);
    private Setting<Boolean> customFont = new ListenableSettingDecorator<>("CustomFont", true, new IRunnable<Boolean>() {

        @Override
        public void run(Boolean value) {
            if(value) {
                if(ClickGUIFontRenderWrapper.getFontRenderer() != customFontRenderer) {
                    ClickGUIFontRenderWrapper.setFontRenderer(customFontRenderer);
                }
            } else {
                if(ClickGUIFontRenderWrapper.getFontRenderer() != DefaultFontRenderer.INSTANCE) {
                    ClickGUIFontRenderWrapper.setFontRenderer(DefaultFontRenderer.INSTANCE);
                }
            }
        }

    });

    public ClickGUIModule() {
        super("ClickGUI", "Default Konas GUI", Keyboard.KEY_Y, Category.CLIENT);
    }

    public static CustomFontRenderer customFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 17);

    @Override
    public void onEnable() {
        this.toggle();
        mc.displayGuiScreen(KonasGlobals.INSTANCE.clickGUI);
    }

}