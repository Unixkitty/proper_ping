package com.unixkitty.proper_ping.network;

import com.unixkitty.proper_ping.ProperPing;
import com.unixkitty.proper_ping.network.packet.BasePacket;
import com.unixkitty.proper_ping.network.packet.PingC2SPacket;
import com.unixkitty.proper_ping.network.packet.PongS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworkDispatcher
{
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    public static void register()
    {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(ProperPing.MODID, "messages"), () -> PROTOCOL_VERSION, ModNetworkDispatcher::shouldAcceptPacket, ModNetworkDispatcher::shouldAcceptPacket);

        //================================================================================================

        registerPacket(PingC2SPacket.class, true);
        registerPacket(PongS2CPacket.class, false);

    }

    private static <T extends BasePacket> void registerPacket(Class<T> packetClass, boolean toServer)
    {
        INSTANCE.messageBuilder(packetClass, packetId++, toServer ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT)
                .decoder(buf ->
                {
                    try
                    {
                        return packetClass.getDeclaredConstructor(buf.getClass()).newInstance(buf);
                    }
                    catch (Exception e)
                    {
                        ProperPing.LOG.error(e);
                        throw new RuntimeException("Failed to decode packet " + packetClass.getSimpleName(), e);
                    }
                })
                .encoder(BasePacket::toBytes)
                .consumerNetworkThread(BasePacket::handle)
                .add();
    }

    private static boolean shouldAcceptPacket(String protocolVersion)
    {
        return PROTOCOL_VERSION.equals(protocolVersion);
    }

    public static void sendToServer(BasePacket message)
    {
        INSTANCE.sendToServer(message);
    }

    public static void sendToClient(BasePacket message, ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    //========================================
}

