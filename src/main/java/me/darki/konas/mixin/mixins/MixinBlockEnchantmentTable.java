package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderEnchantmentTableEvent;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockEnchantmentTable.class)
public class MixinBlockEnchantmentTable {

    @Inject(method = "randomDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"), cancellable = true)
    public void enchantmentParticle(IBlockState stateIn, World worldIn, BlockPos pos, Random rand, CallbackInfo ci) {
        RenderEnchantmentTableEvent event = new RenderEnchantmentTableEvent();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) ci.cancel();
    }

}
