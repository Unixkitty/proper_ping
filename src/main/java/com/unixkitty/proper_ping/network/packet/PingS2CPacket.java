package com.unixkitty.proper_ping.network.packet;

import com.unixkitty.proper_ping.client.gui.PingOverlay;
import com.unixkitty.proper_ping.network.ModNetworkDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PingS2CPacket extends BasePacket
{
    public final long originalTime;
    public final int averageLatency;
    public final int[] rttQueue;

    public PingS2CPacket(long originalTime, int averageLatency, int[] rttQueue)
    {
        this.originalTime = originalTime;
        this.averageLatency = averageLatency;
        this.rttQueue = rttQueue;
    }

    public PingS2CPacket(FriendlyByteBuf buffer)
    {
        this.originalTime = buffer.readLong();
        this.averageLatency = buffer.readVarInt();
        this.rttQueue = buffer.readVarIntArray(5);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeLong(this.originalTime);
        buffer.writeVarInt(this.averageLatency);
        buffer.writeVarIntArray(this.rttQueue);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        //Sending packets from network threads without grabbing the Connection object from the network event here produces a class loading exception on the client on first connection attempt for some reason
        ModNetworkDispatcher.sendToServer(new PongC2SPacket(this.originalTime), context.getNetworkManager());

        context.enqueueWork(() -> PingOverlay.updateLatencyInfo(this));

        context.setPacketHandled(true);
    }
}
