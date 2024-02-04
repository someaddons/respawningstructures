package com.respawningstructures.structure;

import com.respawningstructures.RespawningStructures;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RespawnManager
{
    public static boolean                           respawnInProgress = false;
    public static Object2IntOpenHashMap<EntityType> entityCounts      = new Object2IntOpenHashMap<>();

    /**
     * Gets the structure data for a given pos, does trigger updates to that data
     */
    public static StructureData getForPos(final ServerLevel level, final BlockPos pos, final boolean update)
    {
        final RespawnLevelData respawnData = level.getDataStorage().get(RespawnLevelData::load, RespawnLevelData.ID);
        return respawnData.getForPos(level, pos, update);
    }

    /**
     * Triggers when a spawner is killed by a player
     */
    public static void onSpawnerKilled(final SpawnerBlockEntity entity)
    {
        if (entity.hasLevel() && !entity.getLevel().isClientSide())
        {
            final StructureData structureData = getForPos((ServerLevel) entity.getLevel(), entity.getBlockPos(), true);
            if (structureData != null)
            {
                structureData.spawnerBreak++;
            }
        }
    }

    /**
     * Triggers when a spawner is activated by a nearby player
     */
    public static void onSpawnerActive(final BlockEntity entity)
    {
        if (entity.hasLevel() && !entity.getLevel().isClientSide())
        {
            final StructureData structureData = getForPos((ServerLevel) entity.getLevel(), entity.getBlockPos(), true);
            if (structureData != null)
            {
                structureData.spawnerActivations++;
            }
        }
    }

    /**
     * Triggered when a chest is looted that qualifies as dungeon loot
     */
    public static void onChestLooted(final ServerLevel level, final ResourceLocation lootTable, final BlockPos pos)
    {
        final StructureData structureData = getForPos(level, pos, true);
        if (structureData != null)
        {
            if (lootTable.getPath().toLowerCase().contains("dungeon") || RespawningStructures.config.getCommonConfig().dungeonChestLoottables.contains(lootTable.getPath()))
            {
                structureData.dungeonContainerLooted++;
            }
            else
            {
                structureData.containerLooted++;
            }
        }
    }

    /**
     * Triggered when a mob is killed by a player
     */
    public static void onMobKilled(final LivingEntity entity)
    {
        final StructureData structureData = getForPos((ServerLevel) entity.level(), entity.blockPosition(), true);
        if (structureData != null)
        {
            structureData.mobsKilled++;
        }
    }

    /**
     * Called on block broken by a player
     *
     * @param player
     */
    public static void onBlockBreak(final ServerPlayer player, final BlockPos pos)
    {
        final StructureData structureData = getForPos((ServerLevel) player.level(), pos, true);
        if (structureData != null)
        {
            structureData.blocksBroken++;
        }
    }

    /**
     * On light placement
     */
    public static void onLightPlaced(final ServerPlayer player, final BlockPos pos)
    {
        final StructureData structureData = getForPos((ServerLevel) player.level(), pos, true);
        if (structureData != null)
        {
            structureData.lightsPlaced++;
        }
    }

    /**
     * On Block placement
     */
    public static void onBlockPlaced(final ServerPlayer player, final BlockPos pos)
    {
        final StructureData structureData = getForPos((ServerLevel) player.level(), pos, true);
        if (structureData != null)
        {
            structureData.blocksPlaced++;
        }
    }

    /**
     * On player death
     */
    public static void onPlayerDeath(final ServerPlayer player)
    {
        final StructureData structureData = getForPos((ServerLevel) player.level(), player.blockPosition(), true);
        if (structureData != null)
        {
            structureData.playerDeaths++;
        }
    }

    public static void onPortalUsage(final ServerPlayer player, final BlockPos pos)
    {
        final StructureData structureData = getForPos((ServerLevel) player.level(), pos, true);
        if (structureData != null)
        {
            structureData.portalUsage++;
        }
    }

    /**
     * Every 50 seconds
     *
     * @param level
     */
    public static void onLevelTick(final ServerLevel level)
    {
        if (RespawningStructures.config.getCommonConfig().dimensionBlackList.contains(level.dimension().location().toString()))
        {
            return;
        }

        final RespawnLevelData respawnData = level.getDataStorage().get(RespawnLevelData::load, RespawnLevelData.ID);

        for (final StructureData data : respawnData.getAllStructureData())
        {
            if (data.respawn(level))
            {
                respawnData.remove(data);
                break;
            }
        }
    }

    /**
     * Respawns the given structure
     *
     * @param level
     * @param structureData
     */
    public static void respawnStructure(final ServerLevel level, final StructureData structureData, final boolean checkLoaded)
    {
        if (structureData == null)
        {
            return;
        }

        if (checkLoaded && !level.hasChunk(structureData.pos.x(), structureData.pos.z()))
        {
            return;
        }

        structureData.fillStructureStart(level);
        final StructureStart structureStart = structureData.getStructureStart();

        if (!structureStart.isValid())
        {
            return;
        }

        RespawningStructures.LOGGER.info("Respawning structure: " + structureData.id + " at: " + structureData.pos.origin());


        long time = System.nanoTime();
        BoundingBox boundingbox = structureStart.getBoundingBox();
        ChunkPos chunkPosMin = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.minX()), SectionPos.blockToSectionCoord(boundingbox.minZ()));
        ChunkPos chunkPosMax = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.maxX()), SectionPos.blockToSectionCoord(boundingbox.maxZ()));

        if (checkLoaded)
        {
            for (int x = chunkPosMin.x; x <= chunkPosMax.x; x++)
            {
                for (int z = chunkPosMax.z; z <= chunkPosMax.z; z++)
                {
                    if (!level.hasChunk(x, z))
                    {
                        return;
                    }
                }
            }
        }

        respawnInProgress = true;
        List<Entity> entities = level.getEntitiesOfClass(Entity.class,
          new AABB(boundingbox.minX(), boundingbox.minY(), boundingbox.minZ(), boundingbox.maxX(), boundingbox.maxY(), boundingbox.maxZ()).inflate(20));
        entityCounts = new Object2IntOpenHashMap<>();

        for (final Entity existing : entities)
        {
            entityCounts.put(existing.getType(), entityCounts.getOrDefault(existing.getType(), 0) + 1);
        }

        ChunkPos.rangeClosed(chunkPosMin, chunkPosMax).forEach((chunPos) -> {
            structureStart.placeInChunk(level,
              level.structureManager(),
              level.getChunkSource().getGenerator(),
              level.getRandom(),
              new BoundingBox(chunPos.getMinBlockX(),
                level.getMinBuildHeight(),
                chunPos.getMinBlockZ(),
                chunPos.getMaxBlockX(),
                level.getMaxBuildHeight(),
                chunPos.getMaxBlockZ()),
              chunPos);
            level.getChunk(chunPos.x, chunPos.z).postProcessGeneration();
        });

        time = System.nanoTime() - time;
        if ((time / 1000000000d) > 20)
        {
            RespawningStructures.LOGGER.warn(
              "Structure:" + structureData.id + " took over 20 seconds to respawn, if you want to avoid lagspikes it is recommended to put this ID on the blacklist:"
                + structureData.id);
        }

        final RespawnLevelData respawnData = level.getDataStorage().get(RespawnLevelData::load, RespawnLevelData.ID);
        respawnData.remove(structureData);

        respawnInProgress = false;
    }

    /**
     * Tries to spawn an entity during respawn, gets denied if a similar entity already exists
     *
     * @param entity
     * @param level
     * @param pos
     * @return
     */
    public static boolean tryAddEntityDuringRespawn(final Entity entity, final ServerLevel level, final BlockPos pos)
    {
        if (respawnInProgress)
        {
            if (entityCounts.getInt(entity.getType()) > 0)
            {
                entityCounts.put(entity.getType(), entityCounts.getInt(entity.getType()) - 1);
                return false;
            }
        }

        return true;
    }
}
