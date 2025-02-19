package com.respawningstructures.structure;

import com.respawningstructures.RespawningStructures;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Iterator;
import java.util.Map;

public class StructureData
{
    /**
     * Empty dummy used for "No structure found" at a given pos
     */
    public static final StructureData EMPTY = new StructureData(BlockPos.ZERO, ResourceLocation.fromNamespaceAndPath("dummy", "dummy"));

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
    public int  spawnerActivations = 0;
    public int  spawnerBreak       = 0;
    public int  containerLooted    = 0;
    public int  lightsPlaced       = 0;
    public int  redstonePlaced     = 0;
    public int  blocksPlaced       = 0;
    public int  blocksBroken       = 0;
    public int  mobsKilled         = 0;
    public int  playerDeaths       = 0;
    public int  portalUsage        = 0;
    public int  blockEntities      = 0;
    public long inhabitedStart     = 0;

    /**
     * Timepoint of last activity
     */
    public long lastActivity = 0;

    /**
     * Respawn counter
     */
    public int respawns = 0;

    public StructureData(final BlockPos pos, final ResourceLocation id)
    {
        this.pos = SectionPos.of(pos);
        this.id = id;
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
        tag.putInt("lightsPlaced", lightsPlaced);
        tag.putInt("redstonePlaced", redstonePlaced);
        tag.putInt("blockEntities", blockEntities);
        tag.putLong("inhabitedStart", inhabitedStart);
        tag.putInt("blocksPlaced", blocksPlaced);
        tag.putInt("blocksBroken", blocksBroken);
        tag.putInt("mobsKilled", mobsKilled);
        tag.putInt("playerDeaths", playerDeaths);
        tag.putInt("respawns", respawns);
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
        lightsPlaced = tag.getInt("lightsPlaced");
        if (tag.contains("redstonePlaced"))
        {
            redstonePlaced = tag.getInt("redstonePlaced");
        }
        if (tag.contains("blockEntities"))
        {
            blockEntities = tag.getInt("blockEntities");
        }
        if (tag.contains("inhabitedStart"))
        {
            inhabitedStart = tag.getLong("inhabitedStart");
        }

        blocksPlaced = tag.getInt("blocksPlaced");
        blocksBroken = tag.getInt("blocksBroken");
        mobsKilled = tag.getInt("mobsKilled");
        playerDeaths = tag.getInt("playerDeaths");
        respawns = tag.getInt("respawns");
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
            RespawningStructures.LOGGER.warn("Structure: " + this.id + " could not be found, disabling respawn");
            disabledRespawn = true;
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
        if (canRespawn(level) == RespawnStatus.PENDING_RESPAWN)
        {
            try
            {
                return RespawnManager.respawnStructure(level, this, true);
            }
            catch (Exception e)
            {
                RespawningStructures.LOGGER.warn("Error during respawning structure: " + this.id + " blacklisting structure type", e);
                RespawningStructures.config.getCommonConfig().blacklistedStructures.add(id.toString());
                RespawningStructures.config.save();
            }
        }

        return false;
    }

