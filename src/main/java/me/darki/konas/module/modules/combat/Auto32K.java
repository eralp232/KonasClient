package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.RaionBlockUtils;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.render.TessellatorUtil;
import me.darki.konas.util.render.shader.ShaderHelper;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.UP;

/**
 * @author Robeart
 */
public class Auto32K extends Module {
    public static Setting<Double> rangePlayer = new Setting<>("PlayerRange", 6D, 10D, 1D, 0.1D);
    public static Setting<Double> rangeAttack = new Setting<>("AttackRange", 3.6D, 10D, 1D, 0.1D);
    public static Setting<Boolean> armor = new Setting<>("ArmorCheck", false);
    public static Setting<Boolean> block = new Setting<>("BlockShulker", false);
    public static Setting<Boolean> aura = new Setting<>("KillAura", true);
    public static Setting<AuraMode> auraMode = new Setting<>("Mode", AuraMode.TICK);
    public static Setting<Double> delay = new Setting<>("Delay", 0.02D, 1D, 0.01, 0.1D);
    public static Setting<Integer> tickDelay = new Setting<>("Ticks", 3, 20, 0, 1);
    public static Setting<Boolean> friends = new Setting<>("Friends", false);
    public static Setting<Boolean> walls = new Setting<>("Walls", true);
    public static Setting<Boolean> rangeCircles = new Setting<>("RangeCircles", false);
    public static Setting<Boolean> combine = new Setting<>("Combine", false).withVisibility(rangeCircles::getValue);
    public static Setting<Float> circleWidth = new Setting<>("Width", 2.5F, 5F, 0.1F, 0.1F).withVisibility(rangeCircles::getValue);
    public static Setting<ColorSetting> circleColor = new Setting<>("Color", new ColorSetting(0xFFFF0000)).withVisibility(rangeCircles::getValue);

    private boolean placed;
    private int ticks;
    private EntityPlayer target;
    private BlockPos hopper;
    private BlockPos dispenser;
    private boolean redstonePlaced;
    private int hopperSlot;
    private int shulkerSlot;
    private int blockSlot;

    private enum AuraMode {
        TICK, ALWAYS
    }

    private Timer timer = new Timer();

    public Auto32K() {
        super("Auto32k", "Automatically kills someone using 32k weapons", Category.COMBAT);
    }

