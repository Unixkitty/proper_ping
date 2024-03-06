package com.unixkitty.proper_ping.network.packet;

import com.unixkitty.proper_ping.network.ServerPongPingHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PingC2SPacket extends BasePacket
{
    private final long time;
    private final int lastClientLatency;

    public PingC2SPacket(long time, int lastClientLatency)
    {
        this.time = time;
        this.lastClientLatency = lastClientLatency;
    }

    public PingC2SPacket(FriendlyByteBuf buffer)
    {
        this.time = buffer.readLong();
        this.lastClientLatency = buffer.readVarInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeLong(this.time);
        buffer.writeVarInt(this.lastClientLatency);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        ServerPongPingHandler.handle(context, this.time, this.lastClientLatency);

        context.setPacketHandled(true);
        return true;
    }
}
