package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.PlayerPushedByWaterEvent;
import me.darki.konas.event.events.PlayerTravelEvent;
import me.darki.konas.event.events.WalkingTravelEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {


    @Inject(method = {"travel"}, at = {@At("HEAD")}, cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        if (this.equals(Minecraft.getMinecraft().player)) {
            PlayerTravelEvent event = new PlayerTravelEvent(strafe, vertical, forward);
            EventDispatcher.Companion.dispatch(event);
            if (event.isCancelled()) {
                move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                info.cancel();
            }
        }
    }

    @Inject(method = "isPushedByWater()Z", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> info) {
        PlayerPushedByWaterEvent event = new PlayerPushedByWaterEvent();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @ModifyArgs(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;travel(FFF)V"))
    public void onTravel(Args args) {
        WalkingTravelEvent event = new WalkingTravelEvent(this.getEntityId(), args.get(0), args.get(1), args.get(2));
        EventDispatcher.Companion.dispatch(event);
        args.set(0, event.getStrafe());
        args.set(1, event.getVertical());
        args.set(2, event.getForward());
    }
}
