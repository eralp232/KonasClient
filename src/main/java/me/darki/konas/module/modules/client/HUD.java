package me.darki.konas.module.modules.client;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.commands.FontCommand;
import me.darki.konas.event.events.LoadGuiEvent;
import me.darki.konas.event.events.PotionRenderHUDEvent;
import me.darki.konas.event.events.Render2DEvent;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.module.Module;
import me.darki.konas.setting.IRunnable;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.render.font.CustomFontRenderer;
import me.darki.konas.util.render.font.DefaultFontRenderer;
import me.darki.konas.util.render.font.FontRendererWrapper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Comparator;

public class HUD extends Module {

    public static Setting<Boolean> overlap = new Setting<>("Overlap", true);
    private static Setting<Boolean> potionIcons = new Setting<>("PotionIcons", false);
    private static Setting<Boolean> blur = new Setting<>("Blur", false);
    private static Setting<Boolean> blurEverything = new Setting<>("BlurEverything", true).withVisibility(blur::getValue);
    private Setting<Boolean> customFont = new ListenableSettingDecorator<>("CustomFont", true, new IRunnable<Boolean>() {

        @Override
        public void run(Boolean value) {
            if(value) {
                if(FontRendererWrapper.getFontRenderer() != customFontRenderer) {
                    FontRendererWrapper.setFontRenderer(customFontRenderer);
                }
            } else {
                if(FontRendererWrapper.getFontRenderer() != DefaultFontRenderer.INSTANCE) {
                    FontRendererWrapper.setFontRenderer(DefaultFontRenderer.INSTANCE);
                }
            }
        }

    });
    //private static Setting<Boolean> customFont = new Setting<>("CustomFont", true);

    public HUD() {
        super("HUD", "Displays information on the ingame screen", Category.CLIENT);
    }

    public static CustomFontRenderer customFontRenderer = new CustomFontRenderer(FontCommand.lastFont, 18);

    @Subscriber
    public void onRender2D(Render2DEvent event) {

        if (mc.world == null || mc.player == null) return;


        final FontRenderer fr = mc.fontRenderer;
        final ScaledResolution sr = new ScaledResolution(mc);

        /*
        for (DraggablePanel panel : PanelManager.getPanels()) {
            if (panel.extended && !(KonasGlobals.INSTANCE.clickGUI.hudEditor && mc.currentScreen instanceof ClickGUI)) {
                glTranslated(panel.getX(), panel.getY(), 0);

                panel.renderContent();

                glTranslated(-panel.getX(), -panel.getY(), 0);
            }
        }
         */
    }

    @Subscriber
    public void onPotionEffect(PotionRenderHUDEvent event) {
        if(potionIcons.getValue()) {
            event.cancel();
        }
    }

    @Subscriber
    public void onLoadGui(LoadGuiEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (event.getGui() != null && blur.getValue() && !(event.getGui() instanceof GuiChat)) {
            if(event.getGui() instanceof ClickGUI || blurEverything.getValue()) {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
            }
        } else if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.stopUseShader();
        }

    }

    private void drawPotions(FontRenderer fr, ScaledResolution sr) {
        final int[] yDist;
        if (mc.ingameGUI.getChatGUI().getChatOpen()) {
            yDist = new int[]{sr.getScaledHeight() - (((mc.player.getActivePotionEffects().size() * fr.FONT_HEIGHT) + 5) + (fr.FONT_HEIGHT + 5))};
        } else {
            yDist = new int[]{sr.getScaledHeight() - ((mc.player.getActivePotionEffects().size() * fr.FONT_HEIGHT) + 5)};
        }

        mc.player.getActivePotionEffects().stream()
                .sorted(Comparator.comparingInt(effect -> fr.getStringWidth(getPotionString((PotionEffect) effect))))
                .forEach(potionEffect -> {
                    fr.drawStringWithShadow(getPotionString(potionEffect), sr.getScaledWidth() - 2 - fr.getStringWidth(getPotionString(potionEffect)), yDist[0], getPotionColour(potionEffect));
                    yDist[0] += fr.FONT_HEIGHT;
                });
    }

    private int getPotionColour(PotionEffect effect) {
        String n = getPotionName(effect);

        switch (n) {
            case "Absorption":
                return new Color(33, 118, 255).hashCode();
            case "Fire Resistance":
                return new Color(247, 152, 36).hashCode();
            case "Regeneration":
                return new Color(232, 142, 237).hashCode();
            case "Strength":
            case "Resistance":
                return new Color(237, 28, 36).hashCode();
            case "Hunger":
                return new Color(41, 191, 18).hashCode();
            case "Jump Boost":
                return new Color(0, 204, 51).hashCode();
            case "Haste":
                return new Color(255, 207, 0).hashCode();
            case "Speed":
                return new Color(0, 255, 227).hashCode();
            default:
                return Color.WHITE.hashCode();

        }

    }

    private String getPotionString(PotionEffect effect) {
        return getPotionName(effect) + " " + Potion.getPotionDurationString(effect, 1);
    }

    private String getPotionName(PotionEffect effect) {
        return I18n.format(effect.getEffectName());
    }


}
