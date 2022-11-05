package me.darki.konas.module.modules.client;

import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import org.lwjgl.input.Keyboard;

public class KonasGui extends Module {
    // header/footer
    public static Setting<ColorSetting> mainStart = new Setting<>("MainStart", new ColorSetting(0xFFC82828));
    public static Setting<ColorSetting> mainEnd = new Setting<>("MainEnd", new ColorSetting(0xFFC82828));

    // accent color for line and selected category
    public static Setting<ColorSetting> accentStart = new Setting<>("AccentStart", new ColorSetting(0xFFDC3C28));
    public static Setting<ColorSetting> accentEnd = new Setting<>("AccentEnd", new ColorSetting(0xFFDC3C28));

    // color for categories
    public static Setting<ColorSetting> categoryStart = new Setting<>("CategoryStart", new ColorSetting(0xFF9B1B1B));
    public static Setting<ColorSetting> categoryEnd = new Setting<>("CategoryEnd", new ColorSetting(0xFF9B1B1B));

    // outlines
    public static Setting<ColorSetting> outlineStart = new Setting<>("OutlineStart", new ColorSetting(0x33FFFFFF));
    public static Setting<ColorSetting> outlineEnd = new Setting<>("OutlineEnd", new ColorSetting(0x33FFFFFF));

    // main background
    public static Setting<ColorSetting> backgroundStart = new Setting<>("BackgroundStart", new ColorSetting(0xAA222222));
    public static Setting<ColorSetting> backgroundEnd = new Setting<>("BackgroundEnd", new ColorSetting(0xAA222222));

    // containers for stuff like setting parents and modules list
    public static Setting<ColorSetting> foregroundStart = new Setting<>("ForegroundStart", new ColorSetting(0x60000000));
    public static Setting<ColorSetting> foregroundEnd = new Setting<>("ForegroundEnd", new ColorSetting(0x60000000));

    // Color for sliders, keybinds, etc.
    public static Setting<ColorSetting> primaryStart = new Setting<>("PrimaryStart", new ColorSetting(0xFFB4B4B4));
    public static Setting<ColorSetting> primaryEnd = new Setting<>("PrimaryEnd", new ColorSetting(0xFFB4B4B4));

    // Color checkbox fills, etc.
    public static Setting<ColorSetting> secondaryStart = new Setting<>("SecondaryStart", new ColorSetting(0xFF383838));
    public static Setting<ColorSetting> secondaryEnd = new Setting<>("SecondaryEnd", new ColorSetting(0xFF383838));

    public static Setting<ColorSetting> sliderStart = new Setting<>("SliderStart", new ColorSetting(0xFF979797));
    public static Setting<ColorSetting> sliderEnd = new Setting<>("SliderEnd", new ColorSetting(0xFF979797));

    public static Setting<ColorSetting> textBoxStart = new Setting<>("TextBoxStart", new ColorSetting(0xFFdcdcdc));
    public static Setting<ColorSetting> textBoxEnd = new Setting<>("TextBoxEnd", new ColorSetting(0xFFdcdcdc));

    // Font
    public static Setting<ColorSetting> fontStart = new Setting<>("FontStart", new ColorSetting(0xFFFFFFFF));
    public static Setting<ColorSetting> fontEnd = new Setting<>("FontEnd", new ColorSetting(0xFFFFFFFF));

    public static Setting<ColorSetting> darkFontStart = new Setting<>("DarkFontStart", new ColorSetting(0xFF000000));
    public static Setting<ColorSetting> darkFontEnd = new Setting<>("DarkFontEnd", new ColorSetting(0xFF000000));

    // Secondary Font
    public static Setting<ColorSetting> subFontStart = new Setting<>("SubFontStart", new ColorSetting(0xFFCCCCCC));
    public static Setting<ColorSetting> subFontEnd = new Setting<>("SubFontEnd", new ColorSetting(0xFFCCCCCC));

    public static Setting<Float> highlight = new Setting<>("Highlight", 0.7F, 0.9F, 0.5F, 0.1F);

    public static Setting<Boolean> reload = new Setting<>("Reload", false);
    public static Setting<Integer> height = new Setting<>("Height", 400, 600, 300, 10);
    public static ListenableSettingDecorator<Boolean> singleColumn = new ListenableSettingDecorator<>("SingleColumn", false, (value) -> reload.setValue(true));

    public static int clipboard = -1;

    public KonasGui() {
        super("KonasGui", "Aimware-Styled single panel GUI", Keyboard.KEY_RSHIFT, Category.CLIENT);
    }

    @Override
    public void onEnable() {
        this.toggle();
        mc.displayGuiScreen(KonasGlobals.INSTANCE.konasGuiScreen);
    }
}
