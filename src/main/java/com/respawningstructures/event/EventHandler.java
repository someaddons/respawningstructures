package com.respawningstructures.event;

import com.respawningstructures.structure.RespawnLevelData;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Calendar;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    /**
     * Conditions to trigger respawn watching:
     * - Spawner broken
     * - Spawner spawning blocked by player? difficult if some structure blocks it by default
     * - Dungeon chest looted - detect if it is a dungeon loot table
     * - Killing mobs
     * - Breaking some blocks
     * - placing torches
     * - fighting
     * - player dying
     * <p>
     * Conditions to fulfill for respawn:
     * - Time passed: Config, 10 ingame weeks?
     * - Not too many blocks placed next to, hard to track maybe per player?
     * - Threshold of trigger combination counts, 1 mob killed some blocks placed, sone chests looted, some spawner broken
     * - Not too high inhabited time diff since start of tracking
     * <p>
     * Extra difficulty:
     * Spawn additional or replace mobs during respawn(mark persistent)
     */

    private static long lastTime = 0;

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event)
    {
        if (event.getServer().getTickCount() % 100 == 35)
        {
            if (lastTime == 0)
            {
                lastTime = Calendar.getInstance().getTimeInMillis();
                return;
            }

            if (event.getServer().getPlayerCount() > 0 && (Calendar.getInstance().getTimeInMillis() - lastTime) > 60 * 5 * 1000)
            {
                lastTime = Calendar.getInstance().getTimeInMillis();

                for (final ServerLevel level : event.getServer().getAllLevels())
                {
                    final RespawnLevelData data = level.getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID);
                    ;
                    if (data != null)
                    {
                        data.increaseTime(60 * 5);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(final TickEvent.LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide && event.level.getGameTime() % 1000 == 17)
        {
            RespawnManager.onLevelTick((ServerLevel) event.level);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayer)
        {
            if (event.getState().hasBlockEntity() && event.getLevel().getBlockEntity(event.getPos()) instanceof SpawnerBlockEntity)
            {
                RespawnManager.onSpawnerKilled((SpawnerBlockEntity) event.getLevel().getBlockEntity(event.getPos()));
            }
            else
            {
                RespawnManager.onBlockBreak((ServerPlayer) event.getPlayer(), event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer)
        {
            if (event.getState().getLightEmission() > 0)
            {
                RespawnManager.onLightPlaced((ServerPlayer) event.getEntity(), event.getPos());
            }
            else
            {
                RespawnManager.onBlockPlaced((ServerPlayer) event.getEntity(), event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(final LevelEvent.Load event)
    {
        if (!event.getLevel().isClientSide())
        {
            // ((ServerLevel) event.getLevel()).getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID);
        }
    }

    @SubscribeEvent
    public static void onMobKilled(final LivingDeathEvent event)
    {
        if (!event.getEntity().level().isClientSide() && event.getSource().getEntity() instanceof ServerPlayer)
        {
            RespawnManager.onMobKilled(event.getEntity());
        }
        else if (event.getEntity() instanceof ServerPlayer)
        {
            RespawnManager.onPlayerDeath((ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEntityAdded(final EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide && !RespawnManager.tryAddEntityDuringRespawn(event.getEntity(), (ServerLevel) event.getLevel(), event.getEntity().blockPosition()))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityAdded(final MobSpawnEvent.FinalizeSpawn event)
    {
        if (!event.getLevel().isClientSide() && event.getSpawnType() == MobSpawnType.SPAWNER && event.getEntity() != null)
        {
            RespawnManager.onSpawnerSpawn(event.getEntity());
        }
    }
}
