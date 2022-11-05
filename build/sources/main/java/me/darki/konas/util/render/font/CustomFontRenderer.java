package me.darki.konas.util.render.font;

import me.darki.konas.config.Config;
import me.darki.konas.gui.kgui.fill.Fill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomFontRenderer implements IFontRenderer {
    public final int FONT_HEIGHT = 9;
    private final Map<String, Float> cachedStringWidth = new HashMap<>();
    private float antiAliasingFactor;
    private UnicodeFont unicodeFont;
    private int prevScaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
    private final String name;
    private final float size;

    public static final File FONTS_FOLDER = new File(Config.KONAS_FOLDER, "fonts");

    public CustomFontRenderer(String fontName, float fontSize) {
        name = fontName;
        size = fontSize;

        try {
            initialize();
        } catch (FontFormatException | IOException | SlickException e) {
            try {
                safeInitialize();
            } catch (Exception ignored) {
            }
        }

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        this.antiAliasingFactor = resolution.getScaleFactor();
    }

    private void initialize() throws IOException, FontFormatException, SlickException {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        prevScaleFactor = resolution.getScaleFactor();
        unicodeFont = new UnicodeFont(getFontByName(name).deriveFont(size * prevScaleFactor / 2));
        unicodeFont.addAsciiGlyphs();
        unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
        unicodeFont.loadGlyphs();
    }

    private void safeInitialize() throws IOException, FontFormatException, SlickException {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        prevScaleFactor = resolution.getScaleFactor();
        unicodeFont = new UnicodeFont(getFontByName("default").deriveFont(size * prevScaleFactor / 2));
        unicodeFont.addAsciiGlyphs();
        unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
        unicodeFont.loadGlyphs();
    }

    public CustomFontRenderer(Font font) {
        this(font.getFontName(), font.getSize());
    }

    public static Font getFontByName(String name) throws IOException, FontFormatException {

        if (name.equalsIgnoreCase("geometric")) return getFontFromInput("/assets/konas/fonts/geometric.ttf");
        else if (name.equalsIgnoreCase("verdana")
                || name.equalsIgnoreCase("default")) {
            return getFontFromInput("/assets/konas/fonts/verdana.ttf");
        }

        File fontFile = new File( FONTS_FOLDER, name + ".ttf");

        if (fontFile.exists()) {
            font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            return font;
        }

        return getFontFromInput("/assets/konas/fonts/verdana.ttf");
    }

    static Font font = null;

    public static Font getFontFromInput(String path) throws IOException, FontFormatException {
        font = Font.createFont(Font.TRUETYPE_FONT, CustomFontRenderer.class.getResourceAsStream(path));
        return font;
    }

    public void drawStringScaled(String text, int givenX, int givenY, int color, double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        drawString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public int drawString(String text, float x, float y, int color) {
        return drawString(text, x, y, color,null);
    }

    public int drawString(String text, float x, float y, Fill fill) {
        return drawString(text, x, y, 0xFFFFFFFF,fill);
    }

    public int drawString(String text, float x, float y, int color, Fill fill) {
        if (text == null)
            return 0;


        text = text.replaceAll("§", String.valueOf(ChatColor.COLOR_CHAR));

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        try {
            if (resolution.getScaleFactor() != prevScaleFactor) {
                prevScaleFactor = resolution.getScaleFactor();
                unicodeFont = new UnicodeFont(getFontByName(name).deriveFont(size * prevScaleFactor / 2));
                unicodeFont.addAsciiGlyphs();
                unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
                unicodeFont.loadGlyphs();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        this.antiAliasingFactor = resolution.getScaleFactor();

        GL11.glPushMatrix();
        GlStateManager.scale(1 / antiAliasingFactor, 1 / antiAliasingFactor, 1 / antiAliasingFactor);
        x *= antiAliasingFactor;
        y *= antiAliasingFactor;
        float originalX = x;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);

        int currentColor = color;

        char[] characters = text.toCharArray();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        String[] parts = ChatColor.PATTERN.split(text);
        int index = 0;
        for (String s : parts) {
            for (String s2 : s.split("\n")) {
                for (String s3 : s2.split("\r")) {

                    if (fill == null) {
                        unicodeFont.drawString(x, y, s3, new org.newdawn.slick.Color(currentColor));
                    } else {
                        unicodeFont.drawFillDisplayList(x, y, s3, fill, 0, text.length());
                    }
                    x += unicodeFont.getWidth(s3);

                    index += s3.length();
                    if (index < characters.length && characters[index] == '\r')
                    {
                        x = originalX;
                        index++;
                    }
                }
                if (index < characters.length && characters[index] == '\n') {
                    x = originalX;
                    y += getHeight(s2) * 2;
                    index++;
                }
            }
            if (index < characters.length) {
                char colorCode = characters[index];
                if (colorCode == ChatColor.COLOR_CHAR)
                {
                    char colorChar = characters[index + 1];
                    int codeIndex = ("0123456789" + "abcdef").indexOf(colorChar);
                    if (codeIndex < 0)
                    {
                        if (colorChar == 'r')
                        {
                            currentColor = color;
                        }
                    }
                    else
                    {
                        currentColor = ChatColor.CODES[codeIndex];
                    }
                    index += 2;
                }
            }
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.bindTexture(0);
        GlStateManager.popMatrix();
        return (int) getWidth(text);
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return 0;

        drawString(StringUtils.stripControlCodes(text), x + 0.5F, y + 0.5F, 0x000000);
        return drawString(text, x, y, color);
    }

    public int drawCenteredString(String text, float x, float y, int color) {
        return drawString(text, x - ((int) getWidth(text) >> 1), y, color);
    }

    public void drawCenteredTextScaled(String text, int givenX, int givenY, int color, double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        drawCenteredString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, int color) {
        drawCenteredString(StringUtils.stripControlCodes(text), x + 0.5F, y + 0.5F, color);
        drawCenteredString(text, x, y, color);
    }

    public float getWidth(String text) {
        if (cachedStringWidth.size() > 1000)
            cachedStringWidth.clear();
        return cachedStringWidth.computeIfAbsent(text, e -> unicodeFont.getWidth(ChatColor.stripColor(text)) / antiAliasingFactor);
    }

    public float getCharWidth(char c) {
        return unicodeFont.getWidth(String.valueOf(c));
    }

    public float getHeight(String s) {
        return unicodeFont.getHeight(s) / 2.0F;
    }

    public UnicodeFont getFont() {
        return unicodeFont;
    }

    public void drawSplitString(ArrayList<String> lines, int x, int y, int color) {
        drawString(String.join("\n\r", lines), x, y, color);
    }

    public List<String> splitString(String text, int wrapWidth)
    {
        List<String> lines = new ArrayList<>();

        String[] splitText = text.split(" ");
        StringBuilder currentString = new StringBuilder();

        for (String word : splitText)
        {
            String potential = currentString + " " + word;

            if (getWidth(potential) >= wrapWidth)
            {
                lines.add(currentString.toString());
                currentString = new StringBuilder();
            }

            currentString.append(word).append(" ");
        }

        lines.add(currentString.toString());
        return lines;
    }

    public float getStringWidth(String text) {
        return (unicodeFont.getWidth(ChatColor.stripColor(text)) / 2F) / (new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor() / 2F);
    }

    @Override
    public int getFontHeight() {
        return FONT_HEIGHT;
    }

    public float getStringHeight(String text) {
        return getHeight(text) / (new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor() / 2F);
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = reverse ? text.length() - 1 : 0;
        int k = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k)
        {
            char c0 = text.charAt(l);
            float i1 = this.getWidth(text);

            if (flag)
            {
                flag = false;

                if (c0 != 'l' && c0 != 'L')
                {
                    if (c0 == 'r' || c0 == 'R')
                    {
                        flag1 = false;
                    }
                }
                else
                {
                    flag1 = true;
                }
            }
            else if (i1 < 0)
            {
                flag = true;
            }
            else
            {
                i += i1;

                if (flag1)
                {
                    ++i;
                }
            }

            if (i > width)
            {
                break;
            }

            if (reverse)
            {
                stringbuilder.insert(0, c0);
            }
            else
            {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }
}