    /**
     * Checks if respawn is possible
     *
     * @param level
     * @return
     */
    public RespawnStatus canRespawn(final ServerLevel level)
    {
        if (!RespawningStructures.config.getCommonConfig().enableAutomaticRespawn || disabledRespawn)
        {
            return RespawnStatus.RESPAWN_DISABLED;
        }

        if ((RespawningStructures.config.getCommonConfig().whitelist && !RespawningStructures.config.getCommonConfig().blacklistedStructures.contains(id.toString()))
            || (!RespawningStructures.config.getCommonConfig().whitelist && RespawningStructures.config.getCommonConfig().blacklistedStructures.contains(id.toString())))
        {
            return RespawnStatus.BLACKLISTED;
        }

        if (lastActivity == 0 || (level.getDataStorage().computeIfAbsent(RespawnLevelData.RESPAWNLEVELDATAFACTORY, RespawnLevelData.ID).getLevelTime() - lastActivity)
            < RespawningStructures.config.getCommonConfig().minutesUntilRespawn * 60L)
        {
            if (lastActivity == 0)
            {
                return RespawnStatus.UNUSED;
            }

            return RespawnStatus.WAITING_RESPAWN_TIME;
        }

        if (RespawningStructures.config.getCommonConfig().respawnableStructureIDs.contains(id.toString()))
        {
            return checkStats(level);
        }

        RespawnStatus status = checkBlockingStats(level);
        if (status.isBlocked())
        {
            return status;
        }

        status = checkStats(level);

        if (status == RespawnStatus.PENDING_RESPAWN)
        {
            final RespawnLevelData levelData = level.getDataStorage().computeIfAbsent(RespawnLevelData.RESPAWNLEVELDATAFACTORY, RespawnLevelData.ID);
            for (Iterator<Respawn> iterator = levelData.playerRespawnTracker.values().iterator(); iterator.hasNext(); )
            {
                final Respawn respawnData = iterator.next();
                final BlockPos center = pos.center();
                if (dist2D(respawnData.position, center) < RespawningStructures.config.getCommonConfig().playerRespawnDist
                    && (levelData.getLevelTime() - respawnData.lastUsageLevelTime) < 60 * 60 * 24 * 21)
                {
                    lastActivity = levelData.getLevelTime() - (RespawningStructures.config.getCommonConfig().minutesUntilRespawn * 60L) / 2;
                    return RespawnStatus.BLOCKED_PORTAL;
                }

                if (levelData.getLevelTime() - respawnData.lastUsageLevelTime > 60 * 60 * 24 * 23)
                {
                    iterator.remove();
                }
            }

            if (inhabitedStart != 0 && level.isLoaded(pos.origin()))
            {
                final ChunkAccess chunk = level.getChunk(pos.origin());
                if ((chunk.getInhabitedTime() - inhabitedStart) / 20.0 / ((levelData.getLevelTime() - 60) - lastActivity) > (
                    RespawningStructures.config.getCommonConfig().playerNearbyTime / 100d))
                {
                    lastActivity = levelData.getLevelTime();
                    inhabitedStart = chunk.getInhabitedTime();
                    return RespawnStatus.BLOCKED_PORTAL;
                }
            }
        }

        return status;
    }

    /**
     * Checks if the stats allow respawning
     *
     * @return
     */
    public RespawnStatus checkStats(final ServerLevel level)
    {
        if (spawnerBreak > 0)
        {
            return RespawnStatus.PENDING_RESPAWN;
        }

        if (containerLooted > 0)
        {
            return RespawnStatus.PENDING_RESPAWN;
        }

        if ((spawnerActivations * 3 + lightsPlaced * 3 + blocksPlaced + blocksBroken + mobsKilled * 4 + playerDeaths * 10)
            > 25 - (RespawningStructures.config.getCommonConfig().respawnableStructureIDs.contains(id.toString()) ? 15 : 0))
        {
            return RespawnStatus.PENDING_RESPAWN;
        }

        return RespawnStatus.UNUSED;
    }

