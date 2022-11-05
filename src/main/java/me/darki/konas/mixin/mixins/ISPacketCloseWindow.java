package me.darki.konas.mixin.mixins;

import net.minecraft.network.play.server.SPacketCloseWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketCloseWindow.class)
public interface ISPacketCloseWindow {
    @Accessor(value = "windowId")
    int getWindowId();
}
