package me.darki.konas.event.events;

import net.minecraft.item.ItemStack;

public class RenderItemToolTipEvent extends CancellableEvent {
    private final ItemStack itemStack;

    private final int x, y;

    public RenderItemToolTipEvent(ItemStack itemStack, int x, int y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
