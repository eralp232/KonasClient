package me.darki.konas.module.modules.render;

import com.google.common.collect.Sets;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.mixin.mixins.IWorld;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.server.*;
import net.minecraft.util.SoundEvent;

import java.util.Set;

public class NoRender extends Module {

    private static final Set<SoundEvent> BAT_SOUNDS = Sets.newHashSet(
            SoundEvents.ENTITY_BAT_AMBIENT,
            SoundEvents.ENTITY_BAT_DEATH,
            SoundEvents.ENTITY_BAT_HURT,
            SoundEvents.ENTITY_BAT_LOOP,
            SoundEvents.ENTITY_BAT_TAKEOFF
    );

    private final Setting<Boolean> noHurtCam = new Setting<>("NoHurtCam", true);

    private final Setting<Boolean> noWeather = new Setting<>("NoWeather", true);
    private final Setting<Boolean> noLightning = new Setting<>("NoLightning", true);

    private final Setting<Boolean> noFire = new Setting<>("NoFire", true);
    private final Setting<Boolean> noBossBarSetting = new Setting<>("NoBossBar", false);
    private final Setting<Boolean> noBats = new ListenableSettingDecorator<>("NoBats", true, value -> {
        if (value) {
            purgeBats();
        }
    });

    private final Setting<ArmorMode> armorSetting = new Setting<>("Armor", ArmorMode.NONE);
    private final Setting<Boolean> head = new Setting<>("Head", true).withVisibility(() -> armorSetting.getValue() == ArmorMode.SELECT);
    private final Setting<Boolean> chestplate = new Setting<>("Chestplate", false).withVisibility(() -> armorSetting.getValue() == ArmorMode.SELECT);
    private final Setting<Boolean> leggings = new Setting<>("Leggings", false).withVisibility(() -> armorSetting.getValue() == ArmorMode.SELECT);
    private final Setting<Boolean> boots = new Setting<>("Boots", false).withVisibility(() -> armorSetting.getValue() == ArmorMode.SELECT);

    private final Setting<Boolean> selfShadow = new Setting<>("OwnShadow", true);

    private final Setting<Boolean> mob = new Setting<>("Mob", false);
    private final Setting<Boolean> object = new Setting<>("Object", false);
    private final Setting<Boolean> xp = new Setting<>("XP", true);
    private final Setting<Boolean> explosion = new Setting<>("Explosions", true);
    private final Setting<Boolean> fireworks = new Setting<>("Fireworks", false);
    private final Setting<Boolean> item = new Setting<>("Item", false);

    private final Setting<Boolean> water = new Setting<>("Water", true);
    private final Setting<Boolean> lava = new Setting<>("Lava", true);
    private final Setting<Boolean> blocks = new Setting<>("Blocks", true);

    private final Setting<Boolean> enchantmentTable = new Setting<>("EnchantmentTable", false);
    private final Setting<Boolean> signs = new Setting<>("Signs", false);
    private final Setting<Boolean> maps = new Setting<>("Maps", false);

    private final Setting<Boolean> beacon = new Setting<>("Beacon", false);

    public static final Setting<Boolean> toast = new Setting<>("Toasts", true);
    private final Setting<Boolean> chat = new Setting<>("Chat", false);

    public NoRender() {
        super("NoRender", Category.RENDER);
    }

    private enum ArmorMode {
        NONE, ALL, SELECT
    }

    @Override
    public void onEnable() {
        purgeBats();
    }

    @Subscriber
    public void onRenderMap(RenderMapEvent event) {
        if (maps.getValue()) {
            event.setCancelled(true);
        }
    }

