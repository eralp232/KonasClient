package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.interaction.RotationManager;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static me.darki.konas.util.client.PlayerUtils.calculateLookAt;

public class BedAura extends Module {

    private Setting<Parent> antiCheat = new Setting<>("AntiCheat", new Parent(false));
    private Setting<Boolean> rotate = new Setting<>("Rotate", false).withParent(antiCheat);
    private Setting<Boolean> swing = new Setting<>("Swing", true).withParent(antiCheat);
    private Setting<Boolean> airPlace = new Setting<>("AirPlace", false).withParent(antiCheat);
    private Setting<Boolean> rayTrace = new Setting<>("RayTrace", false).withParent(antiCheat);
    private Setting<Boolean> strictDirection = new Setting<>("StrictDirection", false).withParent(antiCheat);

    private Setting<Parent> ranges = new Setting<>("Ranges", new Parent(false));
    private Setting<Float> breakRange = new Setting<>("BreakRange", 6f, 6f, 1f, 0.1f).withParent(ranges);
    private Setting<Float> placeRange = new Setting<>("PlaceRange", 5f, 6f, 1f, 0.1f).withParent(ranges);

    private Setting<Parent> speeds = new Setting<>("Speeds", new Parent(false));
    private Setting<Integer> breakSpeed = new Setting<>("BreakSpeed", 20, 20, 1, 1).withParent(speeds);
    private Setting<Integer> placeSpeed = new Setting<>("PlaceSpeed", 20, 20, 1, 1).withParent(speeds);

    private Setting<Parent> misc = new Setting<>("Misc", new Parent(false));
    private Setting<Boolean> autoSwitch = new Setting<>("Swap", true).withParent(misc);
    private Setting<Boolean> autoMove = new Setting<>("AutoMove", true).withParent(misc);


    private Timer hitTimer = new Timer();
    private Timer placeTimer = new Timer();

    private final Timer angleInactivityTimer = new Timer();

    private static boolean isSpoofingAngles;
    private static double yaw;
    private static double pitch;

    private BlockPos breakPos = null;

    private BlockPos finalPos = null;
    private EnumFacing finalFacing = null;

    private int priorSlot = -1;

    public BedAura() {
        super("BedAura", Category.COMBAT);
    }

    @Subscriber(priority = 100)
    public void onMotionUpdatePre(UpdateWalkingPlayerEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        breakPos = null;
        finalPos = null;

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally(rotate.getValue())) return;

        if (mc.player.dimension == 0) return;

        if(hitTimer.hasPassed(1000 - breakSpeed.getValue() * 50)) {
            breakPos = findBedTarget();
        }

