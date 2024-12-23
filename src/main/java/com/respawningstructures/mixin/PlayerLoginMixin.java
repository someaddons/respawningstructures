package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerLoginMixin
{
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onNewPlayer(final Connection connection, final ServerPlayer serverPlayer, final CallbackInfo ci)
    {
        RespawnManager.onPlayerLogin(serverPlayer);
    }
}
