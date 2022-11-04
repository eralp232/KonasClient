package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.StopUsingItemEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IMinecraft;
import me.darki.konas.module.Module;
import me.darki.konas.setting.BlockListSetting;
import me.darki.konas.setting.Setting;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;

public class FastUse extends Module {

    private static Setting<Boolean> fastXP = new Setting<>("FastXP", true);
    private static Setting<Boolean> ghostFix = new Setting<>("GhostFix", false);
    private static Setting<Boolean> strict = new Setting<>("Strict", false);
    private static Setting<Boolean> packetEat = new Setting<>("PacketEat", false);
    private static Setting<Boolean> fastBow = new Setting<>("FastBow", false);
    private static Setting<Boolean> bowBomb = new Setting<>("BowBomb", false).withVisibility(fastBow::getValue);
    private static Setting<Boolean> fastPlace = new Setting<>("FastPlace", false);
    private static Setting<Boolean> noCrystalPlace = new Setting<>("NoCrystalPlace", false);
    private static Setting<Boolean> fastPlaceWhitelist = new Setting<>("PlaceWhitelist", false);
    public static Setting<BlockListSetting> whitelist = new Setting<>("Whitelist", new BlockListSetting());

    public FastUse() {
        super("FastUse", "Removes item use delay", Category.PLAYER, "FastXP", "FastBow", "FastPlace");
    }

    @Subscriber
    public void onStopUsingItem(StopUsingItemEvent event) {
        if (mc.player.getHeldItem(mc.player.getActiveHand()).getItem() instanceof ItemFood && packetEat.getValue()) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (strict.getValue() && mc.player.ticksExisted % 2 == 0) return;

        if (fastBow.getValue()
                && mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow
                && mc.player.isHandActive()
                && mc.player.getItemInUseMaxCount() >= 3) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            if (bowBomb.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 0.1D, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 999D, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
            }
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
            mc.player.stopActiveHand();
            return;
        }

        if(shouldFastUse()) {
            if(((IMinecraft) mc).getRightClickDelayTimer() != 0) {
                ((IMinecraft) mc).setRightClickDelayTimer(0);
            }
        }
    }

    public static boolean ignore = false;

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if (ghostFix.getValue() && mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                event.cancel();
            }
        } else if (noCrystalPlace.getValue() && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            if (mc.player.getHeldItem(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getHand()).getItem() instanceof ItemEndCrystal) {
                if (ignore) {
                    ignore = false;
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean shouldFastUse() {

        Item main = mc.player.getHeldItemMainhand().getItem();
        Item off = mc.player.getHeldItemOffhand().getItem();

        if (fastXP.getValue()
                && (main instanceof ItemExpBottle || off instanceof ItemExpBottle)) {
            return true;
        }

        if (fastPlace.getValue()) {
            if (main instanceof ItemBlock) {
                if (whitelist.getValue().getBlocks().contains(((ItemBlock) main).getBlock()) || !fastPlaceWhitelist.getValue()) {
                    ((IMinecraft) mc).setRightClickDelayTimer(0);
                    return true;
                }
            }

            if (off instanceof ItemBlock) {
                if (whitelist.getValue().getBlocks().contains(((ItemBlock) off).getBlock()) || !fastPlaceWhitelist.getValue()) {
                    ((IMinecraft) mc).setRightClickDelayTimer(0);
                    return true;
                }
            }
        }

        if (main instanceof ItemFood) {
            ((IMinecraft) mc).setRightClickDelayTimer(0);
            return true;
        }

        return false;

    }

}
