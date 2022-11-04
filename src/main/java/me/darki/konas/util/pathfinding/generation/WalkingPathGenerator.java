package me.darki.konas.util.pathfinding.generation;

import me.darki.konas.util.pathfinding.node.WalkingNode;
import me.darki.konas.util.pathfinding.queueing.WalkingPathQueue;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class WalkingPathGenerator implements IPathGenerator {
    public static final int MAX_ITERATIONS = 100_000;
    public static final int MAX_DISTANCE = 200;

    public static final int MAX_FALL_DIST = 5;

    private Minecraft mc = Minecraft.getMinecraft();

    private WalkingNode start;
    private WalkingNode current;
    private final BlockPos goal;

    private final HashMap<WalkingNode, Float> nodeCosts = new HashMap<>();
    private final HashMap<WalkingNode, WalkingNode> prevNodes = new HashMap<>();
    private final WalkingPathQueue queue = new WalkingPathQueue();

    private final ArrayList<WalkingNode> path = new ArrayList<>();

    private int totalIterations = 0;

    private final String name;

    public WalkingPathGenerator(BlockPos goal, String name) {
        start = new WalkingNode(new BlockPos(mc.player.posX, mc.player.onGround ? mc.player.posY + 0.5 : mc.player.posY, mc.player.posZ));
        this.goal = goal;

        nodeCosts.put(start, 0F);
        queue.add(start, getGoalCost(start));

        this.name = name;
    }

    public ArrayList<WalkingNode> cycle() {
        start = new WalkingNode(new BlockPos(mc.player.posX, mc.player.onGround ? mc.player.posY + 0.5 : mc.player.posY, mc.player.posZ));

        nodeCosts.clear();
        prevNodes.clear();
        queue.reset();

        nodeCosts.put(start, 0F);
        queue.add(start, getGoalCost(start));

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            totalIterations++;
            if (!queue.getNodeQueue().isEmpty()) {
                current = queue.poll();

                if (goal.equals(current)) {
                    formatPath();
                    return path;
                }

                if (Math.abs(start.getX() - current.getX()) > MAX_DISTANCE || Math.abs(start.getZ() - current.getZ()) > MAX_DISTANCE) {
                    formatPath();
                    return path;
                }

                for (WalkingNode candidate : getNeighborNodes(current)) {
                    float candidateCost = nodeCosts.get(current) + getCost(current, candidate);

                    if (nodeCosts.containsKey(candidate) && nodeCosts.get(candidate) <= candidateCost) {
                        continue;
                    }

                    nodeCosts.put(candidate, candidateCost);
                    prevNodes.put(candidate, current);
                    queue.add(candidate, candidateCost + getGoalCost(candidate));
                }
            }
        }

        formatPath();
        return path;
    }

    private void formatPath() {
        path.clear();

        WalkingNode node = start;

        for (WalkingNode nextNode : prevNodes.keySet()) {
            if (getGoalCost(nextNode) < getGoalCost(node)) {
                node = nextNode;
            }
        }

        while (node != null) {
            path.add(node);
            node = prevNodes.get(node);
        }

        Collections.reverse(path);
    }

    private float getCost(BlockPos current, BlockPos next) {
        float[] costs = {0.5F, 0.5F};
        BlockPos[] twoPositions = new BlockPos[]{current, next};

        for (int i = 0; i < twoPositions.length; i++) {
            Material material = mc.world.getBlockState(twoPositions[i]).getMaterial();

            if (material == Material.WATER) {
                costs[i] *= 1.3164437838225804F;
            }  else if (material == Material.LAVA) {
                costs[i] *= 4.539515393656079F;
            }

            if (mc.world.getBlockState(twoPositions[i].down()).getBlock() instanceof BlockSoulSand) {
                costs[i] *= 2.5F;
            }
        }

        float cost = costs[0] + costs[1];

        if (current.getX() != next.getX() && current.getZ() != next.getZ()) {
            cost *= 1.4142135623730951F; // sqrt 2
        }

        return cost;
    }

    private ArrayList<WalkingNode> getNeighborNodes(WalkingNode pos) {
        ArrayList<WalkingNode> neighborNodes = new ArrayList<>();

        if (Math.abs(start.getX() - pos.getX()) > MAX_DISTANCE || Math.abs(start.getZ() - pos.getZ()) > MAX_DISTANCE) {
            return neighborNodes;
        }

        BlockPos north = pos.north();
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos west = pos.west();

        BlockPos northEast = north.east();
        BlockPos southEast = south.east();
        BlockPos southWest = south.west();
        BlockPos northWest = north.west();

        BlockPos up = pos.up();
        BlockPos down = pos.down();

        boolean onGround = solid(down);

        // we can't iterate over enumfacing horizontals here due to diagnal movement

        if (onGround || pos.isJump()) {
            if (horizontalCheck(pos, north)) {
                neighborNodes.add(new WalkingNode(north));
            }

            if (horizontalCheck(pos, east)) {
                neighborNodes.add(new WalkingNode(east));
            }

            if (horizontalCheck(pos, south)) {
                neighborNodes.add(new WalkingNode(south));
            }

            if (horizontalCheck(pos, west)) {
                neighborNodes.add(new WalkingNode(west));
            }

            if (diagonalCheck(pos, EnumFacing.NORTH, EnumFacing.EAST)) {
                neighborNodes.add(new WalkingNode(northEast));
            }

            if (diagonalCheck(pos, EnumFacing.SOUTH, EnumFacing.EAST)) {
                neighborNodes.add(new WalkingNode(southEast));
            }

            if (diagonalCheck(pos, EnumFacing.SOUTH, EnumFacing.WEST)) {
                neighborNodes.add(new WalkingNode(southWest));
            }

            if (diagonalCheck(pos, EnumFacing.NORTH, EnumFacing.WEST)) {
                neighborNodes.add(new WalkingNode(northWest));
            }
        }

        if (pos.getY() < 256 && throughCheck(up.up()) && (onGround || climbCheck(pos)) && (climbCheck(pos) || goal.equals(up)
                || standCheck(north) || standCheck(east) || standCheck(south) || standCheck(west))) {
            neighborNodes.add(new WalkingNode(up, onGround));
        }

        if (pos.getY() > 0 && throughCheck(down) && walkCheck(down.down()) && (fallCheck(pos))) {
            neighborNodes.add(new WalkingNode(down));
        }

        return neighborNodes;
    }

    private boolean horizontalCheck(BlockPos current, BlockPos next) {
        if (passabilityCheck(next) && (throughCheck(next.down()) || standCheck(next.down()))) {
            return true;
        }

        return false;
    }

    private boolean fallCheck(WalkingNode node) {
        for (int i = 0; i <= MAX_FALL_DIST; i++) {
            if (standCheck(node.down(i))) {
                return true;
            }
        }

        return false;
    }

    private boolean diagonalCheck(BlockPos current, EnumFacing primaryOffset, EnumFacing secondaryOffset) {
        BlockPos primaryHorizontal = current.offset(primaryOffset);
        BlockPos secondaryHorizontal = current.offset(secondaryOffset);
        BlockPos next = primaryHorizontal.offset(secondaryOffset);

        return passabilityCheck(primaryHorizontal) && passabilityCheck(secondaryHorizontal) && horizontalCheck(current, next);
    }

    private boolean climbCheck(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockLadder) && !(block instanceof BlockVine)) {
            return false;
        }

        BlockPos up = pos.up();

        return solid(pos.north()) || solid(pos.east()) || solid(pos.south()) || solid(pos.west())
                || solid(up.north()) || solid(up.east()) || solid(up.south()) || solid(up.west());
    }

    private boolean passabilityCheck(BlockPos pos) {
        return throughCheck(pos) && throughCheck(pos.up()) && walkCheck(pos.down());
    }

    private boolean throughCheck(BlockPos pos) {
        if (!mc.world.isBlockLoaded(pos)) {
            return false;
        }

        Material material = mc.world.getBlockState(pos).getMaterial();
        Block block = mc.world.getBlockState(pos).getBlock();

        if (material.blocksMovement() && !(block instanceof BlockSign)) {
            return false;
        }

        if (block instanceof BlockTripWire || block instanceof BlockPressurePlate) {
            return false;
        }

        if (material == Material.LAVA || material == Material.FIRE) {
            return false;
        }

        return true;
    }

    private boolean walkCheck(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
            return false;
        }

        return true;
    }

    private boolean standCheck(BlockPos pos) {
        Material material = mc.world.getBlockState(pos).getMaterial();

        if (!solid(pos)) {
            return false;
        }

        if (material == Material.CACTUS || material == Material.LAVA) {
            return false;
        }

        return true;
    }

    private float getGoalCost(BlockPos pos) {
        float dx = Math.abs(pos.getX() - goal.getX());
        float dy = Math.abs(pos.getY() - goal.getY());
        float dz = Math.abs(pos.getZ() - goal.getZ());

        return 1.001F * (dx + dy + dz - 0.5857864376269049F * Math.min(dx, dz)); // inlining :muscle:
    }

    private boolean solid(BlockPos pos) {
        Material material = mc.world.getBlockState(pos).getMaterial();
        Block block = mc.world.getBlockState(pos).getBlock();
        return material.blocksMovement() && !(block instanceof BlockSign) || block instanceof BlockLadder;
    }

    public ArrayList<WalkingNode> getPath() {
        return path;
    }

    public BlockPos getGoal() {
        return goal;
    }

    @Override
    public String getName() {
        return name;
    }
}