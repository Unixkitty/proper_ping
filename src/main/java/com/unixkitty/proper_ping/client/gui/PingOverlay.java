package com.unixkitty.proper_ping.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.unixkitty.proper_ping.Config;
import com.unixkitty.proper_ping.client.ClientPingPongHandler;
import com.unixkitty.proper_ping.network.packet.PongS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
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

    private final int pingExtraColumnWidth;
    private final Minecraft minecraft;

    private PingOverlay()
    {
        this.minecraft = Minecraft.getInstance();
        this.pingExtraColumnWidth = this.minecraft.font.width(Component.translatable("multiplayer.status.ping", 999));
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
            MutableComponent rttAverageComponent = Component.translatable("multiplayer.status.ping", ClientPingPongHandler.rttLatency);

            poseStack.pushPose();

            MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            minecraft.font.drawInBatch(
                    rttAverageComponent,
                    Config.pingHudX.get(), Config.pingHudY.get(),
                    getPingColour(ClientPingPongHandler.rttLatency),
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

            poseStack.popPose();
        }
    }

    public boolean renderTabListPing(PoseStack poseStack, int columnWidth, int x, int y, int latency)
    {
        if (Config.playerListNumbers.get())
        {
            Component component = Component.translatable("multiplayer.status.ping", latency);

            minecraft.font.drawShadow(poseStack, component, x + columnWidth - minecraft.font.width(component), y, getPingColour(latency));

            return true;
        }
        else
        {
            return false;
        }
    }

    public int getTabListColumnWidth(int vanillaWidth)
    {
        return Config.playerListNumbers.get() ? vanillaWidth + this.pingExtraColumnWidth : vanillaWidth;
    }

    @SuppressWarnings("DataFlowIssue")
    private int getPingColour(int latency)
    {
        if (latency < 0)
        {
            return WHITE;
        }
        else if (latency < 90)
        {
            return ChatFormatting.DARK_GREEN.getColor();
        }
        else if (latency < 150)
        {
            return ChatFormatting.GREEN.getColor();
        }
        else if (latency < 300)
        {
            return ChatFormatting.YELLOW.getColor();
        }
        else if (latency < 600)
        {
            return ChatFormatting.GOLD.getColor();
        }
        else if (latency < 1000)
        {
            return ChatFormatting.RED.getColor();
        }
        else
        {
            return ChatFormatting.DARK_RED.getColor();
        }
    }
}
