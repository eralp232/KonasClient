package me.darki.konas.gui.container.containers;

import me.darki.konas.gui.container.Container;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.friends.Friends;
import me.darki.konas.util.render.GuiRenderHelper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RadarContainer extends Container {
    private final Setting<Mode> mode = new Setting<>("Rotate", Mode.ARROW);
    
    private final Setting<Boolean> players = new Setting<>("Players", true);
    private final Setting<Boolean> mobs = new Setting<>("Mobs", true);
    private final Setting<Boolean> animals = new Setting<>("Animals", true);
    private final Setting<Boolean> invisibles = new Setting<>("Invisibles", true);
    private final Setting<Double> blipSize = new Setting<>("Size", 1D, 2D, 0.1D, 0.05D);

    private enum Mode {
        ARROW, COMPASS
    }

    public RadarContainer() {
        super("Radar", 2, 100, 100, 100);
        color.getValue().setColor(new Color(31, 31, 31, 170).hashCode());
        outlineColor.getValue().setColor(new Color(255, 85, 255, 255).hashCode());
    }

    @Override
    public void onRender() {
        if(mc.player == null || mc.world == null) return;
        if (!mc.gameSettings.showDebugInfo) {
            int x = (int) getPosX();
            int y = (int) getPosY();
            int radius = (int) (getWidth() * 0.425);
            int centerX = (int) (x + (getWidth() / 2));
            int centerY = (int) (y + (getHeight() / 2));

            // RenderUtils.rectangleBordered(x, y, x + size, y + size, 1,
            // 0xFF000000, 0xFF1F1F1F);
            GuiRenderHelper.drawRect(getPosX(), getPosY(), getWidth(), getHeight(), color.getValue().getColor());
            GuiRenderHelper.drawOutlineRect(getPosX(), getPosY(), getWidth(), getHeight(), 1F, outlineColor.getValue().getColor());
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(GL11.GL_BLEND);

            // Entities
            for (Entity e : mc.world.loadedEntityList) {
                if (isCorrectEntity(e)) {
                    double diffX = mc.player.posX - e.posX;
                    double diffZ = mc.player.posZ - e.posZ;
                    double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
                    if (dist <= (getWidth() / 2.0) - (getWidth() / 50.0)) {

                        GL11.glPushMatrix();

                        double posX = mc.player.posX - e.posX + x + getWidth() / 2;
                        double posY = mc.player.posZ - e.posZ + y + getWidth() / 2;

                        GL11.glTranslated(centerX, centerY, 0);
                        GL11.glRotatef(mode.getValue() != Mode.ARROW ? -mc.player.rotationYaw : -180, 0, 0, 1);
                        GL11.glTranslated(-centerX, -centerY, 0);

                        double headScale = 12;

                        GL11.glTranslated(posX, posY, 0);
                        GL11.glRotatef((mode.getValue() != Mode.ARROW ? mc.player.rotationYaw + 180 : 0) + 180, 0, 0, 1);
                        GL11.glTranslated(-posX, -posY, 0);

                        if (e instanceof EntityPlayer) {
                            if (Friends.isFriend(e.getName())) {
                                GL11.glColor4f(0.3F, 1.0F, 0.3F, 1.0F);
                            } else {
                                GL11.glColor4f(1.0F, 0.3F, 0.3F, 1.0F);
                            }
                        } else if (e instanceof EntityMob) {
                            GL11.glColor4f(1.0F, 0.5F, 0.5F, 1.0F);
                        } else if (e instanceof EntityAnimal) {
                            GL11.glColor4f(0.5F, 1.0F, 0.5F, 1.0F);
                        } else {
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.0F);
                        }
                        GL11.glBegin(GL11.GL_POLYGON);
                        {
                            GL11.glVertex2d(posX, posY + blipSize.getValue().floatValue());
                            GL11.glVertex2d(posX + blipSize.getValue().floatValue(), posY);
                            GL11.glVertex2d(posX, posY - blipSize.getValue().floatValue());
                            GL11.glVertex2d(posX - blipSize.getValue().floatValue(), posY);
                        }
                        GL11.glEnd();
                        GL11.glPopMatrix();
                    }
                }
            }

            // The Player
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPushMatrix();
            GL11.glTranslatef(centerX, centerY, 0);
            GL11.glRotatef(mode.getValue() == Mode.ARROW ? mc.player.rotationYaw : 180, 0, 0, 1);
            GL11.glTranslatef(-centerX, -centerY, 0);
            GL11.glBegin(GL11.GL_POLYGON);
            {
                GL11.glVertex2d(centerX, centerY + 3);
                GL11.glVertex2d(centerX + 1.5, centerY - 3);
                GL11.glVertex2d(centerX - 1.5, centerY - 3);
            }
            GL11.glEnd();
            GL11.glPopMatrix();

            // North, South, East and West
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            Object[][] orientation = { { "N", -90.0 }, { "S", 90.0 }, { "E", 0.0 }, { "W", 180.0 } };
            for (Object[] ori : orientation) {
                if (ori.length < 2) {
                    return;
                }
                if (ori[0] instanceof String) {
                    if (ori[1] instanceof Double) {
                        String s = (String) ori[0];
                        Double i = (Double) ori[1];
                        if (mode.getValue() != Mode.ARROW) {
                            i -= mc.player.rotationYaw;
                            i -= 180;
                        }
                        mc.fontRenderer.drawStringWithShadow(s, (float) (centerX + (radius * Math.cos(Math.toRadians(i)))) - mc.fontRenderer.getStringWidth(s) / 2, (float) (centerY + (radius * Math.sin(Math.toRadians(i)))) - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
                    }
                }
            }
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    public boolean isCorrectEntity(Object o) {
        if (!(o instanceof Entity)) {
            return false;
        }
        Entity ent = (Entity) o;
        if (ent == mc.player) {
            return false;
        }
        if (ent.isInvisible() && !invisibles.getValue()) {
            return false;
        }
        if (o instanceof EntityPlayer) {
            if (players.getValue()) {
                EntityPlayer player = (EntityPlayer) o;
                return true;
            }
        } else if (o instanceof EntityMob) {
            if (mobs.getValue()) {
                return true;
            }
        } else if (o instanceof EntityAnimal) {
            if (animals.getValue()) {
                return true;
            }
        }
        return false;
    }
}
