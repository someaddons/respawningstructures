package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkBlockEntityHookMixin
{
    @Shadow
    public abstract Level getLevel();

    @Shadow
    protected abstract boolean isInLevel();

    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    private void onBERemove(final BlockPos pos, final CallbackInfo ci)
    {
        if (isInLevel() && getLevel() instanceof ServerLevel)
        {
            RespawnManager.onBlockEntityAddRemove((ServerLevel) getLevel(), pos, true);
        }
    }

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;addAndRegisterBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void onBEAdd(final BlockPos pos, final BlockState p_62866_, final boolean p_62867_, final CallbackInfoReturnable<BlockState> cir)
    {
        if (isInLevel() && getLevel() instanceof ServerLevel)
        {
            RespawnManager.onBlockEntityAddRemove((ServerLevel) getLevel(), pos, false);
        }
    }
}
