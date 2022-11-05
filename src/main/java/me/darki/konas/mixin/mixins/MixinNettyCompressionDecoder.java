package me.darki.konas.mixin.mixins;

import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.player.AntiBookBan;
import net.minecraft.network.NettyCompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value={NettyCompressionDecoder.class})
public class MixinNettyCompressionDecoder {
    @ModifyConstant(method={"decode"}, constant={@Constant(intValue=0x200000)})
    private int onCompressionCheck(int n) {
        if (ModuleManager.getModuleByClass(AntiBookBan.class).isEnabled()) return Integer.MAX_VALUE;
        return n;
    }
}
