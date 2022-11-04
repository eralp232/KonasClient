package me.darki.konas.util.pathfinding.queueing;

import me.darki.konas.util.pathfinding.node.WalkingNode;

import java.util.Iterator;
import java.util.PriorityQueue;

public class WalkingPathQueue {
    private final PriorityQueue<WalkingPathQueue.NodeEntery> nodeQueue = new PriorityQueue<>((nodeEnteryA, nodeEnteryB) -> Float.compare(nodeEnteryA.priority, nodeEnteryB.priority));

    private class NodeEntery {
        private WalkingNode node;
        private float priority;

        public NodeEntery(WalkingNode node, float priority)
        {
            this.node = node;
            this.priority = priority;
        }
    }

    public boolean add(WalkingNode node, float priority) {
        return nodeQueue.add(new NodeEntery(node, priority));
    }

    public WalkingNode poll() {
        return nodeQueue.poll().node;
    }

    public void reset() {
        nodeQueue.clear();
    }

    public WalkingNode[] toArray() {
        WalkingNode[] array = new WalkingNode[nodeQueue.size()];
        Iterator<NodeEntery> nodeQueueIterator = nodeQueue.iterator();

        for(int i = 0; i < nodeQueue.size() && nodeQueueIterator.hasNext(); i++) {
            array[i] = nodeQueueIterator.next().node;
        }

        return array;
    }

    public PriorityQueue<NodeEntery> getNodeQueue() {
        return nodeQueue;
    }
}
