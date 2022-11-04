package me.darki.konas.event.events;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class DamageBlockEvent extends CancellableEvent {
    private static DamageBlockEvent INSTANCE = new DamageBlockEvent();

    private BlockPos pos;
    private EnumFacing face;
    private int blockHitDelay;
    private float curBlockDamageMP;

    public static DamageBlockEvent get(BlockPos pos, EnumFacing face, int blockHitDelay, float curBlockDamageMP) {
        INSTANCE.setCancelled(false);
        INSTANCE.pos = pos;
        INSTANCE.face = face;
        INSTANCE.blockHitDelay = blockHitDelay;
        INSTANCE.curBlockDamageMP = curBlockDamageMP;
        return INSTANCE;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public EnumFacing getFace() {
        return face;
    }

    public void setFace(EnumFacing face) {
        this.face = face;
    }

    public int getBlockHitDelay() {
        return blockHitDelay;
    }

    public void setBlockHitDelay(int blockHitDelay) {
        this.blockHitDelay = blockHitDelay;
    }

    public float getCurBlockDamageMP() {
        return curBlockDamageMP;
    }

    public void setCurBlockDamageMP(float curBlockDamageMP) {
        this.curBlockDamageMP = curBlockDamageMP;
    }
}
