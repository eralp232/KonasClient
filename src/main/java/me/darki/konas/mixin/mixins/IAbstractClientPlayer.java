package me.darki.konas.mixin.mixins;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractClientPlayer.class)
public interface IAbstractClientPlayer {

    @Accessor(value = "playerInfo")
    void setPlayerInfo(NetworkPlayerInfo info);

}
