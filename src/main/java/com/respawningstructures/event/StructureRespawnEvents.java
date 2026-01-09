package com.respawningstructures.event;

import com.respawningstructures.structure.StructureData;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This class holds event callbacks for other mods wanting to add compatibility
 */
public final class StructureRespawnEvents
{
    public static List<Predicate<CanRespawnEvent>>                  CAN_RESPAWN_EVENT                     = new ArrayList<>();
    public static List<Consumer<ModifyBlocksBeforePlacementEvent>>  MODIFY_BLOCK_PLACEMENT_EVENT          = new ArrayList<>();
    public static List<Consumer<ModifyStructureBeforeRespawnEvent>> MODIFY_STRUCTURE_BEFORE_RESPAWN_EVENT = new ArrayList<>();
    public static List<Consumer<AfterRespawnEvent>>                 AFTER_RESPAWN_EVENT                   = new ArrayList<>();

    /**
     * Event fired when a structure tries to respawn, returning false prevents the respawn
     */
    public static class CanRespawnEvent
    {
        public final ServerLevel   level;
        public final StructureData structureData;

        public CanRespawnEvent(final ServerLevel level, final StructureData structureData)
        {
            this.level = level;
            this.structureData = structureData;
        }
    }

    /**
     * Event fired during respawning a structure, allows modifying the StructureBlockInfo list.
     * Alter the list to change which blocks respawn at which positions, removing an entry skips that position in the world.
     */
    public static class ModifyBlocksBeforePlacementEvent
    {
        public final ServerLevelAccessor                        level;
        public final StructureData                              structureData;
        public final List<StructureTemplate.StructureBlockInfo> blockInfoList;

        public ModifyBlocksBeforePlacementEvent(
            final ServerLevelAccessor level, final List<StructureTemplate.StructureBlockInfo> blockInfoList,
            final StructureData structureData)
        {
            this.level = level;
            this.blockInfoList = blockInfoList;
            this.structureData = structureData;
        }
    }

    /**
     * Event to allow modifying structure pieces and entity counts right before respawning.
     * Structure piece should reset any boolean toggles e.g. ((NetherFortressPieces.MonsterThrone) piece).hasPlacedSpawner = false; to have those respawn during placement
     * You can also add or remove pieces if you want to
     * <p>
     * Entity Counts are used to skip entity placement during structure placement, in favor of already existing entities. If you want to force an entity to respawn always regardless of it already existing remove it from the map
     */
    public static class ModifyStructureBeforeRespawnEvent
    {
        public final ServerLevel                       level;
        public final StructureData                     structureData;
        public final Object2IntOpenHashMap<EntityType> entityCounts;
        public final List<StructurePiece>              structurePieces;

        public ModifyStructureBeforeRespawnEvent(
            final ServerLevel level,
            final StructureData structureData,
            final List<StructurePiece> structurePieces,
            final Object2IntOpenHashMap<EntityType> entityCounts)
        {
            this.structureData = structureData;
            this.level = level;
            this.entityCounts = entityCounts;
            this.structurePieces = structurePieces;
        }
    }

    /**
     * Event fired after respawning the given structure,
     * if you want to do some cleanup/modifications afterward using structureData -> structureStart -> getPieces() is the easiest way to check the area of the structure.
     */
    public static class AfterRespawnEvent
    {
        public final ServerLevel   level;
        public final StructureData structureData;

        public AfterRespawnEvent(final ServerLevel level, final StructureData structureData)
        {
            this.structureData = structureData;
            this.level = level;
        }
    }
}
