package me.darki.konas.event.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceBlockEvent extends CancellableEvent {
    private final EntityPlayer player;
    private final World world;
    private final BlockPos pos;
    private final EnumFacing side;
    private final float hitX;
    private final float hitY;
    private final float hitZ;

    public PlaceBlockEvent(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        this.player = player;
        this.world = world;
        this.pos = pos;
        this.side = side;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getSide() {
        return side;
    }

    public float getHitX() {
        return hitX;
    }

    public float getHitY() {
        return hitY;
    }

    public float getHitZ() {
        return hitZ;
    }
}
