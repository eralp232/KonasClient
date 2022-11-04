package me.darki.konas.util.pathfinding.node;

import net.minecraft.util.math.BlockPos;

public class WalkingNode extends BlockPos {
    private final boolean jump;

    public WalkingNode(BlockPos pos) {
        super(pos);
        jump = false;
    }

    public WalkingNode(BlockPos pos, boolean jump) {
        super(pos);
        this.jump = jump;
    }

    public boolean isJump() {
        return jump;
    }
}
