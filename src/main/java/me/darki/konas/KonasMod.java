package me.darki.konas;

import me.darki.konas.util.KonasGlobals;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = KonasMod.ID, name = KonasMod.NAME, version = KonasMod.VERSION)
public class KonasMod {

    public static final String ID = "konas";
    public static final String NAME = "Konas";
    public static final String VERSION = "b0.10.5";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        KonasGlobals.INSTANCE.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        KonasGlobals.INSTANCE.init();
    }

}
