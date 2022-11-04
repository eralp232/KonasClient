package me.darki.konas.command.commands;

import me.darki.konas.command.Command;
import me.darki.konas.command.SyntaxChunk;
import me.darki.konas.module.modules.misc.Translate;
import me.darki.konas.util.Logger;
import me.darki.konas.util.translate.TranslateAPI;

import java.io.IOException;
import java.util.Map;

public class LanguageCommand extends Command {

    public LanguageCommand() {
        super("language", "Lets you choose your target language for the Translate Module", new SyntaxChunk("<language>"));
    }

    @Override
    public void onFire(String[] args) {
        if (args.length != getChunks().size() + 1) {
            Logger.sendChatMessage(getChunksAsString());
            return;
        }

        try {
            Map<String, String> langs = TranslateAPI.getLangs();
            System.out.println(langs);
            String language = TranslateAPI.getKeyOrValue(langs, args[1]);
            if (language != null) {
                Translate.targetLanguage = language;
                Logger.sendChatMessage("Set target language to " + Command.SECTIONSIGN + "b" + langs.get(language));
            } else {
                Logger.sendChatErrorMessage("Couldn't find language!");
            }
        } catch (IOException e) {
            Logger.sendChatErrorMessage("Error while fetching languages!");
        }


    }


}
