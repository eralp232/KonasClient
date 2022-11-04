package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.TabGUI;
import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class TabGuiContainer extends Container {

    public static TabGUI tabGUI = new TabGUI();

    public static Setting<ColorSetting> selected = new Setting<>("Selected", new ColorSetting(Color.BLUE.hashCode(), true));
    public static Setting<ColorSetting> toggle = new Setting<>("Toggled", new ColorSetting(Color.BLUE.hashCode(), true));

    public TabGuiContainer() {
        super("TabGUI", 3, 80, 120, 100);
    }

    @Override
    public void onRender() {
        super.onRender();
        tabGUI.draw(getPosX(), getPosY());
        setHeight(tabGUI.getTotalHeight());
        setWidth(tabGUI.getTotalWidth());
    }

    @Override
    public void onKeyTyped(int keyCode) {
        super.onKeyTyped(keyCode);
        if(keyCode == Keyboard.KEY_DOWN) {
            tabGUI.cycleDown();
        } else if(keyCode == Keyboard.KEY_UP) {
            tabGUI.cycleUp();
        } else if(keyCode == Keyboard.KEY_LEFT) {
            tabGUI.cycleLeft();
        } else if(keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_RETURN) {
            tabGUI.cycleRight();
        }

    }



}
