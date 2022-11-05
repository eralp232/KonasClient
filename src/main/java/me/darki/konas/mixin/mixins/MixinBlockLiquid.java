package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.LiquidCanCollideCheckEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {BlockLiquid.class}, priority = 2147483647)
public class MixinBlockLiquid {

    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(IBlockState state, boolean hitIfLiquid, CallbackInfoReturnable<Boolean> info) {
        LiquidCanCollideCheckEvent liquidCanCollideCheckEvent = EventDispatcher.Companion.dispatch(new LiquidCanCollideCheckEvent());;
        if (liquidCanCollideCheckEvent.isCancelled()) info.setReturnValue(true);
    }

}