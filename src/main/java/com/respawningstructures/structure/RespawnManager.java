package com.respawningstructures.structure;

import com.respawningstructures.RespawningStructures;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.structures.*;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RespawnManager
{
    public volatile static StructureData                     respawnInProgress = null;
    public static          Object2IntOpenHashMap<EntityType> entityCounts      = new Object2IntOpenHashMap<>();

    /**
     * Gets the structure data for a given pos, does trigger updates to that data
     */
    public static StructureData getForPos(final ServerLevel level, final BlockPos pos, final boolean update)
    {
        final RespawnLevelData respawnData = level.getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID);
        ;
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
    public static void onSpawnerActive(final ServerLevel level, final BlockPos pos)
    {
        final StructureData structureData = getForPos(level, pos, true);
        if (structureData != null)
        {
            structureData.spawnerActivations++;
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

        final RespawnLevelData respawnData = level.getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID);

        for (final StructureData data : respawnData.getAllStructureData())
        {
            if (data.canRespawn(level))
            {
                data.respawn(level);
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
    public static boolean respawnStructure(final ServerLevel level, final StructureData structureData, final boolean checkLoaded)
    {
        if (structureData == null)
        {
            return false;
        }

        if (checkLoaded && !level.hasChunk(structureData.pos.x(), structureData.pos.z()))
        {
            return false;
        }

        final RespawnLevelData respawnData = level.getDataStorage().computeIfAbsent(RespawnLevelData::load, RespawnLevelData::new, RespawnLevelData.ID);
        if (respawnData == null)
        {
            return false;
        }

        structureData.fillStructureStart(level);
        final StructureStart structureStart = structureData.getStructureStart();

        if (!structureStart.isValid())
        {
            return false;
        }

        long time = System.nanoTime();
        BoundingBox boundingbox = structureStart.getBoundingBox();
        ChunkPos chunkPosMin = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.minX()), SectionPos.blockToSectionCoord(boundingbox.minZ()));
        ChunkPos chunkPosMax = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.maxX()), SectionPos.blockToSectionCoord(boundingbox.maxZ()));

        if (checkLoaded)
        {
            int loaded = 0;
            int unloaded = 0;

            for (int x = chunkPosMin.x; x <= chunkPosMax.x; x++)
            {
                for (int z = chunkPosMax.z; z <= chunkPosMax.z; z++)
                {
                    if (!level.hasChunk(x, z))
                    {
                        unloaded++;
                    }
                    else
                    {
                        loaded++;
                    }
                }
            }

            if (loaded + unloaded > 0 && unloaded / (double) (loaded + unloaded) > 0.3)
            {
                return false;
            }
        }

        RespawningStructures.LOGGER.info("Respawning structure: " + structureData.id + " at: " + structureData.pos.origin());

        respawnInProgress = structureData;
        structureData.respawns++;
        respawnData.setDirty();

        List<Entity> entities = level.getEntitiesOfClass(Entity.class,
          new AABB(boundingbox.minX(), boundingbox.minY(), boundingbox.minZ(), boundingbox.maxX(), boundingbox.maxY(), boundingbox.maxZ()).inflate(20));
        entityCounts = new Object2IntOpenHashMap<>();

        for (final Entity existing : entities)
        {
            entityCounts.put(existing.getType(), entityCounts.getOrDefault(existing.getType(), 0) + 1);
        }

        for (final StructurePiece piece : structureStart.getPieces())
        {
            if (checkLoaded)
            {
                if (!boundingbox.isInside(piece.getBoundingBox().maxX(), piece.getBoundingBox().maxY(), piece.getBoundingBox().maxZ()))
                {
                    if (!level.hasChunk(piece.getBoundingBox().maxX() >> 4, piece.getBoundingBox().maxZ() >> 4))
                    {
                        return false;
                    }
                }

                if (!boundingbox.isInside(piece.getBoundingBox().minX(), piece.getBoundingBox().minY(), piece.getBoundingBox().minZ()))
                {
                    if (!level.hasChunk(piece.getBoundingBox().minX() >> 4, piece.getBoundingBox().minZ() >> 4))
                    {
                        return false;
                    }
                }
            }

            if (piece instanceof NetherFortressPieces.MonsterThrone)
            {
                ((NetherFortressPieces.MonsterThrone) piece).hasPlacedSpawner = false;
            }

            if (piece instanceof StrongholdPieces.PortalRoom)
            {
                ((StrongholdPieces.PortalRoom) piece).hasPlacedSpawner = false;
            }

            if (piece instanceof StrongholdPieces.ChestCorridor)
            {
                ((StrongholdPieces.ChestCorridor) piece).hasPlacedChest = false;
            }

            if (piece instanceof MineshaftPieces.MineShaftCorridor)
            {
                ((MineshaftPieces.MineShaftCorridor) piece).hasPlacedSpider = false;
            }

            if (piece instanceof DesertPyramidPiece)
            {
                ((DesertPyramidPiece) piece).hasPlacedChest[0] = false;
                ((DesertPyramidPiece) piece).hasPlacedChest[1] = false;
                ((DesertPyramidPiece) piece).hasPlacedChest[2] = false;
                ((DesertPyramidPiece) piece).hasPlacedChest[3] = false;
            }

            if (piece instanceof JungleTemplePiece)
            {
                ((JungleTemplePiece) piece).placedMainChest = false;
                ((JungleTemplePiece) piece).placedHiddenChest = false;
            }

            if (piece instanceof NetherFortressPieces.CastleSmallCorridorLeftTurnPiece)
            {
                ((NetherFortressPieces.CastleSmallCorridorLeftTurnPiece) piece).isNeedingChest = RespawningStructures.rand.nextInt(3) == 0;
            }

            if (piece instanceof NetherFortressPieces.CastleSmallCorridorRightTurnPiece)
            {
                ((NetherFortressPieces.CastleSmallCorridorRightTurnPiece) piece).isNeedingChest = RespawningStructures.rand.nextInt(3) == 0;
            }
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

        structureData.onRespawnReset();
        respawnInProgress = null;

        return true;
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
        if (respawnInProgress != null)
        {
            if (entityCounts.getInt(entity.getType()) > 0)
            {
                entityCounts.put(entity.getType(), entityCounts.getInt(entity.getType()) - 1);
                return false;
            }

            if (entity instanceof Mob)
            {
                applyRespawnBonus((Mob) entity, respawnInProgress);
            }
        }

        return true;
    }

    /**
     * Called when an entity spawns via spawner
     *
     * @param entity
     */
    public static void onSpawnerSpawn(final Mob entity)
    {
        final StructureData structureData = getForPos((ServerLevel) entity.level(), entity.blockPosition(), true);
        if (structureData != null && structureData.respawns > 0)
        {
            applyRespawnBonus(entity, structureData);
        }
    }

    /**
     * Applies bonus difficulty to entities spawned within a dungeon
     *
     * @param entity
     */
    private static void applyRespawnBonus(final Mob entity, final StructureData structureData)
    {
        if (!RespawningStructures.config.getCommonConfig().increaseDifficultyWithRespawn)
        {
            return;
        }

        final int respawnDifficulty = Math.min(4, structureData.respawns);

        for (int i = 0; i < respawnDifficulty; i++)
        {
            ItemStack stack = null;
            for (final EquipmentSlot slot : EquipmentSlot.values())
            {
                if (slot == EquipmentSlot.OFFHAND)
                {
                    continue;
                }

                if (!entity.getItemBySlot(slot).isEmpty() && entity.getItemBySlot(slot).getEnchantmentTags().isEmpty())
                {
                    stack = entity.getItemBySlot(slot);
                    break;
                }
            }

            // Randomly add equipment
            if (stack == null && RespawningStructures.rand.nextInt(10) == 0)
            {
                for (final EquipmentSlot slot : EquipmentSlot.values())
                {
                    if (entity.getItemBySlot(slot).isEmpty())
                    {
                        if (slot == EquipmentSlot.OFFHAND)
                        {
                            continue;
                        }

                        if (slot == EquipmentSlot.MAINHAND)
                        {
                            stack = Items.IRON_SWORD.getDefaultInstance();
                            entity.setItemSlot(slot, stack);
                        }

                        if (slot == EquipmentSlot.CHEST)
                        {
                            stack = Items.IRON_CHESTPLATE.getDefaultInstance();
                            entity.setItemSlot(slot, stack);
                        }

                        if (slot == EquipmentSlot.HEAD)
                        {
                            stack = Items.IRON_HELMET.getDefaultInstance();
                            entity.setItemSlot(slot, stack);
                        }

                        if (slot == EquipmentSlot.LEGS)
                        {
                            stack = Items.IRON_LEGGINGS.getDefaultInstance();
                            entity.setItemSlot(slot, stack);
                        }

                        if (slot == EquipmentSlot.FEET)
                        {
                            stack = Items.IRON_BOOTS.getDefaultInstance();
                            entity.setItemSlot(slot, stack);
                        }

                        break;
                    }
                }
            }

            if (stack != null)
            {
                EnchantmentHelper.enchantItem(entity.getRandom(), stack, respawnDifficulty, true);
            }
            else
            {
                MobEffect randomEffect = randomEffects.get(RespawningStructures.rand.nextInt(randomEffects.size()));
                if (!entity.hasEffect(randomEffect))
                {
                    entity.addEffect(new MobEffectInstance(randomEffect, -1));
                }
            }
        }
    }

    private static List<MobEffect> randomEffects = List.of(MobEffects.DAMAGE_RESISTANCE,
      MobEffects.FIRE_RESISTANCE,
      MobEffects.REGENERATION,
      MobEffects.DAMAGE_BOOST,
      MobEffects.FIRE_RESISTANCE,
      MobEffects.ABSORPTION,
      MobEffects.DARKNESS,
      MobEffects.JUMP);
}
