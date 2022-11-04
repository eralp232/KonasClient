package me.darki.konas.mixin.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Entity.class, priority = Integer.MAX_VALUE)
public interface IEntity {

    @Invoker(value = "setFlag")
    void invokeSetFlag(int flag, boolean set);

    @Accessor(value = "inPortal")
    void setInPortal(boolean inPortal);
}