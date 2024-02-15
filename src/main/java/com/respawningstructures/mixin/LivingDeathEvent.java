package com.respawningstructures.mixin;

import com.respawningstructures.event.EventHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingDeathEvent extends Entity
{
    public LivingDeathEvent(final EntityType<?> entityType, final Level level)
    {
        super(entityType, level);
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void onDeath(final DamageSource damageSource, final CallbackInfo ci)
    {
        EventHandler.onMobKilled((LivingEntity) (Object) this, damageSource);
    }
}
