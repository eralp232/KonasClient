package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.exploit.PacketFly;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import net.minecraft.block.*;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.stream.Stream;

public class AutoAnvil extends Module {
    public static Setting<Boolean> pressurePlates = new Setting<>("PressurePlates", true);
    public static Setting<Integer> minHeight = new Setting<>("MinHeight", 2, 5, 2, 1);
    public static Setting<Integer> heightRange = new Setting<>("Range", 2, 5, 1, 1);
    public static Setting<Integer> delay = new Setting<>("Interval", 0, 20, 0, 1);
    public static Setting<Double> range = new Setting<>("Range", 4D, 6D, 1D, 0.1D);

    private int tickCounter = 0;
    private InteractionUtil.Placement postPlacement = null;
    private int itemSlot = -1;

    public void onEnable() {
        postPlacement = null;
        itemSlot = -1;
        tickCounter = delay.getValue();
    }

    public AutoAnvil() {
        super("AutoAnvil", "Automatically places anvils above people's heads to break their helmet", Category.COMBAT);
    }

    @Subscriber(priority = 40)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        postPlacement = null;
        itemSlot = -1;

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;

        if (ModuleManager.getModuleByClass(PacketFly.class).isEnabled()) return;

        if (tickCounter < delay.getValue()) {
            tickCounter++;
        }

        if (tickCounter < delay.getValue()) {
            return;
        }

        EntityPlayer target = getNearestTarget();
        if (target == null) return;

        if (pressurePlates.getValue() && !(mc.world.getBlockState(new BlockPos(target)).getBlock() instanceof BlockPressurePlate)) {
            postPlacement = InteractionUtil.preparePlacement(new BlockPos(target), true);
            if (postPlacement != null) {
                int plateSlot = getPlateSlot();
                if (plateSlot == -1) {
                    postPlacement = null;
                    toggle();
                    Logger.sendChatMessage("No pressure plates found!");
                    return;
                } else {
                    itemSlot = plateSlot;
                    return;
                }
            }
        }

        for (int i = 0; i < heightRange.getValue(); i++) {
            postPlacement = InteractionUtil.preparePlacement(new BlockPos(target).up(minHeight.getValue() + i), true);
            if (postPlacement != null) {
                int anvil = getAnvilSlot();
                if (anvil == -1) {
                    postPlacement = null;
                    toggle();
                    Logger.sendChatMessage("No anvils found!");
                    return;
                } else {
                    itemSlot = anvil;
                    return;
                }
            }
        }
    }

    @Subscriber(priority = 8)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Post event) {
        if (itemSlot != -1 && postPlacement != null) {
            boolean changeItem = mc.player.inventory.currentItem != itemSlot;
            int startingItem = mc.player.inventory.currentItem;

            if (changeItem) {
                mc.player.inventory.currentItem = itemSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(itemSlot));
            }

            InteractionUtil.placeBlockSafely(postPlacement, EnumHand.MAIN_HAND,true);

            tickCounter = 0;

            if (changeItem) {
                mc.player.inventory.currentItem = startingItem;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(startingItem));
            }
        }
    }

    @Subscriber
    public void onDisplayGuiScreen(LoadGuiEvent event) {
        if (event.getGui() instanceof GuiRepair) {
            event.setCancelled(true);
        }
    }

    private EntityPlayer getNearestTarget() {
        Stream<EntityPlayer> stream = mc.world.playerEntities.stream();
        return stream
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> e != mc.player && e != mc.getRenderViewEntity())
                .filter(e -> !Friends.isUUIDFriend(e.getUniqueID().toString()))
                .filter(e -> mc.player.getDistance(e) < range.getValue())
                .filter(this::isValidBase)
                .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                .orElse(null);
    }

    private boolean isValidBase(EntityPlayer player) {
        BlockPos basePos = new BlockPos(player.posX, player.posY, player.posZ).down();

        Block baseBlock = mc.world.getBlockState(basePos).getBlock();

        return !(baseBlock instanceof BlockAir) && !(baseBlock instanceof BlockLiquid);
    }

    public int getPlateSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            Block block = Block.getBlockFromItem(item);
            if (block instanceof BlockPressurePlate) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    public int getAnvilSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            Block block = Block.getBlockFromItem(item);
            if (block instanceof BlockAnvil) {
                slot = i;
                break;
            }
        }
        return slot;
    }
}
