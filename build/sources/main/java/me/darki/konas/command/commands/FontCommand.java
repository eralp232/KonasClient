package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.gui.kgui.render.Renderer;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.module.modules.client.HUD;
import me.darki.konas.module.modules.render.Nametags;
import me.darki.konas.util.Logger;
import me.darki.konas.util.render.font.ClickGUIFontRenderWrapper;
import me.darki.konas.util.render.font.CustomFontRenderer;
import me.darki.konas.util.render.font.FontRendererWrapper;

public class FontCommand extends Command {

    public static String lastFont = "default";

    public FontCommand() {
        super("font", "Change the font", new SyntaxChunk("<Font>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatErrorMessage(getChunksAsString());
            return;
        }

        lastFont = args[1];

        HUD.customFontRenderer = new CustomFontRenderer(args[1], 18);
        FontRendererWrapper.setFontRenderer(HUD.customFontRenderer);

        ClickGUIModule.customFontRenderer = new CustomFontRenderer(args[1], 17);
        ClickGUIFontRenderWrapper.setFontRenderer(ClickGUIModule.customFontRenderer);

        Nametags.customFontRenderer = new CustomFontRenderer(args[1], 20);
        Nametags.highResFontRenderer = new CustomFontRenderer(args[1], 60);
        Nametags.setFontRenderer(Nametags.customFont.getValue());

        Renderer.fontRenderer = new CustomFontRenderer(args[1], 18);
        Renderer.subFontRendrer = new CustomFontRenderer(args[1], 16);
    }

}