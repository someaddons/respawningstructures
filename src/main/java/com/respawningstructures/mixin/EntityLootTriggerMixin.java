package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ContainerEntity.class)
public abstract class EntityLootTriggerMixin
{

    @Shadow
    @Nullable
    public abstract ResourceLocation getLootTable();

    @Inject(method = "unpackChestVehicleLootTable", at = @At("HEAD"))
    private void onUnpack(final Player player, final CallbackInfo ci)
    {
        if (getLootTable() != null && player instanceof ServerPlayer)
        {
            RespawnManager.onChestLooted((ServerLevel) player.level(), getLootTable(), player.blockPosition());
        }
    }
}
