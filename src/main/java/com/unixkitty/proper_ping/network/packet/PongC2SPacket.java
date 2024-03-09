package com.unixkitty.proper_ping.network.packet;

import com.unixkitty.proper_ping.network.ServerPingPongHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PongC2SPacket extends BasePacket
{
    private final long originalTime;

    public PongC2SPacket(long time)
    {
        this.originalTime = time;
    }

    public PongC2SPacket(FriendlyByteBuf buffer)
    {
        this.originalTime = buffer.readLong();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer)
    {
        buffer.writeLong(this.originalTime);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        ServerPlayer player = context.getSender();

        if (player != null)
        {
            ServerPingPongHandler.handlePongFor(context, player, this.originalTime);
        }

        context.setPacketHandled(true);
    }
}
