package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.module.Module;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;

import java.util.Arrays;
import java.util.List;

public class NoSoundLag extends Module {
    private static final List<SoundEvent> sounds = Arrays.asList(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);

    public NoSoundLag() {
        super("NoSoundLag", "Prevents users from lagging you with sound", Category.MISC, "AntiSoundCrash");
    }

    @Subscriber
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect effect = (SPacketSoundEffect) event.getPacket();
            if (sounds.contains(effect.getSound())) {
                event.setCancelled(true);
            }
        }
    }
}
