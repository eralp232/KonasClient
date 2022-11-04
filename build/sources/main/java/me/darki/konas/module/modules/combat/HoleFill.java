package me.darki.konas.module.modules.combat;

import com.google.common.collect.Lists;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.exploit.PacketFly;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.combat.CrystalUtils;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.render.BlockRenderUtil;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class HoleFill extends Module {
    private static Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static Setting<Boolean> swing = new Setting<>("Swing", true);

    private static Setting<Double> rangeXZ = new Setting<>("Range", 5D, 6D, 1D, 0.1D);
    private static Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false);

    private static Setting<Integer> actionShift = new Setting<>("ActionShift", 1, 3, 1, 1);
    private static Setting<Integer> actionInterval = new Setting<>("ActionInterval", 0, 5, 0, 1);

    private static Setting<Boolean> rayTrace = new Setting<>("RayTrace", false);

    private static Setting<Boolean> doubleHoles = new Setting<>("Double", false);

    private static Setting<Boolean> jumpDisable = new Setting<>("JumpDisable", false);
    private static Setting<Boolean> onlyWebs = new Setting<>("OnlyWebs", false);
    private static Setting<SmartMode> smartMode = new Setting<>("Smart", SmartMode.ALWAYS);
    private static Setting<Double> targetRange = new Setting<>("EnemyRange", 10D, 15D, 1D, 0.5D);
    private static Setting<Boolean> disableWhenNone = new Setting<>("DisableWhenNone", false);

    private enum SmartMode {
        NONE, ALWAYS, TARGET
    }

    public HoleFill() {
        super("HoleFill", Category.COMBAT);
    }

    private Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();

    private InteractionUtil.Placement placement = null;

    private int itemSlot;
    
    ArrayList<BlockPos> blocks;

    private Map<BlockPos, Long> placedBlocks = new ConcurrentHashMap<>();

    private int tickCounter = 0;

    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }
        blocks = Lists.newArrayList(BlockPos.getAllInBox(mc.player.getPosition().add(-rangeXZ.getValue(), -rangeXZ.getValue(), -rangeXZ.getValue()), mc.player.getPosition().add(rangeXZ.getValue(), rangeXZ.getValue(), rangeXZ.getValue())));
        tickCounter = actionInterval.getValue();
    }

    @Subscriber(priority = 60)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        placement = null;

        if (jumpDisable.getValue() && mc.player.prevPosY < mc.player.posY) {
            this.toggle();
        }

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally(rotate.getValue())) return;

        if (ModuleManager.getModuleByClass(PacketFly.class).isEnabled()) return;

        if (tickCounter < actionInterval.getValue()) {
            tickCounter++;
        }

        if (tickCounter < actionInterval.getValue()) {
            return;
        }

        int slot = getBlockSlot();
        itemSlot = -1;

        if (slot == -1) {
            return;
        }

        blocks = Lists.newArrayList(BlockPos.getAllInBox(mc.player.getPosition().add(-rangeXZ.getValue(), -rangeXZ.getValue(), -rangeXZ.getValue()), mc.player.getPosition().add(rangeXZ.getValue(), rangeXZ.getValue(), rangeXZ.getValue())));

        int ping = CrystalUtils.ping();

        placedBlocks.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > ping + 100) {
                placedBlocks.remove(pos);
            }
        });

        if (smartMode.getValue() == SmartMode.TARGET && getNearestTarget() == null) return;

        BlockPos pos = StreamSupport.stream(blocks.spliterator(), false)
                .filter(this::isHole)
                .filter(p -> mc.player.getDistance(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) <= rangeXZ.getValue())
                .filter(p -> InteractionUtil.canPlaceBlock(p, strictDirection.getValue(), rayTrace.getValue(), true))
                .min(Comparator.comparing(e -> MathHelper.sqrt(mc.player.getDistanceSq(e))))
                .orElse(null);

        if (pos != null) {
            placement = InteractionUtil.preparePlacement(pos, rotate.getValue(), false, strictDirection.getValue(), rayTrace.getValue());
            if (placement != null) {
                tickCounter = 0;
                itemSlot = slot;
                renderBlocks.put(pos, System.currentTimeMillis());
                placedBlocks.put(pos, System.currentTimeMillis());
            }
        } else if (disableWhenNone.getValue()) {
            toggle();
        }
    }

    @Subscriber(priority = 15)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (placement != null && itemSlot != -1) {
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

            Vec3d hitVec = new Vec3d(placement.getNeighbour()).add(0.5, 0.5, 0.5).add(new Vec3d(placement.getOpposite().getDirectionVec()).scale(0.5));
            InteractionUtil.rightClickBlock(placement.getNeighbour(), hitVec, EnumHand.MAIN_HAND, placement.getOpposite(), true, swing.getValue());

            double dX = mc.player.posX - ((IEntityPlayerSP) mc.player).getLastReportedPosX();
            double dY = mc.player.posY - ((IEntityPlayerSP) mc.player).getLastReportedPosY();
            double dZ = mc.player.posZ - ((IEntityPlayerSP) mc.player).getLastReportedPosZ();

            boolean positionChanged = dX * dX + dY * dY + dZ * dZ > 9.0E-4D;

            int extraBlocks = 0;
            while (extraBlocks < actionShift.getValue() - 1 && !positionChanged) {
                EntityPlayer nearestTarget = getNearestTarget();
                BlockPos pos = StreamSupport.stream(blocks.spliterator(), false)
                        .filter(this::isHole)
                        .min(Comparator.comparing(e -> (smartMode.getValue() != SmartMode.NONE) && nearestTarget != null ? MathHelper.sqrt(mc.player.getDistanceSq(nearestTarget)) : MathHelper.sqrt(mc.player.getDistanceSq(e))))
                        .orElse(null);
                if (pos != null && InteractionUtil.canPlaceBlock(pos, strictDirection.getValue())) {
                    InteractionUtil.Placement nextPlacement = InteractionUtil.preparePlacement(pos, rotate.getValue(), true, strictDirection.getValue());
                    if (nextPlacement != null) {
                        Vec3d nextHitVec = new Vec3d(nextPlacement.getNeighbour()).add(0.5, 0.5, 0.5).add(new Vec3d(nextPlacement.getOpposite().getDirectionVec()).scale(0.5));
                        InteractionUtil.rightClickBlock(nextPlacement.getNeighbour(), nextHitVec, EnumHand.MAIN_HAND, nextPlacement.getOpposite(), true, swing.getValue());
                        placedBlocks.put(pos, System.currentTimeMillis());
                        renderBlocks.put(pos, System.currentTimeMillis());
                        extraBlocks++;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            if (shouldSneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            if (isSprinting) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }

            if (changeItem) {
                mc.player.inventory.currentItem = startingItem;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(startingItem));
            }
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketBlockChange) {
            if (renderBlocks.containsKey(((SPacketBlockChange) event.getPacket()).getBlockPosition())) {
                if (((SPacketBlockChange) event.getPacket()).getBlockState().getBlock() != Blocks.AIR) {
                    renderBlocks.remove(((SPacketBlockChange) event.getPacket()).getBlockPosition());
                }
            }
        }
    }

    @Subscriber
    public void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        renderBlocks.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 1000) {
                renderBlocks.remove(pos);
            } else {
                BlockRenderUtil.renderPlacementPos(pos);
            }
        });
    }

    private boolean isValidItem(Item item) {
        if (item instanceof ItemBlock) {
            if (onlyWebs.getValue()) {
                return ((ItemBlock) item).getBlock() instanceof BlockWeb;
            }
            return true;
        }
        return false;
    }

    private int getBlockSlot() {
        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!stack.isEmpty() && isValidItem(stack.getItem())) {
            return mc.player.inventory.currentItem;
        } else {
            for (int i = 0; i < 9; ++i) {
                stack = mc.player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && isValidItem(stack.getItem())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isHole(BlockPos pos) {
        if (placedBlocks.containsKey(pos)) return false;
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityArrow)) {
                return false;
            }
        }
        if (doubleHoles.getValue()) {
            BlockPos twoPos = BlockUtils.validTwoBlockBedrockXZ(pos);
            if (twoPos == null) {
                twoPos = BlockUtils.validTwoBlockObiXZ(pos);
            }
            if (twoPos != null) {
                return true;
            }
        }
        return BlockUtils.isHole(pos);
    }

    private EntityPlayer getNearestTarget() {
        return mc.world.playerEntities.stream()
                .filter(e -> e != mc.player)
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> !Friends.isUUIDFriend(e.getUniqueID().toString()))
                .filter(e -> mc.player.getDistance(e) < targetRange.getValue())
                .min(Comparator.comparing(e -> mc.player.getDistance(e)))
                .orElse(null);
    }
    
}
