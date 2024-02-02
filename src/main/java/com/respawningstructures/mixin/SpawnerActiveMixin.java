package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BaseSpawner.class)
public abstract class SpawnerActiveMixin
{
    @Shadow
    @Nullable
    public abstract BlockEntity getSpawnerBlockEntity();

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseSpawner;getOrCreateNextSpawnData(Lnet/minecraft/world/level/Level;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/SpawnData;"))
    private void onActive(final ServerLevel level, final BlockPos pos, final CallbackInfo ci)
    {
        RespawnManager.onSpawnerActive(this.getSpawnerBlockEntity());
    }
}