        if (breakPos == null && placeTimer.hasPassed(1000 - placeSpeed.getValue() * 50)) {
            if (mc.player.inventory.getCurrentItem().getItem() == Items.BED || isOffhand()) {
                findPlaceTarget();
            } else if (!(getTargets().isEmpty())) {
                if (autoSwitch.getValue() && !isOffhand()) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack itemStack = mc.player.inventory.mainInventory.get(i);
                        if (itemStack.getItem() == Items.BED) {
                            priorSlot = mc.player.inventory.currentItem;
                            mc.player.inventory.currentItem = i;
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(i));
                            findPlaceTarget();
                            break;
                        }
                    }
                    if (autoMove.getValue() && mc.player.inventory.getCurrentItem().getItem() != Items.BED) {
                        for (int i = 9; i <= 35; i++) {
                            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.BED) {
                                mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                                mc.playerController.windowClick(0, mc.player.inventory.currentItem < 9 ? mc.player.inventory.currentItem + 36 : mc.player.inventory.currentItem, 0, ClickType.PICKUP, mc.player);
                                mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
                            }
                        }
                    }
                }
            }
        } else if (breakPos != null) {
            double[] angle = calculateLookAt(breakPos.getX() + 0.5D, breakPos.getY() + 0.5D, breakPos.getZ() + 0.5D, mc.player);
            yaw = angle[0];
            pitch = angle[1];
            isSpoofingAngles = true;
            angleInactivityTimer.reset();
        }

        if (isSpoofingAngles) {
            KonasGlobals.INSTANCE.rotationManager.setRotations((float) yaw, (float) pitch);
        }

        if (angleInactivityTimer.hasPassed(450)) {
            isSpoofingAngles = false;
        }
    }

    @Subscriber(priority = 8)
    public void onUpdateWalkingPost(UpdateWalkingPlayerEvent.Post event) {
        if (breakPos != null) {
            breakBed(breakPos);
        } else if (finalPos != null) {
            placeBed();
        }

        if (priorSlot != -1 && !isOffhand()) {
            mc.player.inventory.currentItem = priorSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(priorSlot));
            priorSlot = -1;
        }
    }

    public boolean isOffhand() {
        return mc.player.getHeldItemOffhand().getItem() instanceof ItemBed;
    }

    private void breakBed(BlockPos bed) {
        if (bed == null) return;
        RayTraceResult result = strictDirection.getValue() ? mc.world.rayTraceBlocks(mc.player.getPositionEyes(1F), new Vec3d(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5)) : null;
        Vec3d hitVec = new Vec3d(bed).add(0.5, 0.5, 0.5);
        EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
        InteractionUtil.rightClickBlock(bed, hitVec, EnumHand.MAIN_HAND, facing, true, swing.getValue());
        hitTimer.reset();
    }

    private void placeBed() {
        Vec3d hitVec = new Vec3d(finalPos.down()).add(0.5, 0.5, 0.5).add(new Vec3d(finalFacing.getOpposite().getDirectionVec()).scale(0.5));
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        InteractionUtil.rightClickBlock(finalPos.down(), hitVec, isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, EnumFacing.UP, true, swing.getValue());
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        placeTimer.reset();
        finalPos = null;
    }

    private BlockPos findBedTarget() {
        TileEntityBed bed = (TileEntityBed) mc.world.loadedTileEntityList.stream()
                .filter(e -> e instanceof TileEntityBed)
                .filter(e -> ((TileEntityBed) e).isHeadPiece())
                .filter(e -> mc.player.getDistance(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()) <= breakRange.getValue())
                .filter(e -> suicideCheck(e.getPos()))
                .min(Comparator.comparing(e -> mc.player.getDistance(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ())))
                .orElse(null);

        if (bed != null) {
            return bed.getPos();
        }

        return null;
    }

    private void findPlaceTarget() {
        List<EntityPlayer> targets = getTargets();

        if (targets.isEmpty()) return;

        
        checkTarget(new BlockPos(targets.get(0)), true);
    }

    private void checkTarget(BlockPos pos, boolean firstCheck) {
        if (mc.world.getBlockState(pos).getBlock() == Blocks.BED) {
            return;
        }
        float damage = calculateDamage(pos, mc.player);
        if ((double)damage > (double) mc.player.getHealth() + mc.player.getAbsorptionAmount() + 0.5) {
            if (firstCheck && airPlace.getValue()) {
                checkTarget(pos.up(), false);
            }
            return;
        }
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            if (firstCheck && airPlace.getValue()) {
                checkTarget(pos.up(), false);
            }
            return;
        }

        ArrayList<BlockPos> positions = new ArrayList<>();
        HashMap<BlockPos, EnumFacing> facings = new HashMap<>();

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos position;
            if (facing == EnumFacing.DOWN || facing == EnumFacing.UP || !(mc.player.getDistanceSq(position = pos.offset(facing)) <= Math.pow(placeRange.getValue(), 2)) || (!mc.world.getBlockState(position).getMaterial().isReplaceable()) || mc.world.getBlockState(position.down()).getMaterial().isReplaceable()) continue;
            if (rotate.getValue() && EnumFacing.fromAngle(BlockUtils.calculateLookAt(position, EnumFacing.UP, mc.player)[0]).getOpposite() != facing) continue;
            if (rayTrace.getValue() && mc.world.rayTraceBlocks(mc.player.getPositionEyes(1F), new Vec3d(position.getX() + 0.5, position.getY() + 1, position.getZ() + 0.5)) != null) continue;
            positions.add(position);
            facings.put(position, facing.getOpposite());
        }

        if (positions.isEmpty()) {
            if (firstCheck && airPlace.getValue()) {
                checkTarget(pos.up(), false);
            }
            return;
        }
        positions.sort(Comparator.comparingDouble(pos2 -> mc.player.getDistanceSq(pos2)));
        finalPos = positions.get(0);
        finalFacing = facings.get(finalPos);
        if (rotate.getValue()) {
            float[] rotation = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), new Vec3d(finalPos.down().getX() + 0.5, finalPos.down().getY() + 1, finalPos.down().getZ() + 0.5));
            yaw = rotation[0];
            pitch = rotation[1];
            isSpoofingAngles = true;
        } else {
            float[] rotation = simpleFacing(finalFacing);
            yaw = rotation[0];
            pitch = rotation[1];
            isSpoofingAngles = true;
        }
        angleInactivityTimer.reset();
    }

    public static float[] simpleFacing(EnumFacing facing) {
        switch (facing) {
            case DOWN: {
                return new float[]{mc.player.rotationYaw, 90.0f};
            }
            case UP: {
                return new float[]{mc.player.rotationYaw, -90.0f};
            }
            case NORTH: {
                return new float[]{180.0f, 0.0f};
            }
            case SOUTH: {
                return new float[]{0.0f, 0.0f};
            }
            case WEST: {
                return new float[]{90.0f, 0.0f};
            }
        }
        return new float[]{270.0f, 0.0f};
    }

    public List<EntityPlayer> getTargets() {
        return mc.world.playerEntities.stream().filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> !e.isDead)
                .filter(entityPlayer -> !Friends.isFriend(entityPlayer.getName()))
                .filter(entityPlayer -> entityPlayer != mc.player)
                .filter(entityPlayer -> mc.player.getDistance(entityPlayer) < placeRange.getValue() + 2)
                .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                .collect(Collectors.toList());
    }

    private boolean suicideCheck(BlockPos pos) {
        return (mc.player.getHealth() + mc.player.getAbsorptionAmount() - calculateDamage(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, mc.player) > 0.5);
    }

    public float calculateDamage(BlockPos bedPos, Entity entity) {
        return calculateDamage(bedPos.getX() + 0.5D, bedPos.getY() + 1D, bedPos.getZ() + 0.5D, entity);
    }

    public static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        final double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float) (int) ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion((World) Minecraft.getMinecraft().world, (Entity) null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finald;
    }

    public static float getBlastReduction(final EntityLivingBase entity, float damage, final Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer) entity;
            final DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            final int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            final float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private static float getDamageMultiplied(final float damage) {
        final int diff = Minecraft.getMinecraft().world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    private boolean isBlockValid(BlockPos pos) {

        IBlockState state = mc.world.getBlockState(pos.up());

        if (state.getBlock() == Blocks.AIR) {
            return mc.world.getBlockState(pos).isSideSolid(mc.world, pos, EnumFacing.UP);
        }
        return false;
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public List<BlockPos> getSphere(BlockPos loc, float r, float h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

}
