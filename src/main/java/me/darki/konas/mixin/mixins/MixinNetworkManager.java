package me.darki.konas.mixin.mixins;

import com.viaversion.viafabric.handler.CommonTransformer;
import cookiedragon.eventsystem.EventDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.module.modules.render.PacketRender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Send(packet);
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        } else {
            if (event.getPacket() instanceof CPacketPlayer.Rotation || event.getPacket() instanceof CPacketPlayer.PositionRotation) {
                PacketRender.setYaw(((CPacketPlayer) event.getPacket()).getYaw(0));
                PacketRender.setPitch(((CPacketPlayer) event.getPacket()).getPitch(0));
            }
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Receive(packet);
        EventDispatcher.Companion.dispatch(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
        }
    }


    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        ci.cancel();
    }

    @Redirect(method = "setCompressionThreshold", at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lio/netty/channel/ChannelPipeline;addBefore(Ljava/lang/String;Ljava/lang/String;Lio/netty/channel/ChannelHandler;)Lio/netty/channel/ChannelPipeline;"
    ))
    private ChannelPipeline decodeEncodePlacement(ChannelPipeline instance, String base, String newHandler, ChannelHandler handler) {
        // Fixes the handler order
        if (!new File(Minecraft.getMinecraft().gameDir, "novia").exists()) {
            switch (base) {
                case "decoder": {
                    if (instance.get(CommonTransformer.HANDLER_DECODER_NAME) != null)
                        base = CommonTransformer.HANDLER_DECODER_NAME;
                    break;
                }
                case "encoder": {
                    if (instance.get(CommonTransformer.HANDLER_ENCODER_NAME) != null)
                        base = CommonTransformer.HANDLER_ENCODER_NAME;
                    break;
                }
            }
        }
        return instance.addBefore(base, newHandler, handler);
    }
}