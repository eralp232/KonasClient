package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.ICPacketPlayer;
import me.darki.konas.mixin.mixins.IGuiEditSign;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockSign;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static me.darki.konas.util.client.PlayerUtils.calculateLookAt;

public class BlockAura extends Module {

    private static Setting<Boolean> grass = new Setting<>("Grass", true);
    private static Setting<Boolean> autoSign = new Setting<>("AutoSign", true);
    private static Setting<Boolean> sourceRemover = new Setting<>("SourceRemover", true);
    private static Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true);
    private static Setting<Integer> delay = new Setting<>("PlaceDelay", 40, 100, 1, 1);
    private static Setting<Integer> tickDelay = new Setting<>("BreakDelay", 2, 20, 1, 1);
    private static Setting<Integer> rangeXZ = new Setting<>("RangeXZ", 3, 10, 1, 1);
    private static Setting<Integer> rangeY = new Setting<>("RangeY", 3, 10, 1, 1);
    private static Setting<Boolean> rotations = new Setting<>("Rotations", true);

    private ArrayList<BlockPos> blocksToDestroy = new ArrayList<>();
    private ArrayList<BlockPos> signs = new ArrayList<>();

    public static final ArrayList<String> LINES = new ArrayList<>();

    private BlockPos currentPos = null;
    private EnumFacing currentFace = null;

    private int itemSlot;

    private boolean isSpoofingAngles;
    private float yaw;
    private float pitch;

    Timer timer = new Timer();

    public BlockAura() {
        super("BlockAura", Category.MISC, "Nuker", "Lawnmower", "LiquidRemover");
        LINES.add("Signed by <player>");
        LINES.add("using Konas");
        LINES.add("<date>");
    }

    @Subscriber
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (!sourceRemover.getValue()) return;
        if (event instanceof UpdateWalkingPlayerEvent.Pre) {
            currentPos = null;
            currentFace = null;

            int slot = getBlockSlot();
            itemSlot = -1;

            Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-rangeXZ.getValue(), -rangeY.getValue(), -rangeXZ.getValue()), mc.player.getPosition().add(rangeXZ.getValue(), rangeY.getValue(), rangeXZ.getValue()));

            BlockPos pos = StreamSupport.stream(blocks.spliterator(), false)
                    .filter(e -> (mc.world.getBlockState(e).getMaterial() == Material.WATER || mc.world.getBlockState(e).getMaterial() == Material.LAVA))
                    .filter(e -> (mc.world.getBlockState(e).getBlock().getMetaFromState(mc.world.getBlockState(e)) == 0))
                    .min(Comparator.comparing(e -> MathHelper.sqrt(mc.player.getDistanceSq(e))))
                    .orElse(null);

            if (pos != null) {
                if (mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                    Optional<BlockUtils.ClickLocation> posCL = BlockUtils.generateClickLocation(pos);
                    itemSlot = slot;

                    if (posCL.isPresent()) {
                        currentPos = posCL.get().neighbour;
                        currentFace = posCL.get().opposite;

                        double[] yawPitch = BlockUtils.calculateLookAt(currentPos.getX(), currentPos.getY(), currentPos.getZ(), currentFace, mc.player);
                        yaw = (float) yawPitch[0];
                        pitch = (float) yawPitch[1];
                        isSpoofingAngles = true;
                        return;
                    }
                } else if (timer.hasPassed(delay.getValue() * 10)) {
                    isSpoofingAngles = false;
                    return;
                }
            }
        } else {
            if (currentPos != null && currentFace != null && timer.hasPassed(delay.getValue() * 2) && itemSlot != -1) {
                // Soooo clean
                boolean changeItem = mc.player.inventory.currentItem != itemSlot;
                boolean isSprinting = mc.player.isSprinting();
                int startingItem = mc.player.inventory.currentItem;
                boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(currentPos);

                if (changeItem) {
                    // Manually sending packets seems to work better than updateController() onUpdateWalkingPlayer
                    mc.player.inventory.currentItem = itemSlot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(itemSlot));
                }

                if (isSprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                if (shouldSneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }

                // Block utils has cringe right click method
                mc.playerController.processRightClickBlock(mc.player, mc.world, currentPos, currentFace,
                        new Vec3d(currentPos)
                                .add(0.5, 0.5, 0.5)
                                .add(new Vec3d(currentFace.getDirectionVec()).scale(0.5)),
                        EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

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

                timer.reset();
            }
        }
    }

    @Subscriber
    private void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof CPacketPlayer
                && isSpoofingAngles) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (event.getPacket() instanceof CPacketPlayer.Position) {
                event.setCancelled(true);
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(mc.player.posX), packet.getY(mc.player.posY), packet.getZ(mc.player.posZ), (float) yaw, (float) pitch, packet.isOnGround()));
            } else {
                ((ICPacketPlayer) packet).setYaw((float) yaw);
                ((ICPacketPlayer) packet).setPitch((float) pitch);
            }
        }
    }

    private int getBlockSlot() {
        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
            return mc.player.inventory.currentItem;
        } else {
            if (autoSwitch.getValue()) {
                for (int i = 0; i < 9; ++i) {
                    stack = mc.player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;
        blocksToDestroy.clear();
        Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-rangeXZ.getValue(), -rangeY.getValue(), -rangeXZ.getValue()), mc.player.getPosition().add(rangeXZ.getValue(), rangeY.getValue(), rangeXZ.getValue()));
        if (grass.getValue()) {
            blocksToDestroy = (ArrayList<BlockPos>) StreamSupport.stream(blocks.spliterator(), false)
                    .filter(e -> (mc.world.getBlockState(e).getMaterial() == Material.VINE || mc.world.getBlockState(e).getMaterial() == Material.PLANTS))
                    .filter(e -> !blocksToDestroy.contains(e))
                    .sorted(Comparator.comparing(e -> MathHelper.sqrt(mc.player.getDistanceSq(e))))
                    .collect(Collectors.toList());

            if (!blocksToDestroy.isEmpty() && mc.player.ticksExisted % tickDelay.getValue() == 0) {
                destroyBlock(blocksToDestroy.get(0));
            }
        }
        if (autoSign.getValue()) {
            signs = (ArrayList<BlockPos>) StreamSupport.stream(blocks.spliterator(), false)
                    .filter(e -> (mc.world.getBlockState(e).getBlock() instanceof BlockSign))
                    .filter(e -> !signs.contains(e))
                    .filter(e -> !isSigned(e))
                    .sorted(Comparator.comparing(e -> MathHelper.sqrt(mc.player.getDistanceSq(e))))
                    .collect(Collectors.toList());

            if(!signs.isEmpty() && mc.player.ticksExisted % tickDelay.getValue() == 0) {
                sign(signs.get(0));
            }
        }
    }

    @Subscriber
    public void onGuiLoad(LoadGuiEvent event) {
        if (mc.world == null || mc.player == null) return;
        if(event.getGui() instanceof GuiEditSign && autoSign.getValue()) {
            GuiEditSign gui = (GuiEditSign) event.getGui();
            TileEntitySign sign = ((IGuiEditSign) gui).getTileEntitySign();
            sign(sign.getPos());
            event.cancel();
        }
    }

    private void destroyBlock(BlockPos pos) {
        if (rotations.getValue()) lookAtPacket(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, mc.player);
        mc.playerController.clickBlock(pos, EnumFacing.UP);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        blocksToDestroy.remove(pos);
    }

    private void sign(BlockPos pos) {
        ITextComponent[] content = new ITextComponent[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};
        content[0] = new TextComponentString(LINES.get(0).replaceAll("<player>", mc.player.getName()).replaceAll("<date>", new SimpleDateFormat("dd/MM/yy").format(new Date())));
        content[1] = new TextComponentString(LINES.get(1).replaceAll("<player>", mc.player.getName()).replaceAll("<date>", new SimpleDateFormat("dd/MM/yy").format(new Date())));
        content[2] = new TextComponentString(LINES.get(2).replaceAll("<player>", mc.player.getName()).replaceAll("<date>", new SimpleDateFormat("dd/MM/yy").format(new Date())));
        content[3] = new TextComponentString(LINES.get(3).replaceAll("<player>", mc.player.getName()).replaceAll("<date>", new SimpleDateFormat("dd/MM/yy").format(new Date())));
        mc.player.connection.sendPacket(new CPacketUpdateSign(pos, content));
    }

    private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
        double[] v = calculateLookAt(px, py, pz, me);
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation((float) v[0], (float) v[1], false));
    }

    private boolean isSigned(BlockPos pos) {
        TileEntitySign sign = (TileEntitySign) mc.world.getTileEntity(pos);
        if(sign == null) return false;
        return sign.signText[0].getUnformattedText().equals(LINES.get(0)) &&
                sign.signText[1].getUnformattedText().equals(LINES.get(1)) &&
                sign.signText[2].getUnformattedText().equals(LINES.get(2)) &&
                sign.signText[3].getUnformattedText().equals(LINES.get(3));
    }

    public static boolean contains(Iterator<?> iterator, @Nullable Object element) {
        if (element == null) {
            while (iterator.hasNext()) {
                if (iterator.next() == null) {
                    return true;
                }
            }
        } else {
            while (iterator.hasNext()) {
                if (element.equals(iterator.next())) {
                    return true;
                }
            }
        }
        return false;
    }

}
