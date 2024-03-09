package com.unixkitty.proper_ping.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.proper_ping.client.gui.PingOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin
{
    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 1), index = 0)
    private int test(int vanillaWidth)
    {
        return PingOverlay.INSTANCE.getTabListColumnWidth(vanillaWidth);
    }

    @Inject(at = @At("HEAD"), method = "renderPingIcon(Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V", cancellable = true)
    public void onRenderPingIcon(PoseStack poseStack, int columnWidth, int x, int y, PlayerInfo playerInfo, CallbackInfo ci)
    {
        if (PingOverlay.INSTANCE.renderTabListPing(poseStack, columnWidth, x, y, playerInfo))
        {
            ci.cancel();
        }
    }
}
