package me.darki.konas.util.pathfinding;

import me.darki.konas.util.pathfinding.generation.IPathGenerator;

public class PathManager {
    private IPathGenerator currentActiveGenerator = null;

    public boolean trySetCurrentActiveGenerator(IPathGenerator generator) {
        if (currentActiveGenerator == null) {
            currentActiveGenerator = generator;
            return true;
        }

        return false;
    }

    public String getCurrentName() {
        if (currentActiveGenerator == null) {
            return "NONE";
        } else {
            return currentActiveGenerator.getName();
        }
    }

    public void reset() {
        currentActiveGenerator = null;
    }

    public boolean isActive() {
        return currentActiveGenerator != null;
    }
}
