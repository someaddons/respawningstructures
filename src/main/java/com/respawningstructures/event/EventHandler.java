package com.respawningstructures.event;

import com.respawningstructures.RespawningStructures;
import com.respawningstructures.structure.RespawnLevelData;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Calendar;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    private final static TagKey<Block> REDSTONE = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(RespawningStructures.MOD_ID, "redstone"));
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
            else if (event.getState().hasProperty(BlockStateProperties.POWER) || event.getState().is(REDSTONE))
            {
                RespawnManager.onRedstoneDestroyed((ServerPlayer) event.getPlayer(), event.getPos());
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
            else if (event.getState().hasProperty(BlockStateProperties.POWER) || event.getState().is(REDSTONE))
            {
                RespawnManager.onRedstonePlaced((ServerPlayer) event.getEntity(), event.getPos());
            }
            else
            {
                RespawnManager.onBlockPlaced((ServerPlayer) event.getEntity(), event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event)
    {
        if (!event.getLevel().isClientSide)
        {
            RespawnManager.onExplosion(event.getLevel(), event.getExplosion(), event.getAffectedBlocks());
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
        if (!event.getEntity().level.isClientSide() && event.getSource().getEntity() instanceof ServerPlayer)
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
    public static void onEntityAdded(final LivingSpawnEvent.SpecialSpawn event)
    {
        // TODO:Test
        if (!event.getLevel().isClientSide() && event.getSpawner() != null && event.getSpawner().getSpawnerEntity() != null)
        {
            RespawnManager.onSpawnerSpawn(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer)
        {
            RespawnManager.onPlayerLogin((ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer)
        {
            RespawnManager.onPlayerRespawn((ServerPlayer) event.getEntity());
        }
    }
}
