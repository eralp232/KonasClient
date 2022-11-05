package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.CheckIfPlayerSpectatorEvent;
import me.darki.konas.event.events.FogEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Redirect(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getSkyColor(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d getSkyColorRedirect(WorldClient worldClient, Entity entityIn, float partialTicks) {
        Vec3d sky = Minecraft.getMinecraft().world.getSkyColor(entityIn, partialTicks);
        FogEvent.Color event = new FogEvent.Color((float) sky.x, (float) sky.y, (float) sky.z);
        EventDispatcher.Companion.dispatch(event);
        return new Vec3d(event.getR(), event.getG(), event.getB());
    }

    @ModifyVariable(method={"setupTerrain"}, at=@At(value="HEAD"))
    private boolean setupTerrain(boolean playerSpectator) {
        CheckIfPlayerSpectatorEvent event = CheckIfPlayerSpectatorEvent.get();
        EventDispatcher.Companion.dispatch(event);
        if (event.isCancelled()) {
            return true;
        }
        return playerSpectator;
    }

}
