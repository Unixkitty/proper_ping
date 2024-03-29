package com.unixkitty.proper_ping.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class BasePacket
{
    public abstract void toBytes(FriendlyByteBuf buffer);

    public abstract void handle(Supplier<NetworkEvent.Context> contextSupplier);
}
