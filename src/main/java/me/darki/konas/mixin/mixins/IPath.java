package me.darki.konas.mixin.mixins;

import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Path.class)
public interface IPath {
    @Accessor(value = "points")
    PathPoint[] getPoints();

}
