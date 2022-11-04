package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Spawns extends Module {
    public static Setting<Boolean> crystals = new Setting<>("Crystals", true);
    public static Setting<Boolean> onlyOwn = new Setting<>("OnlyOwn", false).withVisibility(crystals::getValue);
    public static Setting<Boolean> players = new Setting<>("Players", false);
    public static Setting<Boolean> mobs = new Setting<>("Mobs", false);
    public static Setting<Boolean> boats = new Setting<>("Boats", false);

    public static Setting<Float> duration = new Setting<>("Duration", 1F, 5F, 0.1F, 0.1F);

    public static Setting<ColorSetting> circleColor = new Setting<>("Color", new ColorSetting(0xFFFF0000, true));
    public static Setting<Float> width = new Setting<>("Width", 2.5f, 10f, 0.1f, 0.1f);

    public Spawns() {
        super("Spawns", "Renders entity spawning", Category.RENDER);
    }

    private final CopyOnWriteArrayList<Spawn> spawns = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<BlockPos, Long> selfPos = new ConcurrentHashMap<>();

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && onlyOwn.getValue()) {
            if (mc.player.getHeldItem(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getHand()).getItem() instanceof ItemEndCrystal) {
                selfPos.put(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos(), System.currentTimeMillis());
            }
        }
    }

    @Subscriber
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        if (mc.player.ticksExisted % 20 == 0 && onlyOwn.getValue()) {
            selfPos.forEach((pos, time) -> {
                if (System.currentTimeMillis() - time > 2500L) {
                    selfPos.remove(pos);
                }
            });
        }
    }

    @Subscriber
    public void onWorldRender(Render3DEvent event) {
        for (Spawn spawn : spawns) {
            if (System.currentTimeMillis() - spawn.spawnTime > 1000F * duration.getValue()) {
                spawns.remove(spawn);
                continue;
            }
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            IRenderManager renderManager = (IRenderManager) mc.getRenderManager();
            float[] hsb = Color.RGBtoHSB(circleColor.getValue().getRed(), circleColor.getValue().getGreen(), circleColor.getValue().getBlue(), null);
            float hue = (float) (System.currentTimeMillis() % 7200L) / 7200F;
            int rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            ArrayList<Vec3d> vecs = new ArrayList<>();
            double x = spawn.spawnVec.x - renderManager.getRenderPosX();
            double y = spawn.spawnVec.y - renderManager.getRenderPosY();
            double z = spawn.spawnVec.z - renderManager.getRenderPosZ();
            GlStateManager.color(1F, 1F, 1F, 1F);
            GL11.glLineWidth(width.getValue());
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glBegin(1);
            for (int i = 0; i <= 360; ++i) {
                Vec3d vec = new Vec3d(x + Math.sin((double) i * Math.PI / 180.0) * spawn.radius, y + (spawn.height * ((System.currentTimeMillis() - spawn.spawnTime) / (1000F * duration.getValue()))) , z + Math.cos((double) i * Math.PI / 180.0) * spawn.radius);
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
                GL11.glVertex3d(vecs.get(j).x, vecs.get(j).y, vecs.get(j).z);
                GL11.glVertex3d(vecs.get(j + 1).x, vecs.get(j + 1).y, vecs.get(j + 1).z);
                hue += (1F / 360F);
                rgb = Color.getHSBColor(hue, hsb[1], hsb[2]).getRGB();
            }
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GlStateManager.enableLighting();
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @Subscriber
    public void onSpawnObject(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSpawnObject) {
            if (((SPacketSpawnObject) event.getPacket()).getType() == 51 && crystals.getValue()) {
                if (onlyOwn.getValue()) {
                    BlockPos spawnPos = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ()).down();
                    if (selfPos.containsKey(spawnPos)) {
                        selfPos.remove(spawnPos);
                        spawns.add(new Spawn(new Vec3d(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ()),
                                1.5F, 0.5F));
                    }
                } else {
                    spawns.add(new Spawn(new Vec3d(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ()),
                            1.5F, 0.5F));
                }
            } else if (((SPacketSpawnObject) event.getPacket()).getType() == 1 && boats.getValue()) {
                spawns.add(new Spawn(new Vec3d(((SPacketSpawnObject) event.getPacket()).getX(), ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ()),
                        1F, 0.75F));
            }
        } else if (event.getPacket() instanceof SPacketSpawnPlayer && players.getValue()) {
            spawns.add(new Spawn(new Vec3d(((SPacketSpawnPlayer) event.getPacket()).getX(), ((SPacketSpawnPlayer) event.getPacket()).getY(), ((SPacketSpawnPlayer) event.getPacket()).getZ()),
                    1.8F, 0.5F));
        } else if (event.getPacket() instanceof SPacketSpawnMob && mobs.getValue()) {
            EntityLivingBase entitylivingbase = (EntityLivingBase) EntityList.createEntityByID(((SPacketSpawnMob) event.getPacket()).getEntityType(), mc.world);
            if (entitylivingbase != null) {
                spawns.add(new Spawn(new Vec3d(((SPacketSpawnMob) event.getPacket()).getX(), ((SPacketSpawnMob) event.getPacket()).getY(), ((SPacketSpawnMob) event.getPacket()).getZ()),
                        entitylivingbase.height, entitylivingbase.width / 2F));
            }
        }
    }

    public static class Spawn {
        private final Vec3d spawnVec;
        private final float height;

        private final float radius;

        private final long spawnTime;

        public Spawn(Vec3d spawnVec, float height, float radius) {
            this.spawnVec = spawnVec;
            this.height = height;
            this.radius = radius;
            this.spawnTime = System.currentTimeMillis();
        }
    }
}
