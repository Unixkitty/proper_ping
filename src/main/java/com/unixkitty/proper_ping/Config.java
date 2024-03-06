package com.unixkitty.proper_ping;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = ProperPing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec SERVER_CONFIG;

    public static ForgeConfigSpec.BooleanValue pingHudEnabled;
    public static ForgeConfigSpec.BooleanValue drawTextWithShadow;
    public static ForgeConfigSpec.BooleanValue showPingQueue;
    public static ForgeConfigSpec.IntValue pingHudX;
    public static ForgeConfigSpec.IntValue pingHudY;

    public static ForgeConfigSpec.BooleanValue playerListNumbers;

    public static ForgeConfigSpec.IntValue multiplayerLatencyBroadcastInterval;

    static
    {
        ForgeConfigSpec.Builder clientConfig = new ForgeConfigSpec.Builder();

        {
            clientConfig.push("Ping HUD");
            pingHudEnabled = clientConfig.comment("Enable ping HUD").define("pingHudEnabled", true);
            drawTextWithShadow = clientConfig.comment("Draw text with a shadow").define("drawTextWithShadow", true);
            showPingQueue = clientConfig.comment("Additionally show 5 last ping values").define("showPingQueue", false);
            pingHudX = clientConfig.defineInRange("pingHudX", 6, 0, Integer.MAX_VALUE);
            pingHudY = clientConfig.defineInRange("pingHudY", 18, 0, Integer.MAX_VALUE);
            clientConfig.pop();
        }

        {
            clientConfig.push("Other");
            playerListNumbers = clientConfig.comment("Show numbers in milliseconds in player list").define("playerListNumbers", true);
            clientConfig.pop();
        }

        CLIENT_CONFIG = clientConfig.build();

        ForgeConfigSpec.Builder serverConfig = new ForgeConfigSpec.Builder();

        {
            multiplayerLatencyBroadcastInterval = serverConfig.comment("How often should the server broadcast player latency values")
                    .comment("Vanilla interval is 600 ticks")
                    .defineInRange("multiplayerLatencyBroadcastInterval", 100, 20, 600);
        }

        SERVER_CONFIG = serverConfig.build();
    }

    private static void reload(ModConfig config, ModConfig.Type type)
    {
        if (Objects.requireNonNull(type) == ModConfig.Type.CLIENT)
        {
            CLIENT_CONFIG.setConfig(config.getConfigData());
        }
        else if (Objects.requireNonNull(type) == ModConfig.Type.SERVER)
        {
            SERVER_CONFIG.setConfig(config.getConfigData());
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event)
    {
        if (!event.getConfig().getModId().equals(ProperPing.MODID)) return;

        reload(event.getConfig(), event.getConfig().getType());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading event)
    {
        if (!event.getConfig().getModId().equals(ProperPing.MODID)) return;

        reload(event.getConfig(), event.getConfig().getType());
    }
}
