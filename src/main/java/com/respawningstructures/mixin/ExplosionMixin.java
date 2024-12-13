package com.respawningstructures.mixin;

import com.respawningstructures.structure.IExplosionPosition;
import com.respawningstructures.structure.RespawnManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin implements IExplosionPosition
{
    @Shadow
    @Final
    private Level level;

    @Shadow
    public abstract List<BlockPos> getToBlow();

    @Shadow
    @Final
    private double x;

    @Shadow
    @Final
    private double y;

    @Shadow
    @Final
    private double z;

    @Inject(method = "explode", at = @At("RETURN"))
    private void onExplode(final CallbackInfo ci)
    {
        RespawnManager.onExplosion(level, (Explosion) (Object) this, getToBlow());
    }

    @Override
    @Unique
    public Vec3 getactualexplosionpos()
    {
        return new Vec3(this.x, this.y, this.z);
    }
}
