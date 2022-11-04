package me.darki.konas.event.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class GetPortalBoundingBoxEvent extends CancellableEvent {
    private final IBlockState state;
    private final IBlockAccess source;
    private final BlockPos pos;

    public GetPortalBoundingBoxEvent(IBlockState state, IBlockAccess source, BlockPos pos) {
        this.state = state;
        this.source = source;
        this.pos = pos;
    }

    public IBlockState getState() {
        return state;
    }

    public IBlockAccess getSource() {
        return source;
    }

    public BlockPos getPos() {
        return pos;
    }
}