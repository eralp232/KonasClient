package me.darki.konas.module.modules.combat;

import cookiedragon.eventsystem.EventDispatcher;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.Render3DEvent;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.client.BlockUtils;
import me.darki.konas.util.client.PlayerUtils;
import me.darki.konas.util.combat.CrystalUtils;
import me.darki.konas.util.render.RenderUtil;
import me.darki.konas.util.timer.Timer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CrystalBlocker extends Module {
    private static Setting<Integer> tickDelay = new Setting<>("TickDelay", 2, 10, 1, 1);
    private static Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true);
    private static Setting<Boolean> strict = new Setting<>("Strict", false);
    private static Setting<Boolean> rotate = new Setting<>("Rotate", true);

    private int itemSlot;

    private static final Setting<Boolean> drawBlock = new Setting<>("DrawBlock", true);
    private static final Setting<ColorSetting> drawColor = new Setting<>("Color", new ColorSetting(0xFFFF0000));

    Timer timer = new Timer();

    private BlockPos renderBlock;

    private int tickCounter = 0;

    private boolean shouldSneak = false;

    public CrystalBlocker() {
        super("CrystalBlocker", "Puts blocks between you and crystals", Category.COMBAT);
    }

    @Override
    public void onEnable() {

        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }

        renderBlock = null;

        tickCounter = tickDelay.getValue();
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        shouldSneak = false;

        if (strict.getValue() && (!mc.player.onGround || !mc.player.collidedVertically)) return;

        if (tickCounter < tickDelay.getValue()) {
            tickCounter++;
        }

        if (tickCounter < tickDelay.getValue()) {
            return;
        } else {
            tickCounter = 0;
        }

        List<Entity> crystalsInRange = mc.world.loadedEntityList.stream()
                .filter(e -> e instanceof EntityEnderCrystal)
                .collect(Collectors.toList());

        EntityEnderCrystal nearestCrystal = (EntityEnderCrystal) crystalsInRange.stream()
                .filter(c -> CrystalUtils.calculateDamage((EntityEnderCrystal) c, mc.player) >= mc.player.getHealth() + mc.player.getAbsorptionAmount())
                .filter(c -> c.getDistance(mc.player) < 8D)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);

        if (nearestCrystal != null) {

            double[] yawToCrystal = PlayerUtils.calculateLookAt(nearestCrystal.posX, nearestCrystal.posY, nearestCrystal.posZ, mc.player);

            BlockPos crystalPos = new BlockPos(mc.player).offset(EnumFacing.byHorizontalIndex(MathHelper.floor((double) (yawToCrystal[0] * 4.0F / 360.0F) + 0.5D) & 3));

            boolean changeItem = mc.player.inventory.currentItem != itemSlot;
            int startingItem = mc.player.inventory.currentItem;

            if (changeItem && autoSwitch.getValue()) {
                mc.player.inventory.currentItem = itemSlot;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(itemSlot));
            }

            boolean isSprinting = mc.player.isSprinting();

            if (isSprinting) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            tryPlace(crystalPos);

            if (shouldSneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                shouldSneak = false;
            }

            if (isSprinting) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }

            if (changeItem && autoSwitch.getValue()) {
                mc.player.inventory.currentItem = startingItem;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(startingItem));
            }
        }


        if (timer.hasPassed(1000)) {
            renderBlock = null;
        }
    }

    private boolean tryPlace(BlockPos pos) {
        if (pos != null) {
            if (mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
                Optional<BlockUtils.ClickLocation> posCL = BlockUtils.generateClickLocation(pos);
                int slot = getBlockSlot();
                if (slot == -1) {
                    Logger.sendOptionalDeletableMessage("No Blocks Found!", 1738);
                    EventDispatcher.Companion.unsubscribe(this);
                    setEnabled(false);
                    onDisable();
                    return false;
                }
                itemSlot = slot;

                if (posCL.isPresent()) {
                    renderBlock = pos;
                    timer.reset();
                    BlockPos currentPos = posCL.get().neighbour;
                    EnumFacing currentFace = posCL.get().opposite;

                    if (!shouldSneak) {
                        shouldSneak = BlockUtils.shouldSneakWhileRightClicking(currentPos);
                        if (shouldSneak) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        }
                    }

                    Vec3d vec = new Vec3d(currentPos)
                            .add(0.5, 0.5, 0.5)
                            .add(new Vec3d(currentFace.getDirectionVec()).scale(0.5));

                    if (rotate.getValue()) {
                        PlayerUtils.faceVectorPacketInstant(vec);
                    }

                    float f = (float)(vec.x - (double)currentPos.getX());
                    float f1 = (float)(vec.y - (double)currentPos.getY());
                    float f2 = (float)(vec.z - (double)currentPos.getZ());

                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPos, currentFace, EnumHand.MAIN_HAND, f, f1, f2));

                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                    timer.reset();

                    return true;
                }
            }
        }
        return false;
    }

    @Subscriber
    public void onRender(Render3DEvent event) {

        if (mc.world == null || mc.player == null) {
            return;
        }

        if (drawBlock.getValue() && renderBlock != null) {

            GlStateManager.pushMatrix();
            RenderUtil.beginRender();
            GlStateManager.enableBlend();
            GlStateManager.glLineWidth(1.0f);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            RenderUtil.drawBoundingBox(new AxisAlignedBB(renderBlock), new Color(drawColor.getValue().getColor()));

            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            RenderUtil.endRender();
            GlStateManager.popMatrix();

        }

    }

    private int getBlockSlot() {
        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
            return mc.player.inventory.currentItem;
        } else {
            if (autoSwitch.getValue()) {
                for (int i = 0; i < 9; ++i) {
                    stack = mc.player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
