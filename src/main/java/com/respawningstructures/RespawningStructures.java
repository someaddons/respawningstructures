package com.respawningstructures;

import com.cupboard.config.CupboardConfig;
import com.respawningstructures.config.CommonConfiguration;
import com.respawningstructures.event.EventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
public class RespawningStructures implements ModInitializer
{
    public static final String                              MOD_ID = "respawningstructures";
    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());
    public static       Random                              rand   = new Random();

    // TODO: Dimension blacklist, difficulty increase option on respawn

    public RespawningStructures()
    {


    }

    @Override
    public void onInitialize()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, c) ->
        {
            dispatcher.register(new Command().build(dedicated));
        });


        ServerTickEvents.END_SERVER_TICK.register(EventHandler::onServerTick);
        ServerTickEvents.END_WORLD_TICK.register(EventHandler::onLevelTick);
    }
}
