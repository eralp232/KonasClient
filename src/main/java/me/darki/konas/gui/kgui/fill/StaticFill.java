package me.darki.konas.gui.kgui.fill;

import me.darki.konas.gui.kgui.shape.AbstractShape;

import java.awt.*;

public class StaticFill implements Fill {
    private final Color color;

    public StaticFill(Color color) {
        this.color = color;
    }

    @Override
    public Color colorAt(AbstractShape abstractShape, float x, float y) {
        return color;
    }
}
