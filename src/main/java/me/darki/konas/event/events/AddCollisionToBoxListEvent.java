package me.darki.konas.event.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class AddCollisionToBoxListEvent extends CancellableEvent {
    private static AddCollisionToBoxListEvent INSTANCE = new AddCollisionToBoxListEvent();

    private Block block;
    private IBlockState state;
    private World world;
    private BlockPos pos;
    private AxisAlignedBB entityBox;
    private List<AxisAlignedBB> collidingBoxes;
    private Entity entity;
    private boolean isActualState;

    public static AddCollisionToBoxListEvent get(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
        INSTANCE.setCancelled(false);
        INSTANCE.block = block;
        INSTANCE.state = state;
        INSTANCE.world = worldIn;
        INSTANCE.pos = pos;
        INSTANCE.entityBox = entityBox;
        INSTANCE.collidingBoxes = collidingBoxes;
        INSTANCE.entity = entityIn;
        INSTANCE.isActualState = isActualState;
        return INSTANCE;
    }


    public Block getBlock() {
        return block;
    }

    public IBlockState getState() {
        return state;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public AxisAlignedBB getEntityBox() {
        return entityBox;
    }

    public void setEntityBox(AxisAlignedBB entityBox) {
        this.entityBox = entityBox;
    }

    public List<AxisAlignedBB> getCollidingBoxes() {
        return collidingBoxes;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isActualState() {
        return isActualState;
    }
}
