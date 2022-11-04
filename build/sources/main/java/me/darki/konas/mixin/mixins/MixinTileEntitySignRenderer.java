package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.RenderSignEvent;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntitySignRenderer.class)
public class MixinTileEntitySignRenderer {

    public ITextComponent[] cache = null;

    @Inject(method = "render", at = @At("HEAD"))
    public void renderHead(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        RenderSignEvent event = new RenderSignEvent();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) {
            cache = new ITextComponent[te.signText.length];
            for(int i = 0; i < te.signText.length; i++) {
                cache[i] = te.signText[i];
                te.signText[i] = null;
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderTail(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        RenderSignEvent event = new RenderSignEvent();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) {
            System.arraycopy(cache, 0, te.signText, 0, te.signText.length);
            cache = null;
        }
    }

}
