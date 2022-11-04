package me.darki.konas.mixin.mixins;

import com.viaversion.viafabric.handler.CommonTransformer;
import com.viaversion.viafabric.handler.clientside.VRDecodeHandler;
import com.viaversion.viafabric.handler.clientside.VREncodeHandler;
import com.viaversion.viafabric.platform.VRClientSideUserConnection;
import com.viaversion.viafabric.protocol.ViaFabricHostnameProtocol;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;

import java.io.File;

@Mixin(targets = "net.minecraft.network.NetworkManager$5")
public abstract class MixinNetworkManagerChInit {

    @Inject(method = "initChannel", at = @At(value = "TAIL"), remap = false)
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        if (new File(Minecraft.getMinecraft().gameDir, "novia").exists()) return;

        if (channel instanceof SocketChannel) {

            UserConnection user = new VRClientSideUserConnection(channel);
            new ProtocolPipeline(user).add(ViaFabricHostnameProtocol.INSTANCE);

            channel.pipeline()
                    .addBefore("encoder", CommonTransformer.HANDLER_ENCODER_NAME, new VREncodeHandler(user))
                    .addBefore("decoder", CommonTransformer.HANDLER_DECODER_NAME, new VRDecodeHandler(user));
        }
    }
}
