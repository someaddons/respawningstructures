package com.respawningstructures.mixin;

import com.respawningstructures.event.EventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerDeathMixin
{
    @Inject(method = "die", at = @At("RETURN"))
    private void onDeath(final DamageSource damageSource, final CallbackInfo ci)
    {
        EventHandler.onMobKilled((LivingEntity) (Object) this, damageSource);
    }
}
