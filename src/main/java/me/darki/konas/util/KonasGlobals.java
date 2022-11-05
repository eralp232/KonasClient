package me.darki.konas.util;

import cookiedragon.eventsystem.EventDispatcher;
import me.darki.konas.KonasRPC;
import me.darki.konas.command.CommandManager;
import me.darki.konas.config.Config;
import me.darki.konas.config.ShutdownHook;
import me.darki.konas.event.listener.*;
import me.darki.konas.gui.altmanager.GuiAltManager;
import me.darki.konas.gui.clickgui.ClickGUI;
import me.darki.konas.gui.container.ContainerManager;
import me.darki.konas.gui.kgui.KonasGuiScreen;
import me.darki.konas.gui.kgui.element.container.MasterContainer;
import me.darki.konas.module.ModuleManager;
import me.darki.konas.module.modules.client.ClickGUIModule;
import me.darki.konas.util.client.TickCalculation;
import me.darki.konas.util.client.TickRateUtil;
import me.darki.konas.util.interaction.RotationManager;
import me.darki.konas.util.math.VectorUtils;
import me.darki.konas.util.pathfinding.PathManager;
import me.darki.konas.util.render.font.CustomFontRenderer;
import me.darki.konas.util.timer.TimerManager;
import me.darki.konas.util.waypoint.WaypointManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KonasGlobals {

    public static KonasGlobals INSTANCE = new KonasGlobals();

    public ClickGUI clickGUI;

    public KonasGuiScreen konasGuiScreen;

    public GuiAltManager altManager;

    public TargetManager targetManager;

    public TimerManager timerManager;

    public PathManager pathManager;

    public ContainerManager containerManager;

    public VectorUtils vectorUtils;

    public WaypointManager waypointManager;

    public RotationManager rotationManager;

    public KonasRPC rpc;

    public void preInit() {
        if (!Config.KONAS_FOLDER.exists()) Config.KONAS_FOLDER.mkdir();

        if (!Config.CONFIGS.exists()) Config.CONFIGS.mkdir();

        if (Config.KONAS_FOLDER.listFiles() != null) {
            List<File> configFiles = Arrays.stream(Config.KONAS_FOLDER.listFiles())
                    .filter(f -> f.getName().endsWith(".json") && !f.getName().equalsIgnoreCase("config.json") && !f.getName().equalsIgnoreCase("accounts.json"))
                    .collect(Collectors.toList());

            for (File configFile : configFiles) {
                Config.migrate(configFile, new File(Config.CONFIGS, configFile.getName()));
            }
        }

        Config.migrate(Config.CONFIG_OLD, Config.CONFIG);
        Config.migrate(Config.ACCOUNTS_OLD, Config.ACCOUNTS);
        Config.migrate(new File(Minecraft.getMinecraft().gameDir, "Fonts"), CustomFontRenderer.FONTS_FOLDER);

        if (new File(Minecraft.getMinecraft().gameDir, "Fonts").exists()) {
            Config.migrate(new File(Minecraft.getMinecraft().gameDir, "Fonts"), CustomFontRenderer.FONTS_FOLDER);
        } else {
            CustomFontRenderer.FONTS_FOLDER.mkdir();
        }
    }

    public void init() {
        // ask server if downloded
        // if downloaded then initialize {
        // else don't crash, but dont initialize

        ModuleManager.init();
        CommandManager.init();

        containerManager = new ContainerManager();
        containerManager.init();

        MinecraftForge.EVENT_BUS.register(Adapter.INSTANCE);
        EventDispatcher.Companion.register(KeyListener.INSTANCE);
        EventDispatcher.Companion.register(CommandListener.INSTANCE);
        EventDispatcher.Companion.subscribe(KeyListener.INSTANCE);
        EventDispatcher.Companion.subscribe(CommandListener.INSTANCE);

        EventDispatcher.Companion.register(JoinLeaveListener.INSTANCE);
        EventDispatcher.Companion.subscribe(JoinLeaveListener.INSTANCE);

        EventDispatcher.Companion.register(TickCalculation.INSTANCE);
        EventDispatcher.Companion.subscribe(TickCalculation.INSTANCE);
        EventDispatcher.Companion.register(BackupListener.INSTANCE);
        EventDispatcher.Companion.subscribe(BackupListener.INSTANCE);
        EventDispatcher.Companion.register(MuteListener.INSTANCE);
        EventDispatcher.Companion.subscribe(MuteListener.INSTANCE);
        EventDispatcher.Companion.register(ServerListener.INSTANCE);
        EventDispatcher.Companion.subscribe(ServerListener.INSTANCE);

        clickGUI = new ClickGUI();
        clickGUI.initialize();

        konasGuiScreen = new KonasGuiScreen();
        konasGuiScreen.initialize();
        MasterContainer.updateColors();

        targetManager = new TargetManager();
        EventDispatcher.Companion.register(targetManager);
        EventDispatcher.Companion.subscribe(targetManager);

        timerManager = new TimerManager();
        EventDispatcher.Companion.register(timerManager);
        EventDispatcher.Companion.subscribe(timerManager);

        vectorUtils = new VectorUtils();
        EventDispatcher.Companion.register(vectorUtils);
        EventDispatcher.Companion.subscribe(vectorUtils);

        rotationManager = new RotationManager();
        EventDispatcher.Companion.register(rotationManager);
        EventDispatcher.Companion.subscribe(rotationManager);

        waypointManager = new WaypointManager();

        pathManager = new PathManager();

        TickRateUtil.INSTANCE = new TickRateUtil();

        altManager = new GuiAltManager(Minecraft.getMinecraft().currentScreen);
        altManager.initializeGui();

        Config.load(Config.getLastModified(), true);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        try {
            if (!System.getProperty("os.arch").equals("aarch64")) {
                rpc = new KonasRPC();
            } else {
                rpc = null;
            }
        } catch (Exception e) {
            rpc = null;
        }
    }

    private KonasGlobals() {}

}
