package com.unixkitty.proper_ping.mixin;

import com.unixkitty.proper_ping.network.ServerPingPongHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin
{
    @Shadow
    public ServerPlayer player;

    @Shadow
    protected abstract boolean isSingleplayerOwner();

    @Inject(method = "tick()V", at = @At(value = "TAIL"))
    public void onTick(CallbackInfo ci)
    {
        ServerPingPongHandler.handleFor(this.player, this.isSingleplayerOwner());
    }

    @Redirect(method = "handleKeepAlive(Lnet/minecraft/network/protocol/game/ServerboundKeepAlivePacket;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;latency:I", opcode = Opcodes.PUTFIELD))
    public void onHandleKeepAlive(ServerPlayer player, int wouldBeValue)
    {
        ServerPingPongHandler.updatePlayerLatency(player, wouldBeValue);
    }
}
