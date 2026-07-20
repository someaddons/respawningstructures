package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class LootTriggerMixin extends BaseContainerBlockEntity implements RandomizableContainer
{
    @Shadow
    protected ResourceKey<LootTable> lootTable;

    protected LootTriggerMixin(
        final BlockEntityType<?> p_155076_,
        final BlockPos p_155077_,
        final BlockState p_155078_)
    {
        super(p_155076_, p_155077_, p_155078_);
    }

    @Inject(method = "setLootTable(Lnet/minecraft/resources/ResourceKey;)V", at = @At("HEAD"))
    private void onUnpack(final ResourceKey<LootTable> newTable, final CallbackInfo ci)
    {
        if (newTable == null && lootTable != null && level != null && !level.isClientSide())
        {
            RespawnManager.onChestLooted((ServerLevel) level, lootTable.identifier(), this.worldPosition);
        }
    }
}
