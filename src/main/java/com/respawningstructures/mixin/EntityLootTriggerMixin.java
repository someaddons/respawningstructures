package com.respawningstructures.mixin;

import com.respawningstructures.structure.RespawnManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartContainer.class)
public abstract class EntityLootTriggerMixin extends AbstractMinecart
{
    @Shadow
    private ResourceLocation lootTable;

    protected EntityLootTriggerMixin(final EntityType<?> p_38087_, final Level p_38088_)
    {
        super(p_38087_, p_38088_);
    }

    @Inject(method = "setLootTable(Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
    private void onUnpack(final ResourceLocation newTable, final CallbackInfo ci)
    {
        if (newTable == null && lootTable != null && !level().isClientSide())
        {
            RespawnManager.onChestLooted((ServerLevel) level(), lootTable, blockPosition());
        }
    }
}
