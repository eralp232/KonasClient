package me.darki.konas.event.events;

import net.minecraft.block.Block;

public class XRayBlockEvent extends CancellableEvent {
    private final Block block;

    public XRayBlockEvent(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }
}
