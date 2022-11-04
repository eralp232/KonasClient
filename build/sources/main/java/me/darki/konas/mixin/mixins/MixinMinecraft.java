package me.darki.konas.mixin.mixins;

import com.viaversion.viafabric.ViaFabric;
import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.event.events.BlockResetEvent;
import me.darki.konas.event.events.InteractEvent;
import me.darki.konas.event.events.KeyEvent;
import me.darki.konas.event.events.RootEvent;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(value = Minecraft.class, priority = Integer.MAX_VALUE - 100)
public class MixinMinecraft {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void injectConstructor(GameConfiguration gameConfiguration, CallbackInfo ci) {
        if (new File(Minecraft.getMinecraft().gameDir, "novia").exists()) return;
        try {
            new ViaFabric().onInitialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    public boolean handActiveRedirect(EntityPlayerSP entityPlayerSP) {
        InteractEvent event = new InteractEvent(entityPlayerSP.isHandActive());
        EventDispatcher.Companion.dispatch(event);
        return event.isInteracting();
    }

    @Inject(method={"runGameLoop"}, at={@At(value="HEAD")})
    private void onRunGameLoop(CallbackInfo callbackInfo) {
        RootEvent event = new RootEvent();
        EventDispatcher.Companion.dispatch(event);
    }

    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/toasts/GuiToast;drawToast(Lnet/minecraft/client/gui/ScaledResolution;)V"))
    public void onRenderToast(GuiToast guiToast, ScaledResolution resolution) {
        if (ModuleManager.getModuleByClass(NoRender.class).isEnabled() && NoRender.toast.getValue()) {
            return;
        }
        guiToast.drawToast(resolution);
    }


    @Inject(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;fireKeyInput()V"))
    public void keyInject(CallbackInfo ci) {
        if(Keyboard.getEventKeyState()) {
            KeyEvent event = new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey());
            EventDispatcher.Companion.dispatch(event);
        }
    }

    @Inject(method = "runTickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;fireMouseInput()V"))
    public void mouseInject(CallbackInfo ci) {
        if(Mouse.getEventButtonState()) {
            KeyEvent event = new KeyEvent(Mouse.getEventButton() - 100);
            EventDispatcher.Companion.dispatch(event);
        }
    }

    @Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", ordinal = 0), require = 1)
    public boolean rightClickRedirect(PlayerControllerMP playerControllerMP) {
        InteractEvent event = new InteractEvent(playerControllerMP.getIsHittingBlock());
        EventDispatcher.Companion.dispatch(event);
        return event.isInteracting();
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;resetBlockRemoving()V"))
    public void resetRedirect(PlayerControllerMP playerControllerMP) {
        BlockResetEvent event = BlockResetEvent.get();
        EventDispatcher.Companion.dispatch(event);
        if(event.isCancelled()) return;
        playerControllerMP.resetBlockRemoving();
    }

}
