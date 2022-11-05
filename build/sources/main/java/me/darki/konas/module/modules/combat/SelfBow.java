package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.StopUsingItemEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;

public class SelfBow extends Module {
    public static final Setting<Boolean> speed = new Setting<>("Swiftness", false);
    public static final Setting<Boolean> strength = new Setting<>("Strength", false);
    public static final Setting<Boolean> toggelable = new Setting<>("Toggelable", false);
    public static final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", false);
    public static final Setting<Boolean> rearrange = new Setting<>("Rearrange", false);
    public static final Setting<Boolean> noGapSwitch = new Setting<>("NoGapSwitch", false);
    public static final Setting<Integer> health = new Setting<>("MinHealth", 20, 36, 0, 1);

    private Timer timer = new Timer();

    private boolean cancelStopUsingItem = false;

    public SelfBow() {
        super("SelfBow", "Shoots yourself", Category.COMBAT);
    }

    @Subscriber(priority = 98)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;

        if (!timer.hasPassed(2500)) return;

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < health.getValue()) return;

        if (noGapSwitch.getValue() && mc.player.getActiveItemStack().getItem() instanceof ItemFood) return;

        if (strength.getValue() && !mc.player.isPotionActive(MobEffects.STRENGTH)) {
            if (isFirstAmmoValid("Arrow of Strength")) {
                shootBow(event);
            } else if (toggelable.getValue()) {
                toggle();
            }
        }

        if (speed.getValue() && !mc.player.isPotionActive(MobEffects.SPEED)) {
            if (isFirstAmmoValid("Arrow of Swiftness")) {
                shootBow(event);
            } else if (toggelable.getValue()) {
                toggle();
            }
        }
    }

    @Subscriber
    public void onStopUsingItem(StopUsingItemEvent event) {
        if (cancelStopUsingItem) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnable() {
        cancelStopUsingItem = false;
    }

    private void shootBow(UpdateWalkingPlayerEvent.Pre event) {
        if (mc.player.inventory.getCurrentItem().getItem() == Items.BOW) {
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, -90, mc.player.onGround));
            ((IEntityPlayerSP) mc.player).setLastReportedYaw(0);
            ((IEntityPlayerSP) mc.player).setLastReportedPitch(-90);
            if (mc.player.getItemInUseMaxCount() >= 3) {
                cancelStopUsingItem = false;
                mc.playerController.onStoppedUsingItem(mc.player);
                if (toggelable.getValue()) {
                    toggle();
                }
                timer.reset();
            } else if (mc.player.getItemInUseMaxCount() == 0) {
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                cancelStopUsingItem = true;
            }
        } else if (autoSwitch.getValue()) {
            int bowSlot = getBowSlot();
            if (bowSlot != -1 && bowSlot != mc.player.inventory.currentItem) {
                mc.player.inventory.currentItem = bowSlot;
                mc.playerController.updateController();
            }
        }
    }

    public int getBowSlot() {
        int bowSlot = -1;

        if (mc.player.getHeldItemMainhand().getItem() == Items.BOW) {
            bowSlot = Module.mc.player.inventory.currentItem;
        }


        if (bowSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.BOW) {
                    bowSlot = l;
                    break;
                }
            }
        }

        return bowSlot;
    }

    private boolean isFirstAmmoValid(String type) {
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TIPPED_ARROW) {
                boolean matches = itemStack.getDisplayName().equalsIgnoreCase(type);
                if (matches) {
                    return true;
                } else if (rearrange.getValue()) {
                    return rearrangeArrow(i, type);
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean rearrangeArrow(int fakeSlot, String type){
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TIPPED_ARROW) {
                if (itemStack.getDisplayName().equalsIgnoreCase(type)) {
                    mc.playerController.windowClick(0, fakeSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, fakeSlot, 0, ClickType.PICKUP, mc.player);
                    return true;
                }
            }
        }
        return false;
    }
}
