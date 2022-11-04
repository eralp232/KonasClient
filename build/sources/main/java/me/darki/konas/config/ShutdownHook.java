package me.darki.konas.config;

public class ShutdownHook extends Thread {

    @Override
    public void run() {
        Config.save(Config.currentConfig);
    }

}
