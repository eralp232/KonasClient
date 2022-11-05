package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ItemListSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.timer.Timer;
import me.darki.konas.util.timer.TimerManager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Locale;

public class NoDesync extends Module {
    private static Setting<Boolean> place = new Setting<>("Place", true);
    private static Setting<Boolean> destroy = new Setting<>("Destroy", false);
    private static Setting<Boolean> limit = new Setting<>("Limit", false);
    public static Setting<Boolean> rotation = new Setting<>("RightClick", false);
    private final Setting<Float> timeout = new Setting<>("Timeout", 1F, 30F, 0.5F,0.5F).withVisibility(rotation::getValue);
    private static Setting<Boolean> use = new Setting<>("Use", false);
    private static Setting<UseMode> useMode = new Setting<>("Mode", UseMode.FOOD).withVisibility(use::getValue);
    public static Setting<ItemListSetting> items = new Setting<>("Items", new ItemListSetting());

    public static final Timer spoofTimer = new Timer();
    public static boolean isSpoofing = false;

    private long lastPacketTime = -1L;

    private float[] packetDiffs = new float[22];
    private int nextIndex = 0;

    public void onEnable() {
        isSpoofing = false;

        lastPacketTime = -1L;

        packetDiffs = new float[44];
        nextIndex = 0;
    }

    private enum UseMode {
        ALL, FOOD, WHITELIST
    }

    public NoDesync() {
        super("NoDesync", "Helps prevent desync", Category.MISC, "NoGlitchBlocks");
    }

    private int slot = -1;

    @Subscriber
    public void onPacketSend(PacketEvent.Send event) {
        if (rotation.getValue() && !isSpoofing && !spoofTimer.hasPassed(1000F * timeout.getValue()) && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            spoofTimer.setTime(0);
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
        }

        if (event.getPacket() instanceof CPacketPlayer && limit.getValue()) {
            if (lastPacketTime != -1L) {
                float timeElapsed = (float) (System.currentTimeMillis() - lastPacketTime) / 50F;
                packetDiffs[(nextIndex % packetDiffs.length)] = timeElapsed;
                nextIndex += 1;
            }

            lastPacketTime = System.currentTimeMillis();
        }
    }

    @Subscriber
    public void onPlayerDestroyBlock(PlayerDestroyBlockEvent event) {
        if (destroy.getValue()) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onPlaceBlock(PlaceBlockEvent event) {
        if (place.getValue()) {
            event.setCancelled(true);
        }
    }

    @Subscriber
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (slot != -1 && use.getValue()) {
            mc.player.inventory.currentItem = slot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            slot = -1;
            return;
        }
    }

    @Subscriber
    public void onUpdatePost(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPhase() == TickEvent.Phase.END) {
            if (use.getValue()) {
                if (isValidItem(mc.player.getActiveItemStack())) {
                    if (mc.player.getItemInUseCount() <= 0) {
                        slot = mc.player.inventory.currentItem;
                    }
                }
            }
            if (limit.getValue()) {
                float average = 0F;
                float amount = 0F;
                for (float diff : packetDiffs) {
                    if (diff > 0F) {
                        average += diff;
                        amount += 1F;
                    }
                }

                average /= amount;

                average = Math.max(average, 0.5F);
                
                if (average < 0.90F) {
                    KonasGlobals.INSTANCE.timerManager.updateTimer(this, 1000, average);
                } else {
                    KonasGlobals.INSTANCE.timerManager.resetTimer(this);
                }
            }
        }
    }

    public boolean isValidItem(ItemStack item) {
        if (useMode.getValue() == UseMode.ALL) {
            return true;
        } else if (useMode.getValue() == UseMode.FOOD) {
            return item.getItem() instanceof ItemFood;
        } else {
            return items.getValue().getItems().contains(item.getDisplayName().toLowerCase(Locale.ENGLISH));
        }
    }
}