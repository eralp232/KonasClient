package me.darki.konas.module.modules.misc;

import cookiedragon.eventsystem.Subscriber;
import me.darki.konas.config.Config;
import me.darki.konas.event.events.UpdateEvent;
import me.darki.konas.module.Module;
import me.darki.konas.setting.ListenableSettingDecorator;
import me.darki.konas.setting.Setting;
import me.darki.konas.util.Logger;
import me.darki.konas.util.timer.Timer;

import java.util.ArrayList;
import java.util.Random;

public class Spammer extends Module {

    private final Setting<Float> delay = new Setting<>("Delay", 5F, 20F, 0.1F, 0.1F);
    public static final ListenableSettingDecorator<String> spammerFile = new ListenableSettingDecorator<>("File", "spammer.txt", arg -> {
        Config.loadSpammerFile(arg, true);
        index = 0;
    });
    private final Setting<Boolean> random = new Setting<>("Random", true);
    private final Setting<Boolean> antiAntiSpam = new Setting<>("AntiAntiSpam", false);

    private final Timer spamTimer = new Timer();

    public static ArrayList<String> spamList = new ArrayList<>();

    private static int index = 0;

    public Spammer() {
        super("Spammer", Category.MISC);
    }

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) return;
       Config.loadSpammerFile(spammerFile.getValue(), true);
       index = 0;
    }

    @Subscriber
    public void onUpdate(UpdateEvent event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if (spamTimer.hasPassed(delay.getValue() * 1000)) {
            if(spamList.isEmpty()) {
                Logger.sendChatErrorMessage("Caution: Spammer file '" + spammerFile.getValue() + "' is empty!");
                spamTimer.reset();
                return;
            }
            String spamMessage;
            if (random.getValue()) {
                spamMessage = spamList.get(new Random().nextInt(spamList.size()));
            } else {
                if (index == spamList.size()) {
                    index = 0;
                }
                spamMessage = spamList.get(index);
                index++;
            }
            if(antiAntiSpam.getValue()) {
                spamMessage += " " + new Random().nextInt(69420);
            }
            mc.player.sendChatMessage(spamMessage);
            spamTimer.reset();
        }
    }

}
