package me.darki.konas.module.modules.misc;

import com.mojang.authlib.GameProfile;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.mixin.mixins.IInventoryPlayer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.EnumHand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created 10 August 2019 by hub
 * Updated 8 December 2019 by hub
 */

public class FakePlayer extends Module {

    private static Setting<SpawnMode> spawnMode = new Setting<>("SpawnMode", SpawnMode.MULTI);

    private static Setting<Boolean> copyInventory = new Setting<>("CopyInventory", false);

    private List<Integer> fakePlayerIdList = null;

    private enum SpawnMode {
        SINGLE, MULTI
    }

    private static final String[][] fakePlayerInfo =
            {
                    {"66666666-6666-6666-6666-666666666600", "soulbond", "-3", "0"},
                    {"66666666-6666-6666-6666-666666666601", "derp1", "0", "-3"},
                    {"66666666-6666-6666-6666-666666666602", "derp2", "3", "0"},
                    {"66666666-6666-6666-6666-666666666603", "derp3", "0", "3"},
                    {"66666666-6666-6666-6666-666666666604", "derp4", "-6", "0"},
                    {"66666666-6666-6666-6666-666666666605", "derp5", "0", "-6"},
                    {"66666666-6666-6666-6666-666666666606", "derp6", "6", "0"},
                    {"66666666-6666-6666-6666-666666666607", "derp7", "0", "6"},
                    {"66666666-6666-6666-6666-666666666608", "derp8", "-9", "0"},
                    {"66666666-6666-6666-6666-666666666609", "derp9", "0", "-9"},
                    {"66666666-6666-6666-6666-666666666610", "derp10", "9", "0"},
                    {"66666666-6666-6666-6666-666666666611", "derp11", "0", "9"}
            };

    public FakePlayer() {
        super("FakePlayer", Category.MISC, "Ghosts");
    }

    @Override
    public void onEnable() {

        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }

        fakePlayerIdList = new ArrayList<>();

        int entityId = -101;

        for (String[] data : fakePlayerInfo) {

            if (spawnMode.getValue().equals(SpawnMode.SINGLE)) {
                addFakePlayer(data[0], data[1], entityId, 0, 0);
                break;
            } else {
                addFakePlayer(data[0], data[1], entityId, Integer.parseInt(data[2]), Integer.parseInt(data[3]));
            }

            entityId--;

        }

    }

    private void addFakePlayer(String uuid, String name, int entityId, int offsetX, int offsetZ) {

        EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString(uuid), name));
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.posX = fakePlayer.posX + offsetX;
        fakePlayer.posZ = fakePlayer.posZ + offsetZ;

        if (copyInventory.getValue()) {
            ((IInventoryPlayer) fakePlayer.inventory).setArmorInventory(mc.player.inventory.armorInventory);
            ((IInventoryPlayer) fakePlayer.inventory).setMainInventory(mc.player.inventory.mainInventory);
            fakePlayer.inventory.currentItem = mc.player.inventory.currentItem;
            fakePlayer.setHeldItem(EnumHand.MAIN_HAND, mc.player.getHeldItemMainhand());
            fakePlayer.setHeldItem(EnumHand.OFF_HAND, mc.player.getHeldItemOffhand());
        }

        mc.world.addEntityToWorld(entityId, fakePlayer);
        fakePlayerIdList.add(entityId);

    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {

        if (fakePlayerIdList == null || fakePlayerIdList.isEmpty() ) {
            this.toggle();
        }

    }

    @Override
    public void onDisable() {

        if (mc.player == null || mc.world == null) {
            return;
        }

        if (fakePlayerIdList != null) {
            for (int id : fakePlayerIdList) {
                mc.world.removeEntityFromWorld(id);
            }
        }

    }

}

