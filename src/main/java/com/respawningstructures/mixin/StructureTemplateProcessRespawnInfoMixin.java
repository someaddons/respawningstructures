package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplateProcessRespawnInfoMixin
{
    @Inject(method = "processBlockInfos",
        at = @At(value = "HEAD"), remap = false)
    private static void prepareRespawnHeightData(
        final ServerLevelAccessor serverLevelAccessor,
        final BlockPos blockPos,
        final BlockPos blockPos2,
        final StructurePlaceSettings structurePlaceSettings,
        final List<StructureTemplate.StructureBlockInfo> structureBlockInfos,
        final CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir)
    {
        if (RespawnManager.respawnInProgress == null)
        {
            return;
        }

        boolean needsHeightCache = false;

        for (final var processor : structurePlaceSettings.getProcessors())
        {
            if (processor instanceof GravityProcessor)
            {
                needsHeightCache = true;
                break;
            }
        }

        if (needsHeightCache)
        {
            Object2IntOpenHashMap<BlockPos> heightMap = new Object2IntOpenHashMap<>();

            for (var blockInfo : structureBlockInfos)
            {
                if (blockInfo.pos().getY() > 0 && !blockInfo.state().isAir())
                {
                    final int height = heightMap.getInt(new BlockPos(blockInfo.pos().getX(), 0, blockInfo.pos().getZ()));
                    if (height < blockInfo.pos().getY())
                    {
                        heightMap.put(new BlockPos(blockInfo.pos().getX(), 0, blockInfo.pos().getZ()), blockInfo.pos().getY());
                    }
                }
            }

            RespawnManager.heightMap = heightMap;
        }
    }
}
