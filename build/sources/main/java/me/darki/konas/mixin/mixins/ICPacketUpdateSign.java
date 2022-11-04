package me.darki.konas.mixin.mixins;

import net.minecraft.network.play.client.CPacketUpdateSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketUpdateSign.class)
public interface ICPacketUpdateSign {
    @Accessor(value = "lines")
    void setLines(String[] lines);

    @Accessor(value = "lines")
    String[] getLines();
}