    public static EnumFacing getFacing(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(EnumFacing.DOWN.getDirectionVec()).scale(0.5));
        double diffX = hitVec.x - eyesPos.x;
        double diffZ = hitVec.z - eyesPos.z;
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float yaw2 = mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
        if (Math.abs(mc.player.posX - (double) ((float) pos.getX() + 0.5F)) < 2.0D && Math.abs(mc.player.posZ - (double) ((float) pos
                .getZ() + 0.5F)) < 2.0D) {
            double d0 = mc.player.posY + (double) mc.player.getEyeHeight();
            if (d0 - (double) pos.getY() > 2.0D) {
                return UP;
            }
            if ((double) pos.getY() - d0 > 0.0D) {
                return DOWN;
            }
        }
        return EnumFacing.byHorizontalIndex(MathHelper.floor((double) (yaw2 * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
    }

    @Override
    public void onEnable() {
        redstonePlaced = false;
        ticks = 0;
        placed = false;
    }

    @Subscriber
    private void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        target = getTarget(getTargets(false));
        if (!placed) {
            hopperSlot = getHotbarSlotOfBlock(Blocks.HOPPER);
            shulkerSlot = getShulkerSlot();
            int dispenserSlot = getHotbarSlotOfBlock(Blocks.DISPENSER);
            int redstoneSlot = getHotbarSlotOfBlock(Blocks.REDSTONE_BLOCK);
            blockSlot = getHotbarSlotOfBlock(Blocks.OBSIDIAN);
            if (hopperSlot == -1 || shulkerSlot == -1 || dispenserSlot == -1 || redstoneSlot == -1 || blockSlot == -1)
                return;
            if (target == null) handlePlacing(mc.player, dispenserSlot, redstoneSlot, blockSlot);
            else handlePlacing(target, dispenserSlot, redstoneSlot, blockSlot);
            placed = true;
        }
        if ((mc.currentScreen instanceof GuiDispenser)) {
            EnumFacing facingDispenser = getFacing(dispenser);
            if (!(mc.player.openContainer.inventorySlots.get(0).inventory.getStackInSlot(0)
                    .getItem() instanceof ItemShulkerBox)) {
                if (mc.world.getBlockState(hopper.up()).getBlock() instanceof BlockShulkerBox) {
                    BlockPos obby = dispenser.offset(facingDispenser).offset(facingDispenser);
                    if (block.getValue() && Blocks.OBSIDIAN.canPlaceBlockAt(mc.world, obby)
                            && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(obby)).isEmpty())
                        RaionBlockUtils.place(obby);
                    mc.player.inventory.currentItem = hopperSlot;
                    RaionBlockUtils.place(hopper);
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(hopper, UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
                }
                else {
                    mc.playerController.windowClick(mc.player.openContainer.windowId, 0, shulkerSlot, ClickType.SWAP, mc.player);
                }
            }
            else {
                if (!redstonePlaced) {
                    EnumFacing facing = canPlaceDispenser(dispenser, facingDispenser);
                    RaionBlockUtils.place(dispenser.offset(facing));
                    mc.player.inventory.currentItem = blockSlot;
                    redstonePlaced = true;
                }
            }
        }

        if (!is32k(mc.player.getHeldItemMainhand())) {
            if ((mc.currentScreen instanceof GuiHopper)) {
                int swapslot = getHotbarSlotOfItem(Items.AIR) == -1 ? mc.player.inventory.currentItem : getHotbarSlotOfItem(Items.AIR);
                for (int i = 0; i < 5; i++) {
                    if (is32k(mc.player.openContainer.inventorySlots.get(0).inventory.getStackInSlot(i))) {
                        mc.playerController.windowClick(mc.player.openContainer.windowId, i, swapslot, ClickType.SWAP, mc.player);
                        mc.player.inventory.currentItem = swapslot;
                        break;
                    }
                }
            }
        }
        if (aura.getValue() && is32k(mc.player.getHeldItemMainhand()) && target != null && auraMode.getValue() == AuraMode.TICK) {
            if (ticks >= tickDelay.getValue() && mc.player.getDistance(target) <= rangeAttack.getValue()) {
                mc.playerController.attackEntity(mc.player, target);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                ticks = 0;
            }
        }
        ticks++;
    }

    private boolean is32k(ItemStack itemStack) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, itemStack) >= 1000;
    }

    private void handlePlacing(EntityLivingBase target, int dispenserIndex, int redstoneIndex, int blockIndex) {
        BlockPos block = mc.player == target ? bestPlace() : bestPlace(target);
        if (block == null) return;
        dispenser = block.up();
        EnumFacing facing = getFacing(dispenser);
        hopper = block.offset(facing);
        mc.player.inventory.currentItem = blockIndex;
        RaionBlockUtils.place(block);
        mc.player.inventory.currentItem = dispenserIndex;
        RaionBlockUtils.place(dispenser);
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(dispenser, UP, EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
        mc.player.inventory.currentItem = redstoneIndex;
    }

    private int getShulkerSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemShulkerBox) {
                return i;
            }
        }
        return -1;
    }

    private List<EntityPlayer> getTargets(boolean range) {
        ArrayList<EntityPlayer> targetlist = new ArrayList<>();
        Iterator targets = mc.world.loadedEntityList.iterator();
        while (targets.hasNext()) {
            Entity en = (Entity) targets.next();
            if (en == null) continue;
            if (!(en instanceof EntityPlayer)) continue;
            EntityPlayer e = (EntityPlayer) en;
            if (mc.player.getDistance(e) > (range ? rangePlayer.getValue() * 4F : rangePlayer.getValue())) continue;
            if (e == mc.player) continue;
            if (!walls.getValue() && !mc.player.canEntityBeSeen(e) && !canEntityFeetBeSeen(e)) continue;
            if (e.getHealth() <= 0 || e.isDead) continue;
            if (!friends.getValue() && Friends.isFriend(e.getName())) continue;
            targetlist.add(e);
        }
        return targetlist;
    }

    private EntityPlayer getTarget(List<EntityPlayer> targets) {
        EntityPlayer target = null;
        double distance = rangePlayer.getValue();
        for (EntityPlayer entity : targets) {
            if (armor.getValue()) {
                if (target != null) {
                    try {
                        if (!(target.getItemStackFromSlot(EntityEquipmentSlot.HEAD)
                                .getItem() == Items.DIAMOND_HELMET
                                || target.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.DIAMOND_LEGGINGS
                                || target.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.DIAMOND_BOOTS)) continue;
                    } catch (NullPointerException e) {
                        System.out.println("Failed getting armor info fromt target");
                    }
                }
            }
            if (mc.player.getDistance(entity) < distance) {
                distance = mc.player.getDistance(entity);
                target = entity;
            }
        }
        return target;
    }

    private BlockPos bestPlace(EntityLivingBase target) {
        BlockPos blockPos = null;
        double distance = 0;
        for (BlockPos pos : canPlaceBlocks()) {
            if (target.getDistance(pos.getX(), pos.getY(), pos.getZ()) > distance) {
                blockPos = pos;
            }
        }
        return blockPos;
    }

    private BlockPos bestPlace() {
        BlockPos blockPos = null;
        double distance = 420;
        for (BlockPos pos : canPlaceBlocks()) {
            if (mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) < distance) {
                blockPos = pos;
            }
        }
        return blockPos;
    }

    private List<BlockPos> canPlaceBlocks() {
        if (mc.player == null) return null;
        List<BlockPos> blockPosList = new ArrayList<>();
        for (BlockPos pos : BlockUtils.getBlocksInSphere(new BlockPos(mc.player), 3)) {
            EnumFacing facing = getFacing(pos.up());
            if (facing == null || facing == DOWN || facing == UP) continue;
            if (RaionBlockUtils.canPlace(pos, Blocks.OBSIDIAN, true)
                    && Blocks.DISPENSER.canPlaceBlockAt(mc.world, pos.up())
                    && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.up())).isEmpty()
                    && (canPlaceDispenser(pos.up(), facing) != null)
                    && Blocks.HOPPER.canPlaceBlockAt(mc.world, pos.offset(facing))
                    && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.offset(facing))).isEmpty()
                    && Blocks.SILVER_SHULKER_BOX.canPlaceBlockAt(mc.world, pos.offset(facing).up())
                    && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.offset(facing).up()))
                    .isEmpty()) {
                blockPosList.add(pos);
            }
        }
        return blockPosList;
    }

    private EnumFacing canPlaceDispenser(BlockPos pos, EnumFacing enumFacing) {
        for (EnumFacing f : EnumFacing.values()) {
            if (f == DOWN || f == enumFacing) continue;
            if (Blocks.REDSTONE_BLOCK.canPlaceBlockAt(mc.world, pos.offset(f))
                    && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.offset(f))).isEmpty())
                return f;
        }
        return null;
    }

    private void drawCircle(Entity e, Render3DEvent event) {
        TessellatorUtil.prepare();
        IRenderManager renderManager = (IRenderManager) mc.getRenderManager();
        float[] hsb = Color.RGBtoHSB(circleColor.getValue().getRed(), circleColor.getValue().getGreen(), circleColor.getValue().getBlue(), null);
        float hue = (float) (System.currentTimeMillis() % 7200L) / 7200F;
        int rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
        ArrayList<Vec3d> vecs = new ArrayList<>();
        double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) event.getPartialTicks() - renderManager.getRenderPosX();
        double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) event.getPartialTicks() - renderManager.getRenderPosY();
        double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) event.getPartialTicks() - renderManager.getRenderPosZ();
        GL11.glLineWidth(circleWidth.getValue());
        GL11.glBegin(1);
        for (int i = 0; i <= 360; ++i) {
            Vec3d vec = new Vec3d(x + Math.sin((double) i * Math.PI / 180.0) * rangeAttack.getValue(), y + 0.01, z + Math.cos((double) i * Math.PI / 180.0) * rangeAttack.getValue());
            vecs.add(vec);
        }
        for (int j = 0; j < vecs.size() - 1; ++j) {
            int alpha = (rgb >> 24) & 0xff;
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = (rgb) & 0xFF;
            if (circleColor.getValue().isCycle()) {
                GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha / 255F);
            } else {
                GL11.glColor4f(circleColor.getValue().getRed() / 255F, circleColor.getValue().getGreen() / 255F, circleColor.getValue().getBlue() / 255F, circleColor.getValue().getAlpha() / 255F);
            }
            if (!combine.getValue() || circleCheck(e, vecs.get(j).x + renderManager.getRenderPosX(), e.posY, vecs.get(j).z + renderManager.getRenderPosZ())) {
                GL11.glVertex3d(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z);
                GL11.glVertex3d(vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z);
            }
            hue += (1F / 360F);
            rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
        }
        GL11.glEnd();
        TessellatorUtil.release();
    }

    private boolean circleCheck(Entity e, double x, double y, double z) {
        List<EntityPlayer> targets = getTargets(true);
        targets.add(mc.player);
        for (EntityPlayer target : targets) {
            if (target.equals(e)) continue;
            if (target.posY != y) continue;
            double distance = Math.sqrt(Math.pow(x - ShaderHelper.interpEntityX(target), 2) + Math.pow(z - ShaderHelper.interpEntityZ(target), 2));
            if (distance < rangeAttack.getValue()) return false;
        }
        return true;
    }

    @Subscriber
    public void onWorldRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (rangeCircles.getValue()) {
            for (Entity entity : getTargets(true)) {
                drawCircle(entity, event);
            }
            drawCircle(mc.player, event);
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
    }

    @Subscriber
    private void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (aura.getValue() && is32k(mc.player.getHeldItemMainhand()) && target != null && auraMode.getValue() == AuraMode.ALWAYS) {
            if (timer.hasPassed(delay.getValue() * 1000) && mc.player.getDistance(target) <= rangeAttack.getValue()) {
                mc.playerController.attackEntity(mc.player, target);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                timer.reset();
            }
        }
    }

    public static int getHotbarSlotOfItem(Item input) {
        for (int i = 0; i < 9; ++i) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) return i;
        }
        return -1;
    }

    public static boolean canEntityFeetBeSeen(Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posX + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }

    public static int getHotbarSlotOfBlock(Block type) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock) {
                ItemBlock block = (ItemBlock) stack.getItem();
                if (block.getBlock() == type) return i;
            }
        }
        return -1;
    }
}