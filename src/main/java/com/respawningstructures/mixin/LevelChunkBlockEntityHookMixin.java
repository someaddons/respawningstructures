package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class LevelChunkBlockEntityHookMixin
{
    @Shadow
    protected ServerLevel level;

    @Inject(method = "destroyBlock", at = @At(value = "HEAD"))
    private void onDestroy(final BlockPos pos, final CallbackInfoReturnable<Boolean> cir)
    {
        if (level instanceof ServerLevel)
        {
            if (level.getBlockEntity(pos) != null)
            {
                RespawnManager.onBlockEntityAddRemove(level, pos, true);
            }
        }
    }

    @Unique
    private boolean  hasBE = false;
    @Unique
    private BlockPos bePos = BlockPos.ZERO;

    @Inject(method = "useItemOn", at = @At(value = "HEAD"))
    private void OnUseStart(
        final ServerPlayer player,
        final Level p_9267_,
        final ItemStack stack,
        final InteractionHand p_9269_,
        final BlockHitResult blockHitResult,
        final CallbackInfoReturnable<InteractionResult> cir)
    {
        if (level instanceof ServerLevel)
        {
            bePos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
            hasBE = level.getBlockEntity(bePos) != null;
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "RETURN"))
    private void OnUseEnd(
        final ServerPlayer player,
        final Level p_9267_,
        final ItemStack stack,
        final InteractionHand p_9269_,
        final BlockHitResult blockHitResult,
        final CallbackInfoReturnable<InteractionResult> cir)
    {
        if (level instanceof ServerLevel)
        {
            if (!hasBE && level.getBlockEntity(bePos) != null && blockHitResult.getBlockPos().relative(blockHitResult.getDirection()).equals(bePos))
            {
                RespawnManager.onBlockEntityAddRemove(level, blockHitResult.getBlockPos(), false);
            }
        }
    }
}