    /**
     * Check stats which can block respawning
     *
     * @return
     */
    public RespawnStatus checkBlockingStats(final ServerLevel level)
    {
        if (portalUsage > 5)
        {
            return RespawnStatus.BLOCKED_PORTAL;
        }

        if ((redstonePlaced * RespawningStructures.config.getCommonConfig().blockCountMod) > 10)
        {
            return RespawnStatus.BLOCKED_REDSTONEPLACED;
        }


        // TODO: block  break percentage
        if (blockEntities > 10)
        {
            return RespawnStatus.BLOCKED_FOUND_BLOCKENTITIES;
        }

        if ((blocksBroken * RespawningStructures.config.getCommonConfig().blockCountMod) / bbSize > 0.15)
        {
            return RespawnStatus.BLOCKED_BROKENBLOCKS;
        }

        if ((blocksPlaced * RespawningStructures.config.getCommonConfig().blockCountMod) > 200 + (bbSize / 10000d)
            && (blocksBroken * RespawningStructures.config.getCommonConfig().blockCountMod) > 200 + (bbSize / 100000d))
        {
            final RespawnLevelData levelData = level.getDataStorage().computeIfAbsent(RespawnLevelData.RESPAWNLEVELDATAFACTORY, RespawnLevelData.ID);
            if ((levelData.getLevelTime() - lastActivity) > 60 * 60 * 24 * 60 && level.isLoaded(this.pos.center()))
            {
                blocksPlaced = (int) (blocksPlaced * 0.99);
                blocksBroken = (int) (blocksBroken * 0.99);
            }

            return RespawnStatus.BLOCKED_PLACEDBROKENBLOCKS;
        }

        return RespawnStatus.PENDING_RESPAWN;
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

    /**
     * Triggered on respawning, clears stats and increases respawn counter
     */
    public void onRespawnReset()
    {
        respawns++;
        spawnerActivations = 0;
        spawnerBreak = 0;
        containerLooted = 0;
        lightsPlaced = 0;
        redstonePlaced = 0;
        blocksPlaced = 0;
        blocksBroken = 0;
        mobsKilled = 0;
        playerDeaths = 0;
        portalUsage = 0;
        blockEntities = 0;
        inhabitedStart = 0;
        lastActivity = 0;
    }

    public Component getStats(final ServerLevel level)
    {
        int dist = Integer.MAX_VALUE;
        final RespawnLevelData levelData = level.getDataStorage().computeIfAbsent(RespawnLevelData.RESPAWNLEVELDATAFACTORY, RespawnLevelData.ID);
        for (Iterator<Respawn> iterator = levelData.playerRespawnTracker.values().iterator(); iterator.hasNext(); )
        {
            final Respawn respawnData = iterator.next();
            final BlockPos center = pos.center();
            if ((levelData.getLevelTime() - respawnData.lastUsageLevelTime) < 60 * 60 * 24 * 21)
            {
                if (dist2D(respawnData.position, center) < dist)
                {
                    dist = dist2D(respawnData.position, center);
                }
            }

            if (levelData.getLevelTime() - respawnData.lastUsageLevelTime > 60 * 60 * 24 * 23)
            {
                iterator.remove();
            }
        }

        int inhabitedTimePct = 0;
        if (inhabitedStart != 0)
        {
            final ChunkAccess chunk = level.getChunk(pos.origin());

            inhabitedTimePct = (int) (((chunk.getInhabitedTime() - inhabitedStart) / 20.0 / ((levelData.getLevelTime() - 60) - lastActivity)) * 100);
        }

        return Component.literal("Broken blocks: " + blocksBroken).withStyle(ChatFormatting.BLUE)
            .append(Component.literal(" Placed blocks: " + blocksPlaced).withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" lights placed: " + lightsPlaced).withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" redstone placed: " + redstonePlaced).withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" blockentities : " + blockEntities).withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" Portal usage: " + portalUsage).withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" Containers looted: " + containerLooted).withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" Mobs killed: " + mobsKilled).withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" Player deaths: " + playerDeaths).withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" Spawner broken: " + spawnerBreak).withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" Nearest player spawn: " + dist).withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" Inhabited time pct: " + inhabitedTimePct).withStyle(ChatFormatting.WHITE));
    }

    public static int dist2D(final BlockPos pos, final BlockPos pos2)
    {
        final int xDiff = pos.getX() - pos2.getX();
        final int zDiff = pos.getZ() - pos2.getZ();
        return (int) Math.sqrt(xDiff * xDiff + zDiff * zDiff);
    }

    public enum RespawnStatus
    {
        UNUSED,
        RESPAWN_DISABLED(true),
        PENDING_RESPAWN,
        BLOCKED_PORTAL(true),
        BLOCKED_PLACEDBROKENBLOCKS(true),
        BLOCKED_BROKENBLOCKS(true),
        BLOCKED_REDSTONEPLACED(true),
        BLACKLISTED(true),
        WAITING_RESPAWN_TIME,
        BLOCKED_FOUND_BLOCKENTITIES(true);
        private final boolean isBLocked;

        RespawnStatus()
        {
            isBLocked = false;
        }

        RespawnStatus(final boolean blocked)
        {
            isBLocked = blocked;
        }

        public boolean isBlocked()
        {
            return isBLocked;
        }
    }
}
