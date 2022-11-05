package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PlayerMoveEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.movement.Sprint;
import me.darki.konas.setting.BlockListSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.interaction.RotationManager;
import me.darki.konas.util.render.BlockRenderUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.Block;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class Scaffold extends Module {
    private List<Block> invalid = Arrays.asList(Blocks.ANVIL, Blocks.AIR, Blocks.WEB, Blocks.WATER, Blocks.FIRE, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_WATER, Blocks.CHEST, Blocks.ENCHANTING_TABLE, Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST, Blocks.GRAVEL, Blocks.LADDER, Blocks.VINE, Blocks.BEACON, Blocks.JUKEBOX, Blocks.ACACIA_DOOR, Blocks.BIRCH_DOOR, Blocks.DARK_OAK_DOOR, Blocks.IRON_DOOR,
            Blocks.JUNGLE_DOOR, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.IRON_TRAPDOOR, Blocks.TRAPDOOR, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX);

    private Timer timerMotion = new Timer();

    private BlockData blockData;

    public static Setting<BlockListSetting> customBlocks = new Setting<>("CustomBlocks", new BlockListSetting());

    private static Setting<FilterMode> filterMode = new Setting<>("Filter", FilterMode.NONE);
    private static Setting<Double> expand = new Setting<>("Expand", 1D, 6D, 0.0D, 0.1D);
    private static Setting<Double> delay = new Setting<>("Delay", 3.5D, 10D, 1D, 0.5D);
    private static Setting<Boolean> Switch = new Setting<>("Switch", true);
    private static Setting<TowerMode> tower = new Setting<>("Tower", TowerMode.NORMAL);
    private static Setting<Boolean> center = new Setting<>("Center", true);
    private static Setting<Boolean> safe = new Setting<>("Safe", true);
    private static Setting<Boolean> keepY = new Setting<>("KeepY", true);
    private static Setting<Boolean> sprint = new Setting<>("Sprint", true);
    private static Setting<Boolean> down = new Setting<>("Down", true);
    private static Setting<Boolean> swing = new Setting<>("Swing", false);

    private enum FilterMode {
        NONE, WHITELIST, BLACKLIST
    }

    private enum TowerMode {
        NONE, NORMAL, FAST
    }

    private int lastY;

    private Timer lastTimer = new Timer();
    private float lastYaw;
    private float lastPitch;

    private BlockPos pos;

    private boolean teleported;

    public Scaffold() {
        super("Scaffold", Category.MISC);
    }

    private boolean isValid(Block block) {
        if (invalid.contains(block)) return false;

        if (filterMode.getValue() == FilterMode.BLACKLIST) {
            if (customBlocks.getValue().getBlocks().contains(block)) return false;
        } else if (filterMode.getValue() == FilterMode.WHITELIST) {
            if (!customBlocks.getValue().getBlocks().contains(block)) return false;
        }

        return true;
    }

    public void onEnable() {
        if (mc.world != null) {
            this.timerMotion.reset();
            this.lastY = MathHelper.floor(mc.player.posY);
        }
    }

    public void onDisable() {
        KonasGlobals.INSTANCE.timerManager.resetTimer(this);
    }

    @Subscriber
    public void onRender3D(Render3DEvent event) {
        if (blockData != null && this.blockData.position != null) {
            BlockRenderUtil.renderPlacementPos(blockData.position);
        }
    }

    @Subscriber(priority = 3)
    public void onUpdateLessPriorty(UpdateWalkingPlayerEvent.Pre event) {
        if (!lastTimer.hasPassed(100D * delay.getValue()) && InteractionUtil.canPlaceNormally()) {
            KonasGlobals.INSTANCE.rotationManager.setRotations((float) lastYaw, (float) lastPitch);
        }
    }

    @Subscriber(priority = 11)
    public void onUpdate(UpdateWalkingPlayerEvent event) {
        int downDistance;
        if (!ModuleManager.getModuleByClass(Sprint.class).isEnabled() && ((
                down.getValue() && mc.gameSettings.keyBindSneak.isKeyDown()) || !sprint.getValue()))
            mc.player.setSprinting(false);
        if (down.getValue() && Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            downDistance = 2;
        } else {
            downDistance = 1;
        }

        if (keepY.getValue()) {
            if ((!PlayerUtils.isPlayerMoving() && mc.gameSettings.keyBindJump.isKeyDown()) || mc.player.collidedVertically || mc.player.onGround)
                this.lastY = MathHelper.floor(mc.player.posY);
        } else {
            this.lastY = MathHelper.floor(mc.player.posY);
        }
        if (event instanceof UpdateWalkingPlayerEvent.Pre) {
            this.blockData = null;
            double x = mc.player.posX;
            double z = mc.player.posZ;
            double y = keepY.getValue() ? this.lastY : mc.player.posY;
            double forward = mc.player.movementInput.moveForward;
            double strafe = mc.player.movementInput.moveStrafe;
            float yaw = mc.player.rotationYaw;
            if (!mc.player.collidedHorizontally && expand.getValue() > 0D) {
                double[] coords = getExpandCoords(x, z, forward, strafe, yaw);
                x = coords[0];
                z = coords[1];
            }
            if (canPlace(mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - downDistance, mc.player.posZ)).getBlock())) {
                x = mc.player.posX;
                z = mc.player.posZ;
            }
            BlockPos blockBelow = new BlockPos(x, y - downDistance, z);
            this.pos = blockBelow;
            if (mc.world.getBlockState(blockBelow).getBlock() == Blocks.AIR) {
                this.blockData = getBlockData2(blockBelow);
                if (this.blockData != null) {
                    float[] angle = RotationManager.calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(this.blockData.position.getX() + 0.5 + this.blockData.face.getDirectionVec().getX() * 0.5,
                            this.blockData.position.getY() + 0.5 + this.blockData.face.getDirectionVec().getY() * 0.5,
                            this.blockData.position.getZ() + 0.5 + this.blockData.face.getDirectionVec().getZ() * 0.5));
                    KonasGlobals.INSTANCE.rotationManager.setRotations(angle[0], angle[1]);
                    lastYaw = angle[0];
                    lastPitch = angle[1];
                    lastTimer.reset();
                }
            }
        } else if (this.blockData != null) {
            if (getBlockCountHotbar() <= 0 || (!this.Switch.getValue() && mc.player.getHeldItemMainhand().getItem() != null && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)))
                return;
            int heldItem = mc.player.inventory.currentItem;
            if (Switch.getValue() && (mc.player.getHeldItemMainhand().getItem() == null || (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) || !isValid(((ItemBlock) mc.player.getHeldItemMainhand().getItem()).getBlock()))))
                for (int j = 0; j < 9; j++) {
                    if (mc.player.inventory.getStackInSlot(j) != null && mc.player.inventory.getStackInSlot(j).getCount() != 0 && mc.player.inventory.getStackInSlot(j).getItem() instanceof ItemBlock && isValid(((ItemBlock) mc.player.inventory.getStackInSlot(j).getItem()).getBlock())) {
                        mc.player.inventory.currentItem = j;
                        break;
                    }
                }
            if (tower.getValue() != TowerMode.NONE) {
                if (mc.gameSettings.keyBindJump.isKeyDown() && mc.player.moveForward == 0.0F && mc.player.moveStrafing == 0.0F && tower.getValue() != TowerMode.NONE && !mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    if (!this.teleported && center.getValue()) {
                        this.teleported = true;
                        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                        mc.player.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                    }
                    if (center.getValue() && !this.teleported)
                        return;
                    if (tower.getValue() == TowerMode.FAST) {
                        KonasGlobals.INSTANCE.timerManager.updateTimer(this, 25, mc.player.ticksExisted % 10 == 0 ? 1F : 1.5782F);
                    }
                    mc.player.motionY = 0.41999998688697815D;
                    mc.player.motionZ = 0.0D;
                    mc.player.motionX = 0.0D;
                    if (this.timerMotion.hasPassed(1500L)) {
                        KonasGlobals.INSTANCE.timerManager.resetTimer(this);
                        timerMotion.reset();
                        mc.player.motionY = -0.28D;
                    }
                } else {
                    KonasGlobals.INSTANCE.timerManager.resetTimer(this);
                    this.timerMotion.reset();
                    if (this.teleported && this.center.getValue())
                        this.teleported = false;
                }
            } else {
                KonasGlobals.INSTANCE.timerManager.resetTimer(this);
            }
            if (mc.playerController.processRightClickBlock(mc.player, mc.world, this.blockData.position, this.blockData.face, new Vec3d(this.blockData.position.getX() + Math.random(), this.blockData.position.getY() + Math.random(), this.blockData.position.getZ() + Math.random()), EnumHand.MAIN_HAND) != EnumActionResult.FAIL)
                if (this.swing.getValue()) {
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                } else {
                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                }
            mc.player.inventory.currentItem = heldItem;
        }
    }

    private int getBlockCount() {
        int blockCount = 0;
        for (int i = 0; i < 45; i++) {
            if (mc.player.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (is.getItem() instanceof ItemBlock &&
                        !this.invalid.contains(((ItemBlock) item).getBlock()))
                    blockCount += is.getCount();
            }
        }
        return blockCount;
    }

    public double[] getExpandCoords(double x, double z, double forward, double strafe, float YAW) {
        BlockPos underPos = new BlockPos(x, mc.player.posY - ((Keyboard.isKeyDown(Keyboard.KEY_LMENU) && this.down.getValue()) ? 2 : 1), z);
        Block underBlock = mc.world.getBlockState(underPos).getBlock();
        double xCalc = -999.0D, zCalc = -999.0D;
        double dist = 0.0D;
        double expandDist = this.expand.getValue() * 2.0D;
        while (!canPlace(underBlock)) {
            xCalc = x;
            zCalc = z;
            dist++;
            if (dist > expandDist)
                dist = expandDist;
            xCalc += (forward * 0.45D * Math.cos(Math.toRadians((YAW + 90.0F))) + strafe * 0.45D * Math.sin(Math.toRadians((YAW + 90.0F)))) * dist;
            zCalc += (forward * 0.45D * Math.sin(Math.toRadians((YAW + 90.0F))) - strafe * 0.45D * Math.cos(Math.toRadians((YAW + 90.0F)))) * dist;
            if (dist == expandDist)
                break;
            underPos = new BlockPos(xCalc, mc.player.posY - ((Keyboard.isKeyDown(Keyboard.KEY_LMENU) && down.getValue()) ? 2 : 1), zCalc);
            underBlock = mc.world.getBlockState(underPos).getBlock();
        }
        return new double[]{xCalc, zCalc};
    }

    public boolean canPlace(Block block) {
        return ((block instanceof net.minecraft.block.BlockAir || block instanceof net.minecraft.block.BlockLiquid) && mc.world != null && mc.player != null && this.pos != null && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(this.pos)).isEmpty());
    }

    private int getBlockCountHotbar() {
        int blockCount = 0;
        for (int i = 36; i < 45; i++) {
            if (mc.player.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (is.getItem() instanceof ItemBlock &&
                        !this.invalid.contains(((ItemBlock) item).getBlock()))
                    blockCount += is.getCount();
            }
        }
        return blockCount;
    }

    @Subscriber
    public void onPlayerMove(PlayerMoveEvent event) {
        double x = event.getX();
        double z = event.getZ();

        if (mc.player.onGround && !mc.player.noClip && safe.getValue() && !Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
            double i;

            for (i = 0.05D; x != 0.0D && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, -1.0f, 0.0D)).isEmpty(); ) {
                if (x < i && x >= -i) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= i;
                } else {
                    x += i;
                }
            }

            while (z != 0.0D && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0D, -1.0f, z)).isEmpty()) {
                if (z < i && z >= -i) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= i;
                } else {
                    z += i;
                }
            }

            while (x != 0.0D && z != 0.0D && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, -1.0f, z)).isEmpty()) {
                if (x < i && x >= -i) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= i;
                } else {
                    x += i;
                }
                if (z < i && z >= -i) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= i;
                } else {
                    z += i;
                }
            }
        }


        event.setX(x);
        event.setZ(z);
    }

    private BlockData getBlockData2(BlockPos pos) {
        if (!this.invalid.contains(mc.world.getBlockState(pos.add(0, -1, 0)).getBlock()))
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos.add(1, 0, 0)).getBlock()))
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos.add(0, 0, 1)).getBlock()))
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos.add(0, 0, -1)).getBlock()))
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos.add(0, 1, 0)).getBlock()))
            return new BlockData(pos.add(0, 1, 0), EnumFacing.DOWN);
        BlockPos pos2 = pos.add(-1, 0, 0);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, -1, 0)).getBlock()))
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, 1, 0)).getBlock()))
            return new BlockData(pos2.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, 1)).getBlock()))
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, -1)).getBlock()))
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos3 = pos.add(1, 0, 0);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, -1, 0)).getBlock()))
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, 1, 0)).getBlock()))
            return new BlockData(pos3.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, 1)).getBlock()))
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, -1)).getBlock()))
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos4 = pos.add(0, 0, 1);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, -1, 0)).getBlock()))
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, 1, 0)).getBlock()))
            return new BlockData(pos4.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, 1)).getBlock()))
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, -1)).getBlock()))
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos5 = pos.add(0, 0, -1);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, -1, 0)).getBlock()))
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, 1, 0)).getBlock()))
            return new BlockData(pos5.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, 1)).getBlock()))
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, -1)).getBlock()))
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos6 = pos.add(-2, 0, 0);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, -1, 0)).getBlock()))
            return new BlockData(pos2.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, 1, 0)).getBlock()))
            return new BlockData(pos2.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(1, 0, 0)).getBlock()))
            return new BlockData(pos2.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, 1)).getBlock()))
            return new BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos2.add(0, 0, -1)).getBlock()))
            return new BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos7 = pos.add(2, 0, 0);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, -1, 0)).getBlock()))
            return new BlockData(pos3.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, 1, 0)).getBlock()))
            return new BlockData(pos3.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(1, 0, 0)).getBlock()))
            return new BlockData(pos3.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, 1)).getBlock()))
            return new BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos3.add(0, 0, -1)).getBlock()))
            return new BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos8 = pos.add(0, 0, 2);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, -1, 0)).getBlock()))
            return new BlockData(pos4.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, 1, 0)).getBlock()))
            return new BlockData(pos4.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(1, 0, 0)).getBlock()))
            return new BlockData(pos4.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, 1)).getBlock()))
            return new BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos4.add(0, 0, -1)).getBlock()))
            return new BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos9 = pos.add(0, 0, -2);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, -1, 0)).getBlock()))
            return new BlockData(pos5.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, 1, 0)).getBlock()))
            return new BlockData(pos5.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(1, 0, 0)).getBlock()))
            return new BlockData(pos5.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, 1)).getBlock()))
            return new BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos5.add(0, 0, -1)).getBlock()))
            return new BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos10 = pos.add(0, -1, 0);
        if (!this.invalid.contains(mc.world.getBlockState(pos10.add(0, -1, 0)).getBlock()))
            return new BlockData(pos10.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos10.add(0, 1, 0)).getBlock()))
            return new BlockData(pos10.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos10.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos10.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos10.add(1, 0, 0)).getBlock()))
            return new BlockData(pos10.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos10.add(0, 0, 1)).getBlock()))
            return new BlockData(pos10.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos10.add(0, 0, -1)).getBlock()))
            return new BlockData(pos10.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos11 = pos10.add(1, 0, 0);
        if (!this.invalid.contains(mc.world.getBlockState(pos11.add(0, -1, 0)).getBlock()))
            return new BlockData(pos11.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos11.add(0, 1, 0)).getBlock()))
            return new BlockData(pos11.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos11.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos11.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos11.add(1, 0, 0)).getBlock()))
            return new BlockData(pos11.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos11.add(0, 0, 1)).getBlock()))
            return new BlockData(pos11.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos11.add(0, 0, -1)).getBlock()))
            return new BlockData(pos11.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos12 = pos10.add(-1, 0, 0);
        if (!this.invalid.contains(mc.world.getBlockState(pos12.add(0, -1, 0)).getBlock()))
            return new BlockData(pos12.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos12.add(0, 1, 0)).getBlock()))
            return new BlockData(pos12.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos12.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos12.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos12.add(1, 0, 0)).getBlock()))
            return new BlockData(pos12.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos12.add(0, 0, 1)).getBlock()))
            return new BlockData(pos12.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos12.add(0, 0, -1)).getBlock()))
            return new BlockData(pos12.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos13 = pos10.add(0, 0, 1);
        if (!this.invalid.contains(mc.world.getBlockState(pos13.add(0, -1, 0)).getBlock()))
            return new BlockData(pos13.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos13.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos13.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos13.add(0, 1, 0)).getBlock()))
            return new BlockData(pos13.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos13.add(1, 0, 0)).getBlock()))
            return new BlockData(pos13.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos13.add(0, 0, 1)).getBlock()))
            return new BlockData(pos13.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos13.add(0, 0, -1)).getBlock()))
            return new BlockData(pos13.add(0, 0, -1), EnumFacing.SOUTH);
        BlockPos pos14 = pos10.add(0, 0, -1);
        if (!this.invalid.contains(mc.world.getBlockState(pos14.add(0, -1, 0)).getBlock()))
            return new BlockData(pos14.add(0, -1, 0), EnumFacing.UP);
        if (!this.invalid.contains(mc.world.getBlockState(pos14.add(0, 1, 0)).getBlock()))
            return new BlockData(pos14.add(0, 1, 0), EnumFacing.DOWN);
        if (!this.invalid.contains(mc.world.getBlockState(pos14.add(-1, 0, 0)).getBlock()))
            return new BlockData(pos14.add(-1, 0, 0), EnumFacing.EAST);
        if (!this.invalid.contains(mc.world.getBlockState(pos14.add(1, 0, 0)).getBlock()))
            return new BlockData(pos14.add(1, 0, 0), EnumFacing.WEST);
        if (!this.invalid.contains(mc.world.getBlockState(pos14.add(0, 0, 1)).getBlock()))
            return new BlockData(pos14.add(0, 0, 1), EnumFacing.NORTH);
        if (!this.invalid.contains(mc.world.getBlockState(pos14.add(0, 0, -1)).getBlock()))
            return new BlockData(pos14.add(0, 0, -1), EnumFacing.SOUTH);
        return null;
    }

    private float[] aimAtLocation(double x, double y, double z, EnumFacing facing) {
        EntitySnowball temp = new EntitySnowball(mc.world);
        temp.posX = x + 0.5D;
        temp.posY = y - 2.7035252353D;
        temp.posZ = z + 0.5D;
        return aimAtLocation(temp.posX, temp.posY, temp.posZ);
    }

    private float[] aimAtLocation(double positionX, double positionY, double positionZ) {
        double x = positionX - mc.player.posX;
        double y = positionY - mc.player.posY;
        double z = positionZ - mc.player.posZ;
        double distance = MathHelper.sqrt(x * x + z * z);
        return new float[]{(float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F, (float) -(Math.atan2(y, distance) * 180.0D / Math.PI)};
    }

    private class BlockData {
        public BlockPos position;

        public EnumFacing face;

        public BlockData(BlockPos position, EnumFacing face) {
            this.position = position;
            this.face = face;
        }
    }
}