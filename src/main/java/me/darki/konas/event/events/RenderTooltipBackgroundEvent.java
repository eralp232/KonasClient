package me.darki.konas.event.events;

import net.minecraft.item.ItemStack;

public class RenderTooltipBackgroundEvent {
    private final ItemStack itemStack;

    private final int x;
    private final int y;

    public RenderTooltipBackgroundEvent(ItemStack itemStack, int x, int y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
