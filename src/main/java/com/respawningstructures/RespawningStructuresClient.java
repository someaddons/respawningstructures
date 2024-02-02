package com.respawningstructures;

import com.respawningstructures.event.ClientEventHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class RespawningStructuresClient
{
    public static void onInitializeClient(final FMLClientSetupEvent event)
    {
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class);
    }
}
