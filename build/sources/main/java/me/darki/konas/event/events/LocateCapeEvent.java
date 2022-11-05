package me.darki.konas.event.events;

import net.minecraft.util.ResourceLocation;

public class LocateCapeEvent extends CancellableEvent {

    ResourceLocation resourceLocation;
    String name;

    public LocateCapeEvent(String name) {
        this.name = name;
        this.resourceLocation = null;
    }

    public String getName() {
        return name;
    }

    public void setUuid(String name) {
        this.name = name;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

}
