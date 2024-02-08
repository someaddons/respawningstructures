package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import static net.minecraft.world.level.chunk.ProtoChunk.packOffsetCoordinates;

/**
 * Allows post-processing of respawned structures
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkPostProcessMixin extends ChunkAccess
{
    public LevelChunkPostProcessMixin(
      final ChunkPos p_187621_,
      final UpgradeData p_187622_,
      final LevelHeightAccessor p_187623_,
      final Registry<Biome> p_187624_,
      final long p_187625_,
      @Nullable final LevelChunkSection[] p_187626_,
      @Nullable final BlendingData p_187627_)
    {
        super(p_187621_, p_187622_, p_187623_, p_187624_, p_187625_, p_187626_, p_187627_);
    }

    @Override
    public void markPosForPostprocessing(BlockPos pos)
    {
        if (RespawnManager.respawnInProgress != null)
        {
            if (!this.isOutsideBuildHeight(pos))
            {
                ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(pos.getY())).add(packOffsetCoordinates(pos));
            }
        }
    }
}
