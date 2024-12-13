package com.respawningstructures.mixin;

import com.respawningstructures.structure.IRemembersPositionPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TemplateStructurePiece.class)
public class TemplateStructurePieceMixin implements IRemembersPositionPiece
{
    @Shadow
    protected BlockPos templatePosition;
    @Unique
    private   BlockPos respawnTemplatePos = null;

    @Inject(method = "postProcess", at = @At("HEAD"))
    private void onPlacement(
      final WorldGenLevel p_226899_,
      final StructureManager p_226900_,
      final ChunkGenerator p_226901_,
      final RandomSource p_226902_,
      final BoundingBox p_226903_,
      final ChunkPos p_226904_,
      final BlockPos p_226905_,
      final CallbackInfo ci)
    {
        if (respawnTemplatePos != null)
        {
            templatePosition = respawnTemplatePos;
            respawnTemplatePos = null;
        }
    }

    @Override
    public void setRespawnTemplatePos(final BlockPos pos)
    {
        respawnTemplatePos = pos;
    }
}
