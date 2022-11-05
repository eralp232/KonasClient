package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.mixin.mixins.ISPacketPlayerPosLook;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.misc.AntiBot;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.TickRateUtil;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.render.TessellatorUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillAura extends Module {

    private static Setting<Float> hitRange = new Setting<>("Range", 4.3f, 6f, 1f, 0.5f);

    public static final Setting<Parent> targeting = new Setting<>("Targeting", new Parent(false));
    private static Setting<Boolean> animalSetting = new Setting<>("Animals", false).withParent(targeting);
    private static Setting<Boolean> mobSetting = new Setting<>("Mobs", true).withParent(targeting);
    private static Setting<Boolean> bullets = new Setting<>("Bullets", false).withParent(targeting);
    private static Setting<Boolean> playerSetting = new Setting<>("Players", true).withParent(targeting);
    private static Setting<Boolean> friendsSetting = new Setting<>("AttackFriends", false).withParent(targeting);

    public static final Setting<Parent> antiCheat = new Setting<>("AntiCheat", new Parent(false));
    private static Setting<TimingMode> timingMode = new Setting<>("Timing", TimingMode.SEQUENTIAL).withParent(antiCheat);
    private static Setting<RotationMode> rotations = new Setting<>("Rotate", RotationMode.TRACK).withParent(antiCheat);
    private static final Setting<Float> wallsRange = new Setting<>("WallsRange", 3f, 6f, 0.5f, 0.5f).withParent(antiCheat);
    private static Setting<Boolean> strict = new Setting<>("Strict", false).withParent(antiCheat);
    private static final Setting<Float> torqueFactor = new Setting<>("YawAngle", 1f, 1f, 0.1f, 0.1f).withParent(antiCheat);
    private static Setting<TpsSyncMode> tpsSyncMode = new Setting<>("TPSSync", TpsSyncMode.NORMAL).withParent(antiCheat);

    public static final Setting<Parent> speed = new Setting<>("Speed", new Parent(false));
    private static Setting<Mode> mode = new Setting<>("Mode", Mode.DYNAMIC).withParent(speed);
    private static Setting<Integer> tickDelay = new Setting<>("TickDelay", 12, 20, 0, 1).withVisibility(() -> mode.getValue() == Mode.STATIC).withParent(speed);

    public static final Setting<Parent> misc = new Setting<>("Misc", new Parent(false));
    private static Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true).withParent(misc);
    private static Setting<Boolean> switchBack = new Setting<>("SwitchBack", false).withParent(misc);
    private static Setting<Boolean> noGapSwitch = new Setting<>("NoGapSwitch", true).withParent(misc);
    private static Setting<Boolean> autoBlock = new Setting<>("AutoBlock", false).withParent(misc);
    private static Setting<Boolean> swordOnly = new Setting<>("SwordOnly", false).withParent(misc);
    private static Setting<Boolean> onlyInHoles = new Setting<>("OnlyInHoles", false).withParent(misc);
    private static Setting<Boolean> onlyWhenFalling = new Setting<>("OnlyWhenFalling", false).withParent(misc);
    private static Setting<Boolean> onlyInAir = new Setting<>("OnlyInAir", false).withParent(misc);
    private static Setting<Boolean> disableWhenCA = new Setting<>("DisableWhenCA", false).withParent(misc);
    private static Setting<Boolean> onlyWhenNoTargets = new Setting<>("OnlyWhenNoTargets", true).withVisibility(disableWhenCA::getValue).withParent(misc);
    private static Setting<Boolean> check32k = new Setting<>("Check32k", false).withVisibility(() -> swordOnly.getValue()).withParent(misc);

    public static final Setting<Parent> render = new Setting<>("Render", new Parent(false));
    private static Setting<Boolean> targetRender = new Setting<>("TargetRender", true).withParent(render);
    private static Setting<Boolean> onlyWhenHitting = new Setting<>("OnlyWhenHitting", true).withParent(render);
    private static Setting<Boolean> depth = new Setting<>("Depth", true).withParent(render);
    private static Setting<Boolean> fill = new Setting<>("Fill", false).withParent(render);
    private static Setting<Boolean> orbit = new Setting<>("Orbit", true).withParent(render);
    private static Setting<Boolean> trial = new Setting<>("Trail", true).withParent(render);
    public static Setting<Float> orbitSpeed = new Setting<>("OrbitSpeed", 1F, 10F, 0.1F, 0.1F).withParent(render);
    public static Setting<Float> animationSpeed = new Setting<>("AnimSpeed", 1F, 10F, 0.1F, 0.1F).withParent(render);
    public static Setting<Float> circleWidth = new Setting<>("Width", 2.5F, 5F, 0.1F, 0.1F).withParent(render);
    public static Setting<ColorSetting> circleColor = new Setting<>("Color", new ColorSetting(0x33da6464, true)).withParent(render);

    private enum RotationMode {
        NONE, TRACK, HIT
    }

    private enum TimingMode {
        SEQUENTIAL, VANILLA
    }

    private enum Mode {
        DYNAMIC, STATIC
    }

    private enum TpsSyncMode {
        NONE, NORMAL, MIN, LATEST
    }

    private int ticksRun = 0;

    private static double yaw;
    private static double pitch;

    private long startTime = 0L;

    private int switchBackSlot = -1;

    Entity currentTarget = null;

    private Timer lastHit = new Timer();

    public KillAura() {
        super("KillAura", Keyboard.KEY_NONE, Category.COMBAT, "Aura", "SwordAura", "HitAura");
    }

    public void onEnable() {
        currentTarget = null;
        switchBackSlot = -1;
    }

    @Subscriber
    public void onWorldRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (targetRender.getValue() && currentTarget != null && (!onlyWhenHitting.getValue() || !lastHit.hasPassed(3500))) {
            GlStateManager.pushMatrix();
            TessellatorUtil.prepare();
            if (depth.getValue()) {
                GlStateManager.enableDepth();
            }
            IRenderManager renderManager = (IRenderManager) mc.getRenderManager();
            float[] hsb = Color.RGBtoHSB(circleColor.getValue().getRed(), circleColor.getValue().getGreen(), circleColor.getValue().getBlue(), null);
            float initialHue = (float) (System.currentTimeMillis() % 7200L) / 7200F;
            float hue = initialHue;
            int rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            ArrayList<Vec3d> vecs = new ArrayList<>();
            double x = currentTarget.lastTickPosX + (currentTarget.posX - currentTarget.lastTickPosX) * (double) event.getPartialTicks() - renderManager.getRenderPosX();
            double y = currentTarget.lastTickPosY + (currentTarget.posY - currentTarget.lastTickPosY) * (double) event.getPartialTicks() - renderManager.getRenderPosY();
            double z = currentTarget.lastTickPosZ + (currentTarget.posZ - currentTarget.lastTickPosZ) * (double) event.getPartialTicks() - renderManager.getRenderPosZ();
            double height = -Math.cos(((System.currentTimeMillis() - startTime) / 1000D) * animationSpeed.getValue()) * (currentTarget.height / 2D) + (currentTarget.height / 2D);
            GL11.glLineWidth(circleWidth.getValue());
            GL11.glBegin(1);
            for (int i = 0; i <= 360; ++i) {
                Vec3d vec = new Vec3d(x + Math.sin((double) i * Math.PI / 180.0) * 0.5D, y + height + 0.01D, z + Math.cos((double) i * Math.PI / 180.0) * 0.5D);
                vecs.add(vec);
            }
            for (int j = 0; j < vecs.size() - 1; ++j) {
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = (rgb) & 0xFF;
                float alpha = orbit.getValue() ?
                        trial.getValue() ? (float) Math.max(0, -(1/Math.PI) * Math.atan(Math.tan((Math.PI * (j+1F) / (float) vecs.size() + (System.currentTimeMillis() / 1000D * orbitSpeed.getValue()))))) :
                        (float) Math.max(0, Math.abs(Math.sin((j+1F)/ (float) vecs.size() * Math.PI + (System.currentTimeMillis() / 1000D * orbitSpeed.getValue()))) * 2 - 1) :
                        fill.getValue() ? 1F : circleColor.getValue().getAlpha() / 255F;
                if (circleColor.getValue().isCycle()) {
                    GL11.glColor4f(red / 255F, green / 255F, blue / 255F, alpha);
                } else {
                    GL11.glColor4f(circleColor.getValue().getRed() / 255F, circleColor.getValue().getGreen() / 255F, circleColor.getValue().getBlue() / 255F, alpha);
                }
                GL11.glVertex3d(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z);
                GL11.glVertex3d(vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z);
                hue += (1F / 360F);
                rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            }
            GL11.glEnd();
            if (fill.getValue()) {
                hue = initialHue;
                GL11.glBegin(GL11.GL_POLYGON);
                for (int j = 0; j < vecs.size() - 1; ++j) {
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = (rgb) & 0xFF;
                    if (circleColor.getValue().isCycle()) {
                        GL11.glColor4f(red / 255F, green / 255F, blue / 255F, circleColor.getValue().getAlpha() / 255F);
                    } else {
                        GL11.glColor4f(circleColor.getValue().getRed() / 255F, circleColor.getValue().getGreen() / 255F, circleColor.getValue().getBlue() / 255F, circleColor.getValue().getAlpha() / 255F);
                    }
                    GL11.glVertex3d(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z);
                    GL11.glVertex3d(vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z);
                    hue += (1F / 360F);
                    rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
                }
                GL11.glEnd();
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            TessellatorUtil.release();
            GlStateManager.popMatrix();
        }
    }
    
    private boolean handlePre() {
        if (disableWhenCA.getValue()) {
            Module ca = ModuleManager.getModuleByClass(AutoCrystal.class);

            if (ca != null) {
                AutoCrystal crystalAura = (AutoCrystal) ca;
                if (ca.isEnabled()) {
                    if (onlyWhenNoTargets.getValue()) {
                        if (crystalAura.getPostBreakPos() != null || crystalAura.getPostPlacePos() != null) {
                            currentTarget = null;
                            if (switchBack.getValue() && switchBackSlot != -1) {
                                mc.player.inventory.currentItem = switchBackSlot;
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                                switchBackSlot = -1;
                            }
                            return false;
                        }
                    } else {
                        currentTarget = null;
                        if (switchBack.getValue() && switchBackSlot != -1) {
                            mc.player.inventory.currentItem = switchBackSlot;
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                            switchBackSlot = -1;
                        }
                        return false;
                    }
                }
            }
        }

        if(swordOnly.getValue()) {
            if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)) return false;
            if (check32k.getValue()) {
                if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, mc.player.getHeldItemMainhand()) < 6) {
                    currentTarget = null;
                    if (switchBack.getValue() && switchBackSlot != -1) {
                        mc.player.inventory.currentItem = switchBackSlot;
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                        switchBackSlot = -1;
                    }
                    return false;
                }
            }
        }

        if (onlyInHoles.getValue()) {
            BlockPos playerPos = new BlockPos(mc.player);
            if (!BlockUtils.isHole(playerPos)) {
                currentTarget = null;
                if (switchBack.getValue() && switchBackSlot != -1) {
                    mc.player.inventory.currentItem = switchBackSlot;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                    switchBackSlot = -1;
                }
                return false;
            }
        }

        List<Entity> targetsInRange = mc.world.loadedEntityList.
                stream()
                .filter(e -> isValidTarget(e, hitRange.getValue()))
                .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                .collect(Collectors.toList());

        if (!targetsInRange.isEmpty()) {
            if (currentTarget == null || !currentTarget.equals(targetsInRange.get(0))) {
                startTime = System.currentTimeMillis();
            }
            currentTarget = targetsInRange.get(0);
        } else {
            currentTarget = null;
        }

        if (autoBlock.getValue() && currentTarget != null && !mc.player.isActiveItemStackBlocking()) {
            if (mc.player.getHeldItemOffhand().getItem() instanceof ItemShield) {
                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
            }
        }

        return true;
    }

    private void handlePost() {
        if (noGapSwitch.getValue() && mc.player.getActiveItemStack().getItem() instanceof ItemFood) return;

        float ticks = 0F;

        if (tpsSyncMode.getValue() == TpsSyncMode.NORMAL) {
            ticks = 20.0F - TickRateUtil.INSTANCE.getTickRate();
        } else if (tpsSyncMode.getValue() == TpsSyncMode.MIN) {
            ticks = 20.0F - TickRateUtil.INSTANCE.getMinTickRate();
        } else if (tpsSyncMode.getValue() == TpsSyncMode.LATEST) {
            ticks = 20.0F - TickRateUtil.INSTANCE.getLatestTickRate();
        }

        if ((mode.getValue() == Mode.STATIC && ticksRun < tickDelay.getValue())) {
            ticksRun++;
        }

        float cooledStr = 1F;

        if (currentTarget != null && currentTarget instanceof EntityShulkerBullet) {
            cooledStr = 0F;
        }

        if ((mode.getValue() == Mode.DYNAMIC && mc.player.getCooledAttackStrength(tpsSyncMode.getValue() != TpsSyncMode.NONE ? -ticks : 0.0F) >= cooledStr) || (mode.getValue() == Mode.STATIC && ticksRun >= tickDelay.getValue())) {
            if (!isValidTarget(currentTarget, hitRange.getValue())) {
                currentTarget = null;
            }
        } else if ((mode.getValue() == Mode.STATIC && ticksRun < tickDelay.getValue())) {
            ticksRun++;
        }

        if ((!onlyWhenFalling.getValue() || mc.player.motionY < 0) && (!onlyInAir.getValue() || mc.world.getBlockState(new BlockPos(mc.player)).getBlock() instanceof BlockAir) && ((mode.getValue() == Mode.DYNAMIC && mc.player.getCooledAttackStrength(tpsSyncMode.getValue() != TpsSyncMode.NONE ? -ticks : 0.0F) >= cooledStr) || (mode.getValue() == Mode.STATIC && ticksRun >= tickDelay.getValue()))) {
            if (currentTarget != null) {
                if (autoSwitch.getValue()) {
                    setSwordSlot(getWeapon());
                }

                boolean sneaking = mc.player.isSneaking();
                boolean sprinting = strict.getValue() && mc.player.isSprinting();
                boolean blocking = mc.player.isActiveItemStackBlocking();

                if (sneaking) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }

                if (sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                if (blocking) {
                    if (mc.player.getHeldItemOffhand().getItem() instanceof ItemShield) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(mc.player), EnumFacing.getFacingFromVector((float)((int) mc.player.posX), (float)((int) mc.player.posY), (float)((int) mc.player.posZ))));
                    }
                }

                mc.playerController.attackEntity(mc.player, currentTarget);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                lastHit.reset();
                ticksRun = 0;

                if (sneaking) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }

                if (sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }

                if (blocking) {
                    if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && mc.player.getHeldItemOffhand().getItem() instanceof ItemShield) {
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
                    }
                }
            } else if (switchBack.getValue() && switchBackSlot != -1) {
                mc.player.inventory.currentItem = switchBackSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                switchBackSlot = -1;
            }
        }
    }

    @Subscriber(priority = 1)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (event.isCancelled() || !InteractionUtil.canPlaceNormally(rotations.getValue() != RotationMode.NONE) || timingMode.getValue() == TimingMode.VANILLA) return;

        if (mc.world == null || mc.player == null) {
            return;
        }

        if (!handlePre()) {
            return;
        }

        boolean doPost = true;

        if (rotations.getValue() != RotationMode.NONE && currentTarget != null) {
            if (rotations.getValue() == RotationMode.HIT) {
                float ticks = 0F;

                if (tpsSyncMode.getValue() == TpsSyncMode.NORMAL) {
                    ticks = 20.0F - TickRateUtil.INSTANCE.getTickRate();
                } else if (tpsSyncMode.getValue() == TpsSyncMode.MIN) {
                    ticks = 20.0F - TickRateUtil.INSTANCE.getMinTickRate();
                } else if (tpsSyncMode.getValue() == TpsSyncMode.LATEST) {
                    ticks = 20.0F - TickRateUtil.INSTANCE.getLatestTickRate();
                }

                if ((mode.getValue() == Mode.STATIC && ticksRun < tickDelay.getValue())) {
                    ticksRun++;
                }

                float cooledStr = 1F;

                if (currentTarget != null && currentTarget instanceof EntityShulkerBullet) {
                    cooledStr = 0F;
                }
                if (lastHit.hasPassed(5000) || yaw == 0D || (mode.getValue() == Mode.DYNAMIC && mc.player.getCooledAttackStrength(tpsSyncMode.getValue() != TpsSyncMode.NONE ? -ticks : 0.0F) >= cooledStr) || (mode.getValue() == Mode.STATIC && ticksRun >= tickDelay.getValue())) {
                    handleSpacialRotation(currentTarget);
                }
            } else {
                handleSpacialRotation(currentTarget);
            }

            if (torqueFactor.getValue() < 1F) {
                float yawDiff = (float) MathHelper.wrapDegrees(yaw - ((IEntityPlayerSP) mc.player).getLastReportedYaw());
                if (Math.abs(yawDiff) > 180 * torqueFactor.getValue()) {
                    yaw = ((IEntityPlayerSP) mc.player).getLastReportedYaw() + (yawDiff * ((180 * torqueFactor.getValue()) / Math.abs(yawDiff)));
                    doPost = false;
                }
            }
            KonasGlobals.INSTANCE.rotationManager.setRotations((float) yaw, (float) pitch);
        }

        if (doPost) {
            handlePost();
        }
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (timingMode.getValue() == TimingMode.SEQUENTIAL) return;

        if (mc.player == null || mc.world == null) return;

        if (!handlePre()) {
            currentTarget = null;
            return;
        }

        handlePost();
    }

    @Subscriber
    private void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof CPacketPlayer
                && rotations.getValue() != RotationMode.NONE && currentTarget != null && timingMode.getValue() == TimingMode.VANILLA) {
            handleSpacialRotation(currentTarget);
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            if (event.getPacket() instanceof CPacketPlayer.Position) {
                event.setCancelled(true);
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(mc.player.posX), packet.getY(mc.player.posY), packet.getZ(mc.player.posZ), (float) yaw, (float) pitch, packet.isOnGround()));
            }
        }
    }

    private boolean isValidTarget(Entity entity, float range) {
        if (entity == mc.player || entity == mc.getRenderViewEntity()) {
            return false;
        }

        if (bullets.getValue() && entity instanceof EntityShulkerBullet && !entity.isDead && rangeCheck(entity, range) && (doRayTrace(entity))) {
            return true;
        }

        if (!(entity instanceof EntityLivingBase)) {
            return false;
        }

        if (!shouldAttack(entity)) {
            return false;
        }

        if (entity.isDead) {
            return false;
        }

        if (((EntityLivingBase) entity).getHealth() <= 0) {
            return false;
        }

        if (!rangeCheck(entity, range)) {
            return false;
        }

        if (!doRayTrace(entity)) {
            return false;
        }

        if (AntiBot.getBots().contains(entity)) {
            return false;
        }

        return true;
    }

    public boolean rangeCheck(Entity e, float range) {
        AxisAlignedBB bb = e.getEntityBoundingBox();

        for (double xS = 0.15D; xS < 0.85D; xS += 0.1D) {
            for (double yS = 0.15D; yS < 0.85D; yS += 0.1D) {
                for (double zS = 0.15D; zS < 0.85D; zS += 0.1D) {
                    Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                    if (mc.player.getDistance(tempVec.x, tempVec.y, tempVec.z) < range) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean doRayTrace(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();

        for (double xS = 0.15D; xS < 0.85D; xS += 0.1D) {
            for (double yS = 0.15D; yS < 0.85D; yS += 0.1D) {
                for (double zS = 0.15D; zS < 0.85D; zS += 0.1D) {
                    Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                    if (isVisible(tempVec)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isVisible(Vec3d vec3d) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox()).minY + mc.player.getEyeHeight(), mc.player.posZ);
        return (mc.world.rayTraceBlocks(eyesPos, vec3d) == null) || vec3d.distanceTo(mc.player.getPositionEyes(1F)) <= wallsRange.getValue();
    }

    public boolean shouldAttack(Entity e) {
        if (animalSetting.getValue() && e instanceof EntityAnimal) {
            return true;
        } else if (mobSetting.getValue() && e instanceof IMob) {
            return true;
        } else if (playerSetting.getValue() && e instanceof EntityPlayer) {
            if(!friendsSetting.getValue()) {
                return !Friends.isFriend(e.getName());
            } else {
                return true;
            }
        }

        return false;

    }

    public void handleSpacialRotation(Entity e) {
        AxisAlignedBB bb = e.getEntityBoundingBox();
        Vec3d gEyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox()).minY + mc.player.getEyeHeight(), mc.player.posZ);

        Vec3d finalVec = null;
        double[] finalRotation = null;

        for (double xS = 0.15D; xS < 0.85D; xS += 0.1D) {
            for (double yS = 0.15D; yS < 0.85D; yS += 0.1D) {
                for (double zS = 0.15D; zS < 0.85D; zS += 0.1D) {
                    Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                    if (isVisible(tempVec)) {
                        double diffX = tempVec.x - gEyesPos.x;
                        double diffY = tempVec.y - gEyesPos.y;
                        double diffZ = tempVec.z - gEyesPos.z;
                        double[] tempRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))};
                        if (finalVec != null && finalRotation != null) {
                            if (Math.hypot((((tempRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                    Math.hypot((((finalRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (finalRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                finalVec = tempVec;
                                finalRotation = tempRotation;
                            }
                        } else {
                            finalVec = tempVec;
                            finalRotation = tempRotation;
                        }
                    }
                }
            }
        }
        if (finalVec != null && finalRotation != null) {
            double yawDiff = ((finalRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F;
            double pitchDiff = ((finalRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()) % 360.0F + 540.0F) % 360.0F - 180.0F;
            double[] finalYawPitch = new double[]{((IEntityPlayerSP) mc.player).getLastReportedYaw() + ((yawDiff > 180.0F) ? 180.0F : Math.max(yawDiff, -180.0F)), ((IEntityPlayerSP) mc.player).getLastReportedPitch() + ((pitchDiff > 180.0F) ? 180.0F : Math.max(pitchDiff, -180.0F))};
            setYawAndPitch((float) finalYawPitch[0], (float) finalYawPitch[1]);
        }
    }

    private static void setYawAndPitch(float yaw1, float pitch1) {
        yaw = yaw1;
        pitch = pitch1;
    }

    public void onDisable() {
        if (mc.player != null) {
            if (switchBack.getValue() && switchBackSlot != -1) {
                mc.player.inventory.currentItem = switchBackSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(switchBackSlot));
                switchBackSlot = -1;
            }
        }
    }

    public int getWeapon() {

        int weaponSlot = -1;

        if (mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD) {
            weaponSlot = mc.player.inventory.currentItem;
        }


        if (weaponSlot == -1) {
            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.DIAMOND_SWORD) {
                    weaponSlot = l;
                    break;
                }
            }
        }

        return weaponSlot;

    }

    public void setSwordSlot(int swordSlot) {
        if (mc.player.inventory.currentItem != swordSlot && swordSlot != -1) {
            if (switchBack.getValue()) {
                switchBackSlot = mc.player.inventory.currentItem;
            }
            mc.player.inventory.currentItem = swordSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(swordSlot));
        }
    }
}
