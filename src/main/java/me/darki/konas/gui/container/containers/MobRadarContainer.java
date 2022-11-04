package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Parent;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Comparator;

public class MobRadarContainer extends Container {

    public Setting<ColorSetting> textColor = new Setting<>("TextColor", new ColorSetting(new Color(255, 85, 255, 255).hashCode(), false));
    private static Setting<Boolean> ghasts = new Setting<>("Ghasts", true);
    private static Setting<Boolean> slimes = new Setting<>("Slimes", true);
    private static Setting<Boolean> donkeys = new Setting<>("Donkeys", true);
    private static Setting<Boolean> llamas = new Setting<>("Llamas", true);
    private static Setting<Boolean> cats = new Setting<>("Cats", true);
    private static Setting<Boolean> dogs = new Setting<>("Dogs", true);
    private static Setting<Boolean> parrots = new Setting<>("Parrots", true);
    private static Setting<Parent> display = new Setting<>("Display", new Parent(false));
    private static Setting<Boolean> health = new Setting<>("Health", true).withParent(display);
    private static Setting<Boolean> entityId = new Setting<>("EntityID", true).withParent(display);
    private static Setting<Boolean> distance = new Setting<>("Distance", true).withParent(display);
    private static Setting<Boolean> coordinates = new Setting<>("Coordinates", true).withParent(display);

    public MobRadarContainer() {
        super("MobRadar", 500, 400, 10, 10);
    }

    @Override
    public void onRender() {
        super.onRender();

        float[] yOffset = {0};
        float[] maxWidth = {0};

        mc.world.loadedEntityList.stream()
                .filter(e -> (ghasts.getValue() && e instanceof EntityGhast)
                        || (slimes.getValue() && e instanceof EntitySlime)
                        || (donkeys.getValue() && e instanceof EntityDonkey)
                        || (llamas.getValue() && e instanceof EntityLlama)
                        || (cats.getValue() && e instanceof EntityOcelot)
                        || (dogs.getValue() && e instanceof EntityWolf)
                        || (parrots.getValue() && e instanceof EntityParrot))
                .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                .forEach(e -> {
                    FontRendererWrapper.drawStringWithShadow(getEntityString((EntityLivingBase) e), getPosX(), getPosY() + yOffset[0], textColor.getValue().getColor());
                    yOffset[0] += FontRendererWrapper.getStringHeight(getEntityString((EntityLivingBase) e));
                    if(FontRendererWrapper.getStringWidth(getEntityString((EntityLivingBase) e)) > maxWidth[0]) maxWidth[0] = FontRendererWrapper.getStringWidth(getEntityString((EntityLivingBase) e));
                });

        setHeight(yOffset[0]);
        setWidth(maxWidth[0]);

        if(yOffset[0] == 0 || maxWidth[0] == 0) {
            setHeight(50);
            setWidth(100);
        }

    }

    private String getEntityString(EntityLivingBase entity) {
        DecimalFormat df = new DecimalFormat("#.##");
        String healthString = " (" + (entity.getHealth() + entity.getAbsorptionAmount()) + ")";
        String entityIdString = " [" + entity.getEntityId() + "]";
        String distanceString = " §c" + df.format(mc.player.getDistance(entity));
        String coordinateString = " §rXYZ " + entity.getPosition().getX() + " " + entity.getPosition().getY() + " " + entity.getPosition().getZ();
        return entity.getName()
                + (health.getValue() ? healthString : "")
                + (entityId.getValue() ? entityIdString : "")
                + (distance.getValue() ? distanceString : "")
                + (coordinates.getValue() ? coordinateString : "");
    }

}
