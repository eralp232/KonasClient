package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.ClickWindowEvent;
import me.darki.konas.event.events.KeyBindingEvent;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.gui.kgui.KonasGuiScreen;
import me.darki.konas.gui.middleclick.GuiMiddleClickMenu;
import me.darki.konas.module.Module;
import me.darki.konas.setting.IRunnable;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class GuiMove extends Module {

    private final Setting<Boolean> strict = new Setting<>("Strict", false);
    private final ListenableSettingDecorator<Boolean> crouch = new ListenableSettingDecorator<>("Crouch", false, new IRunnable<Boolean>() {
        @Override
        public void run(Boolean arg) {
            if (arg) {
                if (!keys.contains(mc.gameSettings.keyBindSneak)) {
                    keys.add(mc.gameSettings.keyBindSneak);
                }
            } else {
                keys.remove(mc.gameSettings.keyBindSneak);
            }
        }
    });

    private final ArrayList<KeyBinding> keys = new ArrayList<>();

    public GuiMove() {
        super("GUIMove", "Lets you move around in GUIs", Category.PLAYER);
        keys.add(mc.gameSettings.keyBindForward);
        keys.add(mc.gameSettings.keyBindBack);
        keys.add(mc.gameSettings.keyBindRight);
        keys.add(mc.gameSettings.keyBindLeft);
        keys.add(mc.gameSettings.keyBindJump);
        if (crouch.getValue()) {
            keys.add(mc.gameSettings.keyBindSneak);
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (mc.currentScreen instanceof GuiOptions
                || mc.currentScreen instanceof GuiVideoSettings
                || mc.currentScreen instanceof GuiScreenOptionsSounds
                || mc.currentScreen instanceof GuiContainer
                || mc.currentScreen instanceof GuiIngameMenu
                || mc.currentScreen instanceof KonasGuiScreen
                || mc.currentScreen instanceof GuiScreenAdvancements
                || mc.currentScreen instanceof ClickGUI
                || mc.currentScreen instanceof GuiMiddleClickMenu) {
            for (KeyBinding key : keys) {
                KeyBinding.setKeyBindState(key.getKeyCode(), GameSettings.isKeyDown(key));
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) mc.player.rotationYaw -= 5;
            if (Keyboard.isKeyDown(Keyboard.KEY_UP) && mc.player.rotationPitch > -84) mc.player.rotationPitch -= 5;
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && mc.player.rotationPitch < 84) mc.player.rotationPitch += 5;
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) mc.player.rotationYaw += 5;
        }
    }

    @Subscriber
    public void onKeyBinding(KeyBindingEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (!(mc.currentScreen instanceof GuiChat) && mc.currentScreen != null) {
            event.holding = event.pressed;
        }
    }

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (strict.getValue() && event.getPacket() instanceof CPacketClickWindow) {
            if (mc.player.isActiveItemStackBlocking()) {
                mc.playerController.onStoppedUsingItem(mc.player);
            }
            if (mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
            if (mc.player.isSprinting()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
    }

    @Subscriber
    public void onWindowClick(ClickWindowEvent event) {
        if (!strict.getValue()) return;

        if (mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (mc.player.isSprinting()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

}