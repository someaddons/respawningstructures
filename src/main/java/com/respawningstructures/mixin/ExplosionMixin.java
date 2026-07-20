package com.respawningstructures.mixin;

import com.respawningstructures.structure.IExplosionPosition;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerExplosion.class)
public abstract class ExplosionMixin implements IExplosionPosition
{
    @Shadow
    @Final
    private Level level;

    @Shadow
    public abstract Vec3 center();

    @Inject(method = "interactWithBlocks", at = @At("RETURN"))
    private void onExplode(final List<BlockPos> targetBlocks, final CallbackInfo ci)
    {
        RespawnManager.onExplosion(level, (Explosion) (Object) this, targetBlocks);
    }

    @Override
    @Unique
    public Vec3 getactualexplosionpos()
    {
        return center();
    }
}
