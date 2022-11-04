package me.darki.konas.event.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LeftClickBlockEvent extends CancellableEvent {

    private final EntityPlayer player;
    private BlockPos pos;
    private EnumFacing face;
    private Vec3d hitVec;

    public LeftClickBlockEvent(EntityPlayer player, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        this.player = player;
        this.pos = pos;
        this.face = face;
        this.hitVec = hitVec;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getFace() {
        return face;
    }

    public Vec3d getHitVec() {
        return hitVec;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setFace(EnumFacing face) {
        this.face = face;
    }

    public void setHitVec(Vec3d hitVec) {
        this.hitVec = hitVec;
    }
}
