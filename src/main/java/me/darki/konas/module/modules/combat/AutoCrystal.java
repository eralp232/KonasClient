package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.mixin.mixins.ICPacketUseEntity;
import me.darki.konas.mixin.mixins.IEntityPlayerSP;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.exploit.PacketFly;
import me.darki.konas.module.modules.player.FastUse;
import me.darki.konas.module.modules.render.Nametags;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.setting.SubBind;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.FakePlayerManager;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.client.TickRateUtil;
import me.darki.konas.util.combat.CrystalUtils;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.interaction.RotationManager;
import me.darki.konas.util.render.RenderUtils;
import me.darki.konas.util.render.TessellatorUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AutoCrystal extends Module {
    public static Setting<Parent> antiCheat = new Setting<>("AntiCheat", new Parent(false));
    public static Setting<TimingMode> timingMode = new Setting<>("Timing", TimingMode.ADAPTIVE).withParent(antiCheat).withDescription("Changes how AC is timed");
    public static Setting<RotationMode> rotationMode = new Setting<>("Rotate", RotationMode.TRACK).withParent(antiCheat).withDescription("Spoof rotations server-side");
    public static Setting<Boolean> inhibit = new Setting<>("Inhibit", false).withParent(antiCheat).withDescription("Prevent unnesasary attacks");
    public static Setting<Boolean> limit = new Setting<>("Limit", true).withParent(antiCheat).withDescription("Limit attacks");
    public static Setting<YawStepMode> yawStep = new Setting<>("YawStep", YawStepMode.OFF).withParent(antiCheat).withDescription("Rotate slower");
    public static Setting<Float> yawAngle = new Setting<>("YawAngle", 0.3F, 1F, 0.1F, 0.1F).withParent(antiCheat).withDescription("Maximum angle to rotate by per tick");
    public static Setting<Integer> yawTicks = new Setting<>("YawTicks", 1, 5, 1, 1).withParent(antiCheat).withDescription("Rotate slower by this amount of ticks");
    public static Setting<Boolean> strictDirection = new Setting<>("StrictDirection", true).withParent(antiCheat).withDescription("Bypass stricter anticheats like 2b2t");

    public static Setting<Parent> placements = new Setting<>("Placements", new Parent(false));
    public static Setting<Boolean> check = new Setting<>("Check", true).withParent(placements).withDescription("Check if you will be able to break the crystals you place");
    public static Setting<DirectionMode> directionMode = new Setting<>("Interact", DirectionMode.NORMAL).withParent(placements).withDescription("Changes how you interact with blocks while placing");
    public static Setting<Boolean> protocol = new Setting<>("Protocol", false).withParent(placements).withDescription("Place in 1x1x1 areas");
    public static Setting<Boolean> liquids = new Setting<>("PlaceInLiquids", false).withParent(placements).withDescription("Place in liquids");
    public static Setting<Boolean> fire = new Setting<>("PlaceInFire", false).withParent(placements).withDescription("Place in fires");

    public static Setting<Parent> speeds = new Setting<>("Speeds", new Parent(false));
    public static Setting<ConfirmMode> confirm = new Setting<>("Confirm", ConfirmMode.OFF).withParent(speeds).withDescription("Do not place elsewhere until previous placement has been executed");
    public static Setting<Integer> delay = new Setting<>("Delay", 0, 20, 0, 1).withParent(speeds).withDescription("Tick delay for 2b2t");
    public static Setting<Integer> attackFactor = new Setting<>("AttackFactor", 3, 20, 1, 1).withParent(speeds).withDescription("Attack factor");
    public static Setting<Float> breakSpeed = new Setting<>("BreakSpeed", 20F, 20F, 1F, 0.1F).withParent(speeds).withDescription("Crystal break speed");
    public static Setting<Float> placeSpeed = new Setting<>("PlaceSpeed", 20F, 20F, 1F, 0.1F).withParent(speeds).withDescription("Crystal place speed");
    public static Setting<SyncMode> syncMode = new Setting<>("Sync", SyncMode.STRICT).withParent(speeds).withDescription("Syncronizes breaking and placing");
    public static Setting<Float> mergeOffset = new Setting<>("Offset", 0F, 0.8F, 0F, 0.1F).withParent(speeds).withVisibility(() -> syncMode.getValue() == SyncMode.MERGE).withDescription("Syncronization offset");

    public static Setting<Parent> ranges = new Setting<>("Ranges", new Parent(false));
    public static Setting<Float> enemyRange = new Setting<>("EnemyRange", 8F, 15F, 4F, 0.5F).withParent(ranges).withDescription("Range from which to select target(s)");
    public static Setting<Float> crystalRange = new Setting<>("CrystalRange", 6F, 12F, 2F, 0.5F).withParent(ranges).withDescription("Maximum range between enemies and placements");
    public static Setting<Float> breakRange = new Setting<>("BreakRange", 4.3F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Break range for breaking visible crystals");
    public static Setting<Float> breakWallsRange = new Setting<>("BreakWalls", 1.5F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Break range for breaking crystals through walls");
    public static Setting<Float> placeRange = new Setting<>("PlaceRange", 4F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Place range for visible blocks");
    public static Setting<Float> placeWallsRange = new Setting<>("PlaceWalls", 3F, 6F, 1F, 0.1F).withParent(ranges).withDescription("Place range for placing through walls");

    public static Setting<Parent> swap = new Setting<>("Swap", new Parent(false));
    public static Setting<Boolean> autoSwap = new Setting<>("AutoSwap", false).withParent(swap).withDescription("Auto Swap");
    public static Setting<Float> swapDelay = new Setting<>("SwapDelay", 1F, 20F, 0F, 1F).withParent(swap).withDescription("Delay in ticks for swapping");
    public static Setting<Float> switchDelay = new Setting<>("GhostDelay", 5F, 10F, 0F, 0.5F).withParent(swap).withDescription("Delay for hitting crystals after swapping");
    public static Setting<Boolean> antiWeakness = new Setting<>("AntiWeakness", false).withParent(swap).withDescription("Swap to sword before hitting crystal when weaknessed");

    public static Setting<Parent> damages = new Setting<>("Damages", new Parent(false));
    public static Setting<TargetingMode> targetingMode = new Setting<>("Target", TargetingMode.ALL).withParent(damages).withDescription("Algorithm to use for selecting target(s)");
    public static Setting<Float> security = new Setting<>("Security", 1.0F, 5F, 0.1F, 0.1F).withParent(damages).withDescription("Damage to self damage ratio for breaking enemy crystals");
    public static Setting<Float> compromise = new Setting<>("Compromise", 1F, 2F, 0.05F, 0.05F).withParent(damages).withDescription("Maximum enemy damage to self damage ratio");
    public static Setting<Float> minPlaceDamage = new Setting<>("MinDamage", 6F, 20F, 0F, 0.5F).withParent(damages).withDescription("Minimum amount of damage for placing crystals");
    public static Setting<Float> maxSelfPlace = new Setting<>("MaxSelfDmg", 12F, 20F, 0F, 0.5F).withParent(damages).withDescription("Maximum amount of self damage for placing crystals");
    public static Setting<Float> suicideHealth = new Setting<>("SuicideHealth", 2F, 10F, 0F, 0.5F).withParent(damages).withDescription("Won't break crystals which lower your health under this");
    public static Setting<Float> faceplaceHealth = new Setting<>("FaceplaceHealth", 4F, 20F, 0F, 0.5F).withParent(damages).withDescription("Health at which to start faceplacing enemies");
    public static Setting<SubBind> forceFaceplace = new Setting<>("Faceplace", new SubBind(Keyboard.KEY_LMENU)).withParent(damages);
    private static final Setting<Boolean> armorBreaker = new Setting<>("ArmorBreaker", true).withParent(damages);
    private static final Setting<Float> depletion = new Setting<>("Depletion", 0.9F, 1F, 0.1F, 0.1F).withParent(damages).withVisibility(armorBreaker::getValue);

    public static Setting<Parent> prediction = new Setting<>("Prediction", new Parent(false));
    public static Setting<Boolean> collision = new Setting<>("Collision", false).withParent(prediction).withDescription("Simulate collision when predicting motion");
    public static Setting<Integer> predictTicks = new Setting<>("PredictTicks", 1, 10, 0, 1).withParent(prediction).withDescription("Predict target motion by this amount of ticks");
    public static Setting<Boolean> predictPops = new Setting<>("PredictPops", false).withParent(prediction).withDescription("Predict enemy totem pops client-side");
    public static Setting<Boolean> terrainIgnore = new Setting<>("PredictDestruction", false).withParent(prediction).withDescription("Ignore destructable blocks when doing damage calculations");

    public static Setting<Parent> pause = new Setting<>("Pause", new Parent(false));
    public static Setting<Boolean> noMineSwitch = new Setting<>("Mining", false).withParent(pause);
    public static Setting<Boolean> noGapSwitch = new Setting<>("Gapping", false).withParent(pause);
    public static Setting<Boolean> rightClickGap = new Setting<>("RightClickGap", false).withVisibility(noGapSwitch::getValue).withParent(pause);
    public static Setting<Boolean> disableWhenKA = new Setting<>("KillAura", true).withParent(pause);
    public static Setting<Boolean> disableWhenPA = new Setting<>("PistonAura", true).withParent(pause);
    public static Setting<Float> disableUnderHealth = new Setting<>("Health", 2f, 10f, 0f, 0.5f).withParent(pause);
    public static Setting<Boolean> disableOnTP = new Setting<>("DisableOnTP", false).withParent(pause);

    public static Setting<Parent> render = new Setting<>("Render", new Parent(false));
    public static Setting<Boolean> swing = new Setting<>("Swing", false).withParent(render).withDescription("Swing arm client-side");
    public static Setting<Boolean> renderBox = new Setting<>("Box", true).withParent(render);
    public static Setting<Boolean> renderBreaking = new Setting<>("Breaking", true).withParent(render);
    public static Setting<Float> outlineWidth = new Setting<>("OutlineWidth", 1.5F, 5F, 0F, 0.1F).withParent(render).withVisibility(renderBox::getValue);
    public static Setting<RenderTextMode> renderDmg  = new Setting<>("Damage", RenderTextMode.NONE).withParent(render);
    public static Setting<Boolean> customFont = new Setting<>("CustomFont", true).withParent(render);
    public static Setting<Float> fade = new Setting<>("Fade", 0.0F, 1.0F, 0.0F, 0.1F).withParent(render);
    public static Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x50bf40bf)).withParent(render);
    public static Setting<ColorSetting> outlineColor = new Setting<>("Outline", new ColorSetting(0xFFbf40bf)).withParent(render);
    public static Setting<Boolean> targetRender = new Setting<>("TargetRender", true).withParent(render).withDescription("Render circle around target");
    public static Setting<Boolean> depth = new Setting<>("Depth", true).withParent(render);
    public static Setting<Boolean> fill = new Setting<>("Fill", false).withParent(render);
    public static Setting<Boolean> orbit = new Setting<>("Orbit", true).withParent(render);
    public static Setting<Boolean> trial = new Setting<>("Trail", true).withParent(render);
    public static Setting<Float> orbitSpeed = new Setting<>("OrbitSpeed", 1F, 10F, 0.1F, 0.1F).withParent(render);
    public static Setting<Float> animationSpeed = new Setting<>("AnimSpeed", 1F, 10F, 0.1F, 0.1F).withParent(render);
    public static Setting<Float> circleWidth = new Setting<>("Width", 2.5F, 5F, 0.1F, 0.1F).withParent(render);
    public static Setting<ColorSetting> circleColor = new Setting<>("TargetColor", new ColorSetting(0x33da6464, true)).withParent(render);

    private enum ConfirmMode {
        OFF, SEMI, FULL
    }

    private enum RenderTextMode {
        NONE, FLAT, SHADED
    }

    private enum RotationMode {
        OFF, TRACK, INTERACT
    }

    private enum TimingMode {
        SEQUENTIAL, ADAPTIVE
    }

    private enum YawStepMode {
        OFF, BREAK, FULL
    }

    private enum TargetingMode {
        ALL, SMART, NEAREST
    }

    public enum DirectionMode {
        VANILLA, NORMAL, STRICT
    }

    public enum SyncMode {
        STRICT, MERGE
    }

    public AutoCrystal() {
        super("AutoCrystal", "Automatically places crystals around you", Category.COMBAT);
    }

    private Vec3d rotationVector = null;
    private float[] rotations = new float[]{0F, 0F};
    private Timer rotationTimer = new Timer();

    private EntityEnderCrystal postBreakPos;
    private BlockPos postPlacePos;
    private BlockPos prevPlacePos = null;
    private EnumFacing postFacing;
    private RayTraceResult postResult;

    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();

    private final Timer noGhostTimer = new Timer();
    private final Timer switchTimer = new Timer();

    private BlockPos renderBlock;
    private float renderDamage = 0.0f;
    private final Timer renderTimeoutTimer = new Timer();

    private BlockPos renderBreakingPos;
    private final Timer renderBreakingTimer = new Timer();

    private boolean isPlacing = false;

    private final ConcurrentHashMap<BlockPos, Long> placeLocations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Long> breakLocations = new ConcurrentHashMap<>();

    private final Map<EntityPlayer, Timer> totemPops = new ConcurrentHashMap<EntityPlayer, Timer>();

    private final List<BlockPos> selfPlacePositions = new CopyOnWriteArrayList<>();

    private AtomicBoolean tickRunning = new AtomicBoolean(false);

    private final Timer linearTimer = new Timer();

    private final Timer cacheTimer = new Timer();
    private BlockPos cachePos = null;

    private final Timer inhibitTimer = new Timer();
    private EntityEnderCrystal inhibitEntity = null;

    private final Timer scatterTimer = new Timer();

    private Vec3d bilateralVec = null;

    private Thread thread;
    private AtomicBoolean shouldRunThread = new AtomicBoolean(false);

    private AtomicBoolean lastBroken = new AtomicBoolean(false);

    private EntityPlayer renderTarget;
    private Timer renderTargetTimer = new Timer();

    private int ticks;

    private boolean foundDoublePop = false;

    public void onEnable() {
        postBreakPos = null;
        postPlacePos = null;
        postFacing = null;
        postResult = null;
        prevPlacePos = null;
        cachePos = null;
        bilateralVec = null;
        lastBroken.set(false);

        rotationVector = null;
        rotationTimer.reset();

        isPlacing = false;

        foundDoublePop = false;
        totemPops.clear();
    }

    @Subscriber
    public void onWorldRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (targetRender.getValue() && renderTarget != null && !renderTargetTimer.hasPassed(3500)) {
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
            double x = renderTarget.lastTickPosX + (renderTarget.posX - renderTarget.lastTickPosX) * (double) event.getPartialTicks() - renderManager.getRenderPosX();
            double y = renderTarget.lastTickPosY + (renderTarget.posY - renderTarget.lastTickPosY) * (double) event.getPartialTicks() - renderManager.getRenderPosY();
            double z = renderTarget.lastTickPosZ + (renderTarget.posZ - renderTarget.lastTickPosZ) * (double) event.getPartialTicks() - renderManager.getRenderPosZ();
            double height = -Math.cos(((System.currentTimeMillis()) / 1000D) * animationSpeed.getValue()) * (renderTarget.height / 2D) + (renderTarget.height / 2D);
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

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (event.getPhase() == TickEvent.Phase.START) {
            tickRunning.set(true);
        } else {
            tickRunning.set(false);
        }
    }

    @Subscriber(priority = 50)
    public void onUpdateWalkingPlayerPre(UpdateWalkingPlayerEvent.Pre event) {
        placeLocations.forEach((pos, time) -> {
            if (System.currentTimeMillis() - time > 1500) {
                placeLocations.remove(pos);
            }
        });

        ticks--;

        if (bilateralVec != null) {
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityEnderCrystal && entity.getDistance(bilateralVec.x, bilateralVec.y, bilateralVec.z) <= 6) {
                    breakLocations.put(entity.getEntityId(), System.currentTimeMillis());
                }
            }
            bilateralVec = null;
        }

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally(rotationMode.getValue() != RotationMode.OFF)) return;

        postBreakPos = null;
        postPlacePos = null;
        postFacing = null;
        postResult = null;
        foundDoublePop = false;

        handleSequential();

        if (rotationMode.getValue() != RotationMode.OFF && !rotationTimer.hasPassed(650) && rotationVector != null) {
            if (rotationMode.getValue() == RotationMode.TRACK) {
                rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
            }

            if (yawAngle.getValue() < 1F && yawStep.getValue() != YawStepMode.OFF && (postBreakPos != null || yawStep.getValue() == YawStepMode.FULL)) {
                if (ticks > 0) {
                    rotations[0] = ((IEntityPlayerSP) mc.player).getLastReportedYaw();
                    postBreakPos = null;
                    postPlacePos = null;
                } else {
                    float yawDiff = MathHelper.wrapDegrees(rotations[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw());
                    if (Math.abs(yawDiff) > 180 * yawAngle.getValue()) {
                        rotations[0] = ((IEntityPlayerSP) mc.player).getLastReportedYaw() + (yawDiff * ((180 * yawAngle.getValue()) / Math.abs(yawDiff)));
                        postBreakPos = null;
                        postPlacePos = null;
                        ticks = yawTicks.getValue();
                    }
                }
            }

            KonasGlobals.INSTANCE.rotationManager.setRotations(rotations[0], rotations[1]);
        }
    }

    @Subscriber(priority = 20)
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (postBreakPos != null) {
            if (breakCrystal(postBreakPos)) {
                breakTimer.reset();
                breakLocations.put(postBreakPos.getEntityId(), System.currentTimeMillis());
                for (Entity entity : mc.world.loadedEntityList) {
                    if (entity instanceof EntityEnderCrystal && entity.getDistance(postBreakPos.posX, postBreakPos.posY, postBreakPos.posZ) <= 6) {
                        breakLocations.put(entity.getEntityId(), System.currentTimeMillis());
                    }
                }
                postBreakPos = null;
                if (syncMode.getValue() == SyncMode.MERGE) {
                    runInstantThread();
                }
            }
        } else if (postPlacePos != null) {
            if (!placeCrystal(postPlacePos, postFacing)) {
                shouldRunThread.set(false);
                postPlacePos = null;
                return;
            }

            placeTimer.reset();
            postPlacePos = null;
        }
    }

    private void handleSequential() {
        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount() < disableUnderHealth.getValue()) || (disableWhenKA.getValue() && ModuleManager.getModuleByClass(KillAura.class).isEnabled()) || (disableWhenPA.getValue() && ModuleManager.getModuleByClass(PistonAura.class).isEnabled()) || (noGapSwitch.getValue() && mc.player.getActiveItemStack().getItem() instanceof ItemFood) || (noMineSwitch.getValue() && mc.playerController.getIsHittingBlock() && mc.player.getHeldItemMainhand().getItem() instanceof ItemTool)) {
            rotationVector = null;
            return;
        }

        if (noGapSwitch.getValue() && rightClickGap.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.inventory.getCurrentItem().getItem() instanceof ItemEndCrystal) {
            int gappleSlot = -1;

            for (int l = 0; l < 9; ++l) {
                if (mc.player.inventory.getStackInSlot(l).getItem() == Items.GOLDEN_APPLE) {
                    gappleSlot = l;
                    break;
                }
            }

            if (gappleSlot != -1 && gappleSlot != mc.player.inventory.currentItem && switchTimer.hasPassed(swapDelay.getValue() * 50)) {
                mc.player.inventory.currentItem = gappleSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(gappleSlot));
                switchTimer.reset();
                noGhostTimer.reset();
                return;
            }
        }

        if (!isOffhand() && !(mc.player.inventory.getCurrentItem().getItem() instanceof ItemEndCrystal)) {
            if (!autoSwap.getValue()) {
                return;
            }
        }

        List<EntityPlayer> targetsInRange = getTargetsInRange();

        EntityEnderCrystal crystal = findCrystalTarget(targetsInRange);

        int adjustedResponseTime = (int) Math.max(100, ((CrystalUtils.ping() + 50) / (TickRateUtil.INSTANCE.getLatestTickRate() / 20F))) + 150;

        if (crystal != null) {
            if (breakTimer.hasPassed(1000F - breakSpeed.getValue() * 50F) && (crystal.ticksExisted >= delay.getValue() || timingMode.getValue() == TimingMode.ADAPTIVE)) {
                postBreakPos = crystal;
                handleBreakRotation(postBreakPos.posX, postBreakPos.posY, postBreakPos.posZ);
            }
        }

        if (crystal == null && (confirm.getValue() != ConfirmMode.FULL || inhibitEntity == null || inhibitEntity.ticksExisted >= Math.floor(delay.getValue()))) {
            if ((syncMode.getValue() != SyncMode.STRICT || breakTimer.hasPassed(950F - breakSpeed.getValue() * 50F - CrystalUtils.ping())) && placeTimer.hasPassed( 1000F - placeSpeed.getValue() * 50F) && (timingMode.getValue() == TimingMode.SEQUENTIAL || linearTimer.hasPassed(delay.getValue() * 5F))) {
                if (confirm.getValue() != ConfirmMode.OFF) {
                    if (cachePos != null && !cacheTimer.hasPassed(adjustedResponseTime + 100) && canPlaceCrystal(cachePos)) {
                        postPlacePos = cachePos;
                        postFacing = handlePlaceRotation(postPlacePos);
                        lastBroken.set(false);
                        return;
                    }
                }
                List<BlockPos> blocks = findCrystalBlocks();
                if (!blocks.isEmpty()) {
                    BlockPos candidatePos = findPlacePosition(blocks, targetsInRange);
                    if (candidatePos != null) {
                        postPlacePos = candidatePos;
                        postFacing = handlePlaceRotation(postPlacePos);
                    }
                }
            }
        }

        lastBroken.set(false);
    }

    private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double d0 = (x1 - x2);
        double d1 = (y1 - y2);
        double d2 = (z1 - z2);
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    private void doInstant() {
        if (confirm.getValue() != ConfirmMode.OFF && (confirm.getValue() != ConfirmMode.FULL || inhibitEntity == null || inhibitEntity.ticksExisted >= Math.floor(delay.getValue()))) {
            int adjustedResponseTime = (int) Math.max(100, ((CrystalUtils.ping() + 50) / (TickRateUtil.INSTANCE.getLatestTickRate() / 20F))) + 150;
            if (cachePos != null && !cacheTimer.hasPassed(adjustedResponseTime + 100) && canPlaceCrystal(cachePos)) {
                postPlacePos = cachePos;
                postFacing = handlePlaceRotation(postPlacePos);
                if (postPlacePos != null) {
                    if (!placeCrystal(postPlacePos, postFacing)) {
                        postPlacePos = null;
                        return;
                    }

                    placeTimer.reset();
                    postPlacePos = null;
                }
                return;
            }
        }
        List<BlockPos> blocks = findCrystalBlocks();
        if (!blocks.isEmpty()) {
            BlockPos candidatePos = findPlacePosition(blocks, getTargetsInRange());
            if (candidatePos != null) {
                postPlacePos = candidatePos;
                postFacing = handlePlaceRotation(postPlacePos);
                if (postPlacePos != null) {
                    if (!placeCrystal(postPlacePos, postFacing)) {
                        postPlacePos = null;
                        return;
                    }

                    placeTimer.reset();
                    postPlacePos = null;
                }
            }
        }
    }

    private void runInstantThread() {
        if (mergeOffset.getValue() == 0F) {
            doInstant();
        } else {
            shouldRunThread.set(true);
            if (thread == null || thread.isInterrupted() || !thread.isAlive()) {
                if (thread == null) {
                    thread = new Thread(InstantThread.getInstance(this));
                }
                if (thread != null && (thread.isInterrupted() || !thread.isAlive())) {
                    thread = new Thread(InstantThread.getInstance(this));
                }
                if (thread != null && thread.getState() == Thread.State.NEW) {
                    try {
                        thread.start();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private static class InstantThread implements Runnable {
        private static InstantThread INSTANCE;
        private AutoCrystal autoCrystal;

        private static InstantThread getInstance(AutoCrystal crystalAura) {
            if (INSTANCE == null) {
                INSTANCE = new InstantThread();
                InstantThread.INSTANCE.autoCrystal = crystalAura;
            }
            return INSTANCE;
        }

        @Override
        public void run() {
            if (autoCrystal.shouldRunThread.get()) {
                try {
                    Thread.sleep((long) (mergeOffset.getValue() * 40F));
                }
                catch (InterruptedException e) {
                    autoCrystal.thread.interrupt();
                }

                if (!autoCrystal.shouldRunThread.get()) return;

                autoCrystal.shouldRunThread.set(false);

                if (autoCrystal.tickRunning.get()) return;

                autoCrystal.doInstant();
            }
        }
    }

    @Subscriber
    public void onSpawnCrystal(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSpawnObject) {
            SPacketSpawnObject packetSpawnObject = (SPacketSpawnObject) event.getPacket();
            if (packetSpawnObject.getType() == 51) {
                placeLocations.forEach((pos, time) -> {
                    if (getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, packetSpawnObject.getX(), packetSpawnObject.getY() - 1, packetSpawnObject.getZ()) < 1) {
                        try {
                            placeLocations.remove(pos);
                            cachePos = null;
                            if (!limit.getValue() && inhibit.getValue()) {
                                scatterTimer.reset();
                            }
                        } catch (ConcurrentModificationException ignored) {

                        }

                        if (timingMode.getValue() != TimingMode.ADAPTIVE) return;

                        if (!noGhostTimer.hasPassed(switchDelay.getValue() * 100F)) return;

                        if (tickRunning.get()) return;

                        if (mc.player.isPotionActive(MobEffects.WEAKNESS)) return;

                        if (breakLocations.containsKey(packetSpawnObject.getEntityID())) {
                            return;
                        }

                        if ((mc.player.getHealth() + mc.player.getAbsorptionAmount() < disableUnderHealth.getValue()) || (disableWhenKA.getValue() && ModuleManager.getModuleByClass(KillAura.class).isEnabled()) || (disableWhenPA.getValue() && ModuleManager.getModuleByClass(PistonAura.class).isEnabled()) || (noGapSwitch.getValue() && mc.player.getActiveItemStack().getItem() instanceof ItemFood) || (noMineSwitch.getValue() && mc.playerController.getIsHittingBlock() && mc.player.getHeldItemMainhand().getItem() instanceof ItemTool)) {
                            rotationVector = null;
                            return;
                        }

                        if (mc.player.getPositionEyes(1F).distanceTo(new Vec3d(packetSpawnObject.getX(), packetSpawnObject.getY(), packetSpawnObject.getZ())) > breakRange.getValue()) {
                            return;
                        }

                        if (!(breakTimer.hasPassed(1000F - breakSpeed.getValue() * 50F))) {
                            return;
                        }

                        if (CrystalUtils.calculateDamage(packetSpawnObject.getX(), packetSpawnObject.getY(), packetSpawnObject.getZ(), mc.player) + suicideHealth.getValue() >= mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
                            return;
                        }

                        breakLocations.put(packetSpawnObject.getEntityID(), System.currentTimeMillis());
                        bilateralVec = new Vec3d(packetSpawnObject.getX(), packetSpawnObject.getY(), packetSpawnObject.getZ());

                        CPacketUseEntity packetUseEntity = new CPacketUseEntity();
                        ((ICPacketUseEntity) packetUseEntity).setEntityId(packetSpawnObject.getEntityID());
                        ((ICPacketUseEntity) packetUseEntity).setAction(CPacketUseEntity.Action.ATTACK);
                        mc.player.connection.sendPacket(new CPacketAnimation(isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
                        mc.player.connection.sendPacket(packetUseEntity);
                        swingArmAfterBreaking(isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                        renderBreakingPos = new BlockPos(packetSpawnObject.getX(), packetSpawnObject.getY() - 1D, packetSpawnObject.getZ());
                        renderBreakingTimer.reset();
                        breakTimer.reset();
                        linearTimer.reset();
                        if (syncMode.getValue() == SyncMode.MERGE) {
                            placeTimer.setTime(0);
                        }
                        if (syncMode.getValue() == SyncMode.STRICT) {
                            lastBroken.set(true);
                        }
                        if (syncMode.getValue() == SyncMode.MERGE) {
                            runInstantThread();
                        }
                    }
                });
            }
        } else if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                if (inhibitEntity != null && inhibitEntity.getDistance(packet.getX(), packet.getY(), packet.getZ()) < 6) {
                    inhibitEntity = null;
                }
                if (security.getValue() >= 0.5F) {
                    try {
                        selfPlacePositions.remove(new BlockPos(packet.getX(), packet.getY() - 1, packet.getZ()));
                    } catch (ConcurrentModificationException ignored) {

                    }
                }
            }
        } else if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus)event.getPacket();
            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                totemPops.put((EntityPlayer) packet.getEntity(mc.world), new Timer());
            }
        } else if (event.getPacket() instanceof SPacketPlayerPosLook && disableOnTP.getValue() && !ModuleManager.getModuleByClass(PacketFly.class).isEnabled()) {
            toggle();
        }
    }

    public boolean placeCrystal(BlockPos pos, EnumFacing facing) {
        if (pos != null) {
            if (autoSwap.getValue()) {
                if (switchTimer.hasPassed(swapDelay.getValue() * 50)) {
                    if (!setCrystalSlot()) return false;
                } else {
                    return false;
                }
            }

            if (!isOffhand() && mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL)
                return false;

            if (mc.world.getBlockState(pos.up()).getBlock() == Blocks.FIRE) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos.up(), EnumFacing.DOWN));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos.up(), EnumFacing.DOWN));
                return true;
            }

            FastUse.ignore = true;
            isPlacing = true;
            if (postResult == null) {
                BlockUtils.rightClickBlock(pos, mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0), isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, facing, true);
            } else {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (float) postResult.hitVec.x, (float) postResult.hitVec.y, (float) postResult.hitVec.z));
                mc.player.connection.sendPacket(new CPacketAnimation(isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
            }
            if (foundDoublePop && renderTarget != null) {
                totemPops.put(renderTarget, new Timer());
            }
            isPlacing = false;
            placeLocations.put(pos, System.currentTimeMillis());
            if (security.getValue() >= 0.5F) {
                selfPlacePositions.add(pos);
            }
            renderTimeoutTimer.reset();
            prevPlacePos = pos;
            return true;
        }
        return false;
    }

    private boolean breakCrystal(EntityEnderCrystal targetCrystal) {
        if (!noGhostTimer.hasPassed(switchDelay.getValue() * 100F)) return false;
        if (targetCrystal != null) {
            if (antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS) && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)) {
                setSwordSlot();
                return false;
            }

            mc.playerController.attackEntity(mc.player, targetCrystal);
            mc.player.connection.sendPacket(new CPacketAnimation(isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
            swingArmAfterBreaking(isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
            if (syncMode.getValue() == SyncMode.MERGE) {
                placeTimer.setTime(0);
            }
            if (syncMode.getValue() == SyncMode.STRICT) {
                lastBroken.set(true);
            }
            inhibitTimer.reset();
            inhibitEntity = targetCrystal;
            renderBreakingPos = new BlockPos(targetCrystal).down();
            renderBreakingTimer.reset();
            return true;
        }
        return false;
    }

    private void swingArmAfterBreaking(EnumHand hand) {
        if (!swing.getValue()) return;
        ItemStack stack = mc.player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.getItem().onEntitySwing(mc.player, stack)) {
            return;
        }
        if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= getSwingAnimTime(mc.player) / 2 || mc.player.swingProgressInt < 0) {
            mc.player.swingProgressInt = -1;
            mc.player.isSwingInProgress = true;
            mc.player.swingingHand = hand;
        }
    }

    private int getSwingAnimTime(EntityLivingBase entity) {
        if (entity.isPotionActive(MobEffects.HASTE)) {
            return 6 - (1 + entity.getActivePotionEffect(MobEffects.HASTE).getAmplifier());
        } else {
            return entity.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + entity.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
        }
    }

    @Subscriber
    public void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        if (renderBox.getValue() && renderBlock != null) {
            if (renderTimeoutTimer.hasPassed(1000L)) {
                return;
            }

            AxisAlignedBB axisAlignedBB = null;

            try {
                axisAlignedBB = mc.world.getBlockState(renderBlock).getBoundingBox(mc.world, renderBlock).offset(renderBlock);
            } catch (Exception e) {

            }

            if (axisAlignedBB == null) {
                return;
            }

            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(axisAlignedBB, color.getValue().withAlpha((int) (color.getValue().getAlpha() * (1F - (Math.max(0, System.currentTimeMillis() - renderTimeoutTimer.getTime() - 150) / 850F) * fade.getValue()))));
            if (outlineWidth.getValue() > 0F) {
                TessellatorUtil.drawBoundingBox(axisAlignedBB, outlineWidth.getValue(), outlineColor.getValue().withAlpha((int) (outlineColor.getValue().getAlpha() * (1F - (Math.max(0, System.currentTimeMillis() - renderTimeoutTimer.getTime() - 150) / 850F) * fade.getValue()))));
            }
            TessellatorUtil.release();
        }

        if (renderBreaking.getValue() && renderBreakingPos != null) {
            if (!renderBreakingTimer.hasPassed(1000L) && !renderBreakingPos.equals(renderBlock)) {
                AxisAlignedBB axisAlignedBB = null;

                try {
                    axisAlignedBB = mc.world.getBlockState(renderBreakingPos).getBoundingBox(mc.world, renderBreakingPos).offset(renderBreakingPos);
                } catch (Exception e) {

                }

                if (axisAlignedBB == null) {
                    return;
                }

                TessellatorUtil.prepare();
                TessellatorUtil.drawBox(axisAlignedBB, color.getValue().withAlpha((int) (color.getValue().withAlpha((int) (color.getValue().getAlpha() * 0.5D)).getAlpha() * (1F - (Math.max(0, System.currentTimeMillis() - renderBreakingTimer.getTime() - 150) / 850F) * fade.getValue()))));
                if (outlineWidth.getValue() > 0F) {
                    TessellatorUtil.drawBoundingBox(axisAlignedBB, outlineWidth.getValue(), outlineColor.getValue().withAlpha((int) (outlineColor.getValue().getAlpha() * (1F - (Math.max(0, System.currentTimeMillis() - renderBreakingTimer.getTime() - 150) / 850F) * fade.getValue()))));
                }
                TessellatorUtil.release();
            }
        }

        if (renderDmg.getValue() != RenderTextMode.NONE && renderBlock != null) {
            if (renderTimeoutTimer.hasPassed(1000L)) {
                return;
            }
            GlStateManager.pushMatrix();
            try {
                RenderUtils.glBillboardDistanceScaled((float) renderBlock.getX() + 0.5f, (float) renderBlock.getY() + 0.5f, (float) renderBlock.getZ() + 0.5f, mc.player, 1);
            } catch (Exception e) {

            }
            String damageText = (Math.floor(renderDamage) == renderDamage ? (int) renderDamage : String.format("%.1f", renderDamage)) + "";
            GlStateManager.disableDepth();
            if (customFont.getValue()) {
                GlStateManager.disableTexture2D();
            }
            GlStateManager.disableLighting();
            GL11.glColor4f(1, 1, 1, 1);
            if (customFont.getValue()) {
                GlStateManager.scale(0.3, 0.3, 1);
                if (renderDmg.getValue() == RenderTextMode.SHADED) {
                    Nametags.highResFontRenderer.drawStringWithShadow(damageText, (float) -(Nametags.highResFontRenderer.getStringWidth(damageText) / 2.0D), (int) (-Nametags.highResFontRenderer.getStringHeight(damageText) / 2F), -1);
                } else {
                    Nametags.highResFontRenderer.drawString(damageText, (float) -(Nametags.highResFontRenderer.getStringWidth(damageText) / 2.0D), (int) (-Nametags.highResFontRenderer.getStringHeight(damageText) / 2F), -1);
                }
                GlStateManager.scale(1/0.3, 1/0.3, 1);
            } else {
                if (renderDmg.getValue() == RenderTextMode.SHADED) {
                    mc.fontRenderer.drawStringWithShadow(damageText, (int) -(mc.fontRenderer.getStringWidth(damageText) / 2.0D), -4, -1);
                } else {
                    mc.fontRenderer.drawString(damageText, (int) -(mc.fontRenderer.getStringWidth(damageText) / 2.0D), -4, -1);
                }
            }
            GlStateManager.enableLighting();
            if (customFont.getValue()) {
                GlStateManager.enableTexture2D();
            }
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    public EntityEnderCrystal getPostBreakPos() {
        return postBreakPos;
    }

    public BlockPos getPostPlacePos() {
        return postPlacePos;
    }

    private List<Entity> getCrystalInRange() {
        return mc.world.loadedEntityList.stream()
                .filter(e -> e instanceof EntityEnderCrystal)
                .filter(e -> isValidCrystalTarget((EntityEnderCrystal) e))
                .collect(Collectors.toList());
    }

    private boolean isValidCrystalTarget(EntityEnderCrystal crystal) {
        if (mc.player.getPositionEyes(1F).distanceTo(crystal.getPositionVector()) > breakRange.getValue()) return false;
        if (breakLocations.containsKey(crystal.getEntityId()) && limit.getValue()) return false;
        if (breakLocations.containsKey(crystal.getEntityId()) && crystal.ticksExisted > delay.getValue() + attackFactor.getValue()) return false;
        if (CrystalUtils.calculateDamage(crystal, mc.player) + suicideHealth.getValue() >= mc.player.getHealth() + mc.player.getAbsorptionAmount()) return false;
        return true;
    }

    private EntityEnderCrystal findCrystalTarget(List<EntityPlayer> targetsInRange) {
        breakLocations.forEach((id, time) -> {
            if (System.currentTimeMillis() - time > 1000) {
                breakLocations.remove(id);
            }
        });

        if (syncMode.getValue() == SyncMode.STRICT && !limit.getValue() && lastBroken.get()) {
            return null;
        }

        EntityEnderCrystal bestCrystal = null;

        int adjustedResponseTime = (int) Math.max(100, ((CrystalUtils.ping() + 50) / (TickRateUtil.INSTANCE.getLatestTickRate() / 20F))) + 150;

        if (inhibit.getValue() && !limit.getValue() && !inhibitTimer.hasPassed(adjustedResponseTime) && inhibitEntity != null) {
            if (mc.world.getEntityByID(inhibitEntity.getEntityId()) != null && isValidCrystalTarget(inhibitEntity)) {
                bestCrystal = inhibitEntity;
                return bestCrystal;
            }
        }

        List<Entity> crystalsInRange = getCrystalInRange();

        if (crystalsInRange.isEmpty()) return null;

        if (security.getValue() >= 1F) {
            double bestDamage = 0.5D;

            for (Entity eCrystal : crystalsInRange) {
                if (eCrystal.getPositionVector().distanceTo(mc.player.getPositionEyes(1F)) < breakWallsRange.getValue() || CrystalUtils.rayTraceBreak(eCrystal.posX, eCrystal.posY, eCrystal.posZ)) {
                    EntityEnderCrystal crystal = (EntityEnderCrystal) eCrystal;

                    double damage = 0.0D;

                    for (EntityPlayer target : targetsInRange) {
                        double targetDamage = CrystalUtils.calculateDamage(crystal, target);
                        damage += targetDamage;
                    }

                    double selfDamage = CrystalUtils.calculateDamage(crystal, mc.player);

                    if (selfDamage > damage * (security.getValue() - 0.8F) && !selfPlacePositions.contains(new BlockPos(eCrystal.posX, eCrystal.posY - 1, eCrystal.posZ))) continue;

                    if (damage > bestDamage) {
                        bestDamage = damage;
                        bestCrystal = crystal;
                    }
                }
            }
        } else if (security.getValue() >= 0.5F) {
            bestCrystal = (EntityEnderCrystal) crystalsInRange.stream()
                    .filter(c -> selfPlacePositions.contains(new BlockPos(c.posX, c.posY - 1, c.posZ)))
                    .filter(c -> c.getPositionVector().distanceTo(mc.player.getPositionEyes(1F)) < breakWallsRange.getValue() || CrystalUtils.rayTraceBreak(c.posX, c.posY, c.posZ))
                    .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                    .orElse(null);
        } else {
            bestCrystal = (EntityEnderCrystal) crystalsInRange.stream()
                    .filter(c -> c.getPositionVector().distanceTo(mc.player.getPositionEyes(1F)) < breakWallsRange.getValue() || CrystalUtils.rayTraceBreak(c.posX, c.posY, c.posZ))
                    .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                    .orElse(null);
        }

        return bestCrystal;
    }

    private boolean shouldArmorBreak(EntityPlayer target) {
        if (!armorBreaker.getValue()) return false;
        for (int index = 3; index >= 0; --index) {
            ItemStack armourStack = target.inventory.armorInventory.get(index);
            if (armourStack != null) {
                double health = armourStack.getItem().getDurabilityForDisplay(armourStack);
                if (health > depletion.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos findPlacePosition(List<BlockPos> blocks, List<EntityPlayer> targets) {
        if (targets.isEmpty()) return null;

        float maxDamage = 0.5F;
        EntityPlayer currentTarget = null;
        BlockPos currentPos = null;
        foundDoublePop = false;

        EntityPlayer targetedPlayer = null;

        for (BlockPos pos : blocks) {
            float selfDamage = CrystalUtils.calculateDamage(pos, mc.player);
            if (!((double)selfDamage + suicideHealth.getValue() < mc.player.getHealth() + mc.player.getAbsorptionAmount()) || !(selfDamage <= maxSelfPlace.getValue())) continue;
            if (targetingMode.getValue() != TargetingMode.ALL) {
                targetedPlayer = targets.get(0);
                if (targetedPlayer.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > crystalRange.getValue()) continue;
                float playerDamage = CrystalUtils.calculateDamage(pos, targetedPlayer);
                if (isDoublePoppable(targetedPlayer, playerDamage) && (currentPos == null || targetedPlayer.getDistanceSq(pos) < targetedPlayer.getDistanceSq(currentPos))) {
                    currentTarget = targetedPlayer;
                    maxDamage = playerDamage;
                    currentPos = pos;
                    foundDoublePop = true;
                    continue;
                }
                if (foundDoublePop || !(playerDamage > maxDamage) || !(playerDamage * compromise.getValue() > selfDamage) && !(playerDamage > targetedPlayer.getHealth() + targetedPlayer.getAbsorptionAmount())) continue;
                if (playerDamage < minPlaceDamage.getValue() && targetedPlayer.getHealth() + targetedPlayer.getAbsorptionAmount() > faceplaceHealth.getValue() && !PlayerUtils.isKeyDown(forceFaceplace.getValue().getKeyCode()) && !shouldArmorBreak(targetedPlayer)) continue;
                maxDamage = playerDamage;
                currentTarget = targetedPlayer;
                currentPos = pos;
                continue;
            }
            for (EntityPlayer player : targets) {
                if (player.equals(targetedPlayer)) continue;
                if (player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > crystalRange.getValue()) continue;
                float playerDamage = CrystalUtils.calculateDamage(pos, player);
                if (isDoublePoppable(player, playerDamage) && (currentPos == null || player.getDistanceSq(pos) < player.getDistanceSq(currentPos))) {
                    currentTarget = player;
                    maxDamage = playerDamage;
                    currentPos = pos;
                    foundDoublePop = true;
                    continue;
                }
                if (foundDoublePop || !(playerDamage > maxDamage) || !(playerDamage * compromise.getValue() > selfDamage) && !(playerDamage > player.getHealth() + player.getAbsorptionAmount()))
                    continue;
                if (playerDamage < minPlaceDamage.getValue() && player.getHealth() + player.getAbsorptionAmount() > faceplaceHealth.getValue() && !PlayerUtils.isKeyDown(forceFaceplace.getValue().getKeyCode()) && !shouldArmorBreak(player)) continue;
                maxDamage = playerDamage;
                currentTarget = player;
                currentPos = pos;
            }
        }

        if (currentTarget != null && currentPos != null) {
            KonasGlobals.INSTANCE.targetManager.addTarget(currentTarget);
            setExtraInfo(currentTarget.getName());
            renderTarget = currentTarget;
            renderTargetTimer.reset();
        } else {
            setExtraInfo(null);
        }

        if (currentPos != null) {
            renderBlock = currentPos;
            renderDamage = maxDamage;
        }

        cachePos = currentPos;
        cacheTimer.reset();

        return currentPos;
    }

    private boolean isDoublePoppable(EntityPlayer player, float damage) {
        if (predictPops.getValue() && player.getHealth() + player.getAbsorptionAmount() <= 2F && (double) damage > (double) player.getHealth() + player.getAbsorptionAmount() + 0.5 && damage <= 4F) {
            Timer timer = totemPops.get(player);
            return timer == null || timer.hasPassed(500);
        }
        return false;
    }

    public void handleBreakRotation(double x, double y, double z) {
        if (rotationMode.getValue() != RotationMode.OFF) {
            if (rotationMode.getValue() == RotationMode.INTERACT && rotationVector != null && !rotationTimer.hasPassed(650)) {
                if (rotationVector.y < y - 0.1) {
                    rotationVector = new Vec3d(rotationVector.x, y, rotationVector.z);
                }
                rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
                rotationTimer.reset();
                return;
            }

            AxisAlignedBB bb = new AxisAlignedBB(x - 1D, y, z - 1D, x + 1D, y + 2D, z + 1D);

            Vec3d gEyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()), mc.player.posZ);

            double increment = 0.1D;
            double start = 0.15D;
            double end = 0.85D;

            if (bb.intersects(mc.player.getEntityBoundingBox())) {
                start = 0.4D;
                end = 0.6D;
                increment = 0.05D;
            }

            Vec3d finalVec = null;
            double[] finalRotation = null;
            boolean finalVisible = false;

            for (double xS = start; xS <= end; xS += increment) {
                for (double yS = start; yS <= end; yS += increment) {
                    for (double zS = start; zS <= end; zS += increment) {
                        Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                        double diffX = tempVec.x - gEyesPos.x;
                        double diffY = tempVec.y - gEyesPos.y;
                        double diffZ = tempVec.z - gEyesPos.z;
                        double[] tempRotation = new double[]{MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float)-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))};

                        boolean isVisible = true;

                        if (directionMode.getValue() != DirectionMode.VANILLA) {
                            if (!CrystalUtils.isVisible(tempVec)) {
                                isVisible = false;
                            }
                        }

                        if (strictDirection.getValue()) {
                            if (finalVec != null && finalRotation != null) {
                                if ((isVisible || !finalVisible)) {
                                    if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(tempVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(finalVec)) {
                                        finalVec = tempVec;
                                        finalRotation = tempRotation;
                                    }
                                }
                            } else {
                                finalVec = tempVec;
                                finalRotation = tempRotation;
                                finalVisible = isVisible;
                            }
                        } else {
                            if (finalVec != null && finalRotation != null) {
                                if (isVisible || !finalVisible) {
                                    if (Math.hypot((((tempRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                            Math.hypot((((finalRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (finalRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                        finalVec = tempVec;
                                        finalRotation = tempRotation;
                                    }
                                }
                            } else {
                                finalVec = tempVec;
                                finalRotation = tempRotation;
                                finalVisible = isVisible;
                            }
                        }
                    }
                }
            }
            if (finalVec != null && finalRotation != null) {
                rotationTimer.reset();
                rotationVector = finalVec;
                rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
            }
        }
    }

    public EnumFacing handlePlaceRotation(BlockPos pos) {
        if (pos == null || mc.player == null) {
            return null;
        }
        EnumFacing facing = null;
        if (directionMode.getValue() != DirectionMode.VANILLA) {
            Vec3d placeVec = null;
            double[] placeRotation = null;

            double increment = 0.45D;
            double start = 0.05D;
            double end = 0.95D;

            Vec3d eyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()), mc.player.posZ);

            for (double xS = start; xS <= end; xS += increment) {
                for (double yS = start; yS <= end; yS += increment) {
                    for (double zS = start; zS <= end; zS += increment) {
                        Vec3d posVec = (new Vec3d(pos)).add(xS, yS, zS);

                        double distToPosVec = eyesPos.distanceTo(posVec);
                        double diffX = posVec.x - eyesPos.x;
                        double diffY = posVec.y - eyesPos.y;
                        double diffZ = posVec.z - eyesPos.z;
                        double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

                        double[] tempPlaceRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))};

                        // inline values for slightly better perfornamce
                        float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                        float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                        float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                        float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                        Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                        Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);

                        RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, true, false);
                        if (placeWallsRange.getValue() >= placeRange.getValue() || (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.getBlockPos().equals(pos))) {
                            Vec3d currVec = posVec;
                            double[] currRotation = tempPlaceRotation;

                            if (strictDirection.getValue()) {
                                if (placeVec != null && placeRotation != null && ((rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) || facing == null)) {
                                    if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(currVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(placeVec)) {
                                        placeVec = currVec;
                                        placeRotation = currRotation;
                                        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                            facing = rayTraceResult.sideHit;
                                            postResult = rayTraceResult;
                                        }
                                    }
                                } else {
                                    placeVec = currVec;
                                    placeRotation = currRotation;
                                    if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                        facing = rayTraceResult.sideHit;
                                        postResult = rayTraceResult;
                                    }
                                }
                            } else {
                                if (placeVec != null && placeRotation != null && ((rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) || facing == null)) {
                                    if (Math.hypot((((currRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (currRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                            Math.hypot((((placeRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (placeRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                        placeVec = currVec;
                                        placeRotation = currRotation;
                                        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                            facing = rayTraceResult.sideHit;
                                            postResult = rayTraceResult;
                                        }
                                    }
                                } else {
                                    placeVec = currVec;
                                    placeRotation = currRotation;
                                    if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                        facing = rayTraceResult.sideHit;
                                        postResult = rayTraceResult;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (placeWallsRange.getValue() < placeRange.getValue() && directionMode.getValue() == DirectionMode.STRICT) {
                if (placeRotation != null && facing != null) {
                    rotationTimer.reset();
                    rotationVector = placeVec;
                    rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
                    return facing;
                } else {
                    for (double xS = start; xS <= end; xS += increment) {
                        for (double yS = start; yS <= end; yS += increment) {
                            for (double zS = start; zS <= end; zS += increment) {
                                Vec3d posVec = (new Vec3d(pos)).add(xS, yS, zS);

                                double distToPosVec = eyesPos.distanceTo(posVec);
                                double diffX = posVec.x - eyesPos.x;
                                double diffY = posVec.y - eyesPos.y;
                                double diffZ = posVec.z - eyesPos.z;
                                double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

                                double[] tempPlaceRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))};

                                // inline values for slightly better perfornamce
                                float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                                float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                                float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                                float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                                Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                                Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);

                                RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, true, true);
                                if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                    Vec3d currVec = posVec;
                                    double[] currRotation = tempPlaceRotation;

                                    if (strictDirection.getValue()) {
                                        if (placeVec != null && placeRotation != null && ((rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) || facing == null)) {
                                            if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(currVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(placeVec)) {
                                                placeVec = currVec;
                                                placeRotation = currRotation;
                                                if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                    facing = rayTraceResult.sideHit;
                                                    postResult = rayTraceResult;
                                                }
                                            }
                                        } else {
                                            placeVec = currVec;
                                            placeRotation = currRotation;
                                            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                facing = rayTraceResult.sideHit;
                                                postResult = rayTraceResult;
                                            }
                                        }
                                    } else {
                                        if (placeVec != null && placeRotation != null && ((rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) || facing == null)) {
                                            if (Math.hypot((((currRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (currRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                                    Math.hypot((((placeRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (placeRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                                placeVec = currVec;
                                                placeRotation = currRotation;
                                                if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                    facing = rayTraceResult.sideHit;
                                                    postResult = rayTraceResult;
                                                }
                                            }
                                        } else {
                                            placeVec = currVec;
                                            placeRotation = currRotation;
                                            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                facing = rayTraceResult.sideHit;
                                                postResult = rayTraceResult;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (placeRotation != null) {
                    rotationTimer.reset();
                    rotationVector = placeVec;
                    rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
                }
                if (facing != null) {
                    return facing;
                }
            }
        } else {
            EnumFacing bestFacing = null;
            Vec3d bestVector = null;
            for (EnumFacing enumFacing : EnumFacing.values()) {
                Vec3d cVector = new Vec3d(pos.getX() + 0.5 + enumFacing.getDirectionVec().getX() * 0.5,
                        pos.getY() + 0.5 + enumFacing.getDirectionVec().getY() * 0.5,
                        pos.getZ() + 0.5 + enumFacing.getDirectionVec().getZ() * 0.5);
                RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), cVector, false, true, false);
                if (rayTraceResult != null && rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) && rayTraceResult.getBlockPos().equals(pos)) {
                    if (strictDirection.getValue()) {
                        if (bestVector == null || mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(cVector) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(bestVector)) {
                            bestVector = cVector;
                            bestFacing = enumFacing;
                            postResult = rayTraceResult;
                        }
                    } else {
                        rotationTimer.reset();
                        rotationVector = cVector;
                        rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
                        return enumFacing;
                    }
                }
            }
            if (bestFacing != null) {
                rotationTimer.reset();
                rotationVector = bestVector;
                rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
                return bestFacing;
            } else if (strictDirection.getValue()) {
                for (EnumFacing enumFacing : EnumFacing.values()) {
                    Vec3d cVector = new Vec3d(pos.getX() + 0.5 + enumFacing.getDirectionVec().getX() * 0.5,
                            pos.getY() + 0.5 + enumFacing.getDirectionVec().getY() * 0.5,
                            pos.getZ() + 0.5 + enumFacing.getDirectionVec().getZ() * 0.5);
                    if (bestVector == null || mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(cVector) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(bestVector)) {
                        bestVector = cVector;
                        bestFacing = enumFacing;
                    }
                }
                if (bestFacing != null)  {
                    rotationTimer.reset();
                    rotationVector = bestVector;
                    rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
                    return bestFacing;
                }
            }
        }
        if ((double) pos.getY() > mc.player.posY + (double) mc.player.getEyeHeight()) {
            rotationTimer.reset();
            rotationVector = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
            return EnumFacing.DOWN;
        }
        rotationTimer.reset();
        rotationVector = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        rotations = RotationManager.calculateAngle(mc.player.getPositionEyes(1F), rotationVector);
        return EnumFacing.UP;
    }

    private List<BlockPos> findCrystalBlocks() {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(getSphere(new BlockPos(mc.player), strictDirection.getValue() ? placeRange.getValue() + 2F : placeRange.getValue(), placeRange.getValue().intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public boolean canPlaceCrystal(BlockPos blockPos) {
        if (!(mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)) return false;

        BlockPos boost = blockPos.add(0, 1, 0);

        if (!(mc.world.getBlockState(boost).getBlock() == Blocks.AIR)) {
            if (!((mc.world.getBlockState(boost).getBlock() == Blocks.FIRE && fire.getValue()) || (mc.world.getBlockState(boost).getBlock() instanceof BlockLiquid && liquids.getValue()))) {
                return false;
            }
        }

        BlockPos boost2 = blockPos.add(0, 2, 0);

        if (!AutoCrystal.protocol.getValue()) {
            if (!(mc.world.getBlockState(boost2).getBlock() == Blocks.AIR)) {
                if (!(mc.world.getBlockState(boost).getBlock() instanceof BlockLiquid && liquids.getValue())) {
                    return false;
                }
            }
        }

        if (check.getValue() && !CrystalUtils.rayTraceBreak(blockPos.getX() + 0.5, blockPos.getY() + 1.0,blockPos.getZ() + 0.5)) {
            if (mc.player.getPositionEyes(1F).distanceTo(new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.0,blockPos.getZ() + 0.5)) > breakWallsRange.getValue()) {
                return false;
            }
        }

        if (placeWallsRange.getValue() < placeRange.getValue()) {
            if (!CrystalUtils.rayTracePlace(blockPos)) {
                if (strictDirection.getValue()) {
                    Vec3d eyesPos = mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0);
                    boolean inRange = false;
                    if (directionMode.getValue() == DirectionMode.VANILLA) {
                        for (EnumFacing facing : EnumFacing.values()) {
                            Vec3d cVector = new Vec3d(blockPos.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5,
                                    blockPos.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5,
                                    blockPos.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5);
                            if (eyesPos.distanceTo(cVector) <= placeWallsRange.getValue()) {
                                inRange = true;
                                break;
                            }
                        }
                    } else {
                        double increment = 0.45D;
                        double start = 0.05D;
                        double end = 0.95D;

                        loop:
                        for (double xS = start; xS <= end; xS += increment) {
                            for (double yS = start; yS <= end; yS += increment) {
                                for (double zS = start; zS <= end; zS += increment) {
                                    Vec3d posVec = (new Vec3d(blockPos)).add(xS, yS, zS);

                                    double distToPosVec = eyesPos.distanceTo(posVec);

                                    if (distToPosVec <= placeWallsRange.getValue()) {
                                        inRange = true;
                                        break loop;
                                    }
                                }
                            }
                        }
                    }
                    if (!inRange) return false;
                } else {
                    if ((double) blockPos.getY() > mc.player.posY + (double) mc.player.getEyeHeight()) {
                        if (mc.player.getDistance(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5) > placeWallsRange.getValue()) {
                            return false;
                        }
                    } else if (mc.player.getDistance(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5) > placeWallsRange.getValue()) {
                        return false;
                    }
                }
            }
        } else if (strictDirection.getValue()) {
            Vec3d eyesPos = mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0);
            boolean inRange = false;
            if (directionMode.getValue() == DirectionMode.VANILLA) {
                for (EnumFacing facing : EnumFacing.values()) {
                    Vec3d cVector = new Vec3d(blockPos.getX() + 0.5 + facing.getDirectionVec().getX() * 0.5,
                            blockPos.getY() + 0.5 + facing.getDirectionVec().getY() * 0.5,
                            blockPos.getZ() + 0.5 + facing.getDirectionVec().getZ() * 0.5);
                    if (eyesPos.distanceTo(cVector) <= placeRange.getValue()) {
                        inRange = true;
                        break;
                    }
                }
            } else {
                double increment = 0.45D;
                double start = 0.05D;
                double end = 0.95D;

                loop:
                for (double xS = start; xS <= end; xS += increment) {
                    for (double yS = start; yS <= end; yS += increment) {
                        for (double zS = start; zS <= end; zS += increment) {
                            Vec3d posVec = (new Vec3d(blockPos)).add(xS, yS, zS);

                            double distToPosVec = eyesPos.distanceTo(posVec);

                            if (distToPosVec <= placeRange.getValue()) {
                                inRange = true;
                                break loop;
                            }
                        }
                    }
                }
            }
            if (!inRange) return false;
        }

        return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost, boost2.add(1, 1, 1))).stream()
                .filter(entity -> !breakLocations.containsKey(entity.getEntityId()) && (!(entity instanceof EntityEnderCrystal) || entity.ticksExisted > 20)).count() == 0;
    }

    private List<EntityPlayer> getTargetsInRange() {
        List<EntityPlayer> stream = mc.world.playerEntities
                .stream()
                .filter(e -> e != mc.player && e != mc.getRenderViewEntity())
                .filter(e -> !e.isDead)
                .filter(e -> !FakePlayerManager.isFake(e))
                .filter(e -> !Friends.isUUIDFriend(e.getUniqueID().toString()))
                .filter(e -> e.getHealth() > 0)
                .filter(e -> mc.player.getDistance(e) < enemyRange.getValue())
                .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                .collect(Collectors.toList());

        if (targetingMode.getValue() == TargetingMode.SMART) {
            List<EntityPlayer> safeStream = stream.stream()
                    .filter(e -> !(BlockUtils.isHole(new BlockPos(e)) || (mc.world.getBlockState(new BlockPos(e)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(e)).getBlock() != Blocks.WEB && !(mc.world.getBlockState(new BlockPos(e)).getBlock() instanceof BlockLiquid))))
                    .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                    .collect(Collectors.toList());

            if (safeStream.size() > 0) stream = safeStream;

            safeStream = stream.stream()
                    .filter(e -> e.getHealth() + e.getAbsorptionAmount() < 10F)
                    .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                    .collect(Collectors.toList());

            if (safeStream.size() > 0) stream = safeStream;
        }

        return stream;
    }

    public void setSwordSlot() {
        int swordSlot = CrystalUtils.getSwordSlot();
        if (mc.player.inventory.currentItem != swordSlot && swordSlot != -1) {
            mc.player.inventory.currentItem = swordSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(swordSlot));
            switchTimer.reset();
            noGhostTimer.reset();
        }
    }

    public boolean isOffhand() {
        return mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
    }

    public boolean setCrystalSlot() {
        if (isOffhand()) {
            return true;
        }
        int crystalSlot = CrystalUtils.getCrystalSlot();
        if (crystalSlot == -1) {
            return false;
        }
        else if (mc.player.inventory.currentItem != crystalSlot) {
            mc.player.inventory.currentItem = crystalSlot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(crystalSlot));
            switchTimer.reset();
            noGhostTimer.reset();
        }
        return true;
    }

    public static List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
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
