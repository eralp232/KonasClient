package me.darki.konas.mixin.mixins;

import cookiedragon.eventsystem.EventDispatcher;
import kotlin.collections.CollectionsKt;
import me.darki.konas.event.events.RenderPlayerInTabEvent;
import me.darki.konas.module.modules.render.ExtraTab;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = GuiPlayerTabOverlay.class, priority = Integer.MAX_VALUE - 20000)
public class MixinGuiPlayerTabOverlay {

    private List<NetworkPlayerInfo> preSubList = CollectionsKt.emptyList();

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    public List<NetworkPlayerInfo> renderPlayerlistStorePlayerListPre(List<NetworkPlayerInfo> list) {
        preSubList = list;
        return list;
    }

    @ModifyVariable(method = "renderPlayerlist", at = @At(value = "STORE", ordinal = 1), ordinal = 0)
    public List<NetworkPlayerInfo> renderPlayerlistStorePlayerListPost(List<NetworkPlayerInfo> list) {
        return ExtraTab.subList(preSubList, list);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    public int onRenderPlayerName(FontRenderer fontRenderer, String text, float x, float y, int color)  {
        RenderPlayerInTabEvent event = new RenderPlayerInTabEvent(text);
        EventDispatcher.Companion.dispatch(event);
        return fontRenderer.drawStringWithShadow(event.getName(), x, y, color);
    }


}
