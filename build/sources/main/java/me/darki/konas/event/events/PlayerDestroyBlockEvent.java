package me.darki.konas.event.events;

import net.minecraft.util.math.BlockPos;

public class PlayerDestroyBlockEvent extends CancellableEvent {
    private final BlockPos blockPos;

    public PlayerDestroyBlockEvent(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
