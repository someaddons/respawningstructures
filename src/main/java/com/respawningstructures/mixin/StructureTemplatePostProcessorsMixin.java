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

import static com.respawningstructures.event.EventHandler.KEEP_ALWAYS;

@Mixin(StructureTemplate.class)
public class StructureTemplatePostProcessorsMixin
{
    @Inject(method = "processBlockInfos(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;Ljava/util/List;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;)Ljava/util/List;", at = @At(value = "RETURN"), remap = false)
    private static void onReturn(
        final ServerLevelAccessor serverLevelAccessor,
        final BlockPos p_74519_,
        final BlockPos p_74520_,
        final StructurePlaceSettings p_74521_,
        final List<StructureTemplate.StructureBlockInfo> p_74522_,
        final StructureTemplate template,
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
                    || blockInfo.state().is(EventHandler.NO_RESPAWN)
                    || serverLevelAccessor.getBlockState(blockInfo.pos()).is(KEEP_ALWAYS))
                {
                    iterator.remove();
                }
            }
        }
    }
}
