package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
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
        if (RespawnManager.respawnInProgress != null)
        {
            ci.cancel();
        }
    }

    /**
     * Loot drop on destroyed
     *
     * @param level
     * @param x
     * @param y
     * @param z
     * @param container
     * @param ci
     */
    @Inject(method = "dropContents(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/Container;)V", at = @At("HEAD"))
    private static void onLootDropped(final Level level, final double x, final double y, final double z, final Container container, final CallbackInfo ci)
    {
        if (level instanceof ServerLevel)
        {
            if (container instanceof RandomizableContainerBlockEntity)
            {
                if (((RandomizableContainerBlockEntity) container).lootTable != null)
                {
                    RespawnManager.onChestLooted((ServerLevel) level, ((RandomizableContainerBlockEntity) container).lootTable, BlockPos.containing(x, y, z));
                }
            }
            else if (container instanceof ContainerEntity)
            {
                if (((ContainerEntity) container).getLootTable() != null)
                {
                    RespawnManager.onChestLooted((ServerLevel) level, ((ContainerEntity) container).getLootTable(), BlockPos.containing(x, y, z));
                }
            }
        }
    }
}
