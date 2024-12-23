package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerRespawnMixin
{
    @Inject(method = "respawn", at = @At("TAIL"))
    private void onRespawn(final ServerPlayer serverPlayer, final boolean bl, final CallbackInfoReturnable<ServerPlayer> cir)
    {
        RespawnManager.onPlayerRespawn(serverPlayer);
    }
}
