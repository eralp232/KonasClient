package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.FreecamEntityEvent;
import me.darki.konas.event.events.FreecamEvent;
import me.darki.konas.event.events.RenderItemOverlayEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.setting.SubBind;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.entity.FreecamCamera;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.input.Keyboard;

public class Freecam extends Module {

    public static Setting<SubBind> movePlayer = new Setting<>("Control", new SubBind(Keyboard.KEY_LMENU));
    private Setting<Boolean> follow = new Setting<>("Follow", false);
    private Setting<Boolean> copyInventory = new Setting<>("CopyInv", false);
    private Setting<Float> hSpeed = new Setting<>("HSpeed", 1.0f, 2.0f, 0.2f, 0.1f);
    private Setting<Float> vSpeed = new Setting<>("VSpeed", 1.0f, 2.0f, 0.2f, 0.1f);


    private Entity cachedActiveEntity = null;
    private int lastActiveTick = -1;

    private Entity oldRenderEntity = null;
    private FreecamCamera camera = null;
    private MovementInput cameraMovement = new MovementInputFromOptions(mc.gameSettings) {
        @Override
        public void updatePlayerMoveState() {
            if (!PlayerUtils.isKeyDown(movePlayer.getValue().getKeyCode())) {
                super.updatePlayerMoveState();
            } else {
                this.moveStrafe = 0f;
                this.moveForward = 0f;
                this.forwardKeyDown = false;
                this.backKeyDown = false;
                this.leftKeyDown = false;
                this.rightKeyDown = false;
                this.jump = false;
                this.sneak = false;
            }
        }
    };

    private MovementInput playerMovement = new MovementInputFromOptions(mc.gameSettings) {
        @Override
        public void updatePlayerMoveState() {
            if (PlayerUtils.isKeyDown(movePlayer.getValue().getKeyCode())) {
                super.updatePlayerMoveState();
            } else {
                this.moveStrafe = 0f;
                this.moveForward = 0f;
                this.forwardKeyDown = false;
                this.backKeyDown = false;
                this.leftKeyDown = false;
                this.rightKeyDown = false;
                this.jump = false;
                this.sneak = false;
            }
        }
    };


    public Freecam() {
        super("Freecam", "Control your camera separately to your body", Category.PLAYER);
    }

    public Entity getActiveEntity() {
        if (cachedActiveEntity == null) {
            cachedActiveEntity = mc.player;
        }

        int currentTick = mc.player.ticksExisted;
        if (lastActiveTick != currentTick) {
            lastActiveTick = currentTick;

            if (this.isEnabled()) {
                if (PlayerUtils.isKeyDown(movePlayer.getValue().getKeyCode())) {
                    cachedActiveEntity = mc.player;
                } else {
                    cachedActiveEntity = mc.getRenderViewEntity() == null ? mc.player : mc.getRenderViewEntity();
                }
            } else {
                cachedActiveEntity = mc.player;
            }
        }
        return cachedActiveEntity;
    }

    @Subscriber
    public void onWorldLoad(WorldEvent.Unload event) {
        mc.setRenderViewEntity(mc.player);
        toggle();
    }

    @Subscriber
    public void onFreecam(FreecamEvent event) {
        event.cancel();
    }

    @Subscriber
    public void onFreecamEntity(FreecamEntityEvent event) {
        if(getActiveEntity() != null) {
            event.setEntity((EntityPlayerSP) getActiveEntity());
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if(mc.player == null || mc.world == null) return;
        camera.setCopyInventory(copyInventory.getValue());
        camera.setFollow(follow.getValue());
        camera.sethSpeed(hSpeed.getValue());
        camera.setvSpeed(vSpeed.getValue());
    }

    @Override
    public void onEnable() {
        if(mc.player == null) return;

        camera = new FreecamCamera(copyInventory.getValue(), follow.getValue(), hSpeed.getValue(), vSpeed.getValue());
        camera.movementInput = cameraMovement;
        mc.player.movementInput = playerMovement;
        mc.world.addEntityToWorld(-921, camera);
        oldRenderEntity = mc.getRenderViewEntity();
        mc.setRenderViewEntity(camera);
        mc.renderChunksMany = false;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        if(camera != null) mc.world.removeEntity(camera);
        camera = null;
        mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
        mc.setRenderViewEntity(oldRenderEntity);
        mc.renderChunksMany = true;
    }

    @Subscriber
    public void onRenderOverlay(RenderItemOverlayEvent event) {
        event.cancel();
    }
}