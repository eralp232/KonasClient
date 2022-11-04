package me.darki.konas.event.events;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ProcessRightClickBlockEvent extends CancellableEvent {
    private final EntityPlayerSP player;
    private final WorldClient worldIn;
    private final BlockPos pos;
    private final EnumFacing direction;
    private final Vec3d vec;
    private final EnumHand hand;


    public ProcessRightClickBlockEvent(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand) {
        this.player = player;
        this.worldIn = worldIn;
        this.pos = pos;
        this.direction = direction;
        this.vec = vec;
        this.hand = hand;
    }

    public EntityPlayerSP getPlayer() {
        return player;
    }

    public WorldClient getWorldIn() {
        return worldIn;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getDirection() {
        return direction;
    }

    public Vec3d getVec() {
        return vec;
    }

    public EnumHand getHand() {
        return hand;
    }
}
