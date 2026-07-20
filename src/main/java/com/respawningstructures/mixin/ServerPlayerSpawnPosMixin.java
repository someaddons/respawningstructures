package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerSpawnPosMixin
{
    @Inject(method = "setRespawnPosition", at = @At("RETURN"))
    private void onSetSpawn(final ServerPlayer.RespawnConfig respawnConfig, final boolean showMessage, final CallbackInfo ci)
    {
        RespawnManager.onPlayerSetSpawn((ServerPlayer) (Object) this);
    }
}
