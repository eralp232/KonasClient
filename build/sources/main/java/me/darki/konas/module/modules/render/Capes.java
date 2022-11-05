package me.darki.konas.module.modules.render;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.LocateCapeEvent;
import me.darki.konas.module.Module;
import me.darki.konas.util.network.APIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class Capes extends Module {

    public HashMap<String, ResourceLocation> RESOURCE_LOCATIONS = new HashMap<>();

    public HashMap<String, String> capes = new HashMap<>();

    public Capes() {
        super("Capes", Category.RENDER);
    }
    @Override
    public void onEnable() {
        if(capes.isEmpty()) {
            capes = APIUtils.getCapeTypes();
        }
    }

    @Subscriber
    public void onLocateCape(LocateCapeEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (capes.get(event.getName().toUpperCase()) != null) {
            String type = capes.get(event.getName().toUpperCase());
            if(RESOURCE_LOCATIONS.get(type) == null) {
                mc.addScheduledTask(()  -> {
                    DynamicTexture image = APIUtils.getCapeDynamicTexture(type);
                    ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(type, image);
                    RESOURCE_LOCATIONS.put(type, location);
                    Minecraft.getMinecraft().getTextureManager().loadTexture(location, image);
                });
            }
            event.setResourceLocation(RESOURCE_LOCATIONS.get(type));
            event.cancel();
        }
    }

}
