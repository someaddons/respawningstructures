package com.respawningstructures.structure;

import com.respawningstructures.RespawningStructures;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Map;

public class StructureData
{
    /**
     * Empty dummy used for "No structure found" at a given pos
     */
    public static final StructureData EMPTY = new StructureData(BlockPos.ZERO, new ResourceLocation("dummy"));

    /**
     * Static version to handle potential upgrade conflicts easily
     */
    private static final int DATA_VERSION = 1;

    /**
     * The center section of the structures bounding box
     */
    public final SectionPos pos;

    /**
     * The structure resource location ID
     */
    public final ResourceLocation id;

    /**
     * Temporary Structure start reference
     */
    private StructureStart structureStart = null;

    /**
     * The size of the bounding box
     */
    public int bbSize = 0;

    /**
     * Disables respawn of a certain structure
     */
    public boolean disabledRespawn = false;

    /**
     * Trigger data counts
     */
    public int spawnerActivations     = 0;
    public int spawnerBreak           = 0;
    public int containerLooted        = 0;
    public int dungeonContainerLooted = 0;
    public int lightsPlaced           = 0;
    public int blocksPlaced           = 0;
    public int blocksBroken           = 0;
    public int mobsKilled             = 0;
    public int playerDeaths           = 0;
    public int portalUsage            = 0;

    /**
     * Timepoint of last activity
     */
    public long lastActivity = 0;

    public StructureData(final BlockPos pos, final ResourceLocation id)
    {
        this.pos = SectionPos.of(pos);
        this.id = id;
    }

    public boolean doesFulfillRespawnRequirements(final long currentTime)
    {
        return false;
    }

    /**
     * @param timepoint
     */
    public void setLastModifiedTime(final long timepoint)
    {
        lastActivity = timepoint;
    }

    public CompoundTag serializeNbt()
    {
        CompoundTag tag = new CompoundTag();

        tag.putInt("version", DATA_VERSION);
        tag.putInt("posx", pos.getX());
        tag.putInt("posy", pos.getY());
        tag.putInt("posz", pos.getZ());
        tag.putString("id", id.toString());
        tag.putBoolean("disabledRespawn", disabledRespawn);
        tag.putInt("spawnerActivations", spawnerActivations);
        tag.putInt("bbSize", bbSize);
        tag.putInt("spawnerBreak", spawnerBreak);
        tag.putInt("portalUsage", portalUsage);
        tag.putInt("containerLooted", containerLooted);
        tag.putInt("dungeonContainerLooted", dungeonContainerLooted);
        tag.putInt("lightsPlaced", lightsPlaced);
        tag.putInt("blocksPlaced", blocksPlaced);
        tag.putInt("blocksBroken", blocksBroken);
        tag.putInt("mobsKilled", mobsKilled);
        tag.putInt("playerDeaths", playerDeaths);
        tag.putLong("lastActivity", lastActivity);

        return tag;
    }

    public StructureData(final CompoundTag tag)
    {
        final int version = tag.getInt("version");
        pos = SectionPos.of(tag.getInt("posx"), tag.getInt("posy"), tag.getInt("posz"));
        id = ResourceLocation.tryParse(tag.getString("id"));
        spawnerActivations = tag.getInt("spawnerActivations");
        bbSize = tag.getInt("bbSize");
        disabledRespawn = tag.getBoolean("disabledRespawn");
        spawnerBreak = tag.getInt("spawnerBreak");
        portalUsage = tag.getInt("portalUsage");
        containerLooted = tag.getInt("containerLooted");
        dungeonContainerLooted = tag.getInt("dungeonContainerLooted");
        lightsPlaced = tag.getInt("lightsPlaced");
        blocksPlaced = tag.getInt("blocksPlaced");
        blocksBroken = tag.getInt("blocksBroken");
        mobsKilled = tag.getInt("mobsKilled");
        playerDeaths = tag.getInt("playerDeaths");
        lastActivity = tag.getLong("lastActivity");
    }

    public StructureStart fillStructureStart(final ServerLevel level)
    {
        if (structureStart != null)
        {
            return structureStart;
        }

        for (final Map.Entry<Structure, LongSet> entry : level.structureManager().getAllStructuresAt(pos.center()).entrySet())
        {
            if (id.equals(level.registryAccess().registry(Registries.STRUCTURE).get().getKey(entry.getKey())))
            {
                level.structureManager().fillStartsForStructure(entry.getKey(), entry.getValue(),
                  structureStart ->
                  {
                      if (SectionPos.of(structureStart.getBoundingBox().getCenter()).equals(pos))
                      {
                          this.structureStart = structureStart;
                      }
                  });
            }
        }

        if (structureStart == null)
        {
            RespawningStructures.LOGGER.warn("failed to fill!");
        }

        return structureStart;
    }

    /**
     * Checks respawn conditions and tries to respawn the structure
     *
     * @param level
     * @return
     */
    public boolean respawn(final ServerLevel level)
    {
        if (canRespawn(level))
        {
            RespawnManager.respawnStructure(level, this, true);
            return true;
        }

        return false;
    }

    /**
     * Checks if respawn is possible
     *
     * @param level
     * @return
     */
    public boolean canRespawn(final ServerLevel level)
    {
        if (!RespawningStructures.config.getCommonConfig().enableAutomaticRespawn || disabledRespawn)
        {
            return false;
        }

        if (RespawningStructures.config.getCommonConfig().blacklistedStructures.contains(id.toString()))
        {
            return false;
        }

        if ((level.getDataStorage().get(RespawnLevelData::load, RespawnLevelData.ID).getLevelTime() - lastActivity)
              < RespawningStructures.config.getCommonConfig().minutesUntilRespawn * 60L)
        {
            return false;
        }

        if (checkBlockingStats())
        {
            return false;
        }

        if (RespawningStructures.config.getCommonConfig().respawnableStructureIDs.contains(id.toString()) || checkStats())
        {
            return true;
        }

        return false;
    }

    /**
     * Checks if the stats allow respawning
     *
     * @return
     */
    public boolean checkStats()
    {
        if (spawnerBreak > 0)
        {
            return true;
        }

        if (dungeonContainerLooted > 0)
        {
            return true;
        }

        if ((spawnerActivations * 3 + containerLooted * 10 + lightsPlaced * 3 + blocksPlaced + blocksBroken + mobsKilled * 4 + playerDeaths * 10) > 30)
        {
            return true;
        }

        return false;
    }

    /**
     * Check stats which can block respawning
     *
     * @return
     */
    public boolean checkBlockingStats()
    {
        if (portalUsage > 0)
        {
            return true;
        }

        if (blocksPlaced > 100 + (bbSize / 10000d) && (double) blocksBroken > 20 + (bbSize / 100000d))
        {
            return true;
        }

        return false;
    }

    public StructureStart getStructureStart()
    {
        return structureStart;
    }

    public void setStructureStart(final StructureStart structureStart)
    {
        final Vec3i length = structureStart.getBoundingBox().getLength();
        bbSize = length.getX() * length.getY() * length.getZ();
        this.structureStart = structureStart;
    }
}
