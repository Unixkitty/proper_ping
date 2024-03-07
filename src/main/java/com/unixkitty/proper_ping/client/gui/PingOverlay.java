package com.unixkitty.proper_ping.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.unixkitty.proper_ping.Config;
import com.unixkitty.proper_ping.client.ClientPingPongHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

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

        String text = Component.translatable("multiplayer.status.ping", ClientPingPongHandler.rttLatency).getString();
        String extraText;
        int length;

        poseStack.pushPose();

        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        final boolean leftOrRight = Config.leftOrRight.get();
        final int horizontalPadding = Config.horizontalPadding.get();
        final int verticalPadding = Config.verticalPadding.get();
        final int lineFromTop = Config.lineFromTop.get();
        final boolean drawTextWithShadow = Config.drawTextWithShadow.get();

        int y = verticalPadding + (minecraft.font.lineHeight * lineFromTop);

        Matrix4f matrix4f = poseStack.last().pose();

        if (Config.showPingQueue.get())
        {
            extraText = ClientPingPongHandler.queueText;
            length = minecraft.font.width(text + extraText);

            minecraft.font.drawInBatch(
                    extraText,
                    leftOrRight ? horizontalPadding + minecraft.font.width(text) : screenWidth - horizontalPadding - minecraft.font.width(extraText),
                    y,
                    WHITE,
                    drawTextWithShadow,
                    matrix4f, buffer, false, 0, 15728880);
        }
        else
        {
            length = minecraft.font.width(text);
        }

        minecraft.font.drawInBatch(
                text,
                leftOrRight ? horizontalPadding : screenWidth - horizontalPadding - length,
                verticalPadding + (minecraft.font.lineHeight * lineFromTop),
                getPingColour(ClientPingPongHandler.rttLatency),
                drawTextWithShadow,
                matrix4f, buffer, false, 0, 15728880);

        buffer.endBatch();

        poseStack.popPose();
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
