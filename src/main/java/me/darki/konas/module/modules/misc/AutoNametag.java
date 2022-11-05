package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateWalkingPlayerEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.KonasGlobals;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.interaction.InteractionUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Items;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

public class AutoNametag extends Module {
    private static final Setting<Float> range = new Setting<>("Range", 4.3F, 6F, 0.5F, 0.1F);
    private static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    private static final Setting<Boolean> ignoreNamed = new Setting<>("IgnoreNamed", true);
    private static final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true);
    private static final Setting<Boolean> withers = new Setting<>("Withers", true);
    private static final Setting<Boolean> mobs = new Setting<>("Mobs", false);
    private static final Setting<Boolean> animals = new Setting<>("Animals", false);

    private Timer angleInactivityTimer = new Timer();
    private float yaw;
    private float pitch;

    private EntityLivingBase entity = null;
    private int startingSlot;

    public AutoNametag() {
        super("AutoNametag", "Automatically right clicks entities to name them", Category.MISC);
    }

    @Subscriber(priority = 3)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent.Pre event) {
        entity = null;

        if (event.isCancelled() || !InteractionUtil.canPlaceNormally()) return;

        startingSlot = mc.player.inventory.currentItem;

        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemNameTag) && !isOffhand()) {
            int slot = -1;

            if (autoSwitch.getValue()) {
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack.isEmpty()) {
                        continue;
                    }

                    if (stack.getItem() instanceof ItemNameTag) {
                        if (!stack.hasDisplayName()) {
                            continue;
                        }

                        slot = i;
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }

            if (slot == -1) {
                return;
            }
        }

        ItemStack stack = isOffhand() ? mc.player.getHeldItemOffhand() : mc.player.getHeldItemMainhand();

        if (!stack.hasDisplayName()) {
            return;
        }

        entity = mc.world.loadedEntityList.stream()
                .filter(this::isValid)
                .map(target -> (EntityLivingBase) target)
                .min(Comparator.comparing(target -> mc.player.getDistance(target)))
                .orElse(null);

        if (entity != null) {
            if (rotate.getValue()) {
                double[] rotations = PlayerUtils.calculateLookAt(entity.posX, entity.posY, entity.posZ, mc.player);
                yaw = (float) rotations[0];
                pitch = (float) rotations[1];
                angleInactivityTimer.reset();
            }
        }

        if (rotate.getValue() && !angleInactivityTimer.hasPassed(350)) {
            KonasGlobals.INSTANCE.rotationManager.setRotations(yaw, pitch);
        }
    }

    public boolean isOffhand() {
        return mc.player.getHeldItemOffhand().getItem() == Items.NAME_TAG;
    }

    @Subscriber
    public void onUpdateWalkingPlayerPost(UpdateWalkingPlayerEvent.Post event) {
        if (entity != null) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entity, isOffhand() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
        }

        if (startingSlot != mc.player.inventory.currentItem) {
            mc.player.inventory.currentItem = startingSlot;
            mc.playerController.updateController();
        }
    }

    private boolean isValid(Entity entity) {
        if (!entity.getCustomNameTag().isEmpty() && ignoreNamed.getValue()) {
            return false;
        }

        if (animals.getValue() && entity instanceof EntityAnimal) {
            return true;
        } else if (mobs.getValue() && entity instanceof IMob) {
            return true;
        } else if (withers.getValue() && entity instanceof EntityWither) {
            return true;
        }

        return false;
    }
}
