package com.unixkitty.proper_ping.client;

import com.unixkitty.proper_ping.ProperPing;
import com.unixkitty.proper_ping.client.gui.PingOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ProperPing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModBusEvents
{
    @SubscribeEvent
    public static void onRegisterGuiOverlays(final RegisterGuiOverlaysEvent event)
    {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "ping", PingOverlay.INSTANCE);
    }
}
