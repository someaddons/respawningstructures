package com.respawningstructures.structure;

import com.respawningstructures.RespawningStructures;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RespawnLevelData extends SavedData
{
    public static final String ID = "respawningdungeonsdata";

    /**
     * Chunk section position to structure data matching map, multiple positions can point to the same structure
     */
    private final Long2ObjectOpenHashMap<StructureData> structurePositions = new Long2ObjectOpenHashMap<>();

    /**
     * Set holding all unique structure data entries
     */
    private final Set<StructureData> allStructureData = new HashSet<>();

    /**
     * Time elapsed on the world
     */
    private long elapsedTime = 0;

    public RespawnLevelData()
    {

    }

    public static RespawnLevelData load(CompoundTag tag)
    {
        RespawnLevelData data = new RespawnLevelData();
        data.read(tag);
        return data;
    }

    public void read(CompoundTag nbt)
    {
        elapsedTime = nbt.getLong("elapsedTime");
        ListTag list = nbt.getList("Structures", Tag.TAG_COMPOUND);

        for (final Tag tag : list)
        {
            if (tag instanceof CompoundTag)
            {
                final StructureData data = new StructureData((CompoundTag) tag);
                structurePositions.put(data.pos.asLong(), data);
                allStructureData.add(data);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag nbt)
    {
        nbt.putLong("elapsedTime", elapsedTime);

        ListTag list = new ListTag();
        for (final StructureData data : allStructureData)
        {
            if (data != StructureData.EMPTY)
            {
                list.add(data.serializeNbt());
            }
        }

        nbt.put("Structures", list);

        setDirty(false);
        return nbt;
    }

    /**
     * Get the structure data for a given pos when a trigger update happened
     *
     * @param level
     * @param pos
     * @return
     */
    public StructureData getForPos(final ServerLevel level, final BlockPos pos)
    {
        return getForPos(level, pos, true);
    }

    /**
     * Get the structure data for a given pos when a trigger update happened
     *
     * @param level
     * @param pos
     * @return
     */
    public StructureData getForPos(final ServerLevel level, final BlockPos pos, final boolean update)
    {
        final StructureData alreadyContainedAtPos = structurePositions.get(SectionPos.asLong(pos));
        if (alreadyContainedAtPos == StructureData.EMPTY)
        {
            return null;
        }

        if (alreadyContainedAtPos != null)
        {
            if (update)
            {
                alreadyContainedAtPos.setLastModifiedTime(elapsedTime);
                setDirty(true);
            }
            return alreadyContainedAtPos;
        }

        final StructureStart structureStart = getClosestStart(level, pos);

        if (structureStart == null)
        {
            structurePositions.put(SectionPos.asLong(pos), StructureData.EMPTY);
            return null;
        }

        final boolean[] found = new boolean[1];
        for (final Map.Entry<Structure, LongSet> entry : level.structureManager()
          .getAllStructuresAt(structureStart.getBoundingBox().getCenter())
          .entrySet())
        {
            level.structureManager().fillStartsForStructure(entry.getKey(), entry.getValue(),
              start ->
              {
                  if (start == structureStart)
                  {
                      found[0] = true;
                  }
              });
        }

        if (!found[0])
        {
            RespawningStructures.LOGGER.warn("Bad structure start!");
        }

        final StructureData data = structurePositions.computeIfAbsent(SectionPos.asLong(structureStart.getBoundingBox().getCenter()), (p) -> {
            StructureData newData = new StructureData(structureStart.getBoundingBox().getCenter(),
              level.registryAccess()
                .registry(Registries.STRUCTURE)
                .get()
                .getKey(structureStart.getStructure()));

            allStructureData.add(newData);
            return newData;
        });

        // Add cache lookup for queried chunksection
        structurePositions.put(SectionPos.asLong(pos), data);

        // Dirty on access since values change
        setDirty(true);
        data.setStructureStart(structureStart);
        if (update)
        {
            data.setLastModifiedTime(elapsedTime);
        }
        return data;
    }

    /**
     * Gets the closest structure startin a 3  chunk sqradius
     *
     * @param level
     * @param pos
     * @return
     */
    public static StructureStart getClosestStart(final ServerLevel level, final BlockPos pos)
    {
        Map<Structure, LongSet> structures = null;

        final ChunkPos start = new ChunkPos(pos);

        for (final Map.Entry<Structure, LongSet> entry : level.structureManager()
          .getAllStructuresAt(new BlockPos((start.x) << 4, 0, (start.z) << 4))
          .entrySet())
        {
            if (structures == null)
            {
                structures = new HashMap<>();
            }
            structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
        }

        if (structures == null || structures.isEmpty())
        {
            for (int x = -2; x < 2; x++)
            {
                for (int z = -2; z < 2; z++)
                {
                    if (x == 0 && z == 0)
                    {
                        continue;
                    }

                    if (!level.hasChunk(start.x + x, start.z + z))
                    {
                        continue;
                    }

                    for (final Map.Entry<Structure, LongSet> entry : level.structureManager()
                      .getAllStructuresAt(new BlockPos((start.x + x) << 4, 0, (start.z + z) << 4))
                      .entrySet())
                    {
                        if (structures == null)
                        {
                            structures = new HashMap<>();
                        }
                        structures.computeIfAbsent(entry.getKey(), k -> new LongOpenHashSet(entry.getValue())).addAll(entry.getValue());
                    }
                }
            }
        }

        if (structures == null || structures.isEmpty())
        {
            return null;
        }

        final StructureStart[] closest = new StructureStart[1];

        for (Map.Entry<Structure, LongSet> structureEntry : structures.entrySet())
        {
            level.structureManager().fillStartsForStructure(structureEntry.getKey(), structureEntry.getValue(),
              structureStart ->
              {
                  if (pos.equals(structureStart.getBoundingBox().getCenter()))
                  {
                      if (closest[0] == null || closest[0].getBoundingBox().getCenter().distSqr(pos) > structureStart.getBoundingBox().getCenter().distSqr(pos))
                      {
                          closest[0] = structureStart;
                      }
                  }

                  for (final StructurePiece piece : structureStart.getPieces())
                  {
                      if (piece != null && piece.getBoundingBox().isInside(pos))
                      {
                          if (closest[0] == null || closest[0].getBoundingBox().getCenter().distSqr(pos) > structureStart.getBoundingBox().getCenter().distSqr(pos))
                          {
                              closest[0] = structureStart;
                          }

                          break;
                      }
                  }
              }
            );
        }

        return closest[0];
    }

    /**
     * Increases internal level time point
     *
     * @param seconds
     */
    public void increaseTime(final int seconds)
    {
        elapsedTime += seconds;
        setDirty(true);
    }

    /**
     * Get the level time point
     *
     * @return
     */
    public long getLevelTime()
    {
        return elapsedTime;
    }

    /**
     * Gets all pending structures
     *
     * @return
     */
    public Set<StructureData> getAllStructureData()
    {
        return allStructureData;
    }
}
