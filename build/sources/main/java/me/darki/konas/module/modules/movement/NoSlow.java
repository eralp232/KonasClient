package me.darki.konas.module.modules.movement;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.mixin.mixins.ICPacketPlayer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class NoSlow extends Module {
    private static final Setting<Boolean> glide = new Setting<>("Glide", false);
    private static final Setting<Boolean> strict = new Setting<>("Strict", false);
    private static final Setting<Boolean> airStrict = new Setting<>("AirStrict", false);
    private static final Setting<Boolean> ncp = new Setting<>("NCP", false).withProtocolRange(4, 47);

    public NoSlow() {
        super("NoSlow", "Makes you not slow down while i. e. eating", Category.MOVEMENT);
    }

    private boolean serverSneaking;
    private boolean gliding;

    @Subscriber
    public void onMoveInput(MoveInputEvent event) {
        if(mc.player.isHandActive() && !mc.player.isRiding()) {
            event.getInput().moveStrafe *= 5;
            event.getInput().moveForward *= 5;
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        Item item = mc.player.getActiveItemStack().getItem();
        if (((!mc.player.isHandActive() && item instanceof ItemFood || item instanceof ItemBow || item instanceof ItemPotion) || (!(item instanceof ItemFood) || !(item instanceof ItemBow) || !(item instanceof ItemPotion)))) {
            if (serverSneaking && strict.getValue()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                serverSneaking = false;
            }
            if (gliding) {
                gliding = false;
            }
        }
    }

    @Subscriber
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Pre event) {
        if (mc.world == null || mc.player == null) return;

        if (mc.player.getActiveItemStack().getItem() instanceof ItemBow) return;

        if (ncp.getValue() && mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword && ncp.isProtocolValid()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
    }

    @Subscriber
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        if (mc.player.getActiveItemStack().getItem() instanceof ItemBow) return;
        
        if (ncp.getValue() && mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword && ncp.isProtocolValid()) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(-1, -1, -1), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0, 0, 0));
        }
    }

    @Subscriber
    public void onPlayerUseItemEvent(PlayerUseItemEvent event) {
        if (mc.player.getActiveItemStack().getItem() instanceof ItemBow) return;

        if (glide.getValue()) {
            if (!gliding) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, false));
            }
            gliding = true;
        }

        if (!serverSneaking && strict.getValue() && (!airStrict.getValue() || !mc.player.onGround || glide.getValue())) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            serverSneaking = true;
        }
    }

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && gliding) {
            ((ICPacketPlayer) event.getPacket()).setOnGround(false);
        }
    }
}
