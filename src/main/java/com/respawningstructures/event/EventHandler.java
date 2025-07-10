package com.respawningstructures.event;

import com.respawningstructures.RespawningStructures;
import com.respawningstructures.structure.RespawnLevelData;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Calendar;
import java.util.HashSet;

import static com.respawningstructures.structure.RespawnLevelData.RESPAWNLEVELDATAFACTORY;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    private final static TagKey<Block> REDSTONE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(RespawningStructures.MOD_ID, "redstone"));
    public final static TagKey<Block> KEEP_EXISTING = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(RespawningStructures.MOD_ID, "keepexisting"));
    public final static TagKey<Block> NO_RESPAWN    = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(RespawningStructures.MOD_ID, "norespawn"));
    private static       long          lastTime = 0;

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event)
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
                    final RespawnLevelData data = level.getDataStorage().computeIfAbsent(RESPAWNLEVELDATAFACTORY, RespawnLevelData.ID);
                    if (data != null)
                    {
                        data.increaseTime(60 * 5);
                    }
                }
            }
        }

        if (RespawningStructures.config.getCommonConfig().needReload)
        {
            RespawningStructures.config.getCommonConfig().needReload = false;
            final HashSet<String> toAddBlackList = new HashSet<>();
            try
            {
                for (final String blackListEntry : RespawningStructures.config.getCommonConfig().blacklistedStructures)
                {
                    if (blackListEntry.startsWith("#"))
                    {
                        final ResourceLocation id = ResourceLocation.tryParse(blackListEntry.replace("#", ""));
                        if (id != null)
                        {
                            event.getServer()
                                .registryAccess()
                                .registry(Registries.STRUCTURE)
                                .get()
                                .getOrCreateTag(TagKey.create(Registries.STRUCTURE, id))
                                .forEach(a -> toAddBlackList.add(a.unwrapKey().get().location().toString()));
                        }
                    }
                }
            }
            catch (Exception e)
            {
                RespawningStructures.LOGGER.warn("Error during trying to parse structure blacklist for tags: ", e);
            }

            RespawningStructures.config.getCommonConfig().blacklistedStructures.addAll(toAddBlackList);

            final HashSet<String> toAddWhitelist = new HashSet<>();
            try
            {
                for (final String blackListEntry : RespawningStructures.config.getCommonConfig().respawnableStructureIDs)
                {
                    if (blackListEntry.startsWith("#"))
                    {
                        final ResourceLocation id = ResourceLocation.tryParse(blackListEntry.replace("#", ""));
                        if (id != null)
                        {
                            event.getServer()
                                .registryAccess()
                                .registry(Registries.STRUCTURE)
                                .get()
                                .getOrCreateTag(TagKey.create(Registries.STRUCTURE, id))
                                .forEach(a -> toAddWhitelist.add(a.unwrapKey().get().location().toString()));
                        }
                    }
                }
            }
            catch (Exception e)
            {
                RespawningStructures.LOGGER.warn("Error during trying to parse structure whitelist for tags: ", e);
            }

            RespawningStructures.config.getCommonConfig().respawnableStructureIDs.addAll(toAddWhitelist);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Post event)
    {
        if (!event.getLevel().isClientSide && event.getLevel().getGameTime() % 1000 == 17)
        {
            RespawnManager.onLevelTick((ServerLevel) event.getLevel());
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
    public static void onEntityAdded(final MobSpawnEvent.PositionCheck event)
    {
        if (!event.getLevel().isClientSide() && event.getSpawnType() == MobSpawnType.SPAWNER && event.getEntity() != null)
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
