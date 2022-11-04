package me.darki.konas.module.modules.render;

import com.google.common.io.ByteStreams;
import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.event.events.PacketEvent;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ColorSetting;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.CrosshairRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.opengl.GL11;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;
import java.io.*;

public class HitMarkers extends Module {
    private static Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET);
    private static Setting<Boolean> sFX = new Setting<>("SFX", false);
    private static Setting<Float> volume = new Setting<>("Volume", 2.5F, 5F, 0F, 0.1F).withVisibility(sFX::getValue);
    private static Setting<Integer> time = new Setting<>("Time", 5, 20, 1, 1);
    private static Setting<Integer> offset = new Setting<>("Offset", 5, 20, 1, 1);
    private static Setting<Integer> length = new Setting<>("Length", 10, 50, 1, 1);
    private static Setting<Float> width = new Setting<>("Thickness", 1F, 5F, 0.1F, 0.1F);
    private static Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0xFFFFFFFF));

    private enum Mode {
        MOUSE, PACKET
    }

    private int hitting = 0;

    private final File hitSound = new File(mc.gameDir + File.separator + "Konas" + File.separator + "hitmarker.wav");

    public HitMarkers() {
        super("HitMarkers", Category.RENDER);

        try {
            if (!hitSound.exists()) {
                InputStream stream = getClass().getClassLoader().getResourceAsStream("assets/sounds/hitmarker.wav");
                FileOutputStream outputStream = new FileOutputStream(hitSound);
                ByteStreams.copy(stream, outputStream);
            }
        } catch (Exception e) {

        }
    }

    private void playSound() {
        if (!hitSound.exists()) return;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(hitSound);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            FloatControl gainControl =
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-50F + volume.getValue() * 10F);
            clip.start();
        } catch (Exception ex) {
        }
    }

    @Subscriber
    public void onCPacketUseEntity(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mode.getValue() == Mode.MOUSE) return;
        try {
            if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK) {
                hitting = time.getValue();
                if (sFX.getValue()) {
                    playSound();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscriber
    public void onMouseClick(MouseEvent event) {
        if (mc.player == null) return;
        if (mode.getValue() == Mode.PACKET) return;
        try {
            if (event.getButton() == 0 & mc.objectMouseOver.entityHit != null) {
                hitting = time.getValue();
                if (sFX.getValue()) {
                    playSound();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscriber
    public void onRender(Render2DEvent event) {
        try {
            if (hitting > 0) {
                ScaledResolution resolution = new ScaledResolution(mc);
                draw(resolution.getScaledWidth() / 2, resolution.getScaledHeight() / 2);
                hitting--;
            }
        } catch (Exception ignored) {

        }
    }

    private void draw(int drawX, int drawY) {
        try {
            GL11.glPushMatrix();
            GL11.glTranslatef(drawX, drawY, 0.0F);
            GL11.glRotatef(45, drawX, drawY, 8000.0F);
            GL11.glTranslatef(-drawX, -drawY, 0.0F);
            renderCrosshair(drawX, drawY, color.getValue().getColorObject());
            GL11.glPopMatrix();
        } catch (Exception ignored) {

        }
    }

    private void renderCrosshair(int drawX, int drawY, Color renderColour) {
        try {
            float thickness = width.getValue();
            CrosshairRenderer.drawFilledRectangle(drawX - thickness, (drawY - offset.getValue() - length.getValue()), drawX + thickness, (drawY - offset.getValue()), renderColour, true);
            CrosshairRenderer.drawFilledRectangle(drawX - thickness, (drawY + offset.getValue()), drawX + thickness, (drawY + offset.getValue() + length.getValue()), renderColour, true);
            CrosshairRenderer.drawFilledRectangle((drawX - offset.getValue() - length.getValue()), drawY - thickness, (drawX - offset.getValue()), drawY + thickness, renderColour, true);
            CrosshairRenderer.drawFilledRectangle((drawX + offset.getValue()), drawY - thickness, (drawX + offset.getValue() + length.getValue()), drawY + thickness, renderColour, true);
        } catch (Exception ignored) {

        }
    }
}