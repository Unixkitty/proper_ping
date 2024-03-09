package com.unixkitty.proper_ping;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ProperPing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec SERVER_CONFIG;

    public static ForgeConfigSpec.BooleanValue pingHudEnabled;
    public static ForgeConfigSpec.BooleanValue showPingQueue;
    public static ForgeConfigSpec.BooleanValue drawTextWithShadow;
    public static ForgeConfigSpec.BooleanValue leftOrRight;
    public static ForgeConfigSpec.IntValue horizontalPadding;
    public static ForgeConfigSpec.IntValue lineFromTop;
    public static ForgeConfigSpec.IntValue verticalPadding;

    public static ForgeConfigSpec.BooleanValue playerListNumbers;

    public static ForgeConfigSpec.IntValue multiplayerLatencyBroadcastInterval;

    static
    {
        ForgeConfigSpec.Builder clientConfig = new ForgeConfigSpec.Builder();

        {
            clientConfig.push("Ping HUD");
            pingHudEnabled = clientConfig.comment("Enable ping HUD").define("pingHudEnabled", true);
            showPingQueue = clientConfig.comment("Additionally show 5 last ping values").define("showPingQueue", false);
            drawTextWithShadow = clientConfig.comment("Draw text with a shadow").define("drawTextWithShadow", true);
            leftOrRight = clientConfig.comment("Draw ping either on the left side (true), or on the right (false)").define("leftOrRight", true);
            horizontalPadding = clientConfig.comment("How many scaled GUI pixels to start drawing from the edge of the screen").defineInRange("horizontalPadding", 4, 0, Integer.MAX_VALUE);
            lineFromTop = clientConfig.comment("On which line counting from the top to start drawing from").defineInRange("lineFromTop", 1, 0, Integer.MAX_VALUE);
            verticalPadding = clientConfig.comment("How many scaled GUI pixels to start drawing from the top of the screen").defineInRange("verticalPadding", 4, 0, Integer.MAX_VALUE);
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
        if (config.getModId().equals(ProperPing.MODID))
        {
            if (type == ModConfig.Type.SERVER)
            {
                SERVER_CONFIG.setConfig(config.getConfigData());
            }
            else if (type == ModConfig.Type.CLIENT)
            {
                CLIENT_CONFIG.setConfig(config.getConfigData());
            }
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
