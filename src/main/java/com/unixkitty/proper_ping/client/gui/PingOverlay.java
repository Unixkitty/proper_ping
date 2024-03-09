package com.unixkitty.proper_ping.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.unixkitty.proper_ping.Config;
import com.unixkitty.proper_ping.network.packet.PingS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class PingOverlay implements IGuiOverlay
{
    public static final PingOverlay INSTANCE = new PingOverlay();

    private static final int WHITE = 0xFFFFFF;

    private final int pingExtraColumnWidth;
    private final Minecraft minecraft;

    private int averageLatency = 0;
    private String latencyText = "";
    private String queueText = "";

    private PingOverlay()
    {
        this.minecraft = Minecraft.getInstance();
        this.pingExtraColumnWidth = this.minecraft.font.width(Component.translatable("multiplayer.status.ping", 999));
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight)
    {
        if (
                minecraft.player == null
                        || !Config.pingHudEnabled.get()
                        || minecraft.level == null
                        || minecraft.isLocalServer()
                        || minecraft.options.hideGui
                        || minecraft.options.renderDebug
        )
        {
            return;
        }

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        final boolean leftOrRight = Config.leftOrRight.get();
        final int horizontalPadding = Config.horizontalPadding.get();
        final int verticalPadding = Config.verticalPadding.get();
        final int lineFromTop = Config.lineFromTop.get();
        final boolean drawTextWithShadow = Config.drawTextWithShadow.get();

        int y = verticalPadding + (minecraft.font.lineHeight * lineFromTop);

        int length;

        if (Config.showPingQueue.get())
        {
            length = minecraft.font.width(this.latencyText + this.queueText);

            guiGraphics.drawString(
                    minecraft.font,
                    this.queueText,
                    leftOrRight ? horizontalPadding + minecraft.font.width(this.latencyText) : screenWidth - horizontalPadding - minecraft.font.width(this.queueText),
                    y,
                    WHITE,
                    drawTextWithShadow
            );
        }
        else
        {
            length = minecraft.font.width(this.latencyText);
        }

        guiGraphics.drawString(
                minecraft.font,
                this.latencyText,
                leftOrRight ? horizontalPadding : screenWidth - horizontalPadding - length,
                verticalPadding + (minecraft.font.lineHeight * lineFromTop),
                getPingColour(averageLatency),
                drawTextWithShadow
        );

        poseStack.popPose();
    }

    public boolean renderTabListPing(GuiGraphics guiGraphics, int columnWidth, int x, int y, final PlayerInfo playerInfo)
    {
        if (minecraft.player == null || !Config.playerListNumbers.get()) return false;

        int latency = minecraft.player.getGameProfile().getName().equalsIgnoreCase(playerInfo.getProfile().getName()) ? this.averageLatency : playerInfo.getLatency();

        Component component = Component.translatable("multiplayer.status.ping", latency);

        guiGraphics.drawString(minecraft.font, component, x + columnWidth - minecraft.font.width(component), y, getPingColour(latency));

        return true;
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

    public static void updateLatencyInfo(final PingS2CPacket packet)
    {
        INSTANCE.averageLatency = packet.averageLatency;
        INSTANCE.latencyText = Component.translatable("multiplayer.status.ping", INSTANCE.averageLatency).getString();
        INSTANCE.queueText = " " + Arrays.toString(packet.rttQueue);
    }
}
