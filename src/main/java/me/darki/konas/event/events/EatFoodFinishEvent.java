package me.darki.konas.event.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EatFoodFinishEvent {

    private ItemStack item;
    private EntityPlayer player;

    public EatFoodFinishEvent(ItemStack item, EntityPlayer player) {
        this.item = item;
        this.player = player;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }
}
