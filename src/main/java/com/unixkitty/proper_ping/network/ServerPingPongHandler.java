package com.unixkitty.proper_ping.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.unixkitty.proper_ping.Config;
import com.unixkitty.proper_ping.network.packet.PingS2CPacket;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public class ServerPingPongHandler
{
    private static final Cache<UUID, ServerPlayerPingInfo> cache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(35)).build();

    public static void handleFor(final ServerPlayer player, boolean singleplayerOwner)
    {
        if (player == null || singleplayerOwner) return;

        UUID playerUUID = player.getUUID();
        ServerPlayerPingInfo playerPingInfo = getCachedInfo(playerUUID);

        long currentTime = Util.getMillis();

        if (currentTime - playerPingInfo.pingTime >= 1000 && !playerPingInfo.pingPending)
        {
            playerPingInfo.pingPending = true;
            playerPingInfo.pingTime = currentTime;
            playerPingInfo.pingTimeChallenge = currentTime;

            ModNetworkDispatcher.sendToClient(new PingS2CPacket(currentTime, playerPingInfo.averageLatency, playerPingInfo.RTT_QUEUE.list.elements()), player);

            cache.put(playerUUID, playerPingInfo);
        }
    }

    public static void handlePongFor(final NetworkEvent.Context context, @NotNull final ServerPlayer player, long originalTime)
    {
        UUID playerUUID = player.getUUID();
        ServerPlayerPingInfo playerPingInfo = getCachedInfo(playerUUID);

        if (playerPingInfo.pingPending && originalTime == playerPingInfo.pingTimeChallenge)
        {
            int rtt = (int) (Util.getMillis() - playerPingInfo.pingTime);

            playerPingInfo.pingPending = false;

            context.enqueueWork(() ->
            {
                playerPingInfo.RTT_QUEUE.add(rtt);

                player.latency = playerPingInfo.updateAverage();
            });
        }
    }

    public static void updatePlayerLatency(final ServerPlayer player, int wouldBeValue)
    {
        ServerPlayerPingInfo playerPingInfo = getCachedInfo(player.getUUID());

        player.latency = playerPingInfo.averageLatency > 0 ? playerPingInfo.averageLatency : wouldBeValue;
    }

    public static int getLatencyUpdateInterval()
    {
        return Config.multiplayerLatencyBroadcastInterval.get();
    }

    private static ServerPlayerPingInfo getCachedInfo(UUID playerUUID)
    {
        ServerPlayerPingInfo playerPingInfo = cache.getIfPresent(playerUUID);

        return playerPingInfo == null ? new ServerPlayerPingInfo() : playerPingInfo;
    }

    private static class ServerPlayerPingInfo
    {
        private long pingTime = 0;
        private boolean pingPending = false;
        private long pingTimeChallenge = 0;
        private int averageLatency = 0;

        private final IntEvictingQueue RTT_QUEUE = new IntEvictingQueue(5);

        private int updateAverage()
        {
            this.averageLatency = 0;

            for (int element : RTT_QUEUE.list.elements())
            {
                this.averageLatency += element;
            }

            return this.averageLatency = this.averageLatency / RTT_QUEUE.list.size();
        }
    }

    private static class IntEvictingQueue
    {
        private final int maxSize;
        private final IntArrayList list;

        IntEvictingQueue(int maxSize)
        {
            this.maxSize = maxSize;
            this.list = new IntArrayList(this.maxSize);
        }

        private void add(int element)
        {
            if (this.list.size() == maxSize)
            {
                this.list.removeInt(0);
            }

            this.list.add(element);
        }
    }
}
