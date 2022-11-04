package me.darki.konas.module.modules.client;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.PlayerConnectEvent;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.mixin.mixins.IRenderManager;
import me.darki.konas.module.Module;
import me.darki.konas.module.modules.render.Search;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.render.BlockRenderUtil;
import me.darki.konas.util.render.TessellatorUtil;
import me.darki.konas.util.waypoint.Waypoint;
import me.darki.konas.util.waypoint.WaypointType;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Waypoints extends Module {
    public Setting<Parent> custom = new Setting<>("Custom", new Parent(false));
    private final Setting<Boolean> cTracers = new Setting<>("CTracers", false).withParent(custom);
    private final Setting<ColorSetting> cTracersC = new Setting<>("CTracersC", new ColorSetting(0xFF0000FF)).withParent(custom);
    private final Setting<Boolean> cFill = new Setting<>("CFill", true).withParent(custom);
    private final Setting<ColorSetting> cFillC = new Setting<>("CFillC", new ColorSetting(0x550000FF)).withParent(custom);
    private final Setting<Boolean> cOutline = new Setting<>("COutline", true).withParent(custom);
    private final Setting<ColorSetting> cOutlineC = new Setting<>("COutlineC", new ColorSetting(0xFF0000FF)).withParent(custom);

    public Setting<Parent> logouts = new Setting<>("Logouts", new Parent(false));
    public Setting<Boolean> renderLogouts = new Setting<>("RenderLogouts", true).withParent(logouts);
    private final Setting<Boolean> lTracers = new Setting<>("LTracers", false).withParent(logouts);
    private final Setting<ColorSetting> lTracersC = new Setting<>("LTracersC", new ColorSetting(0xFF000000)).withParent(logouts);
    private final Setting<Boolean> lFill = new Setting<>("LFill", true).withParent(logouts);
    private final Setting<ColorSetting> lFillC = new Setting<>("LFillC", new ColorSetting(0x55FF00FF)).withParent(logouts);
    private final Setting<Boolean> lOutline = new Setting<>("LOutline", true).withParent(logouts);
    private final Setting<ColorSetting> lOutlineC = new Setting<>("LOutlineC", new ColorSetting(0xFFFF0000)).withParent(logouts);

    public Setting<Parent> deaths = new Setting<>("Deaths", new Parent(false));
    public Setting<Boolean> renderDeaths = new Setting<>("RenderDeaths", true).withParent(deaths);
    public Setting<Boolean> onlyLast = new Setting<>("OnlyLast", false).withParent(deaths);
    private final Setting<Boolean> dTracers = new Setting<>("DTracers", false).withParent(deaths);
    private final Setting<ColorSetting> dTracersC = new Setting<>("DTracersC", new ColorSetting(0xFF00FF00)).withParent(deaths);
    private final Setting<Boolean> dFill = new Setting<>("DFill", true).withParent(deaths);
    private final Setting<ColorSetting> dFillC = new Setting<>("DFillC", new ColorSetting(0x5500FF00)).withParent(deaths);
    private final Setting<Boolean> dOutline = new Setting<>("DOutline", true).withParent(deaths);
    private final Setting<ColorSetting> dOutlineC = new Setting<>("DOutlineC", new ColorSetting(0xFF00FF00)).withParent(deaths);

    private enum RenderType {
        CUSTOM, LOGOUT, DEATH
    }

    private final ConcurrentHashMap<EntityPlayer, Long> loggedPlayers = new ConcurrentHashMap<>();

    private final DecimalFormat df = new DecimalFormat("#.##");

    public Waypoints() {
        super("Waypoints", "Shows waypoints", Category.CLIENT);
    }

    public ConcurrentHashMap<EntityPlayer, Long> getLoggedPlayers() {
        return loggedPlayers;
    }

    @Subscriber
    public void onDisplayerGuiScreen(LoadGuiEvent event) {
        if (event.getGui() instanceof GuiConnecting || event.getGui() instanceof GuiDownloadTerrain || event.getGui() instanceof GuiDisconnected || event.getGui() instanceof GuiMultiplayer) {
            loggedPlayers.clear();
        } else if (event.getGui() instanceof GuiGameOver && renderDeaths.getValue()) {
            if (onlyLast.getValue()) {
                KonasGlobals.INSTANCE.waypointManager.delDeaths();
            }
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
            Date date = new Date();
            KonasGlobals.INSTANCE.waypointManager.addWaypoint(new Waypoint("Death_" + formatter.format(date), Double.parseDouble(df.format(mc.player.posX)), Double.parseDouble(df.format(mc.player.posY)), Double.parseDouble(df.format(mc.player.posZ)), mc.player.dimension, WaypointType.DEATH));
        }
    }

    @Subscriber
    public void onJoin(PlayerConnectEvent.Join event) {

        if(mc.player == null || mc.world == null) return;

        if (!renderLogouts.getValue()) return;

        for (Map.Entry<EntityPlayer, Long> entry : loggedPlayers.entrySet()) {
            if (entry.getKey().getUniqueID().equals(event.getUuid())) {
                loggedPlayers.remove(entry.getKey());
            }
        }
    }

    @Subscriber
    public void onLeave(PlayerConnectEvent.Leave event) {
        if(mc.player == null || mc.world == null) return;

        if (!renderLogouts.getValue()) return;

        EntityPlayer player = mc.world.getPlayerEntityByUUID(event.getUuid());
        if (player != null) {
            loggedPlayers.put(player, System.currentTimeMillis());
        }
    }

    @Subscriber
    public void onEvent(Render3DEvent event) {

        if (mc.world == null || mc.player == null) return;

        if (renderLogouts.getValue()) {
            for (Map.Entry<EntityPlayer, Long> entry : loggedPlayers.entrySet()) {
                EntityPlayer entity = entry.getKey();
                if (entity != mc.player) {
                    drawWaypoint(entity.getEntityBoundingBox(), RenderType.LOGOUT);
                }
            }
        }

        for (Waypoint waypoint : KonasGlobals.INSTANCE.waypointManager.getWaypoints()) {
            drawWaypoint(new AxisAlignedBB(waypoint.getX(), waypoint.getY(), waypoint.getZ(), waypoint.getX() + 1, waypoint.getY() + 2, waypoint.getZ() + 1), waypoint.getType() == WaypointType.DEATH ? RenderType.DEATH : RenderType.CUSTOM);
        }
    }

    private void drawWaypoint(AxisAlignedBB bb, RenderType type) {
        ColorSetting outlineColor = null;
        ColorSetting fillColor = null;
        ColorSetting tracerColor = null;

        switch (type) {
            case CUSTOM: {
                if (cOutline.getValue()) {
                    outlineColor = cOutlineC.getValue();
                }
                if (cFill.getValue()) {
                    fillColor = cFillC.getValue();
                }
                if (cTracers.getValue()) {
                    tracerColor = cTracersC.getValue();
                }
                break;
            }
            case LOGOUT: {
                if (lOutline.getValue()) {
                    outlineColor = lOutlineC.getValue();
                }
                if (lFill.getValue()) {
                    fillColor = lFillC.getValue();
                }
                if (lTracers.getValue()) {
                    tracerColor = lTracersC.getValue();
                }
                break;
            }
            case DEATH: {
                if (dOutline.getValue()) {
                    outlineColor = dOutlineC.getValue();
                }
                if (dFill.getValue()) {
                    fillColor = dFillC.getValue();
                }
                if (dTracers.getValue()) {
                    tracerColor = dTracersC.getValue();
                }
                break;
            }
        }

        if (fillColor != null) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBox(bb, fillColor);
            TessellatorUtil.release();
        }

        if (outlineColor != null) {
            TessellatorUtil.prepare();
            TessellatorUtil.drawBoundingBox(bb, 1.5F, outlineColor);
            TessellatorUtil.release();
        }

        if (tracerColor != null) {
            Vec3d eyes = new Vec3d(0, 0, 1)
                    .rotatePitch(-(float) Math
                            .toRadians(mc.player.rotationPitch))
                    .rotateYaw(-(float) Math
                            .toRadians(mc.player.rotationYaw));

            Vec3d vec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * 0.5D), bb.minY + ((bb.maxY - bb.minY) * 0.5D), bb.minZ + ((bb.maxZ - bb.minZ) * 0.5D));

            Search.renderTracer(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z,
                    vec.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(),
                    vec.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(),
                    vec.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ(),
                    tracerColor.getColor());
        }
    }
}
