package me.darki.konas.event.events;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class TransformEatFirstPersonEvent extends CancellableEvent {
    private final float p_187454_1_;
    private final EnumHandSide hand;
    private final ItemStack stack;

    public TransformEatFirstPersonEvent(float p_187454_1_, EnumHandSide hand, ItemStack stack) {
        this.p_187454_1_ = p_187454_1_;
        this.hand = hand;
        this.stack = stack;
    }

    public float getP_187454_1_() {
        return p_187454_1_;
    }

    public EnumHandSide getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }
}