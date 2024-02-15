package com.respawningstructures.mixin;

import com.respawningstructures.event.EventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class AddEntityEvent
{
    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void onAdd(final Entity entity, final CallbackInfoReturnable<Boolean> cir)
    {
        if (!EventHandler.onEntityAdded(entity))
        {
            cir.setReturnValue(false);
        }
    }
}
