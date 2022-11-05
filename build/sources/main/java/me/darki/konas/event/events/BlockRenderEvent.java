package me.darki.konas.event.events;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BlockRenderEvent extends CancellableEvent {
    private Block block;
    private BlockPos pos;

    public BlockRenderEvent(Block block, BlockPos pos) {
        this.block = block;
        this.pos = pos;
    }

    public Block getBlock() {
        return block;
    }

    public BlockPos getPos() {
        return pos;
    }
}
