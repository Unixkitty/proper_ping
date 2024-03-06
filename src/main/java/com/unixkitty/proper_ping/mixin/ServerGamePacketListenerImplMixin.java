package com.unixkitty.proper_ping.mixin;

import com.unixkitty.proper_ping.network.ServerPongPingHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{
    @Redirect(method = "handleKeepAlive(Lnet/minecraft/network/protocol/game/ServerboundKeepAlivePacket;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;latency:I", opcode = Opcodes.PUTFIELD))
    public void onHandleKeepAlive(ServerPlayer player, int wouldBeValue)
    {
        ServerPongPingHandler.updatePlayerLatency(player, wouldBeValue);
    }
}
