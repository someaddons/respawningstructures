package com.respawningstructures.mixin;

import com.respawningstructures.structure.IBBCacheFixStructureStart;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ChunkAccess.class)
public class ChunkSetStartBBFix
{
    @Inject(method = "setAllStarts", at = @At("RETURN"))
    private void on(final Map<Structure, StructureStart> data, final CallbackInfo ci)
    {
        for (final StructureStart structureStart : data.values())
        {
            ((IBBCacheFixStructureStart) (Object) structureStart).clearCachedBB();
        }
    }
}
