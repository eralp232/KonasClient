package me.darki.konas.module.modules.player;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.*;
import me.darki.konas.mixin.mixins.IItemTool;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.timer.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.math.BlockPos;

public class AutoTool extends Module {

    private Setting<Boolean> weapons = new Setting<>("Weapons", true);
    private Setting<Mining> miningMode = new Setting<>("Mining", Mining.NORMAL);
    private Setting<Boolean> strict = new Setting<>("Strict", true);

    private me.darki.konas.util.timer.Timer switchTimer = new Timer();
    private boolean hasSwapped = false;
    int priorSlot = -1;

    private enum Mining {
        NONE, NORMAL, SILENT, RETURN
    }

    public AutoTool() {
        super("AutoTool", "Swap to the best tool when mining and attacking", Category.PLAYER);
    }

    @Subscriber
    public void onDamageBlock(DamageBlockEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (miningMode.getValue() != Mining.SILENT) return;
        if (hasSwapped) {
            equip(priorSlot, true);
        }
        equip(findBestTool(event.getPos()), false);
    }

    @Subscriber
    public void onPostMining(PostMiningEvent event) {
        if (hasSwapped && miningMode.getValue() == Mining.SILENT) {
            equip(priorSlot, true);
        }
    }

    @Subscriber
    public void onLeftClick(LeftClickBlockEvent event) {
        if (mc.player == null || mc.world == null || event.getPlayer() != mc.player) return;
        if (miningMode.getValue() == Mining.NONE || miningMode.getValue() == Mining.SILENT) return;
        if(miningMode.getValue() == Mining.RETURN && hasSwapped) return;
        equip(findBestTool(event.getPos()), false);
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null || miningMode.getValue() != Mining.RETURN) return;
        if (!mc.playerController.getIsHittingBlock() && hasSwapped && (!strict.getValue() || switchTimer.hasPassed(100))) {
            equip(priorSlot, true);
        } else if (hasSwapped && miningMode.getValue() == Mining.SILENT) {
            equip(priorSlot, true);
        }
    }

    @Subscriber
    public void onAttack(EntityAttackEvent event) {
        if (mc.player == null || mc.world == null || !weapons.getValue()) return;
        equip(findBestWeapon(event.getTarget()), false);
    }

    public void equip(int slot, boolean returning) {
        if (slot != -1) {
            if (!returning) {
                hasSwapped = true;
                priorSlot = mc.player.inventory.currentItem;
            } else {
                hasSwapped = false;
                priorSlot = -1;
            }
            mc.player.inventory.currentItem = slot;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
        }
        switchTimer.reset();
    }

    public static int findBestTool(BlockPos pos) {
        IBlockState state = mc.world.getBlockState(pos);
        int bestSlot = -1;
        double bestSpeed = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;
            float speed = stack.getDestroySpeed(state);
            int eff;
            if (speed > 1) {
                speed += ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? (Math.pow(eff, 2) + 1) : 0);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    public int findBestWeapon(Entity target) {
        int bestSlot = -1;
        float bestDamage = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;
            if (stack.getItem() instanceof ItemSword) {
                float damage = ((ItemSword) stack.getItem()).getAttackDamage() + EnchantmentHelper.getModifierForCreature(stack, target instanceof EntityLivingBase ? ((EntityLivingBase) target).getCreatureAttribute() : EnumCreatureAttribute.UNDEFINED);
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            } else if (stack.getItem() instanceof ItemTool) {
                float damage = ((IItemTool) stack.getItem()).getAttackDamage() + EnchantmentHelper.getModifierForCreature(stack, target instanceof EntityLivingBase ? ((EntityLivingBase) target).getCreatureAttribute() : EnumCreatureAttribute.UNDEFINED);
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

}
