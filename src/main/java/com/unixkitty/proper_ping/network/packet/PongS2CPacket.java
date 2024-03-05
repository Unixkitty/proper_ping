package com.unixkitty.proper_ping.network.packet;

import com.google.common.collect.EvictingQueue;
import com.unixkitty.proper_ping.client.ClientPingPongHandler;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class PongS2CPacket extends BasePacket
{
    public static final EvictingQueue<Integer> RTT_QUEUE = EvictingQueue.create(5);

    private final long originalTime;

    public PongS2CPacket(long originalTime)
    {
        this.originalTime = originalTime;
    }

    public PongS2CPacket(FriendlyByteBuf buffer)
    {
        this.originalTime = buffer.readLong();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeLong(this.originalTime);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (ClientPingPongHandler.pingTimeChallenge == this.originalTime)
        {
            long currentTime = Util.getMillis();
            int rtt = (int) (currentTime - this.originalTime);

            context.enqueueWork(() ->
            {
                RTT_QUEUE.add(rtt);

                ClientPingPongHandler.pingDataUpdated = true;
            });
        }

        context.setPacketHandled(true);
        return true;
    }
}
