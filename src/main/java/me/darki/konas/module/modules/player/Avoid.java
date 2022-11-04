package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.CollisionBoxEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockWeb;
import net.minecraft.init.Blocks;

public class Avoid extends Module {
    private final Setting<Boolean> fire = new Setting<>("Fire", true);
    private final Setting<Boolean> webs = new Setting<>("Webs", true);
    private final Setting<Boolean> pPlates = new Setting<>("PressurePlates", true);
    private final Setting<Boolean> wires = new Setting<>("Wires", true);

    private final Setting<Boolean> cactus = new Setting<>("Cactus", true);
    private final Setting<Boolean> unloaded = new Setting<>("Unloaded", true);

    public Avoid() {
        super("Avoid", "Avoids hazards", Category.PLAYER, "AntiFlame", "AntiFire", "AntiCactus");
    }

    @Subscriber
    public void onCollisionBox(CollisionBoxEvent event) {
        Block block = event.getBlock();
        if (event.getPos().getY() >= Math.floor(mc.player.posY)) {
            if ((block.equals(Blocks.FIRE) && fire.getValue()) || (block instanceof BlockWeb && webs.getValue()) || (block instanceof BlockBasePressurePlate && pPlates.getValue()) || (block == Blocks.TRIPWIRE && wires.getValue()) || (block.equals(Blocks.CACTUS) && cactus.getValue()) || ((!mc.world.isBlockLoaded(event.getPos(), false) || event.getPos().getY() < 0) && unloaded.getValue())) {
                event.setBoundingBox(Block.FULL_BLOCK_AABB);
            }
        }
    }
}
