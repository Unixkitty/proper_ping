package com.unixkitty.proper_ping.client;

import com.unixkitty.proper_ping.ProperPing;
import com.unixkitty.proper_ping.network.ModNetworkDispatcher;
import com.unixkitty.proper_ping.network.packet.PingC2SPacket;
import com.unixkitty.proper_ping.network.packet.PongS2CPacket;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ProperPing.MODID, value = Dist.CLIENT)
public class ClientPingPongHandler
{
    public static long pingTimeChallenge = Util.getMillis();
    public static boolean pingDataUpdated = false;
    public static int rttLatency = 0;
    public static String queueText = "";

    private static int tickCount = 0;
    private static int lastPingTime = 0;

    @SubscribeEvent
    public static void tick(final TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().isLocalServer() || event.phase != TickEvent.Phase.END)
        {
            return;
        }

        if (tickCount - lastPingTime >= 20)
        {
            lastPingTime = tickCount;

            ModNetworkDispatcher.sendToServer(new PingC2SPacket(pingTimeChallenge = Util.getMillis(), rttLatency));
        }

        if (tickCount % 20 == 0 && pingDataUpdated)
        {
            rttLatency = calculateAverage();

            queueText = " [" + PongS2CPacket.RTT_QUEUE.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";

            pingDataUpdated = false;
        }

        tickCount = tickCount == Integer.MAX_VALUE ? 0 : ++tickCount;
    }

    /*private static int calculateAverage()
    {
        if (PongS2CPacket.RTT_QUEUE.isEmpty())
        {
            return 0;
        }

        int sum = 0;

        for (int value : PongS2CPacket.RTT_QUEUE)
        {
            sum += value;
        }

        return Math.max(sum / PongS2CPacket.RTT_QUEUE.size(), 0);
    }*/

    private static int calculateAverage()
    {
        return PongS2CPacket.RTT_QUEUE.isEmpty() ? 0 : Math.max(PongS2CPacket.RTT_QUEUE.stream().mapToInt(Integer::intValue).sum() / PongS2CPacket.RTT_QUEUE.size(), 0);
    }

}
