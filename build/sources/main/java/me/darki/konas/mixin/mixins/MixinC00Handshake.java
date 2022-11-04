package me.darki.konas.mixin.mixins;

import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.NoForge;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C00Handshake.class)
public class MixinC00Handshake {

    @Shadow int protocolVersion;
    @Shadow String ip;
    @Shadow int port;
    @Shadow
    EnumConnectionState requestedState;

    @Inject(method = "writePacketData", at = @At(value = "HEAD"), cancellable = true)
    public void writePacketData(PacketBuffer buf, CallbackInfo info) {
        if (ModuleManager.getModuleByClass(NoForge.class).isEnabled()) {
            System.out.println("Cancelling packet data");
            info.cancel();
            buf.writeVarInt(protocolVersion);
            buf.writeString(ip);
            buf.writeShort(port);
            buf.writeVarInt(requestedState.getId());
        }
    }

}