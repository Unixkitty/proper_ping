package com.unixkitty.proper_ping.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.unixkitty.proper_ping.Config;
import com.unixkitty.proper_ping.client.ClientPingPongHandler;
import com.unixkitty.proper_ping.network.packet.PongS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class PingOverlay extends GuiComponent implements IGuiOverlay
{
    public static final PingOverlay INSTANCE = new PingOverlay();

    private static final int WHITE = 0xFFFFFF;

    private final Minecraft minecraft;

    private PingOverlay()
    {
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight)
    {
        if (
                minecraft.player != null
                        && Config.pingHudEnabled.get()
                        && minecraft.level != null
                        && !minecraft.isLocalServer()
                        && !minecraft.options.hideGui
                        && !minecraft.options.renderDebug
        )
        {
            //TODO remove vanilla value from here
            PlayerInfo info = minecraft.player.connection.getPlayerInfo(minecraft.player.getUUID());

            if (info != null)
            {
                MutableComponent vanillaLatencyComponent = Component.translatable("multiplayer.status.ping", info.getLatency());
                MutableComponent rttAverageComponent = Component.translatable("multiplayer.status.ping", ClientPingPongHandler.rttLatency);
                int colour;

                if (ClientPingPongHandler.rttLatency < 0)
                {
                    colour = WHITE;
                }
                else if (ClientPingPongHandler.rttLatency < 150)
                {
                    colour = ChatFormatting.GREEN.getColor();
                }
                else if (ClientPingPongHandler.rttLatency < 300)
                {
                    colour = ChatFormatting.YELLOW.getColor();
                }
                else if (ClientPingPongHandler.rttLatency < 600)
                {
                    colour = ChatFormatting.GOLD.getColor();
                }
                else if (ClientPingPongHandler.rttLatency < 1000)
                {
                    colour = ChatFormatting.RED.getColor();
                }
                else
                {
                    colour = ChatFormatting.DARK_RED.getColor();
                }

                poseStack.pushPose();

                //Custom rtt
                MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

                minecraft.font.drawInBatch(
                        rttAverageComponent,
                        Config.pingHudX.get(), Config.pingHudY.get(),
                        colour,
                        Config.drawTextWithShadow.get(),
                        poseStack.last().pose(), buffer, false, 0, 15728880);

                if (Config.showPingQueue.get())
                {
                    minecraft.font.drawInBatch(
                            " [" + PongS2CPacket.RTT_QUEUE.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]",
                            Config.pingHudX.get() + minecraft.font.width(rttAverageComponent), Config.pingHudY.get(),
                            WHITE,
                            Config.drawTextWithShadow.get(),
                            poseStack.last().pose(), buffer, false, 0, 15728880);
                }

                buffer.endBatch();

                //Vanilla latency value
                minecraft.font.drawShadow(poseStack, vanillaLatencyComponent, Config.pingHudX.get(), Config.pingHudY.get() + minecraft.font.lineHeight, WHITE);
                minecraft.font.drawShadow(poseStack, "tickCount: " + gui.getGuiTicks(), Config.pingHudX.get(), Config.pingHudY.get() + minecraft.font.lineHeight * 2, WHITE);

                poseStack.popPose();
            }
        }
    }
}
