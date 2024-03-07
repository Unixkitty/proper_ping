package com.unixkitty.proper_ping.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unixkitty.proper_ping.Config;
import com.unixkitty.proper_ping.network.packet.PongS2CPacket;
import it.unimi.dsi.fastutil.longs.LongIntMutablePair;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.time.Duration;
import java.util.UUID;

public class ServerPongPingHandler
{
    private static final Cache<UUID, LongIntMutablePair> cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(35)).build();

    public static void handle(NetworkEvent.Context context, long time, int lastClientLatency)
    {
        ServerPlayer player = context.getSender();

        if (player != null)
        {
            LongIntMutablePair lastResponseTimePair = cache.getIfPresent(player.getUUID());

            if (lastResponseTimePair == null)
            {
                lastResponseTimePair = LongIntMutablePair.of(-1, 0);
            }

            final long lastResponseTime = lastResponseTimePair.leftLong();
            long currentTime = Util.getMillis();

            if (currentTime - lastResponseTime >= 1000 || lastResponseTime == -1)
            {
                ModNetworkDispatcher.sendToClient(new PongS2CPacket(time), player);

                context.enqueueWork(() -> player.latency = lastClientLatency);

                lastResponseTimePair.left(currentTime);
                lastResponseTimePair.right(lastClientLatency);

                //Guava Cache should be thread safe
                cache.put(player.getUUID(), lastResponseTimePair);
            }
        }
    }

    public static void updatePlayerLatency(ServerPlayer player, int wouldBeValue)
    {
        LongIntMutablePair lastResponseTimePair = cache.getIfPresent(player.getUUID());

        if (lastResponseTimePair != null && lastResponseTimePair.rightInt() > 0)
        {
            player.latency = lastResponseTimePair.rightInt();
        }
        else
        {
            player.latency = wouldBeValue;
        }
    }

    public static int getLatencyUpdateInterval()
    {
        return Config.multiplayerLatencyBroadcastInterval.get();
    }
}
