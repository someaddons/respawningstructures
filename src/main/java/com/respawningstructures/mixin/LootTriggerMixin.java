package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class LootTriggerMixin extends BaseContainerBlockEntity
{
    @Shadow
    protected ResourceLocation lootTable;

    protected LootTriggerMixin(
      final BlockEntityType<?> p_155076_,
      final BlockPos p_155077_,
      final BlockState p_155078_)
    {
        super(p_155076_, p_155077_, p_155078_);
    }

    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    private void onUnpack(final Player player, final CallbackInfo ci)
    {
        if (lootTable != null && player instanceof ServerPlayer && this.hasLevel())
        {
            RespawnManager.onChestLooted((ServerLevel) player.level(), lootTable, getBlockPos());
        }
    }
}
