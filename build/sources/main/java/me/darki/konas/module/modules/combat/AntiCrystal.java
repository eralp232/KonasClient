package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.combat.CrystalUtils;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AntiCrystal extends Module {
    private static final Setting<Float> health = new Setting<>("Health", 10f, 36f, 0f, 0.1f);
    private static final Setting<Float> range = new Setting<>("Range", 4f, 10f, 1f, 0.1f);
    private static final Setting<Float> enemyRange = new Setting<>("EnemyRange", 10f, 20f, 1f, 0.1f);
    private static final Setting<Float> minDamage = new Setting<>("MinDamage", 5f, 10f, 1f, 0.1f);

    private static Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static Setting<Integer> actionShift = new Setting<>("ActionShift", 3, 8, 1, 1);
    private static Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 10, 0, 1);

    public AntiCrystal() {
        super("AntiCrystal", "Places pressure plates under crystals", Category.COMBAT);
    }

    private int tickCounter = 0;

    InteractionUtil.Placement placement = null;

    private List<Entity> validCrystals = new ArrayList<>();

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }

        placement = null;
        tickCounter = actionInterval.getValue();
        validCrystals.clear();
    }

    @Subscriber(priority = 99)
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent.Pre event) {
        validCrystals.clear();
        placement = null;

        int plateSlot = getPlateSlot();
        if (plateSlot == -1) {
            toggle();
            Logger.sendChatMessage("No pressure plates found");
            return;
        }

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally(rotate.getValue())) return;

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        if (tickCounter < actionInterval.getValue()) {
            return;
        }

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.getValue()) {
            return;
        }

        if (getNearestTarget() == null) return;

        validCrystals = mc.world.loadedEntityList.stream()
                .filter(e -> e instanceof EntityEnderCrystal)
                .filter(e -> mc.player.getPositionEyes(1F).distanceTo(e.getPositionVector()) <= range.getValue())
                .filter(e -> CrystalUtils.calculateDamage((EntityEnderCrystal) e, mc.player) >= minDamage.getValue())
                .filter(e -> InteractionUtil.canPlaceBlock(new BlockPos(e), false, false))
                .sorted(Comparator.comparing(e -> CrystalUtils.calculateDamage((EntityEnderCrystal) e, mc.player)))
                .collect(Collectors.toList());

        if (validCrystals.isEmpty()) return;

        if (plateSlot != mc.player.inventory.currentItem) {
            mc.player.inventory.currentItem = plateSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(plateSlot));
        }

        placement = InteractionUtil.preparePlacement(new BlockPos(validCrystals.get(0)), rotate.getValue());
    }

    @Subscriber(priority = 35)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (placement == null) return;
        boolean isSprinting = mc.player.isSprinting();
        boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(placement.getNeighbour());

        if (isSprinting) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
        }

        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }

        InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, true);

        int extraBlocks = 0;
        while (extraBlocks < actionShift.getValue() - 1 && extraBlocks + 1 < validCrystals.size()) {
            BlockPos nextPos = new BlockPos(validCrystals.get(extraBlocks + 1));
            extraBlocks++;
            if (nextPos != null && InteractionUtil.canPlaceBlock(nextPos, false, false)) {
                InteractionUtil.Placement nextPlacement = InteractionUtil.preparePlacement(nextPos, rotate.getValue(), true);
                if (nextPlacement != null) {
                    InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, true);
                }
            }
        }

        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }

        if (isSprinting) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

    private EntityPlayer getNearestTarget() {
        Stream<EntityPlayer> stream = mc.world.playerEntities.stream();
        return stream
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> e != mc.player)
                .filter(e -> !Friends.isUUIDFriend(e.getUniqueID().toString()))
                .filter(e -> mc.player.getDistance(e) <= enemyRange.getValue())
                .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                .orElse(null);
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
}
