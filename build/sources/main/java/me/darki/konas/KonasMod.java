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

    public static final String ID = "yoinkhack";
    public static final String NAME = "yoinkhack";
    public static final String VERSION = "0.10-b15";

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
