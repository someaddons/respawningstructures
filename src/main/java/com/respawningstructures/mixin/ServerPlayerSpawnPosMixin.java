package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerSpawnPosMixin
{
    @Inject(method = "setRespawnPosition", at = @At("RETURN"))
    private void onSetSpawn(final ResourceKey<Level> p_9159_, final BlockPos p_9160_, final float p_9161_, final boolean p_9162_, final boolean p_9163_, final CallbackInfo ci)
    {
        RespawnManager.onPlayerSetSpawn((ServerPlayer) (Object) this);
    }
}
