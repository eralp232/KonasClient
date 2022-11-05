package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.UpdateEquippedItemEvent;
import me.darki.konas.mixin.mixins.IItemRenderer;
import me.darki.konas.module.Module;
import me.darki.konas.setting.Setting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class OldAnimations extends Module {
    private final Setting<Boolean> showSwapping = new Setting<>("ShowSwapping", true);

    public OldAnimations() {
        super("OldAnimations", "1.8 Hit Animations", Category.RENDER);
    }

    @Subscriber
    public void onUpdateEquippedItem(UpdateEquippedItemEvent event) {
        event.setCancelled(true);
        ((IItemRenderer) mc.entityRenderer.itemRenderer).settPrevEquippedProgressMainHand(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressMainHand());
        ((IItemRenderer) mc.entityRenderer.itemRenderer).settPrevEquippedProgressOffHand(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressOffHand());
        EntityPlayerSP entityplayersp = mc.player;
        ItemStack itemstack = entityplayersp.getHeldItemMainhand();
        ItemStack itemstack1 = entityplayersp.getHeldItemOffhand();

        if (entityplayersp.isRowingBoat()) {
            ((IItemRenderer) mc.entityRenderer.itemRenderer).settEquippedProgressMainHand(MathHelper.clamp(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressMainHand() - 0.4F, 0.0F, 1.0F));
            ((IItemRenderer) mc.entityRenderer.itemRenderer).settEquippedProgressOffHand(MathHelper.clamp(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressOffHand() - 0.4F, 0.0F, 1.0F));
        }
        else {
            boolean requipM = showSwapping.getValue() && net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(((IItemRenderer) mc.entityRenderer.itemRenderer).gettItemStackMainHand(), itemstack, entityplayersp.inventory.currentItem);
            boolean requipO = showSwapping.getValue() && net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(((IItemRenderer) mc.entityRenderer.itemRenderer).gettItemStackOffHand(), itemstack1, -1);

            if (!requipM && !Objects.equals(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressMainHand(), itemstack))
                ((IItemRenderer) mc.entityRenderer.itemRenderer).settItemStackMainHand(itemstack);
            if (!requipM && !Objects.equals(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressOffHand(), itemstack1))
                ((IItemRenderer) mc.entityRenderer.itemRenderer).settItemStackOffHand(itemstack1);

            ((IItemRenderer) mc.entityRenderer.itemRenderer).settEquippedProgressMainHand(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressMainHand() + MathHelper.clamp((!requipM ? 1F * 1F * 1F : 0.0F) - ((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressMainHand(), -0.4F, 0.4F));
            ((IItemRenderer) mc.entityRenderer.itemRenderer).settEquippedProgressOffHand(((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressOffHand() + MathHelper.clamp((float)(!requipO ? 1 : 0) - ((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressOffHand(), -0.4F, 0.4F));
        }

        if (((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressMainHand() < 0.1F) {
            ((IItemRenderer) mc.entityRenderer.itemRenderer).settItemStackMainHand(itemstack);
        }

        if (((IItemRenderer) mc.entityRenderer.itemRenderer).gettEquippedProgressOffHand() < 0.1F) {
            ((IItemRenderer) mc.entityRenderer.itemRenderer).settItemStackOffHand(itemstack1);
        }
    }
}
