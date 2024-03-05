package com.unixkitty.proper_ping;

import com.unixkitty.proper_ping.network.ModNetworkDispatcher;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ProperPing.MODID)
public class ProperPing
{
    // The MODID value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "proper_ping";
    public static final String MODNAME = "Proper Ping";

    public static final Logger LOG = LogManager.getLogger(MODNAME);

    public ProperPing()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(ModNetworkDispatcher::register);
    }
}
