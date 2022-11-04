package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.exploit.PacketFly;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.setting.SubBind;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.interaction.RotationManager;
import me.darki.konas.util.render.BlockRenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import org.lwjgl.input.Keyboard;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Surround extends Module {
    private static final Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL);
    private Setting<SubBind> forceDouble = new Setting<>("Double", new SubBind(Keyboard.KEY_LMENU)).withVisibility(() -> mode.getValue() != Mode.DOUBLE);
    private static final Setting<Boolean> onlyWhenSneak = new Setting<>("OnlyWhenSneak", false);
    private Setting<Boolean> eChest = new Setting<>("EChests", false);
    private Setting<Boolean> predict = new Setting<>("Predict", false);

    private static final Setting<Parent> antiCheat = new Setting<>("AntiCheat", new Parent(false));
    private static final Setting<Boolean> rotate = new Setting<>("Rotate", true).withParent(antiCheat);
    private static final Setting<Boolean> swing = new Setting<>("Swing", true).withParent(antiCheat);
    private Setting<Boolean> strict = new Setting<>("Strict", false).withParent(antiCheat);
    private static final Setting<Integer> actionShift = new Setting<>("ActionShift", 3, 3, 1, 1).withParent(antiCheat);
    private static final Setting<Integer> tickDelay = new Setting<>("ActionInterval", 0, 5, 0, 1).withParent(antiCheat);
    private static final Setting<Boolean> forceNoDesync = new Setting<>("ForceNoDesync", false).withParent(antiCheat);

    private static final Setting<Parent> triggers = new Setting<>("AutoDisable", new Parent(false));
    private static final Setting<Boolean> triggerable = new Setting<>("Triggerable", false).withParent(triggers);
    public static final Setting<Boolean> disableOnJump = new Setting<>("DisableOnJump", false).withParent(triggers);
    private static final Setting<Boolean> tpDisable = new Setting<>("TPDisable", true).withParent(triggers);

    private static final Setting<Parent> extra = new Setting<>("Movement", new Parent(false));
    private static final Setting<Boolean> anchor = new Setting<>("Anchor", false).withParent(extra);
    private static final Setting<Boolean> autoCenter = new Setting<>("AutoCenter", true).withParent(extra);
    private static final Setting<Boolean> disableStrafe = new Setting<>("DisableStrafe", false).withParent(extra);

    private enum Mode {
        NORMAL, DOUBLE, ANTIPA, ANTICITY
    }

    private static final Vec3d[] STRICT = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1)
    };

    private static final Vec3d[] NORMAL = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(1, -1, 0),
            new Vec3d(0, -1, 1),
            new Vec3d(-1, -1, 0),
            new Vec3d(0, -1, -1),
            new Vec3d(0, -1, 0)
    };

    private static final Vec3d[] DOUBLE = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(1, 1, 0),
            new Vec3d(0, 1, 1),
            new Vec3d(-1, 1, 0),
            new Vec3d(0, 1, -1),
            new Vec3d(1, -1, 0),
            new Vec3d(0, -1, 1),
            new Vec3d(-1, -1, 0),
            new Vec3d(0, -1, -1),
            new Vec3d(0, -1, 0)
    };

    private static final Vec3d[] DOUBLESTRICT = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(1, 1, 0),
            new Vec3d(0, 1, 1),
            new Vec3d(-1, 1, 0),
            new Vec3d(0, 1, -1)
    };

    private static final Vec3d[] ANTIPA = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(2, 1, 0),
            new Vec3d(0, 1, 2),
            new Vec3d(-2, 1, 0),
            new Vec3d(0, 1, -2),
            new Vec3d(1, -1, 0),
            new Vec3d(0, -1, 1),
            new Vec3d(-1, -1, 0),
            new Vec3d(0, -1, -1),
            new Vec3d(0, -1, 0)
    };

    private static final Vec3d[] ANTICITY = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
            new Vec3d(2, 0, 0),
            new Vec3d(0, 0, 2),
            new Vec3d(-2, 0, 0),
            new Vec3d(0, 0, -2),
            new Vec3d(1, -1, 0),
            new Vec3d(0, -1, 1),
            new Vec3d(-1, -1, 0),
            new Vec3d(0, -1, -1)
    };


    public Surround() {
        super("Surround", Keyboard.KEY_NONE, Category.COMBAT, "AutoObsidian");
    }

    private int offsetStep = 0;
    private int delayStep = 0;

    private int startingSlot = -1;
    private int prevSlot = -1;

    private int totalTicksRunning = 0;
    private boolean firstRun;

    private ConcurrentHashMap<BlockPos, Long> renderPoses = new ConcurrentHashMap<>();

    public void onEnable() {

        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }

        if (disableStrafe.getValue()) {
            Module strafe = ModuleManager.getModuleByName("Speed");
            if (strafe != null) {
                if (strafe.isEnabled()) {
                    strafe.toggle();
                }
            }
        }

        // auto center
        if (autoCenter.getValue()) {
            PlayerUtils.moveToBlockCenter();
        }

        if (anchor.getValue()) {
            mc.player.setVelocity(0, mc.player.motionY, 0);
        }

        firstRun = true;
        startingSlot = mc.player.inventory.currentItem;
        prevSlot = -1;
    }

    @Override
    public String getExtraInfo() {
        return mode.getValue().toString().charAt(0) + mode.getValue().toString().substring(1).toLowerCase();
    }

    public void onDisable() {
        if (mc.player == null || mc.world == null) return;

        if (prevSlot != startingSlot && startingSlot != -1) {
            mc.player.inventory.currentItem = startingSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(startingSlot));
        }

        startingSlot = -1;
        prevSlot = -1;
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        startingSlot = mc.player.inventory.currentItem;

        if (ModuleManager.getModuleByName("Blink").isEnabled()) {
            return;
        }

        if (ModuleManager.getModuleByClass(PacketFly.class).isEnabled()) return;

        if (disableOnJump.getValue() && mc.player.posY > mc.player.prevPosY) {
            this.toggle();
            return;
        }

        if (onlyWhenSneak.getValue() && !mc.gameSettings.keyBindSneak.isKeyDown()) return;

        if (triggerable.getValue() && totalTicksRunning >= 50) {
            totalTicksRunning = 0;
            toggle();
            return;
        }

        if (!firstRun) {
            if (delayStep < tickDelay.getValue()) {
                delayStep++;
                return;
            } else {
                delayStep = 0;
            }
        }

        if (firstRun) {
            firstRun = false;
            if (findObiInHotbar() == -1) {
                toggle();
                return;
            }
        }

        Vec3d[] offsetPattern = new Vec3d[0];
        int maxSteps = 0;

        if (mode.getValue().equals(Mode.NORMAL)) {
            if (strict.getValue()) {
                offsetPattern = STRICT;
                maxSteps = STRICT.length;
            } else {
                offsetPattern = NORMAL;
                maxSteps = NORMAL.length;
            }
        }

        if (mode.getValue().equals(Mode.ANTIPA)) {
            offsetPattern = ANTIPA;
            maxSteps = ANTIPA.length;
        }

        if (mode.getValue().equals(Mode.ANTICITY)) {
            offsetPattern = ANTICITY;
            maxSteps = ANTICITY.length;
        }

        if (mode.getValue().equals(Mode.DOUBLE) || (PlayerUtils.isKeyDown(forceDouble.getValue().getKeyCode()) && !(mc.currentScreen instanceof ClickGUI))) {
            if (strict.getValue()) {
                offsetPattern = DOUBLESTRICT;
                maxSteps = DOUBLESTRICT.length;
            } else {
                offsetPattern = DOUBLE;
                maxSteps = DOUBLE.length;
            }
        }

        int blocksPlaced = 0;

        while (blocksPlaced < actionShift.getValue()) {
            if (offsetStep >= maxSteps) {
                offsetStep = 0;
                break;
            }

            BlockPos offsetPos = new BlockPos(offsetPattern[offsetStep]);
            BlockPos targetPos = new BlockPos(mc.player.getPositionVector()).add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
            if(mc.world.getBlockState(new BlockPos(mc.player.getPositionVector())).getBoundingBox(mc.world, new BlockPos(mc.player.getPositionVector())).maxY < 1) {
                targetPos = targetPos.up();
            }

            if (placeBlock(targetPos)) {
                blocksPlaced++;
            }

            offsetStep++;
        }

        if (mc.player.inventory.currentItem != startingSlot && startingSlot != -1) {
            mc.player.inventory.currentItem = startingSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(startingSlot));
            prevSlot = startingSlot;
        }

        totalTicksRunning++;
    }

    private boolean placeBlock(BlockPos pos) {
        if (mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            Optional<BlockUtils.ClickLocation> posCL = BlockUtils.generateClickLocation(pos, false, false, predict.getValue());

            if (posCL.isPresent()) {
                renderPoses.put(pos, System.currentTimeMillis());

                int obiSlot = findObiInHotbar();

                if (obiSlot == -1) {
                    return false;
                }

                if (prevSlot != obiSlot) {
                    mc.player.inventory.currentItem = obiSlot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(obiSlot));
                    prevSlot = obiSlot;
                }

                BlockPos currentPos = posCL.get().neighbour;
                EnumFacing currentFace = posCL.get().opposite;

                boolean isSprinting = mc.player.isSprinting();
                boolean shouldSneak = BlockUtils.shouldSneakWhileRightClicking(currentPos);

                if (isSprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                if (shouldSneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }

                Vec3d hitVec = new Vec3d(currentPos)
                        .add(0.5, 0.5, 0.5)
                        .add(new Vec3d(currentFace.getDirectionVec()).scale(0.5));

                if (rotate.getValue()) {
                    float[] rots = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), hitVec);
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rots[0], rots[1], mc.player.onGround));
                    ((IEntityPlayerSP) mc.player).setLastReportedYaw(rots[0]);
                    ((IEntityPlayerSP) mc.player).setLastReportedPitch(rots[1]);
                }

                mc.playerController.processRightClickBlock(mc.player, mc.world, currentPos, currentFace,
                        hitVec, EnumHand.MAIN_HAND);


                if (swing.getValue()) {
                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                }

                if (forceNoDesync.getValue() && !mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                }

                if (shouldSneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }

                if (isSprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }
            }
        }
        return false;
    }

    @Subscriber
    public void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        renderPoses.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 350) {
                renderPoses.remove(pos);
            } else {
                BlockRenderUtil.renderPlacementPos(pos);
            }
        });
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (tpDisable.getValue()) {
                toggle();
            }
        }
    }

    private int findObiInHotbar() {
        int slot = -1;
        if (eChest.getValue()) {
            for (int i = 0; i < 9; i++) {
                final ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                    final Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (block instanceof BlockEnderChest) {
                        slot = i;
                        return slot;
                    }
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block instanceof BlockObsidian) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }
}