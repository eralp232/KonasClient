package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.command.Command;
import me.darki.konas.event.events.ChatEvent;
import me.darki.konas.module.Module;
import me.darki.konas.util.Logger;
import me.darki.konas.util.translate.TranslateAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Translate extends Module {

    private static final ReentrantLock mutex = new ReentrantLock();

    private boolean translatedMessageIncoming = false;

    public static String targetLanguage = null;

    public Translate() {
        super("Translate", "Translate your chat message into any language", Category.MISC);
    }

    @Override
    public void onEnable() {

        if (targetLanguage == null) {

            Logger.sendChatErrorMessage("Target language is null, please use the " + Command.getPrefix() + "language command to set your target language!");
            this.toggle();

        }

    }

    @Subscriber
    public void onMessage(ChatEvent event) {

        String s = event.getMessage();

        if (s.startsWith("/") || s.startsWith(Command.getPrefix())) return;

        if (translatedMessageIncoming) {
            event.cancel();
            translatedMessageIncoming = false;
        }

        if (s.endsWith(ChatAppend.KONAS_APPEND)) {
            s = s.substring(0, s.length() - ChatAppend.KONAS_APPEND.length());
        }


        String[] sArray = s.split(" ");

        final String[] inputLanguage = {null};


        try {
            new Thread(() -> {
                try {

                    mutex.lock();

                    ArrayList<String> detectedLanguages = new ArrayList<>();

                    for (String i : sArray) {
                        detectedLanguages.add(TranslateAPI.detectLanguage(i));
                    }

                    inputLanguage[0] = detectedLanguages.stream()
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                            .entrySet().stream().max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey).orElse(null);

                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.sendChatMessage("Couldnt find Input Language");
                } finally {
                    mutex.unlock();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String[] translatedMessage = {""};

        try {
            String finalTargetLanguage = targetLanguage;
            String finalS = s;
            new Thread(() -> {
                try {
                    mutex.lock();
                    translatedMessage[0] = TranslateAPI.translate(finalS, inputLanguage[0], finalTargetLanguage);
                    if (!translatedMessage[0].equals("")) {
                        translatedMessageIncoming = true;
                        mc.player.sendChatMessage(translatedMessage[0]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.sendChatMessage("Couldn't translate message");
                } finally {
                    mutex.unlock();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
