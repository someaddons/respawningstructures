package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class LootTriggerMixin extends BaseContainerBlockEntity implements RandomizableContainer
{
    @Shadow
    @Nullable
    protected ResourceKey<LootTable> lootTable;

    protected LootTriggerMixin(
        final BlockEntityType<?> p_155076_,
        final BlockPos p_155077_,
        final BlockState p_155078_)
    {
        super(p_155076_, p_155077_, p_155078_);
    }

    @Override
    public void unpackLootTable(@Nullable Player player)
    {
        if (lootTable != null && player instanceof ServerPlayer && this.hasLevel())
        {
            RespawnManager.onChestLooted((ServerLevel) player.level(), lootTable.location(), getBlockPos());
        }
        RandomizableContainer.super.unpackLootTable(player);
    }
}
