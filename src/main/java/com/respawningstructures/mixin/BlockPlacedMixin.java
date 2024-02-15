package com.respawningstructures.mixin;

import com.respawningstructures.event.EventHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockPlacedMixin
{
    @Inject(method = "placeBlock", at = @At("HEAD"))
    private void onPlace(final BlockPlaceContext blockPlaceContext, final BlockState blockState, final CallbackInfoReturnable<Boolean> cir)
    {
        if (blockPlaceContext.getPlayer() instanceof ServerPlayer && blockState != null)
        {
            EventHandler.onBlockPlaced((ServerPlayer) blockPlaceContext.getPlayer(), blockPlaceContext.getClickedPos(), blockState);
        }
    }
}
