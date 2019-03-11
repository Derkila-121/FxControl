package mcjty.fxcontrol;


import mcjty.fxcontrol.compat.EnigmaSupport;
import mcjty.fxcontrol.compat.LostCitySupport;
import mcjty.fxcontrol.proxy.CommonProxy;
import mcjty.tools.cache.StructureCache;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@Mod(modid = FxControl.MODID, name = FxControl.MODNAME,
        dependencies =
                "after:forge@[" + FxControl.MIN_FORGE11_VER + ",)",
        version = FxControl.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)",
        acceptableRemoteVersions = "*")
public class FxControl {

    public static final String MODID = "fxcontrol";
    public static final String MODNAME = "FxControl";
    public static final String VERSION = "0.1.6";
    public static final String MIN_FORGE11_VER = "13.19.0.2176";

    @SidedProxy(clientSide = "mcjty.fxcontrol.proxy.ClientProxy", serverSide = "mcjty.fxcontrol.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static FxControl instance;

    public static Logger logger;

    public static boolean lostcities = false;
    public static boolean gamestages = false;
    public static boolean sereneSeasons = false;
    public static boolean baubles = false;
    public static boolean enigma = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        logger = event.getModLog();
        proxy.preInit(event);

        lostcities = Loader.isModLoaded("lostcities");
        gamestages = Loader.isModLoaded("gamestages");
        sereneSeasons = Loader.isModLoaded("sereneseasons");
        baubles = Loader.isModLoaded("baubles");
        enigma = Loader.isModLoaded("enigma");

        if (lostcities) {
            LostCitySupport.register();
            logger.log(Level.INFO, "Enabling support for Lost Cities");
        }
        if (gamestages) {
            logger.log(Level.INFO, "Enabling support for Game Stages");
        }
        if (sereneSeasons) {
            logger.log(Level.INFO, "Enabling support for Serene Seasons");
        }
        if (baubles) {
            logger.log(Level.INFO, "Enabling support for Baubles");
        }
        if (enigma) {
            EnigmaSupport.register();
            logger.log(Level.INFO, "Enabling support for EnigmaScript");
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent e) {
        RulesManager.readRules();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CmdReload());
        event.registerServerCommand(new CmdDebug());
        event.registerServerCommand(new CmdDumpItemNBT());
        event.registerServerCommand(new CmdDumpBlockNBT());
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        StructureCache.CACHE.clean();
    }
}
