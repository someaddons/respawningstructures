package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Containers.class)
public class ContainerDropRespawnMixin
{
    @Inject(method = "dropItemStack", at = @At("HEAD"), cancellable = true)
    private static void preventDropOnRespawn(
      final Level p_18993_,
      final double p_18994_,
      final double p_18995_,
      final double p_18996_,
      final ItemStack p_18997_,
      final CallbackInfo ci)
    {
        if (RespawnManager.respawnInProgress)
        {
            ci.cancel();
        }
    }
}
