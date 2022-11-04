package me.darki.konas.gui.kgui.fill;

import me.darki.konas.gui.kgui.shape.AbstractShape;

import java.awt.*;

public interface Fill {
    Color colorAt(AbstractShape abstractShape, float x, float y);
}
