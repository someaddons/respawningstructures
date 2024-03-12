package com.respawningstructures.mixin;

import com.respawningstructures.structure.IBBCacheFixStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(StructureStart.class)
public abstract class StructureStartBBFixMixin implements IBBCacheFixStructureStart
{
    @Shadow
    @Nullable
    private volatile BoundingBox cachedBoundingBox;

    @Override
    public void clearCachedBB()
    {
        cachedBoundingBox = null;
    }
}
