package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.exploit.PacketFly;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.render.BlockRenderUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;

public class AutoWeb extends Module {
    private static final Setting<Float> placeRange = new Setting<>("TargetRange", 4.5f, 16f, 1f, 0.1f);
    private static Setting<Integer> actionShift = new Setting<>("ActionShift", 2, 2, 1, 1);
    private static Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 10, 0, 1);

    private static final Setting<Boolean> top = new Setting<>("Top", true);
    private static final Setting<Boolean> self = new Setting<>("Self", false);
    private static Setting<Boolean> strict = new Setting<>("Strict", false);

    private static Setting<Boolean> rotate = new Setting<>("Rotate", true);

    private static Setting<Boolean> switchBack = new Setting<>("SwitchBack", false);

    private int itemSlot;
    Timer timer = new Timer();
    private BlockPos renderBlock;
    private int tickCounter = 0;
    private BlockPos playerPos = null;
    private InteractionUtil.Placement placement;
    private InteractionUtil.Placement lastPlacement;
    private Timer lastPlacementTimer = new Timer();

    public AutoWeb() {
        super("AutoWeb", Keyboard.KEY_NONE, Category.COMBAT, "WebAura", "AutoWebber");
    }

    @Override
    public void onEnable() {

        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }

        renderBlock = null;
        playerPos = null;
        placement = null;
        lastPlacement = null;
        tickCounter = actionInterval.getValue();
    }

    @Subscriber(priority = 71)
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent.Pre event) {
        if (placement != null) {
            lastPlacement = placement;
            lastPlacementTimer.reset();
        }
        playerPos = null;
        placement = null;

        if (timer.hasPassed(350)) {
            renderBlock = null;
        }

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally(rotate.getValue())) return;

        if (ModuleManager.getModuleByClass(PacketFly.class).isEnabled()) return;

        if (strict.getValue() && (!mc.player.onGround || !mc.player.collidedVertically)) return;

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        EntityPlayer nearestPlayer = getNearestTarget();

        if (nearestPlayer == null) {
            return;
        }

        if (tickCounter < actionInterval.getValue()) {
            if (lastPlacement != null && !lastPlacementTimer.hasPassed(650)) {
                KonasGlobals.INSTANCE.rotationManager.setRotations(lastPlacement.getYaw(), lastPlacement.getPitch());
            }
            return;
        }

        playerPos = new BlockPos(nearestPlayer.posX, nearestPlayer.posY, nearestPlayer.posZ);

        int slot = getBlockSlot();
        if (slot == -1) {
            Logger.sendChatErrorMessage("No Webs Found!");
            toggle();
            return;
        }
        itemSlot = slot;

        if (InteractionUtil.canPlaceBlock(playerPos, false, false)) {
            placement = InteractionUtil.preparePlacement(playerPos, rotate.getValue());
        } else if (top.getValue() && InteractionUtil.canPlaceBlock(playerPos.up(), false, false)) {
            placement = InteractionUtil.preparePlacement(playerPos.up(), rotate.getValue());
        }

        if (placement != null) {
            tickCounter = 0;
            renderBlock = playerPos;
            timer.reset();
        }
    }

    @Subscriber(priority = 12)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (placement != null && playerPos != null && itemSlot != -1) {
            boolean changeItem = mc.player.inventory.currentItem != itemSlot;
            int startingItem = mc.player.inventory.currentItem;

            if (changeItem) {
                mc.player.inventory.currentItem = itemSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(itemSlot));
            }

            boolean isSprinting = mc.player.isSprinting();
            boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(placement.getNeighbour());

            if (isSprinting) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            if (shouldSneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }

            InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, false);

            if (actionShift.getValue() == 2 && top.getValue() && InteractionUtil.canPlaceBlock(playerPos.up(), false, false)) {
                placement = InteractionUtil.preparePlacement(playerPos.up(), rotate.getValue(), true);
                if (placement != null) {
                    renderBlock = playerPos;
                    timer.reset();
                    InteractionUtil.placeBlock(placement, EnumHand.MAIN_HAND, false);
                }
            }

            if (shouldSneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            if (isSprinting) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }

            if (changeItem && switchBack.getValue()) {
                mc.player.inventory.currentItem = startingItem;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(startingItem));
            }
        }
    }

    @Subscriber
    public void onRender(Render3DEvent event) {

        if (mc.world == null || mc.player == null) {
            return;
        }

        if (renderBlock != null) {
            BlockRenderUtil.renderPlacementPos(renderBlock);
        }

    }

    private int getBlockSlot(){
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block instanceof BlockWeb) {
                    slot = i;
                    break;
                }
            }
        }

        return slot;
    }

    private EntityPlayer getNearestTarget() {
        return mc.world.playerEntities
                .stream()
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> e != mc.player && e != mc.getRenderViewEntity())
                .filter(e -> !e.isDead)
                .filter(e -> !Friends.isUUIDFriend(e.getUniqueID().toString()))
                .filter(e -> e.getHealth() > 0)
                .filter(e -> mc.player.getDistance(e) < Math.max(placeRange.getValue() - 1.0F, 1.0F))
                .filter(this::isValidBase)
                .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                .orElse(self.getValue() ? mc.player : null);
    }

    private boolean isValidBase(EntityPlayer player) {
        BlockPos basePos = new BlockPos(player.posX, player.posY, player.posZ).down();

        Block baseBlock = mc.world.getBlockState(basePos).getBlock();

        if (baseBlock instanceof BlockAir || baseBlock instanceof BlockLiquid) {
            return false;
        }

        return true;
    }
}