    private void purgeBats() {
        if (noBats.getValue() && mc.player != null && mc.world != null) {
            mc.world.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityBat).forEach(entity -> mc.world.removeEntity(entity));
        }
    }

    @Subscriber
    public void onRenderPlayerShadow(RenderPlayerShadowEvent event) {
        if (selfShadow.getValue()) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onParticleEffect(ParticleEffectEvent event) {
        if (fireworks.getValue() && (event.getParticle() instanceof ParticleFirework.Overlay || event.getParticle() instanceof ParticleFirework.Spark || event.getParticle() instanceof ParticleFirework.Starter)) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onArmorRender(ArmorRenderEvent event) {
        if (armorSetting.getValue() == ArmorMode.ALL) {
            event.cancel();
        } else if (armorSetting.getValue() == ArmorMode.SELECT) {
            if (event.getSlot() == EntityEquipmentSlot.HEAD && head.getValue()) {
                event.cancel();
            } else if (event.getSlot() == EntityEquipmentSlot.CHEST && chestplate.getValue()) {
                event.cancel();
            } else if (event.getSlot() == EntityEquipmentSlot.LEGS && leggings.getValue()) {
                event.cancel();
            } else if (event.getSlot() == EntityEquipmentSlot.FEET && boots.getValue()) {
                event.cancel();
            }
        }
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSpawnGlobalEntity && noLightning.getValue()) {
            if (((SPacketSpawnGlobalEntity) event.getPacket()).getType() == 1) {
                event.setCancelled(true);
            }
        }
        if (event.getPacket() instanceof SPacketMaps && maps.getValue()) {
            event.setCancelled(true);
        }
        if (noBats.getValue() && (event.getPacket() instanceof SPacketSpawnMob && ((SPacketSpawnMob) event.getPacket()).getEntityType() == 65 ||
                event.getPacket() instanceof SPacketSoundEffect && BAT_SOUNDS.contains(((SPacketSoundEffect) event.getPacket()).getSound()))) {
            event.cancel();
        }
        if ((event.getPacket() instanceof SPacketSpawnMob && mob.getValue()) ||
                (event.getPacket() instanceof SPacketSpawnObject && object.getValue()) ||
                (event.getPacket() instanceof SPacketSpawnExperienceOrb && xp.getValue()) ||
                (event.getPacket() instanceof SPacketExplosion && explosion.getValue()) ||
                (event.getPacket() instanceof SPacketSpawnObject && item.getValue() && ((SPacketSpawnObject) event.getPacket()).getType() == 2) ||
                (event.getPacket() instanceof SPacketSpawnObject && fireworks.getValue() && ((SPacketSpawnObject) event.getPacket()).getType() == 76))
            event.cancel();
    }

    @Subscriber
    public void onRenderOverlay(RenderOverlayEvent event) {
        boolean shouldCancel = false;

        switch (event.getOverlayType()) {
            case FIRE:
                if (noFire.getValue()) {
                    shouldCancel = true;
                }

                break;
            case WATER:
                if (water.getValue()) {
                    shouldCancel = true;
                }

                break;
            case BLOCK:
                if (blocks.getValue()) {
                    shouldCancel = true;
                }
                break;
        }

        if (shouldCancel) {
            event.cancel();
        }
    }

    @Subscriber
    public void onRenderFogDensity(RenderFogDensityEvent event) {
        if (lava.getValue() && event.getState().getMaterial().equals(Material.LAVA)) {
            event.cancel();
        }
    }

    @Subscriber
    public void onHurtCam(HurtCameraEvent event) {
        if (noHurtCam.getValue()) {
            event.cancel();
        }
    }

    @Subscriber
    public void onBossBar(BossBarEvent event) {
        if(noBossBarSetting.getValue()) {
            event.cancel();
        }
    }

    @Subscriber
    public void onEnchantmentTableRender(RenderEnchantmentTableEvent event) {
        if(enchantmentTable.getValue()) event.cancel();
    }

    @Subscriber
    public void onBeamRender(RenderBeaconBeamEvent event) {
        if(beacon.getValue()) event.cancel();
    }

    @Subscriber
    public void onGetRainStrength(GetRainStrengthEvent event) {
        if (noWeather.getValue()) {
            event.cancel();
        }
    }

    @Subscriber
    public void onSignRender(RenderSignEvent event) {
        if(signs.getValue()) event.cancel();
    }

    private EntityPlayer.EnumChatVisibility prevVis = null;

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (chat.getValue()) {
            if (prevVis == null) {
                prevVis = mc.gameSettings.chatVisibility;
            }
            mc.gameSettings.chatVisibility = EntityPlayer.EnumChatVisibility.HIDDEN;
        } else if (prevVis != null) {
            mc.gameSettings.chatVisibility = prevVis;
            prevVis = null;
        }

        if (noWeather.getValue()) {
            if (((IWorld) mc.world).getRainingStrength() > 0.9) {
                setExtraInfo("Thunder");
            } else if (((IWorld) mc.world).getRainingStrength() > 0.2) {
                setExtraInfo("Rain");
            } else {
                setExtraInfo("Clear");
            }
        } else {
            setExtraInfo("");
        }
    }

}
