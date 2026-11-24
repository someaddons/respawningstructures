package com.respawningstructures.mixin;

import com.respawningstructures.RespawningStructures;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuriedTreasurePieces.BuriedTreasurePiece.class)
public class BuriedTreasureChestRespawnFix
{
    @ModifyVariable(method = "postProcess", at = @At(value = "STORE"), ordinal = 0)
    private BlockState replaceChests(final BlockState prev)
    {
        if (RespawnManager.respawnInProgress != null && prev.getBlock() == Blocks.CHEST)
        {
            return Blocks.SAND.defaultBlockState();
        }
        else
        {
            return prev;
        }
    }

    @Inject(method = "postProcess", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/structures/BuriedTreasurePieces$BuriedTreasurePiece;createChest(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void mod$refillChest(
        final WorldGenLevel level,
        final StructureManager p_227371_,
        final ChunkGenerator p_227372_,
        final RandomSource p_227373_,
        final BoundingBox bounds,
        final ChunkPos p_227375_,
        final BlockPos chestPos,
        final CallbackInfo ci)
    {
        if (RespawnManager.respawnInProgress == null || !bounds.isInside(chestPos))
        {
            return;
        }

        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)
        {
            chest.setLootTable(BuiltInLootTables.BURIED_TREASURE, RespawningStructures.rand.nextLong());
            chest.setChanged();
        }
    }
}
