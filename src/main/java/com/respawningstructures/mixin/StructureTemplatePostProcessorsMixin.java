package com.respawningstructures.mixin;

import com.respawningstructures.event.EventHandler;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplatePostProcessorsMixin
{
    @Inject(method = "processBlockInfos", at = @At(value = "RETURN"), remap = false)
    private static void onReturn(
        final ServerLevelAccessor serverLevelAccessor,
        final BlockPos blockPos,
        final BlockPos blockPos2,
        final StructurePlaceSettings structurePlaceSettings,
        final List<StructureTemplate.StructureBlockInfo> orignal,
        final CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir)
    {
        if (RespawnManager.respawnInProgress != null)
        {
            List<StructureTemplate.StructureBlockInfo> list = cir.getReturnValue();
            for (Iterator<StructureTemplate.StructureBlockInfo> iterator = list.iterator(); iterator.hasNext(); )
            {
                final StructureTemplate.StructureBlockInfo blockInfo = iterator.next();
                if ((blockInfo.nbt() != null && blockInfo.state().is(EventHandler.KEEP_EXISTING)
                    && serverLevelAccessor.getBlockState(blockInfo.pos()).getBlock().equals(blockInfo.state().getBlock()))
                    || blockInfo.state().is(EventHandler.NO_RESPAWN))
                {
                    iterator.remove();
                }
            }
        }
    }
}
